package com.example.rendimento.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.rendimento.constants.AppMessages;
import com.example.rendimento.dto.AppMetadataDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.enums.PeriodicitaBollo;
import com.example.rendimento.enums.PeriodicitaCedole;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.service.AppMetadataService;
import com.example.rendimento.service.SimulazioneService;
import com.example.rendimento.service.TitoloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST che fornisce API per il frontend dell'applicazione.
 */
@RestController
@RequestMapping("/api/frontend")
public class FrontendApiController {
    
    private static final Logger log = LoggerFactory.getLogger(FrontendApiController.class);

    @Autowired
    private AppMetadataService appMetadataService;
    
    @Autowired
    private TitoloService titoloService;
    
    @Autowired
    private SimulazioneService simulazioneService;

    /**
     * Restituisce le informazioni sull'applicazione.
     * 
     * @return DTO contenente i metadati dell'applicazione
     */
    @GetMapping("/app-info")
    public AppMetadataDTO getAppInfo() {
        log.info("Ricevuta richiesta GET /api/frontend/app-info");
        AppMetadataDTO result = appMetadataService.getAppMetadataByName("Rendimento");
        log.info("Risposta per GET /api/frontend/app-info: {}", result);
        return result;
    }
    
    /**
     * Recupera tutti i titoli.
     * 
     * @return lista di tutti i titoli
     */
    @GetMapping("/titolo")
    public List<TitoloDTO> getAllTitoli() {
        log.info("Ricevuta richiesta GET /api/frontend/titolo");
        List<TitoloDTO> result = titoloService.getAllTitoli();
        log.info("Risposta per GET /api/frontend/titolo: {} titoli trovati", result.size());
        return result;
    }

    /**
     * Crea un nuovo titolo o aggiorna un titolo esistente con lo stesso codice ISIN.
     * 
     * @param titoloDTO il DTO contenente i dati del titolo
     * @return il DTO del titolo salvato o aggiornato
     */
    @PostMapping("/titolo")
    public ResponseEntity<?> createTitolo(@RequestBody TitoloDTO titoloDTO) {
        log.info("Ricevuta richiesta POST /api/frontend/titolo con dati: {}", titoloDTO);
        // Verifica se esiste già un titolo con lo stesso codice ISIN
        if (titoloService.existsByCodiceIsin(titoloDTO.getCodiceIsin())) {
            // Recupera il titolo esistente
            TitoloDTO esistente = titoloService.findByCodiceIsin(titoloDTO.getCodiceIsin());
            
            // Imposta l'ID del titolo esistente nel DTO in arrivo per forzare l'aggiornamento
            titoloDTO.setIdTitolo(esistente.getIdTitolo());
            
            // Salva il titolo aggiornato
            TitoloDTO updatedTitolo = titoloService.saveTitolo(titoloDTO);
            
            // Restituisci una risposta di successo con il titolo aggiornato e un messaggio
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("titolo", updatedTitolo);
            response.put("message", AppMessages.TITOLO_AGGIORNATO);
            
            log.info("Risposta per POST /api/frontend/titolo (aggiornamento): {}", response);
            return ResponseEntity.ok(response);
        }
        
        // Se non esiste, crea un nuovo titolo
        TitoloDTO savedTitolo = titoloService.saveTitolo(titoloDTO);
        log.info("Risposta per POST /api/frontend/titolo (creazione): {}", savedTitolo);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(savedTitolo);
    }

