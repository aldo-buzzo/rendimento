package com.example.rendimento.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.rendimento.constants.RendimentoConstants;
import com.example.rendimento.dto.RisultatoRendimentoAdvancedDTO;
import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.dto.TrendRendimentiDTO;
import com.example.rendimento.dto.TrendRendimentiDTO.TitoloRendimentoDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.service.BorsaItalianaService;
import com.example.rendimento.service.SimulazioneService;
import com.example.rendimento.service.TrendService;
import com.example.rendimento.service.UtenteService;
import com.example.rendimento.service.factory.BorsaItalianaServiceFactory;

import jakarta.persistence.EntityNotFoundException;

/**
 * Controller REST per la gestione delle simulazioni.
 */
@RestController
@RequestMapping("/api/simulazioni")
public class SimulazioneController {

    private static final Logger log = LoggerFactory.getLogger(SimulazioneController.class);
    
    private final SimulazioneService simulazioneService;
    private final TitoloRepository titoloRepository;
    private final BorsaItalianaServiceFactory borsaItalianaServiceFactory;
    private final UtenteService utenteService;
    private final TrendService trendService;

    /**
     * Costruttore con parametri per l'iniezione delle dipendenze.
     *
     * @param simulazioneService servizio per la gestione delle simulazioni
     */
    @Autowired
    public SimulazioneController(SimulazioneService simulazioneService, TitoloRepository titoloRepository, 
                                BorsaItalianaServiceFactory borsaItalianaServiceFactory, UtenteService utenteService,
                                TrendService trendService) {
        this.simulazioneService = simulazioneService;
        this.titoloRepository = titoloRepository;
        this.borsaItalianaServiceFactory = borsaItalianaServiceFactory;
        this.utenteService = utenteService;
        this.trendService = trendService;
    }
    
    /**
     * Classe interna per contenere i risultati dell'elaborazione della simulazione.
     */
    private static class ElaborazioneRisultato {
        private final SimulazioneDTO simulazione;
        private final RisultatoRendimentoAdvancedDTO risultatoDettagliato;
        
        public ElaborazioneRisultato(SimulazioneDTO simulazione, RisultatoRendimentoAdvancedDTO risultatoDettagliato) {
            this.simulazione = simulazione;
            this.risultatoDettagliato = risultatoDettagliato;
        }
        
        public SimulazioneDTO getSimulazione() {
            return simulazione;
        }
        
        public RisultatoRendimentoAdvancedDTO getRisultatoDettagliato() {
            return risultatoDettagliato;
        }
    }

    /**
     * Endpoint per il calcolo del rendimento di un titolo.
     *
     * @param idTitolo ID del titolo
     * @param prezzoAcquisto prezzo di acquisto inserito dall'utente
     * @param importo importo dell'investimento
     * @return DTO contenente tutti i risultati del calcolo
     */
    @PostMapping("/calcola-rendimento")
    public ResponseEntity<RisultatoSimulazioneDTO> calcolaRendimento(
            @RequestParam Integer idTitolo,
            @RequestParam BigDecimal prezzoAcquisto,
            @RequestParam BigDecimal importo) {
        
        log.info("Ricevuta richiesta POST /api/simulazioni/calcola-rendimento con idTitolo: {}, prezzoAcquisto: {}, importo: {}", 
                idTitolo, prezzoAcquisto, importo);
        
        RisultatoSimulazioneDTO risultato = simulazioneService.calcolaRendimento(
            idTitolo, prezzoAcquisto, importo);
        
        log.info("Risposta per POST /api/simulazioni/calcola-rendimento: {}", risultato);
        return ResponseEntity.ok(risultato);
    }

    /**
     * Endpoint per il salvataggio di una simulazione.
     *
     * @param simulazioneDTO il DTO contenente i dati della simulazione
     * @return il DTO della simulazione salvata con ID aggiornato
     */
    @PostMapping
    public ResponseEntity<SimulazioneDTO> salvaSimulazione(@RequestBody SimulazioneDTO simulazioneDTO) {
        log.info("Ricevuta richiesta POST /api/simulazioni con dati: {}", simulazioneDTO);
        SimulazioneDTO savedSimulazione = simulazioneService.salvaSimulazione(simulazioneDTO);
        log.info("Risposta per POST /api/simulazioni: {}", savedSimulazione);
        return ResponseEntity.ok(savedSimulazione);
    }
    
