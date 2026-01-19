package com.example.rendimento.service.impl;

import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.exception.ConflittoModificaException;
import com.example.rendimento.mapper.TitoloMapper;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.repository.UtenteRepository;
import com.example.rendimento.service.BorsaItalianaService;
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

import java.util.List;

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

    @Override
    public List<TitoloDTO> getAllTitoli() {
        List<Titolo> titoli = titoloRepository.findAll();
        return titoloMapper.toDtoList(titoli);
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
        return titoloRepository.findById(id)
                .map(titoloMapper::toDto)
                .orElse(null);
    }
    
    @Override
    public TitoloDTO findByCodiceIsin(String codiceIsin) {
        Titolo titolo = titoloRepository.findByCodiceIsin(codiceIsin);
        return titoloMapper.toDto(titolo);
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
}
