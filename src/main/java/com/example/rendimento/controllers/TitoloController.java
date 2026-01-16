package com.example.rendimento.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.service.TitoloService;

/**
 * Controller REST che fornisce API per la gestione dei titoli.
 */
@RestController
@RequestMapping("/api/titolo")
public class TitoloController {
    
    private static final Logger log = LoggerFactory.getLogger(TitoloController.class);

    @Autowired
    private TitoloService titoloService;

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
            TitoloDTO titoloDTO = titoloService.findById(id);
            if (titoloDTO != null) {
                log.info("Risposta per GET /api/titolo/{}: {}", id, titoloDTO);
                return ResponseEntity.ok(titoloDTO);
            } else {
                log.info("Risposta per GET /api/titolo/{}: Titolo non trovato", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Errore nella richiesta GET /api/titolo/{}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
