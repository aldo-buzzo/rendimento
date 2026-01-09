package com.example.rendimento.controllers;

import com.example.rendimento.constants.AppMessages;
import com.example.rendimento.dto.AppMetadataDTO;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.enums.PeriodicitaBollo;
import com.example.rendimento.enums.PeriodicitaCedole;
import com.example.rendimento.service.AppMetadataService;
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

    @Autowired
    private AppMetadataService appMetadataService;
    
    @Autowired
    private TitoloService titoloService;

    /**
     * Restituisce le informazioni sull'applicazione.
     * 
     * @return DTO contenente i metadati dell'applicazione
     */
    @GetMapping("/app-info")
    public AppMetadataDTO getAppInfo() {
        return appMetadataService.getAppMetadataByName("Rendimento");
    }
    
    /**
     * Recupera tutti i titoli.
     * 
     * @return lista di tutti i titoli
     */
    @GetMapping("/titolo")
    public List<TitoloDTO> getAllTitoli() {
        return titoloService.getAllTitoli();
    }

    /**
     * Crea un nuovo titolo.
     * 
     * @param titoloDTO il DTO contenente i dati del titolo
     * @return il DTO del titolo salvato
     */
    @PostMapping("/titolo")
    public ResponseEntity<?> createTitolo(@RequestBody TitoloDTO titoloDTO) {
        // Verifica se esiste già un titolo con lo stesso codice ISIN
        if (titoloService.existsByCodiceIsin(titoloDTO.getCodiceIsin())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap("message", AppMessages.TITOLO_GIA_PRESENTE));
        }
        
        TitoloDTO savedTitolo = titoloService.saveTitolo(titoloDTO);
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
        TitoloDTO titoloDTO = titoloService.findByCodiceIsin(codiceIsin);
        return titoloDTO != null ? ResponseEntity.ok(titoloDTO) : ResponseEntity.notFound().build();
    }

    /**
     * Elimina un titolo per ID.
     * 
     * @param id l'ID del titolo da eliminare
     * @return 204 No Content se l'eliminazione è avvenuta con successo
     */
    @DeleteMapping("/titolo/{id}")
    public ResponseEntity<Void> deleteTitolo(@PathVariable Integer id) {
        titoloService.deleteTitolo(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Restituisce i valori dell'enum PeriodicitaCedole.
     * 
     * @return mappa con i valori dell'enum
     */
    @GetMapping("/enum/periodicita-cedole")
    public Map<String, String> getPeriodicitaCedoleValues() {
        Map<String, String> values = new LinkedHashMap<>();
        for (PeriodicitaCedole periodicita : PeriodicitaCedole.values()) {
            values.put(periodicita.name(), periodicita.getDescrizione());
        }
        return values;
    }

    /**
     * Restituisce i valori dell'enum PeriodicitaBollo.
     * 
     * @return mappa con i valori dell'enum
     */
    @GetMapping("/enum/periodicita-bollo")
    public Map<String, String> getPeriodicitaBolloValues() {
        Map<String, String> values = new LinkedHashMap<>();
        for (PeriodicitaBollo periodicita : PeriodicitaBollo.values()) {
            values.put(periodicita.name(), periodicita.getDescrizione());
        }
        return values;
    }
}