    /**
     * Endpoint per il calcolo e il salvataggio di una simulazione in un'unica operazione.
     * Questo endpoint ricalcola tutti i valori prima di salvare la simulazione.
     *
     * @param idTitolo ID del titolo
     * @param prezzoAcquisto prezzo di acquisto inserito dall'utente
     * @param importo importo dell'investimento
     * @param dataAcquisto data di acquisto
     * @return il DTO della simulazione salvata con ID aggiornato
     */
    @PostMapping("/calcola-e-salva")
    public ResponseEntity<SimulazioneDTO> calcolaESalvaSimulazione(
            @RequestParam Integer idTitolo,
            @RequestParam BigDecimal prezzoAcquisto,
            @RequestParam BigDecimal importo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAcquisto) {
        
        log.info("Ricevuta richiesta POST /api/simulazioni/calcola-e-salva con idTitolo: {}, prezzoAcquisto: {}, importo: {}, dataAcquisto: {}", 
                idTitolo, prezzoAcquisto, importo, dataAcquisto);
        
        SimulazioneDTO savedSimulazione = simulazioneService.calcolaESalvaSimulazione(
            idTitolo, prezzoAcquisto, importo, dataAcquisto);
        
        log.info("Risposta per POST /api/simulazioni/calcola-e-salva: {}", savedSimulazione);
        return ResponseEntity.ok(savedSimulazione);
    }

    /**
     * Endpoint per il recupero di tutte le simulazioni.
     * Se il parametro 'latest' è true, restituisce solo le simulazioni più recenti per ogni titolo.
     *
     * @param latest se true, restituisce solo le simulazioni più recenti per ogni titolo
     * @return lista di simulazioni
     */
    @GetMapping
    public ResponseEntity<List<SimulazioneDTO>> getAllSimulazioni(
            @RequestParam(required = false, defaultValue = "true") boolean latest) {
        log.info("Ricevuta richiesta GET /api/simulazioni con parametro latest: {}", latest);
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElseThrow(() -> new IllegalStateException("Utente non autenticato"));
        
        // Usa il metodo ottimizzato che filtra direttamente nel database e ordina per data di scadenza crescente
        List<SimulazioneDTO> simulazioni = simulazioneService.getSimulazioniByUtenteIdOrderByScadenzaAsc(utenteId, latest);
        log.info("Recuperate {} simulazioni per l'utente ID: {} (latest: {}) ordinate per data di scadenza crescente", 
                simulazioni.size(), utenteId, latest);
        
        return ResponseEntity.ok(simulazioni);
    }

