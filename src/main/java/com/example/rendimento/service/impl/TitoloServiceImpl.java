package com.example.rendimento.service.impl;

import com.example.rendimento.dto.RendimentiDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.enums.PeriodoScadenza;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.exception.ConflittoModificaException;
import com.example.rendimento.mapper.TitoloMapper;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.repository.UtenteRepository;
import com.example.rendimento.service.BorsaItalianaService;
import com.example.rendimento.service.SimulazioneService;
import com.example.rendimento.service.TitoloService;
import com.example.rendimento.service.UtenteService;
import com.example.rendimento.service.factory.BorsaItalianaServiceFactory;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione dell'interfaccia TitoloService.
 */
@Service
public class TitoloServiceImpl implements TitoloService {

    private static final Logger log = LoggerFactory.getLogger(TitoloServiceImpl.class);
    
    @Autowired
    private TitoloRepository titoloRepository;
    
    @Autowired
    private TitoloMapper titoloMapper;
    
    @Autowired
    private BorsaItalianaServiceFactory borsaItalianaServiceFactory;
    
    @Autowired
    private UtenteRepository utenteRepository;
    
    @Autowired
    private UtenteService utenteService;
    
    @Autowired
    private SimulazioneService simulazioneService;

    @Override
    public List<TitoloDTO> getAllTitoli() {
        List<Titolo> titoli = titoloRepository.findAll();
        List<TitoloDTO> titoloDTOs = titoloMapper.toDtoList(titoli);
        
        // Per ogni titolo, recupera la simulazione più recente e imposta il campo corso
        for (TitoloDTO titoloDTO : titoloDTOs) {
            try {
                // Recupera la simulazione più recente per il titolo
                SimulazioneDTO simulazione = simulazioneService.getLatestSimulazioneByTitoloId(titoloDTO.getIdTitolo());
                
                // Se esiste una simulazione, imposta il campo corso con il prezzo di acquisto
                if (simulazione != null) {
                    titoloDTO.setCorso(simulazione.getPrezzoAcquisto());
                    log.debug("Corso impostato a {} per il titolo ID: {} (ISIN: {})", 
                            simulazione.getPrezzoAcquisto(), titoloDTO.getIdTitolo(), titoloDTO.getCodiceIsin());
                }
            } catch (Exception e) {
                // Se non esiste una simulazione per il titolo, lascia il campo corso invariato
                log.debug("Nessuna simulazione trovata per il titolo ID: {}", titoloDTO.getIdTitolo());
            }
        }
        
        return titoloDTOs;
    }
    
    @Override
    public List<TitoloDTO> getTitoliByUtenteId(Integer utenteId) {
        List<Titolo> titoli = titoloRepository.findByUtente_IdUtente(utenteId);
        List<TitoloDTO> titoloDTOs = titoloMapper.toDtoList(titoli);
        
        // Per ogni titolo, recupera la simulazione più recente e imposta il campo corso
        for (TitoloDTO titoloDTO : titoloDTOs) {
            try {
                // Recupera la simulazione più recente per il titolo
                SimulazioneDTO simulazione = simulazioneService.getLatestSimulazioneByTitoloId(titoloDTO.getIdTitolo());
                
                // Se esiste una simulazione, imposta il campo corso con il prezzo di acquisto
                if (simulazione != null) {
                    titoloDTO.setCorso(simulazione.getPrezzoAcquisto());
                    log.debug("Corso impostato a {} per il titolo ID: {} (ISIN: {})", 
                            simulazione.getPrezzoAcquisto(), titoloDTO.getIdTitolo(), titoloDTO.getCodiceIsin());
                }
            } catch (Exception e) {
                // Se non esiste una simulazione per il titolo, lascia il campo corso invariato
                log.debug("Nessuna simulazione trovata per il titolo ID: {}", titoloDTO.getIdTitolo());
            }
        }
        
        return titoloDTOs;
    }

