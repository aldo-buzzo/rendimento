package com.example.rendimento.controllers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.rendimento.constants.AppMessages;
import com.example.rendimento.dto.AppMetadataDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.enums.PeriodicitaBollo;
import com.example.rendimento.enums.PeriodicitaCedole;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.service.AppMetadataService;
import com.example.rendimento.service.SimulazioneService;
import com.example.rendimento.service.TitoloService;
import com.example.rendimento.service.UtenteService;

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
    
    @Autowired
    private UtenteService utenteService;

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
    
    // I metodi relativi ai titoli sono stati spostati nel TitoloController
    
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
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElseThrow(() -> new IllegalStateException("Utente non autenticato"));
        
        // Usa il metodo ottimizzato che filtra direttamente nel database
        List<SimulazioneDTO> result = simulazioneService.getSimulazioniByUtenteId(utenteId, true);
        log.info("Risposta per GET /api/frontend/simulazioni/latest: {} simulazioni trovate per l'utente ID: {}", result.size(), utenteId);
        return result;
    }
    
}
