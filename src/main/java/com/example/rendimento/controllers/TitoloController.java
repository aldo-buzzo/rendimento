package com.example.rendimento.controllers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rendimento.constants.AppMessages;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.service.TitoloService;
import com.example.rendimento.service.UtenteService;

/**
 * Controller REST che fornisce API per la gestione dei titoli.
 */
@RestController
@RequestMapping("/api/titolo")
public class TitoloController {
    
    private static final Logger log = LoggerFactory.getLogger(TitoloController.class);

    @Autowired
    private TitoloService titoloService;
    
    @Autowired
    private UtenteService utenteService;

    /**
     * Trova un titolo per ID.
     * 
     * @param id l'ID del titolo da cercare
     * @return il titolo trovato o 404 se non esiste
     */
    @GetMapping("/{id}")
    public ResponseEntity<TitoloDTO> getTitoloById(@PathVariable Integer id) {
        log.info("Ricevuta richiesta GET /api/titolo/{} con id: {}", "id", id);
        try {
            // Ottieni l'utente corrente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Ottieni l'ID dell'utente corrente
            Integer utenteId = utenteService.findByUsername(username)
                    .map(UtenteResponseDTO::getIdUtente)
                    .orElse(null);
            
            TitoloDTO titoloDTO = titoloService.findById(id);
            
            // Verifica che il titolo esista e appartenga all'utente corrente
            if (titoloDTO != null && (titoloDTO.getUtenteId() == null || 
                    titoloDTO.getUtenteId().equals(utenteId))) {
                log.info("Risposta per GET /api/titolo/{}: {}", id, titoloDTO);
                return ResponseEntity.ok(titoloDTO);
            } else {
                log.info("Risposta per GET /api/titolo/{}: Titolo non trovato o non autorizzato", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Errore nella richiesta GET /api/titolo/{}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Crea un nuovo titolo o aggiorna un titolo esistente con lo stesso codice ISIN.
     * 
     * @param titoloDTO il DTO contenente i dati del titolo
     * @return il DTO del titolo salvato o aggiornato
     */
    @PostMapping
    public ResponseEntity<?> createTitolo(@RequestBody TitoloDTO titoloDTO) {
        log.info("Ricevuta richiesta POST /api/titolo con dati: {}", titoloDTO);
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElseThrow(() -> new IllegalStateException("Utente non autenticato"));
        
        // Imposta l'ID dell'utente nel DTO
        titoloDTO.setUtenteId(utenteId);
        
        // Verifica se esiste già un titolo con lo stesso codice ISIN
        if (titoloService.existsByCodiceIsin(titoloDTO.getCodiceIsin())) {
            // Recupera il titolo esistente
            TitoloDTO esistente = titoloService.findByCodiceIsin(titoloDTO.getCodiceIsin());
            
            // Verifica che il titolo appartenga all'utente corrente
            if (!utenteId.equals(esistente.getUtenteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Non sei autorizzato a modificare questo titolo"));
            }
            
            // Imposta l'ID del titolo esistente nel DTO in arrivo per forzare l'aggiornamento
            titoloDTO.setIdTitolo(esistente.getIdTitolo());
            
            // Salva il titolo aggiornato
            TitoloDTO updatedTitolo = titoloService.saveTitolo(titoloDTO);
            
            // Restituisci una risposta di successo con il titolo aggiornato e un messaggio
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("titolo", updatedTitolo);
            response.put("message", AppMessages.TITOLO_AGGIORNATO);
            
            log.info("Risposta per POST /api/titolo (aggiornamento): {}", response);
            return ResponseEntity.ok(response);
        }
        
        // Se non esiste, crea un nuovo titolo
        TitoloDTO savedTitolo = titoloService.saveTitolo(titoloDTO);
        log.info("Risposta per POST /api/titolo (creazione): {}", savedTitolo);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(savedTitolo);
    }
}