    /**
     * Trova un titolo per codice ISIN.
     * 
     * @param codiceIsin il codice ISIN del titolo da cercare
     * @return il titolo trovato o 404 se non esiste
     */
    @GetMapping("/titolo/isin/{codiceIsin}")
    public ResponseEntity<TitoloDTO> getTitoloByCodiceIsin(@PathVariable String codiceIsin) {
        log.info("Ricevuta richiesta GET /api/frontend/titolo/isin/{} con codiceIsin: {}", "codiceIsin", codiceIsin);
        TitoloDTO titoloDTO = titoloService.findByCodiceIsin(codiceIsin);
        if (titoloDTO != null) {
            log.info("Risposta per GET /api/frontend/titolo/isin/{}: {}", codiceIsin, titoloDTO);
            return ResponseEntity.ok(titoloDTO);
        } else {
            log.info("Risposta per GET /api/frontend/titolo/isin/{}: Titolo non trovato", codiceIsin);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un titolo per ID.
     * 
     * @param id l'ID del titolo da eliminare
     * @return 204 No Content se l'eliminazione è avvenuta con successo
     */
    @DeleteMapping("/titolo/{id}")
    public ResponseEntity<Void> deleteTitolo(@PathVariable Integer id) {
        log.info("Ricevuta richiesta DELETE /api/frontend/titolo/{} con id: {}", "id", id);
        titoloService.deleteTitolo(id);
        log.info("Risposta per DELETE /api/frontend/titolo/{}: Titolo eliminato con successo", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Importa un titolo da Borsa Italiana dato il codice ISIN e il tipo.
     * 
     * @param codiceIsin il codice ISIN del titolo
     * @param tipoTitolo il tipo del titolo (BTP, BOT, ecc.)
     * @return il titolo importato e salvato o un messaggio di errore
     */
    @PostMapping("/titolo/importa")
    public ResponseEntity<?> importaTitoloDaBorsaItaliana(
            @RequestParam String codiceIsin, 
            @RequestParam String tipoTitolo) {
        
        log.info("Ricevuta richiesta POST /api/frontend/titolo/importa con codiceIsin: {}, tipoTitolo: {}", 
                codiceIsin, tipoTitolo);
        
        try {
            // Verifica se esiste già un titolo con lo stesso codice ISIN
            if (titoloService.existsByCodiceIsin(codiceIsin)) {
                // Importa il titolo da Borsa Italiana (questo aggiornerà i dati)
                TitoloDTO updatedTitolo = titoloService.importaTitoloDaBorsaItaliana(codiceIsin, tipoTitolo);
                
                // Restituisci una risposta di successo con il titolo aggiornato e un messaggio
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("titolo", updatedTitolo);
                response.put("message", AppMessages.TITOLO_AGGIORNATO);
                
                log.info("Risposta per POST /api/frontend/titolo/importa (aggiornamento): {}", response);
                return ResponseEntity.ok(response);
            }
            
            // Se non esiste, importa e crea un nuovo titolo
            TitoloDTO savedTitolo = titoloService.importaTitoloDaBorsaItaliana(codiceIsin, tipoTitolo);
            log.info("Risposta per POST /api/frontend/titolo/importa (creazione): {}", savedTitolo);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTitolo);
            
        } catch (IllegalArgumentException e) {
            log.error("Errore nella richiesta POST /api/frontend/titolo/importa: {}", e.getMessage());
            Map<String, String> errorResponse = Collections.singletonMap("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Errore interno nella richiesta POST /api/frontend/titolo/importa: {}", e.getMessage());
            Map<String, String> errorResponse = Collections.singletonMap("error", "Errore durante l'importazione del titolo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Restituisce i valori dell'enum PeriodicitaCedole.
     * 
     * @return mappa con i valori dell'enum
     */
    @GetMapping("/enum/periodicita-cedole")
    public Map<String, String> getPeriodicitaCedoleValues() {
        log.info("Ricevuta richiesta GET /api/frontend/enum/periodicita-cedole");
        Map<String, String> values = new LinkedHashMap<>();
        for (PeriodicitaCedole periodicita : PeriodicitaCedole.values()) {
            values.put(periodicita.name(), periodicita.getDescrizione());
        }
        log.info("Risposta per GET /api/frontend/enum/periodicita-cedole: {}", values);
        return values;
    }

    /**
     * Restituisce i valori dell'enum PeriodicitaBollo.
     * 
     * @return mappa con i valori dell'enum
     */
    @GetMapping("/enum/periodicita-bollo")
    public Map<String, String> getPeriodicitaBolloValues() {
        log.info("Ricevuta richiesta GET /api/frontend/enum/periodicita-bollo");
        Map<String, String> values = new LinkedHashMap<>();
        for (PeriodicitaBollo periodicita : PeriodicitaBollo.values()) {
            values.put(periodicita.name(), periodicita.getDescrizione());
        }
        log.info("Risposta per GET /api/frontend/enum/periodicita-bollo: {}", values);
        return values;
    }
    
    /**
     * Restituisce i valori dell'enum TipoTitolo.
     * 
     * @return mappa con i valori dell'enum
     */
    @GetMapping("/enum/tipo-titolo")
    public Map<String, String> getTipoTitoloValues() {
        log.info("Ricevuta richiesta GET /api/frontend/enum/tipo-titolo");
        Map<String, String> values = new LinkedHashMap<>();
        for (TipoTitolo tipo : TipoTitolo.values()) {
            values.put(tipo.name(), tipo.getDescrizione());
        }
        log.info("Risposta per GET /api/frontend/enum/tipo-titolo: {}", values);
        return values;
    }
    
    /**
     * Recupera l'ultima simulazione per ogni titolo.
     * 
     * @return lista delle ultime simulazioni per ogni titolo
     */
    @GetMapping("/simulazioni/latest")
    public List<SimulazioneDTO> getLatestSimulazioniPerTitolo() {
        log.info("Ricevuta richiesta GET /api/frontend/simulazioni/latest");
        List<SimulazioneDTO> result = simulazioneService.getLatestSimulazioneForEachTitolo();
        log.info("Risposta per GET /api/frontend/simulazioni/latest: {} simulazioni trovate", result.size());
        return result;
    }
    
}