    /**
     * Endpoint per il recupero di una simulazione per ID.
     *
     * @param id l'ID della simulazione da cercare
     * @return la simulazione trovata
     */
    @GetMapping("/{id}")
    public ResponseEntity<SimulazioneDTO> getSimulazioneById(@PathVariable Integer id) {
        log.info("Ricevuta richiesta GET /api/simulazioni/{} con id: {}", "id", id);
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElse(null);
        
        SimulazioneDTO simulazione = simulazioneService.findById(id);
        
        // Verifica che la simulazione appartenga all'utente corrente
        if (simulazione != null && simulazione.getTitolo() != null && 
                (simulazione.getTitolo().getUtenteId() == null || 
                 simulazione.getTitolo().getUtenteId().equals(utenteId))) {
            log.info("Risposta per GET /api/simulazioni/{}: {}", id, simulazione);
            return ResponseEntity.ok(simulazione);
        } else {
            log.info("Risposta per GET /api/simulazioni/{}: Simulazione non trovata o non autorizzata", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint per l'eliminazione di una simulazione per ID.
     *
     * @param id l'ID della simulazione da eliminare
     * @return risposta vuota con stato 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimulazione(@PathVariable Integer id) {
        log.info("Ricevuta richiesta DELETE /api/simulazioni/{} con id: {}", "id", id);
        simulazioneService.deleteSimulazione(id);
        log.info("Risposta per DELETE /api/simulazioni/{}: Simulazione eliminata con successo", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Endpoint per il recupero dei dettagli di simulazione per un titolo specifico.
     * Restituisce la simulazione più recente per il titolo specificato, con i dettagli del titolo inclusi.
     *
     * @param idTitolo l'ID del titolo per cui recuperare i dettagli di simulazione
     * @return la simulazione più recente per il titolo specificato, con i dettagli del titolo inclusi
     */
    @GetMapping("/titolo/{idTitolo}")
    public ResponseEntity<SimulazioneDTO> getSimulazioneByTitoloId(@PathVariable Integer idTitolo) {
        log.info("Ricevuta richiesta GET /api/simulazioni/titolo/{} con idTitolo: {}", "idTitolo", idTitolo);
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElse(null);
        
        // Ottieni la simulazione più recente per il titolo specificato
        SimulazioneDTO simulazione = simulazioneService.getLatestSimulazioneByTitoloId(idTitolo);
        
        // Ottieni i dettagli del titolo
        Titolo titolo = titoloRepository.findById(idTitolo)
            .orElseThrow(() -> new EntityNotFoundException("Titolo non trovato con ID: " + idTitolo));
        
        // Verifica che il titolo appartenga all'utente corrente
        if (titolo.getUtente() != null && utenteId != null && !titolo.getUtente().getIdUtente().equals(utenteId)) {
            log.info("Risposta per GET /api/simulazioni/titolo/{}: Titolo non trovato o non autorizzato", idTitolo);
            return ResponseEntity.notFound().build();
        }
        
        // Converti il titolo in DTO
        TitoloDTO titoloDTO = new TitoloDTO();
        titoloDTO.setIdTitolo(titolo.getIdTitolo());
        titoloDTO.setNome(titolo.getNome());
        titoloDTO.setCodiceIsin(titolo.getCodiceIsin());
        titoloDTO.setDataScadenza(titolo.getDataScadenza());
        titoloDTO.setTassoNominale(titolo.getTassoNominale());
        titoloDTO.setPeriodicitaCedole(titolo.getPeriodicitaCedole().toString());
        titoloDTO.setPeriodicitaBollo(titolo.getPeriodicitaBollo().toString());
        titoloDTO.setTipoTitolo(titolo.getTipoTitolo());
        titoloDTO.setUtenteId(titolo.getUtente() != null ? titolo.getUtente().getIdUtente() : null);
        
        // Imposta il titolo nella simulazione
        simulazione.setTitolo(titoloDTO);
        
        log.info("Risposta per GET /api/simulazioni/titolo/{}: {}", idTitolo, simulazione);
        return ResponseEntity.ok(simulazione);
    }
    
    /**
     * Endpoint per il recupero di tutte le simulazioni per un titolo specifico.
     * Restituisce tutte le simulazioni per il titolo specificato, ordinate per data di acquisto decrescente.
     *
     * @param idTitolo l'ID del titolo per cui recuperare le simulazioni
     * @return lista di tutte le simulazioni per il titolo specificato
     */
    @GetMapping("/titolo/{idTitolo}/all")
    public ResponseEntity<List<SimulazioneDTO>> getAllSimulazioniByTitoloId(@PathVariable Integer idTitolo) {
        log.info("Ricevuta richiesta GET /api/simulazioni/titolo/{}/all con idTitolo: {}", "idTitolo", idTitolo);
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElse(null);
        
        // Verifica che il titolo appartenga all'utente corrente
        Titolo titolo = titoloRepository.findById(idTitolo).orElse(null);
        if (titolo != null && titolo.getUtente() != null && utenteId != null && 
                !titolo.getUtente().getIdUtente().equals(utenteId)) {
            log.info("Risposta per GET /api/simulazioni/titolo/{}/all: Titolo non trovato o non autorizzato", idTitolo);
            return ResponseEntity.notFound().build();
        }
        
        // Ottieni tutte le simulazioni per il titolo specificato
        List<SimulazioneDTO> simulazioni = simulazioneService.findByTitoloId(idTitolo);
        
        log.info("Risposta per GET /api/simulazioni/titolo/{}/all: {} simulazioni trovate", idTitolo, simulazioni.size());
        return ResponseEntity.ok(simulazioni);
    }
    
    /**
     * Endpoint per il recupero dei dettagli di una simulazione con i valori ricalcolati.
     * Questo endpoint è utile per visualizzare i dettagli di una simulazione esistente
     * con i valori ricalcolati in base ai parametri attuali.
     *
     * @param id l'ID della simulazione da ricalcolare
     * @return i risultati del calcolo del rendimento
     */
    @GetMapping("/{id}/ricalcola")
    public ResponseEntity<RisultatoSimulazioneDTO> ricalcolaSimulazione(@PathVariable Integer id) {
        log.info("Ricevuta richiesta GET /api/simulazioni/{}/ricalcola con id: {}", "id", id);
        
        // Ottieni la simulazione per ID
        SimulazioneDTO simulazione = simulazioneService.findById(id);
        
        // Ricalcola i valori della simulazione
        RisultatoSimulazioneDTO risultato = simulazioneService.ricalcolaValoriSimulazione(simulazione);
        
        log.info("Risposta per GET /api/simulazioni/{}/ricalcola: {}", id, risultato);
        return ResponseEntity.ok(risultato);
    }
    
    /**
     * Endpoint per calcolare il rendimento di tutti i titoli con scadenza futura.
     * Per ogni titolo viene calcolato il rendimento con un importo fisso di 10.000 euro.
     *
     * @return lista di simulazioni calcolate e salvate
     */
    @PostMapping("/calcola-rendimenti-tutti-titoli")
    public ResponseEntity<List<SimulazioneDTO>> calcolaRendimentiTuttiTitoli() {
        log.info("Ricevuta richiesta POST /api/simulazioni/calcola-rendimenti-tutti-titoli");
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElseThrow(() -> new IllegalStateException("Utente non autenticato"));
        
        // Recupera tutti i titoli con data di scadenza futura che appartengono all'utente corrente
        List<Titolo> titoliValidi = titoloRepository.findByDataScadenzaAfterAndUtente_IdUtente(LocalDate.now(), utenteId);
        log.info("Trovati {} titoli con scadenza futura per l'utente ID: {}", titoliValidi.size(), utenteId);
        
        List<SimulazioneDTO> simulazioniSalvate = new ArrayList<>();
        
        // Per ogni titolo, calcola e salva una simulazione
        for (Titolo titolo : titoliValidi) {
            try {
                // Ottieni il prezzo attuale del titolo
                BigDecimal prezzoAcquisto = getPrezzoAcquistoPerTitolo(titolo);
                if (prezzoAcquisto == null) {
                    continue; // Salta questo titolo e passa al prossimo
                }
                
                // Elabora la simulazione e ottieni il risultato dettagliato
                ElaborazioneRisultato risultatoElaborazione = elaboraSimulazionePerTitolo(titolo, prezzoAcquisto);
                if (risultatoElaborazione == null || risultatoElaborazione.getSimulazione() == null) {
                    continue; // Salta questo titolo se l'elaborazione ha fallito
                }
                
                // Salva il trend per il titolo utilizzando il rendimento senza costi
                trendService.salvaTrendPerTitolo(
                    titolo,
                    prezzoAcquisto,
                    risultatoElaborazione.getRisultatoDettagliato().getRendimentoSenzaCosti()
                );
                log.info("Trend salvato per il titolo ID: {}, ISIN: {}", 
                        titolo.getIdTitolo(), titolo.getCodiceIsin());
                
                simulazioniSalvate.add(risultatoElaborazione.getSimulazione());
            } catch (Exception e) {
                log.error("Errore nel calcolo della simulazione per il titolo ID: {}, ISIN: {}, Errore: {}", 
                        titolo.getIdTitolo(), titolo.getCodiceIsin(), e.getMessage());
            }
        }
        
        log.info("Risposta per POST /api/simulazioni/calcola-rendimenti-tutti-titoli: {} simulazioni aggiornate/create", 
                simulazioniSalvate.size());
        return ResponseEntity.ok(simulazioniSalvate);
    }
    
    /**
     * Endpoint per il recupero dei dati dettagliati di calcolo per una simulazione.
     * Questo endpoint restituisce un oggetto RisultatoRendimentoAdvancedDTO con tutti i dati
     * necessari per visualizzare i calcoli dettagliati nella pagina info-titolo-rendimenti.html.
     *
     * @param id l'ID della simulazione per cui recuperare i dati dettagliati
     * @return i dati dettagliati di calcolo
     */
    @GetMapping("/{id}/calcolo-dettagliato")
    public ResponseEntity<RisultatoRendimentoAdvancedDTO> getCalcoloDettagliato(@PathVariable Integer id) {
        log.info("Ricevuta richiesta GET /api/simulazioni/{}/calcolo-dettagliato con id: {}", "id", id);
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElse(null);
        
        // Recupera la simulazione
        SimulazioneDTO simulazione = simulazioneService.findById(id);
        if (simulazione == null) {
            log.info("Risposta per GET /api/simulazioni/{}/calcolo-dettagliato: Simulazione non trovata", id);
            return ResponseEntity.notFound().build();
        }
        
        // Verifica che la simulazione appartenga all'utente corrente
        if (simulazione.getTitolo() != null && simulazione.getTitolo().getUtenteId() != null && 
                utenteId != null && !simulazione.getTitolo().getUtenteId().equals(utenteId)) {
            log.info("Risposta per GET /api/simulazioni/{}/calcolo-dettagliato: Simulazione non autorizzata", id);
            return ResponseEntity.notFound().build();
        }
        
        // Recupera il titolo associato
        Titolo titolo = titoloRepository.findById(simulazione.getIdTitolo())
                .orElseThrow(() -> new EntityNotFoundException("Titolo non trovato con ID: " + simulazione.getIdTitolo()));
        
        // Utilizza la logica esistente per calcolare i rendimenti dettagliati
        RisultatoRendimentoAdvancedDTO risultato = simulazioneService.calcolaRendimentoAdvanced(
                titolo,
                simulazione.getPrezzoAcquisto(),
                simulazione.getNominale() != null ? simulazione.getNominale() : new BigDecimal("10000"),
                simulazione.getDataAcquisto()
        );
        
        log.info("Risposta per GET /api/simulazioni/{}/calcolo-dettagliato: {}", id, risultato);
        return ResponseEntity.ok(risultato);
    }
    
    /**
     * Endpoint per il recupero dei dati di trend dei rendimenti per un determinato periodo.
     * Questo endpoint restituisce un oggetto TrendRendimentiDTO con i dati statistici
     * (rendimento minimo, medio, massimo) e la lista dei titoli con i loro rendimenti.
     *
     * @param periodo il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     * @return i dati di trend dei rendimenti
     */
    @GetMapping("/trends/{periodo}")
    public ResponseEntity<TrendRendimentiDTO> getTrendRendimenti(@PathVariable String periodo) {
        log.info("Ricevuta richiesta GET /api/simulazioni/trends/{} con periodo: {}", "periodo", periodo);
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElseThrow(() -> new IllegalStateException("Utente non autenticato"));
        
        // Recupera tutti i titoli con data di scadenza futura che appartengono all'utente corrente
        List<Titolo> titoliValidi = titoloRepository.findByDataScadenzaAfterAndUtente_IdUtente(LocalDate.now(), utenteId);
        log.info("Trovati {} titoli con scadenza futura per l'utente ID: {}", titoliValidi.size(), utenteId);
        
        // Filtra i titoli in base al periodo di scadenza specificato
        List<Titolo> titoliFiltrati = filtraTitoliPerPeriodo(titoliValidi, periodo);
        log.info("Filtrati {} titoli per il periodo: {}", titoliFiltrati.size(), periodo);
        
        // Ordina i titoli per data di scadenza crescente
        titoliFiltrati.sort(Comparator.comparing(Titolo::getDataScadenza));
        log.info("Titoli ordinati per data di scadenza crescente");
        
        // Recupera le simulazioni più recenti per i titoli filtrati
        List<SimulazioneDTO> simulazioni = new ArrayList<>();
        for (Titolo titolo : titoliFiltrati) {
            try {
                SimulazioneDTO simulazione = simulazioneService.getLatestSimulazioneByTitoloId(titolo.getIdTitolo());
                simulazioni.add(simulazione);
            } catch (EntityNotFoundException e) {
                // Se non esiste una simulazione per il titolo, lo ignoriamo
                log.warn("Nessuna simulazione trovata per il titolo ID: {}", titolo.getIdTitolo());
            }
        }
        
        // Calcola i rendimenti per ogni titolo
        List<TitoloRendimentoDTO> titoliRendimento = new ArrayList<>();
        List<BigDecimal> rendimentiBolloAnnuale = new ArrayList<>();
        
        for (SimulazioneDTO simulazione : simulazioni) {
            try {
                // Recupera il titolo associato
                Titolo titolo = titoloRepository.findById(simulazione.getIdTitolo())
                        .orElseThrow(() -> new EntityNotFoundException("Titolo non trovato con ID: " + simulazione.getIdTitolo()));
                
                // Calcola i rendimenti dettagliati
                RisultatoRendimentoAdvancedDTO risultato = simulazioneService.calcolaRendimentoAdvanced(
                        titolo,
                        simulazione.getPrezzoAcquisto(),
                        simulazione.getNominale() != null ? simulazione.getNominale() : new BigDecimal("10000"),
                        simulazione.getDataAcquisto()
                );
                
                // Crea un oggetto TitoloRendimentoDTO con i dati del titolo e i rendimenti calcolati
                TitoloRendimentoDTO titoloRendimento = new TitoloRendimentoDTO(
                        titolo.getIdTitolo(),
                        titolo.getNome(),
                        titolo.getCodiceIsin(),
                        risultato.getRendimentoConCommissioniEBolloMensile(),
                        risultato.getRendimentoConCommissioniEBolloAnnuale()
                );
                
                titoliRendimento.add(titoloRendimento);
                rendimentiBolloAnnuale.add(risultato.getRendimentoConCommissioniEBolloAnnuale());
            } catch (Exception e) {
                log.error("Errore nel calcolo dei rendimenti per il titolo ID: {}, Errore: {}", 
                        simulazione.getIdTitolo(), e.getMessage());
            }
        }
        
        // Calcola i rendimenti minimi, medi e massimi
        BigDecimal rendimentoMinimo = BigDecimal.ZERO;
        BigDecimal rendimentoMedio = BigDecimal.ZERO;
        BigDecimal rendimentoMassimo = BigDecimal.ZERO;
        
        if (!rendimentiBolloAnnuale.isEmpty()) {
            rendimentoMinimo = rendimentiBolloAnnuale.stream()
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            
            rendimentoMassimo = rendimentiBolloAnnuale.stream()
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            
            rendimentoMedio = rendimentiBolloAnnuale.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(rendimentiBolloAnnuale.size()), 4, BigDecimal.ROUND_HALF_UP);
        }
        
        // Crea l'oggetto TrendRendimentiDTO con i dati calcolati
        TrendRendimentiDTO trendRendimenti = new TrendRendimentiDTO(
                rendimentoMinimo,
                rendimentoMedio,
                rendimentoMassimo,
                titoliRendimento
        );
        
        log.info("Risposta per GET /api/simulazioni/trends/{}: {} titoli con rendimenti", periodo, titoliRendimento.size());
        return ResponseEntity.ok(trendRendimenti);
    }
    
    /**
     * Ottiene il prezzo di acquisto per un titolo utilizzando il servizio appropriato.
     * 
     * @param titolo il titolo per cui ottenere il prezzo
     * @return il prezzo di acquisto o null se non disponibile
     */
    private BigDecimal getPrezzoAcquistoPerTitolo(Titolo titolo) {
        try {
            // Ottieni il servizio appropriato in base al tipo di titolo
            BorsaItalianaService borsaItalianaService = borsaItalianaServiceFactory.getBorsaItalianaService(titolo.getTipoTitolo());
            
            // Ottieni direttamente il corso (prezzo) del titolo
            BigDecimal prezzoAcquisto = borsaItalianaService.getCorsoByIsin(titolo.getCodiceIsin());
            
            if (prezzoAcquisto != null) {
                log.info("Prezzo attuale ottenuto per il titolo {}: {}", titolo.getCodiceIsin(), prezzoAcquisto);
                return prezzoAcquisto;
            } else {
                log.warn("Prezzo non disponibile per il titolo {}, simulazione saltata", titolo.getCodiceIsin());
                return null;
            }
        } catch (Exception e) {
            log.error("Errore nel recupero del prezzo per il titolo {}: {}", titolo.getCodiceIsin(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Elabora la simulazione per un titolo.
     * 
     * @param titolo il titolo per cui elaborare la simulazione
     * @param prezzoAcquisto il prezzo di acquisto del titolo
     * @return un oggetto contenente la simulazione elaborata e il risultato dettagliato, o null in caso di errore
     */
    private ElaborazioneRisultato elaboraSimulazionePerTitolo(Titolo titolo, BigDecimal prezzoAcquisto) {
        try {
            // Calcola i rendimenti dettagliati per il titolo
            LocalDate oggi = LocalDate.now();
            RisultatoRendimentoAdvancedDTO risultatoDettagliato = simulazioneService.calcolaRendimentoAdvanced(
                titolo,
                prezzoAcquisto,
                RendimentoConstants.IMPORTO_FISSO_SIMULAZIONE,
                oggi
            );
            
            // Verifica se esiste già una simulazione per questo titolo nella stessa giornata
            List<SimulazioneDTO> simulazioniOggi = simulazioneService.findByTitoloIdAndDataAcquisto(titolo.getIdTitolo(), oggi);
            SimulazioneDTO simulazione;
            
            if (!simulazioniOggi.isEmpty()) {
                // Aggiorna la simulazione esistente della giornata corrente
                SimulazioneDTO simulazioneEsistente = simulazioniOggi.get(0);
                log.info("Trovata simulazione esistente per il titolo ID: {}, ISIN: {} nella data odierna, aggiornamento in corso", 
                        titolo.getIdTitolo(), titolo.getCodiceIsin());
                
                // Aggiorna la simulazione esistente utilizzando direttamente il risultato dettagliato già calcolato
                simulazione = simulazioneService.aggiornaSimulazione(simulazioneEsistente, risultatoDettagliato, RendimentoConstants.IMPORTO_FISSO_SIMULAZIONE);
                log.info("Simulazione aggiornata per il titolo ID: {}, ISIN: {}", 
                        titolo.getIdTitolo(), titolo.getCodiceIsin());
            } else {
                // Crea una nuova simulazione
                simulazione = simulazioneService.calcolaESalvaSimulazione(
                    titolo.getIdTitolo(),
                    prezzoAcquisto,
                    RendimentoConstants.IMPORTO_FISSO_SIMULAZIONE,
                    oggi
                );
                log.info("Nuova simulazione creata per il titolo ID: {}, ISIN: {}", 
                        titolo.getIdTitolo(), titolo.getCodiceIsin());
            }
            
            return new ElaborazioneRisultato(simulazione, risultatoDettagliato);
        } catch (Exception e) {
            log.error("Errore nell'elaborazione della simulazione per il titolo ID: {}, ISIN: {}, Errore: {}", 
                    titolo.getIdTitolo(), titolo.getCodiceIsin(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Filtra i titoli in base al periodo di scadenza specificato.
     * 
     * @param titoli la lista di titoli da filtrare
     * @param periodo il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     * @return la lista di titoli filtrati
     */
    private List<Titolo> filtraTitoliPerPeriodo(List<Titolo> titoli, String periodo) {
        if (periodo.equals("tutti")) {
            return titoli;
        }
        
        LocalDate oggi = LocalDate.now();
        LocalDate dataMinima;
        LocalDate dataMassima;
        
        switch (periodo) {
            case "trimestrali":
                // Titoli in scadenza tra 2 e 3 mesi
                dataMinima = oggi.plusMonths(2);
                dataMassima = oggi.plusMonths(3);
                break;
            case "semestrali":
                // Titoli in scadenza tra 5 e 6 mesi
                dataMinima = oggi.plusMonths(5);
                dataMassima = oggi.plusMonths(6);
                break;
            case "annuali":
                // Titoli in scadenza tra 11 e 12 mesi
                dataMinima = oggi.plusMonths(11);
                dataMassima = oggi.plusMonths(12);
                break;
            case "triennali":
                // Titoli in scadenza tra 30 e 36 mesi (2 anni e mezzo - 3 anni)
                dataMinima = oggi.plusMonths(30);
                dataMassima = oggi.plusMonths(36);
                break;
            default:
                return titoli;
        }
        
        return titoli.stream()
                .filter(titolo -> {
                    LocalDate dataScadenza = titolo.getDataScadenza();
                    return dataScadenza != null && 
                           dataScadenza.isAfter(dataMinima.minusDays(1)) && 
                           dataScadenza.isBefore(dataMassima.plusDays(1));
                })
                .collect(Collectors.toList());
    }
}