    @Override
    @Transactional
    public TitoloDTO saveTitolo(TitoloDTO titoloDTO) {
        try {
            log.info("Inizio salvataggio titolo - ISIN: {}", titoloDTO.getCodiceIsin());
            
            // Ottieni l'utente corrente se non è già impostato
            if (titoloDTO.getUtenteId() == null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                
                Integer utenteId = utenteService.findByUsername(username)
                        .map(UtenteResponseDTO::getIdUtente)
                        .orElseThrow(() -> new IllegalStateException("Utente non autenticato"));
                
                titoloDTO.setUtenteId(utenteId);
                log.info("Utente ID impostato automaticamente a {} per il titolo ISIN: {}", utenteId, titoloDTO.getCodiceIsin());
            }
            
            // Recupera l'utente dal repository
            Utente utente = utenteRepository.findById(titoloDTO.getUtenteId())
                    .orElseThrow(() -> new IllegalStateException("Utente non trovato con ID: " + titoloDTO.getUtenteId()));
            
            // Tenta di recuperare i dati aggiornati da Borsa Italiana se è specificato un tipo di titolo
            if (titoloDTO.getTipoTitolo() != null && titoloDTO.getCodiceIsin() != null) {
                try {
                    log.info("Tentativo di recupero dati da Borsa Italiana - ISIN: {}, Tipo: {}", 
                            titoloDTO.getCodiceIsin(), titoloDTO.getTipoTitolo());
                    
                    // Ottieni il servizio appropriato per il tipo di titolo
                    BorsaItalianaService service = borsaItalianaServiceFactory.getBorsaItalianaService(titoloDTO.getTipoTitolo());
                    
                    // Recupera i dati del titolo da Borsa Italiana
                    TitoloDTO titoloAggiornato = null;
                    if (titoloDTO.getTipoTitolo() == TipoTitolo.BTP) {
                        titoloAggiornato = service.getTitoloByIsin(titoloDTO.getCodiceIsin());
                    } else if (titoloDTO.getTipoTitolo() == TipoTitolo.BOT) {
                        titoloAggiornato = service.getTitoloBotByIsin(titoloDTO.getCodiceIsin());
                    }
                    
                    // Se abbiamo ottenuto dati da Borsa Italiana, aggiorna il DTO
                    if (titoloAggiornato != null) {
                        log.info("Dati recuperati con successo da Borsa Italiana - ISIN: {}", titoloDTO.getCodiceIsin());
                        
                        // Mantieni l'ID e l'ID utente originali
                        Integer idTitolo = titoloDTO.getIdTitolo();
                        Integer utenteId = titoloDTO.getUtenteId();
                        
                        // Aggiorna il DTO con i dati da Borsa Italiana
                        titoloDTO.setNome(titoloAggiornato.getNome());
                        titoloDTO.setDataScadenza(titoloAggiornato.getDataScadenza());
                        titoloDTO.setTassoNominale(titoloAggiornato.getTassoNominale());
                        titoloDTO.setCorso(titoloAggiornato.getCorso());
                        
                        // Ripristina l'ID e l'ID utente
                        titoloDTO.setIdTitolo(idTitolo);
                        titoloDTO.setUtenteId(utenteId);
                    }
                } catch (Exception e) {
                    log.warn("Impossibile recuperare dati da Borsa Italiana - ISIN: {}, Errore: {}", 
                            titoloDTO.getCodiceIsin(), e.getMessage());
                    // Continua con i dati forniti dall'utente
                }
            }
            
            // Verifica che i dati minimi necessari siano presenti
            verificaDatiMinimi(titoloDTO);
            
            // Creazione di un nuovo titolo con l'utente specificato
            Titolo titolo = titoloMapper.toEntity(titoloDTO, utente);
            titolo = titoloRepository.save(titolo);
            
            log.info("Titolo salvato con successo - ISIN: {}, ID: {}", titoloDTO.getCodiceIsin(), titolo.getIdTitolo());
            return titoloMapper.toDto(titolo);
        } catch (ObjectOptimisticLockingFailureException e) {
            // Gestione dell'eccezione di concorrenza ottimistica
            log.error("Errore di concorrenza ottimistica durante il salvataggio del titolo - ISIN: {}", titoloDTO.getCodiceIsin(), e);
            throw new ConflittoModificaException(
                "Il titolo è stato modificato da un altro utente. Ricarica e riprova.", e);
        } catch (Exception e) {
            log.error("Errore durante il salvataggio del titolo - ISIN: {}, Errore: {}", titoloDTO.getCodiceIsin(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Verifica che i dati minimi necessari siano presenti nel DTO e imposta valori di default se necessario
     * @param titoloDTO il DTO da verificare
     */
    private void verificaDatiMinimi(TitoloDTO titoloDTO) {
        // Verifica il nome
        if (titoloDTO.getNome() == null || titoloDTO.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del titolo è obbligatorio");
        }
        
        // Verifica il codice ISIN
        if (titoloDTO.getCodiceIsin() == null || titoloDTO.getCodiceIsin().trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice ISIN è obbligatorio");
        }
        
        // Verifica la data di scadenza
        if (titoloDTO.getDataScadenza() == null) {
            throw new IllegalArgumentException("La data di scadenza è obbligatoria");
        }
        
        // Verifica il tasso nominale e imposta a zero se null
        if (titoloDTO.getTassoNominale() == null) {
            titoloDTO.setTassoNominale(BigDecimal.ZERO);
            log.info("Tasso nominale non disponibile, impostato a 0 - ISIN: {}", titoloDTO.getCodiceIsin());
        }
        
        // Verifica la periodicità delle cedole
        if (titoloDTO.getPeriodicitaCedole() == null) {
            titoloDTO.setPeriodicitaCedole("SEMESTRALE");
            log.info("Periodicità cedole non disponibile, impostata a SEMESTRALE - ISIN: {}", titoloDTO.getCodiceIsin());
        }
        
        // Verifica la periodicità del bollo
        if (titoloDTO.getPeriodicitaBollo() == null) {
            titoloDTO.setPeriodicitaBollo("ANNUALE");
            log.info("Periodicità bollo non disponibile, impostata a ANNUALE - ISIN: {}", titoloDTO.getCodiceIsin());
        }
        
        // Verifica il tipo di titolo
        if (titoloDTO.getTipoTitolo() == null) {
            throw new IllegalArgumentException("Il tipo di titolo è obbligatorio");
        }
    }

    @Override
    public TitoloDTO findById(Integer id) {
        TitoloDTO titoloDTO = titoloRepository.findById(id)
                .map(titoloMapper::toDto)
                .orElse(null);
        
        if (titoloDTO != null) {
            try {
                // Recupera la simulazione più recente per il titolo
                SimulazioneDTO simulazione = simulazioneService.getLatestSimulazioneByTitoloId(titoloDTO.getIdTitolo());
                
                // Se esiste una simulazione, imposta il campo corso con il prezzo di acquisto
                if (simulazione != null) {
                    titoloDTO.setCorso(simulazione.getPrezzoAcquisto());
                    log.debug("Corso impostato a {} per il titolo ID: {} (ISIN: {})", 
                            simulazione.getPrezzoAcquisto(), titoloDTO.getIdTitolo(), titoloDTO.getCodiceIsin());
                }
            } catch (Exception e) {
                // Se non esiste una simulazione per il titolo, lascia il campo corso invariato
                log.debug("Nessuna simulazione trovata per il titolo ID: {}", titoloDTO.getIdTitolo());
            }
        }
        
        return titoloDTO;
    }
    
    @Override
    public TitoloDTO findByCodiceIsin(String codiceIsin) {
        Titolo titolo = titoloRepository.findByCodiceIsin(codiceIsin);
        TitoloDTO titoloDTO = titoloMapper.toDto(titolo);
        
        if (titoloDTO != null) {
            try {
                // Recupera la simulazione più recente per il titolo
                SimulazioneDTO simulazione = simulazioneService.getLatestSimulazioneByTitoloId(titoloDTO.getIdTitolo());
                
                // Se esiste una simulazione, imposta il campo corso con il prezzo di acquisto
                if (simulazione != null) {
                    titoloDTO.setCorso(simulazione.getPrezzoAcquisto());
                    log.debug("Corso impostato a {} per il titolo ID: {} (ISIN: {})", 
                            simulazione.getPrezzoAcquisto(), titoloDTO.getIdTitolo(), titoloDTO.getCodiceIsin());
                }
            } catch (Exception e) {
                // Se non esiste una simulazione per il titolo, lascia il campo corso invariato
                log.debug("Nessuna simulazione trovata per il titolo ID: {}", titoloDTO.getIdTitolo());
            }
        }
        
        return titoloDTO;
    }

    @Override
    public boolean existsByCodiceIsin(String codiceIsin) {
        return titoloRepository.existsByCodiceIsin(codiceIsin);
    }

    @Override
    @Transactional
    public void deleteTitolo(Integer id) {
        titoloRepository.deleteById(id);
    }
    
    @Override
    public List<TitoloDTO> getTitoliByUtenteIdAndDataScadenzaBefore(Integer utenteId, java.time.LocalDate dataScadenza) {
        List<Titolo> titoli = titoloRepository.findByDataScadenzaAfterAndUtente_IdUtente(java.time.LocalDate.now(), utenteId);
        // Filtra i titoli che hanno una data di scadenza precedente o uguale alla data specificata
        List<Titolo> titoliFiltered = titoli.stream()
                .filter(titolo -> titolo.getDataScadenza().isBefore(dataScadenza) || titolo.getDataScadenza().isEqual(dataScadenza))
                .toList();
        
        List<TitoloDTO> titoloDTOs = titoloMapper.toDtoList(titoliFiltered);
        
        // Per ogni titolo, recupera la simulazione più recente e imposta il campo corso
        for (TitoloDTO titoloDTO : titoloDTOs) {
            try {
                // Recupera la simulazione più recente per il titolo
                SimulazioneDTO simulazione = simulazioneService.getLatestSimulazioneByTitoloId(titoloDTO.getIdTitolo());
                
                // Se esiste una simulazione, imposta il campo corso con il prezzo di acquisto
                if (simulazione != null) {
                    titoloDTO.setCorso(simulazione.getPrezzoAcquisto());
                    log.debug("Corso impostato a {} per il titolo ID: {} (ISIN: {})", 
                            simulazione.getPrezzoAcquisto(), titoloDTO.getIdTitolo(), titoloDTO.getCodiceIsin());
                }
            } catch (Exception e) {
                // Se non esiste una simulazione per il titolo, lascia il campo corso invariato
                log.debug("Nessuna simulazione trovata per il titolo ID: {}", titoloDTO.getIdTitolo());
            }
        }
        
        return titoloDTOs;
    }
    
    @Override
    @Transactional
    public TitoloDTO importaTitoloDaBorsaItaliana(String codiceIsin, String tipoTitoloStr) {
        log.info("Importazione titolo da Borsa Italiana - ISIN: {}, Tipo: {}", codiceIsin, tipoTitoloStr);
        
        try {
            // Converti la stringa del tipo titolo in enum
            TipoTitolo tipoTitolo = TipoTitolo.valueOf(tipoTitoloStr.toUpperCase());
            
            // Ottieni il servizio appropriato per il tipo di titolo
            BorsaItalianaService service = borsaItalianaServiceFactory.getBorsaItalianaService(tipoTitolo);
            
            // Recupera i dati del titolo da Borsa Italiana
            TitoloDTO titoloDTO;
            if (tipoTitolo == TipoTitolo.BTP) {
                titoloDTO = service.getTitoloByIsin(codiceIsin);
            } else if (tipoTitolo == TipoTitolo.BOT) {
                titoloDTO = service.getTitoloBotByIsin(codiceIsin);
            } else {
                throw new IllegalArgumentException("Tipo titolo non supportato: " + tipoTitolo);
            }
            
            if (titoloDTO == null) {
                log.error("Titolo non trovato su Borsa Italiana - ISIN: {}, Tipo: {}", codiceIsin, tipoTitoloStr);
                throw new IllegalArgumentException("Titolo non trovato su Borsa Italiana");
            }
            
            // Assicurati che il tasso sia un numero (se N/A, imposta a 0)
            if (titoloDTO.getTassoNominale() == null) {
                titoloDTO.setTassoNominale(BigDecimal.ZERO);
                log.info("Tasso nominale non disponibile, impostato a 0 - ISIN: {}", codiceIsin);
            }
            
            // Imposta il tipo di titolo
            titoloDTO.setTipoTitolo(tipoTitolo);
            
            // Imposta la periodicità delle cedole se non presente
            if (titoloDTO.getPeriodicitaCedole() == null) {
                titoloDTO.setPeriodicitaCedole("SEMESTRALE");
                log.info("Periodicità cedole non disponibile, impostata a SEMESTRALE - ISIN: {}", codiceIsin);
            }
            
            // Imposta la periodicità del bollo
            titoloDTO.setPeriodicitaBollo("ANNUALE");
            
            // Salva il titolo nel database
            TitoloDTO savedTitolo = saveTitolo(titoloDTO);
            log.info("Titolo importato con successo - ISIN: {}, ID: {}", codiceIsin, savedTitolo.getIdTitolo());
            
            return savedTitolo;
        } catch (IllegalArgumentException e) {
            log.error("Errore durante l'importazione del titolo - ISIN: {}, Tipo: {}, Errore: {}", 
                    codiceIsin, tipoTitoloStr, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Errore imprevisto durante l'importazione del titolo - ISIN: {}, Tipo: {}, Errore: {}", 
                    codiceIsin, tipoTitoloStr, e.getMessage());
            throw new RuntimeException("Errore durante l'importazione del titolo", e);
        }
    }
    
    @Override
    public RendimentiDTO calcolaRendimentiPerPeriodo(PeriodoScadenza periodo) {
        log.info("Calcolo rendimenti per periodo: {}", periodo);
        
        try {
            // Recupera tutti i titoli
            List<Titolo> titoli = titoloRepository.findAll();
            
            // Filtra i titoli in base al periodo di scadenza
            List<Titolo> titoliFiltrati = titoli.stream()
                    .filter(titolo -> {
                        // Calcola i mesi tra oggi e la data di scadenza
                        LocalDate oggi = LocalDate.now();
                        LocalDate scadenza = titolo.getDataScadenza();
                        long mesiAllaScadenza = ChronoUnit.MONTHS.between(oggi, scadenza);
                        
                        // Verifica se il titolo rientra nel periodo specificato
                        return mesiAllaScadenza >= periodo.getMesiMin() && mesiAllaScadenza <= periodo.getMesiMax();
                    })
                    .collect(Collectors.toList());
            
            return calcolaRendimenti(titoliFiltrati);
        } catch (Exception e) {
            log.error("Errore durante il calcolo dei rendimenti per periodo: {}, Errore: {}", 
                    periodo, e.getMessage(), e);
            throw new RuntimeException("Errore durante il calcolo dei rendimenti", e);
        }
    }
    
    @Override
    public RendimentiDTO calcolaRendimentiPerPeriodo(String periodoString) {
        PeriodoScadenza periodo = PeriodoScadenza.fromString(periodoString);
        return calcolaRendimentiPerPeriodo(periodo);
    }
    
    /**
     * Calcola i rendimenti per una lista di titoli.
     * 
     * @param titoli Lista di titoli per cui calcolare i rendimenti
     * @return DTO contenente i rendimenti calcolati
     */
    private RendimentiDTO calcolaRendimenti(List<Titolo> titoli) {
        if (titoli.isEmpty()) {
            return new RendimentiDTO(0.0, 0.0, 0.0, new ArrayList<>());
        }
        
        List<RendimentiDTO.TitoloRendimentoDTO> titoliRendimento = new ArrayList<>();
        double rendimentoMinimoTrimestrale = Double.MAX_VALUE;
        double rendimentoMassimoTrimestrale = Double.MIN_VALUE;
        double rendimentoMinimoAnnuale = Double.MAX_VALUE;
        double rendimentoMassimoAnnuale = Double.MIN_VALUE;
        double sommaRendimentiTrimestrali = 0.0;
        double sommaRendimentiAnnuali = 0.0;
        double sommaRendimentiBolloMensile = 0.0;
        double sommaRendimentiBolloAnnuale = 0.0;
        
        // Aliquota imposta di bollo (0.2% annuale)
        final double ALIQUOTA_BOLLO_ANNUALE = 0.002;
        final double ALIQUOTA_BOLLO_MENSILE = ALIQUOTA_BOLLO_ANNUALE / 12;
        
        for (Titolo titolo : titoli) {
            // Calcola il rendimento trimestrale (tasso nominale / 4)
            double rendimentoTrimestrale = titolo.getTassoNominale().doubleValue() / 4;
            
            // Calcola il rendimento annuale (tasso nominale)
            double rendimentoAnnuale = titolo.getTassoNominale().doubleValue();
            
            // Calcola il rendimento con bollo mensile
            double rendimentoBolloMensile = rendimentoAnnuale - (ALIQUOTA_BOLLO_MENSILE * 12 * 100);
            
            // Calcola il rendimento con bollo annuale
            double rendimentoBolloAnnuale = rendimentoAnnuale - (ALIQUOTA_BOLLO_ANNUALE * 100);
            
            // Aggiorna i valori minimi e massimi
            rendimentoMinimoTrimestrale = Math.min(rendimentoMinimoTrimestrale, rendimentoTrimestrale);
            rendimentoMassimoTrimestrale = Math.max(rendimentoMassimoTrimestrale, rendimentoTrimestrale);
            rendimentoMinimoAnnuale = Math.min(rendimentoMinimoAnnuale, rendimentoAnnuale);
            rendimentoMassimoAnnuale = Math.max(rendimentoMassimoAnnuale, rendimentoAnnuale);
            
            // Aggiorna le somme per il calcolo delle medie
            sommaRendimentiTrimestrali += rendimentoTrimestrale;
            sommaRendimentiAnnuali += rendimentoAnnuale;
            sommaRendimentiBolloMensile += rendimentoBolloMensile;
            sommaRendimentiBolloAnnuale += rendimentoBolloAnnuale;
            
            // Crea il DTO per il titolo
            RendimentiDTO.TitoloRendimentoDTO titoloRendimentoDTO = new RendimentiDTO.TitoloRendimentoDTO(
                    titolo.getNome(),
                    rendimentoTrimestrale,
                    rendimentoAnnuale,
                    rendimentoBolloMensile,
                    rendimentoBolloAnnuale
            );
            
            titoliRendimento.add(titoloRendimentoDTO);
        }
        
        // Calcola le medie
        double rendimentoMedioTrimestrale = sommaRendimentiTrimestrali / titoli.size();
        double rendimentoMedioAnnuale = sommaRendimentiAnnuali / titoli.size();
        
        // Calcola il rendimento medio complessivo (media tra trimestrale e annuale)
        double rendimentoMedio = (rendimentoMedioTrimestrale + rendimentoMedioAnnuale) / 2;
        
        // Calcola il rendimento minimo complessivo (media tra trimestrale e annuale)
        double rendimentoMinimo = (rendimentoMinimoTrimestrale + rendimentoMinimoAnnuale) / 2;
        
        // Calcola il rendimento massimo complessivo (media tra trimestrale e annuale)
        double rendimentoMassimo = (rendimentoMassimoTrimestrale + rendimentoMassimoAnnuale) / 2;
        
        return new RendimentiDTO(rendimentoMinimo, rendimentoMedio, rendimentoMassimo, titoliRendimento);
    }
}
