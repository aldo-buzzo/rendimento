package com.example.rendimento.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.enums.ModalitaCalcoloBollo;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.service.SimulazioneService;
import com.example.rendimento.service.factory.BorsaItalianaServiceFactory;
import com.example.rendimento.service.BorsaItalianaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Costruttore con parametri per l'iniezione delle dipendenze.
     *
     * @param simulazioneService servizio per la gestione delle simulazioni
     */
    @Autowired
    public SimulazioneController(SimulazioneService simulazioneService, TitoloRepository titoloRepository, 
                                BorsaItalianaServiceFactory borsaItalianaServiceFactory) {
        this.simulazioneService = simulazioneService;
        this.titoloRepository = titoloRepository;
        this.borsaItalianaServiceFactory = borsaItalianaServiceFactory;
    }

    /**
     * Endpoint per il calcolo del rendimento di un titolo.
     *
     * @param idTitolo ID del titolo
     * @param prezzoAcquisto prezzo di acquisto inserito dall'utente
     * @param importo importo dell'investimento
     * @param modalitaBollo modalità di calcolo del bollo (ANNUALE o MENSILE)
     * @return DTO contenente tutti i risultati del calcolo
     */
    @PostMapping("/calcola-rendimento")
    public ResponseEntity<RisultatoSimulazioneDTO> calcolaRendimento(
            @RequestParam Integer idTitolo,
            @RequestParam BigDecimal prezzoAcquisto,
            @RequestParam BigDecimal importo,
            @RequestParam ModalitaCalcoloBollo modalitaBollo) {
        
        log.info("Ricevuta richiesta POST /api/simulazioni/calcola-rendimento con idTitolo: {}, prezzoAcquisto: {}, importo: {}, modalitaBollo: {}", 
                idTitolo, prezzoAcquisto, importo, modalitaBollo);
        
        RisultatoSimulazioneDTO risultato = simulazioneService.calcolaRendimento(
            idTitolo, prezzoAcquisto, importo, modalitaBollo);
        
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
     * @param modalitaBollo modalità di calcolo del bollo (ANNUALE o MENSILE)
     * @param commissioniAcquisto commissioni di acquisto (in percentuale, es. 0.09 per 0.09%)
     * @return il DTO della simulazione salvata con ID aggiornato
     */
    @PostMapping("/calcola-e-salva")
    public ResponseEntity<SimulazioneDTO> calcolaESalvaSimulazione(
            @RequestParam Integer idTitolo,
            @RequestParam BigDecimal prezzoAcquisto,
            @RequestParam BigDecimal importo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAcquisto,
            @RequestParam ModalitaCalcoloBollo modalitaBollo,
            @RequestParam BigDecimal commissioniAcquisto) {
        
        log.info("Ricevuta richiesta POST /api/simulazioni/calcola-e-salva con idTitolo: {}, prezzoAcquisto: {}, importo: {}, dataAcquisto: {}, modalitaBollo: {}, commissioniAcquisto: {}", 
                idTitolo, prezzoAcquisto, importo, dataAcquisto, modalitaBollo, commissioniAcquisto);
        
        // Converti le commissioni da percentuale a decimale (es. da 0.09% a 0.0009)
        BigDecimal commissioniDecimali = commissioniAcquisto.divide(new BigDecimal("100"), 8, BigDecimal.ROUND_HALF_UP);
        
        SimulazioneDTO savedSimulazione = simulazioneService.calcolaESalvaSimulazione(
            idTitolo, prezzoAcquisto, importo, dataAcquisto, modalitaBollo, commissioniDecimali);
        
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
        
        List<SimulazioneDTO> simulazioni;
        if (latest) {
            simulazioni = simulazioneService.getLatestSimulazioneForEachTitolo();
            log.info("Risposta per GET /api/simulazioni con latest=true: {} simulazioni più recenti trovate", simulazioni.size());
        } else {
            simulazioni = simulazioneService.getAllSimulazioni();
            log.info("Risposta per GET /api/simulazioni con latest=false: {} simulazioni trovate", simulazioni.size());
        }
        
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
        SimulazioneDTO simulazione = simulazioneService.findById(id);
        log.info("Risposta per GET /api/simulazioni/{}: {}", id, simulazione);
        return ResponseEntity.ok(simulazione);
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
        
        // Ottieni la simulazione più recente per il titolo specificato
        SimulazioneDTO simulazione = simulazioneService.getLatestSimulazioneByTitoloId(idTitolo);
        
        // Ottieni i dettagli del titolo
        Titolo titolo = titoloRepository.findById(idTitolo)
            .orElseThrow(() -> new EntityNotFoundException("Titolo non trovato con ID: " + idTitolo));
        
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
        
        // Recupera tutti i titoli con data di scadenza futura
        List<Titolo> titoliValidi = titoloRepository.findByDataScadenzaAfter(LocalDate.now());
        log.info("Trovati {} titoli con scadenza futura", titoliValidi.size());
        
        List<SimulazioneDTO> simulazioniSalvate = new ArrayList<>();
        BigDecimal importoFisso = new BigDecimal("10000"); // Importo fisso di 10.000 euro
        BigDecimal commissioniDefault = new BigDecimal("0.0009"); // 0,09%
        
        // Per ogni titolo, calcola e salva una simulazione
        for (Titolo titolo : titoliValidi) {
            try {
                // Ottieni il prezzo attuale del titolo tramite BorsaItalianaService
                BigDecimal prezzoAcquisto = new BigDecimal("100"); // Valore di default
                
                try {
                    // Ottieni il servizio appropriato in base al tipo di titolo
                    BorsaItalianaService borsaItalianaService = borsaItalianaServiceFactory.getBorsaItalianaService(titolo.getTipoTitolo());
                    
                    // Ottieni il titolo con il prezzo attuale
                    TitoloDTO titoloDTO = borsaItalianaService.getTitoloByIsin(titolo.getCodiceIsin());
                    
                    // Se il prezzo è disponibile, usalo
                    if (titoloDTO != null && titoloDTO.getCorso() != null) {
                        prezzoAcquisto = titoloDTO.getCorso();
                        log.info("Prezzo attuale ottenuto per il titolo {}: {}", titolo.getCodiceIsin(), prezzoAcquisto);
                    } else {
                        log.warn("Prezzo non disponibile per il titolo {}, uso valore di default", titolo.getCodiceIsin());
                    }
                } catch (Exception e) {
                    log.error("Errore nel recupero del prezzo per il titolo {}: {}", titolo.getCodiceIsin(), e.getMessage());
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
                        importoFisso,
                        ModalitaCalcoloBollo.ANNUALE
                    );
                    
                    // Aggiorna i campi della simulazione esistente
                    simulazioneEsistente.setPrezzoAcquisto(prezzoAcquisto);
                    simulazioneEsistente.setDataAcquisto(LocalDate.now());
                    simulazioneEsistente.setCommissioniAcquisto(commissioniDefault);
                    simulazioneEsistente.setRendimentoLordo(risultato.getTasso().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    simulazioneEsistente.setRendimentoTassato(risultato.getTasso().multiply(new BigDecimal("0.875"))
                                                  .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    simulazioneEsistente.setRendimentoNettoCedole(risultato.getTassoNettoCommissioni()
                                                      .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    simulazioneEsistente.setImpostaBollo(risultato.getImpostaBollo());
                    simulazioneEsistente.setRendimentoNettoBollo(risultato.getTassoNettoBollo());
                    simulazioneEsistente.setPlusMinusValenza(risultato.getPlusvalenzaNetta());
                    
                    // Salva la simulazione aggiornata
                    simulazione = simulazioneService.salvaSimulazione(simulazioneEsistente);
                    log.info("Simulazione aggiornata per il titolo ID: {}, ISIN: {}", 
                            titolo.getIdTitolo(), titolo.getCodiceIsin());
                } else {
                    // Crea una nuova simulazione
                    simulazione = simulazioneService.calcolaESalvaSimulazione(
                        titolo.getIdTitolo(),
                        prezzoAcquisto,
                        importoFisso,
                        LocalDate.now(),
                        ModalitaCalcoloBollo.ANNUALE,
                        commissioniDefault
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
