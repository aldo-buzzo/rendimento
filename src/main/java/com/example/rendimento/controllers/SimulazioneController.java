package com.example.rendimento.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.service.BorsaItalianaService;
import com.example.rendimento.service.SimulazioneService;
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

    /**
     * Costruttore con parametri per l'iniezione delle dipendenze.
     *
     * @param simulazioneService servizio per la gestione delle simulazioni
     */
    @Autowired
    public SimulazioneController(SimulazioneService simulazioneService, TitoloRepository titoloRepository, 
                                BorsaItalianaServiceFactory borsaItalianaServiceFactory, UtenteService utenteService) {
        this.simulazioneService = simulazioneService;
        this.titoloRepository = titoloRepository;
        this.borsaItalianaServiceFactory = borsaItalianaServiceFactory;
        this.utenteService = utenteService;
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
                // Ottieni il prezzo attuale del titolo tramite BorsaItalianaService
                BigDecimal prezzoAcquisto = null;
                
                try {
                    // Ottieni il servizio appropriato in base al tipo di titolo
                    BorsaItalianaService borsaItalianaService = borsaItalianaServiceFactory.getBorsaItalianaService(titolo.getTipoTitolo());
                    
                    // Ottieni direttamente il corso (prezzo) del titolo
                    prezzoAcquisto = borsaItalianaService.getCorsoByIsin(titolo.getCodiceIsin());
                    
                    if (prezzoAcquisto != null) {
                        log.info("Prezzo attuale ottenuto per il titolo {}: {}", titolo.getCodiceIsin(), prezzoAcquisto);
                    } else {
                        log.warn("Prezzo non disponibile per il titolo {}, simulazione saltata", titolo.getCodiceIsin());
                        continue; // Salta questo titolo e passa al prossimo
                    }
                } catch (Exception e) {
                    log.error("Errore nel recupero del prezzo per il titolo {}: {}", titolo.getCodiceIsin(), e.getMessage());
                    continue; // Salta questo titolo e passa al prossimo
                }
                
                // Verifica se esiste già una simulazione per questo titolo nella stessa giornata
                LocalDate oggi = LocalDate.now();
                List<SimulazioneDTO> simulazioniOggi = simulazioneService.findByTitoloIdAndDataAcquisto(titolo.getIdTitolo(), oggi);
                SimulazioneDTO simulazione;
                
                if (!simulazioniOggi.isEmpty()) {
                    // Aggiorna la simulazione esistente della giornata corrente
                    SimulazioneDTO simulazioneEsistente = simulazioniOggi.get(0);
                    log.info("Trovata simulazione esistente per il titolo ID: {}, ISIN: {} nella data odierna, aggiornamento in corso", 
                            titolo.getIdTitolo(), titolo.getCodiceIsin());
                    
                    // Calcola i nuovi valori
                    RisultatoSimulazioneDTO risultato = simulazioneService.calcolaRendimento(
                        titolo.getIdTitolo(),
                        prezzoAcquisto,
                        RendimentoConstants.IMPORTO_FISSO_SIMULAZIONE
                    );
                    
                    // Aggiorna la simulazione esistente utilizzando il nuovo metodo
                    // che garantisce che tutti i campi siano aggiornati correttamente
                    simulazione = simulazioneService.aggiornaSimulazione(simulazioneEsistente, risultato, RendimentoConstants.IMPORTO_FISSO_SIMULAZIONE);
                    log.info("Simulazione aggiornata per il titolo ID: {}, ISIN: {}", 
                            titolo.getIdTitolo(), titolo.getCodiceIsin());
                } else {
                    // Crea una nuova simulazione
                    simulazione = simulazioneService.calcolaESalvaSimulazione(
                        titolo.getIdTitolo(),
                        prezzoAcquisto,
                        RendimentoConstants.IMPORTO_FISSO_SIMULAZIONE,
                        LocalDate.now()
                    );
                    log.info("Nuova simulazione creata per il titolo ID: {}, ISIN: {}", 
                            titolo.getIdTitolo(), titolo.getCodiceIsin());
                }
                
                simulazioniSalvate.add(simulazione);
            } catch (Exception e) {
                log.error("Errore nel calcolo della simulazione per il titolo ID: {}, ISIN: {}, Errore: {}", 
                        titolo.getIdTitolo(), titolo.getCodiceIsin(), e.getMessage());
            }
        }
        
        log.info("Risposta per POST /api/simulazioni/calcola-rendimenti-tutti-titoli: {} simulazioni aggiornate/create", 
                simulazioniSalvate.size());
        return ResponseEntity.ok(simulazioniSalvate);
    }
}
