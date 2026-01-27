package com.example.rendimento.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.rendimento.constants.AppMessages;
import com.example.rendimento.dto.TitoloImportDTO;
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
     * Recupera tutti i titoli dell'utente corrente.
     * 
     * @return lista dei titoli dell'utente corrente
     */
    @GetMapping
    public List<TitoloDTO> getAllTitoli() {
        log.info("Ricevuta richiesta GET /api/titolo");
        
        // Inizia il conteggio del tempo
        long startTime = System.currentTimeMillis();
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElseThrow(() -> new IllegalStateException("Utente non autenticato"));
        
        // Usa il metodo che filtra per utente
        List<TitoloDTO> result = titoloService.getTitoliByUtenteId(utenteId);
        
        // Calcola il tempo di esecuzione in secondi
        long endTime = System.currentTimeMillis();
        double executionTimeInSeconds = (endTime - startTime) / 1000.0;
        
        log.info("Risposta per GET /api/titolo: {} titoli trovati per l'utente ID: {} - Tempo di esecuzione: {} secondi", 
                result.size(), utenteId, executionTimeInSeconds);
        return result;
    }

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
     * Trova un titolo per codice ISIN.
     * 
     * @param codiceIsin il codice ISIN del titolo da cercare
     * @return il titolo trovato o 404 se non esiste
     */
    @GetMapping("/isin/{codiceIsin}")
    public ResponseEntity<TitoloDTO> getTitoloByCodiceIsin(@PathVariable String codiceIsin) {
        log.info("Ricevuta richiesta GET /api/titolo/isin/{} con codiceIsin: {}", "codiceIsin", codiceIsin);
        TitoloDTO titoloDTO = titoloService.findByCodiceIsin(codiceIsin);
        if (titoloDTO != null) {
            log.info("Risposta per GET /api/titolo/isin/{}: {}", codiceIsin, titoloDTO);
            return ResponseEntity.ok(titoloDTO);
        } else {
            log.info("Risposta per GET /api/titolo/isin/{}: Titolo non trovato", codiceIsin);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Recupera i titoli con scadenza entro un determinato periodo.
     * 
     * @param periodo il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     * @return lista dei titoli con scadenza entro il periodo specificato
     */
    @GetMapping("/scadenza/{periodo}")
    public ResponseEntity<List<TitoloDTO>> getTitoliByScadenza(@PathVariable String periodo) {
        log.info("Ricevuta richiesta GET /api/titolo/scadenza/{} con periodo: {}", "periodo", periodo);
        
        // Ottieni l'utente corrente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElseThrow(() -> new IllegalStateException("Utente non autenticato"));
        
        // Calcola la data di scadenza in base al periodo
        LocalDate dataScadenza = LocalDate.now();
        
        switch (periodo.toLowerCase()) {
            case "trimestrali":
                dataScadenza = dataScadenza.plusMonths(3);
                break;
            case "semestrali":
                dataScadenza = dataScadenza.plusMonths(6);
                break;
            case "annuali":
                dataScadenza = dataScadenza.plusYears(1);
                break;
            case "triennali":
                dataScadenza = dataScadenza.plusYears(3);
                break;
            case "tutti":
                // Non applicare filtro per data di scadenza
                dataScadenza = null;
                break;
            default:
                return ResponseEntity.badRequest().build();
        }
        
        List<TitoloDTO> result;
        if (dataScadenza != null) {
            // Usa il metodo che filtra per utente e data di scadenza
            result = titoloService.getTitoliByUtenteIdAndDataScadenzaBefore(utenteId, dataScadenza);
        } else {
            // Se il periodo è "tutti", recupera tutti i titoli dell'utente
            result = titoloService.getTitoliByUtenteId(utenteId);
        }
        
        log.info("Risposta per GET /api/titolo/scadenza/{}: {} titoli trovati", periodo, result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * Elimina un titolo per ID.
     * 
     * @param id l'ID del titolo da eliminare
     * @return 200 OK con un messaggio di successo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTitolo(@PathVariable Integer id) {
        log.info("Ricevuta richiesta DELETE /api/titolo/{} con id: {}", "id", id);
        titoloService.deleteTitolo(id);
        log.info("Risposta per DELETE /api/titolo/{}: Titolo eliminato con successo", id);
        
        // Restituisci un codice 200 OK con un messaggio di successo
        Map<String, String> response = Collections.singletonMap("message", "Titolo eliminato con successo");
        return ResponseEntity.ok(response);
    }

    /**
     * Importa un titolo da Borsa Italiana dato il codice ISIN e il tipo.
     * 
     * @param codiceIsin il codice ISIN del titolo
     * @param tipoTitolo il tipo del titolo (BTP, BOT, ecc.)
     * @return il titolo importato e salvato
     */
    @PostMapping("/importa")
    public ResponseEntity<?> importaTitoloDaBorsaItaliana(
            @RequestParam String codiceIsin, 
            @RequestParam String tipoTitolo) {
            
            log.info("Ricevuta richiesta POST /api/titolo/importa con codiceIsin: {}, tipoTitolo: {}", 
                    codiceIsin, tipoTitolo);
            
            try {
                // Ottieni l'utente corrente
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                
                // Ottieni l'ID dell'utente corrente
                Integer utenteId = utenteService.findByUsername(username)
                        .map(UtenteResponseDTO::getIdUtente)
                        .orElseThrow(() -> new IllegalStateException("Utente non autenticato"));
                
                // Verifica se esiste già un titolo con lo stesso codice ISIN
                if (titoloService.existsByCodiceIsin(codiceIsin)) {
                    // Recupera il titolo esistente
                    TitoloDTO esistente = titoloService.findByCodiceIsin(codiceIsin);
                    
                    // Verifica che il titolo appartenga all'utente corrente
                    if (!utenteId.equals(esistente.getUtenteId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Collections.singletonMap("error", "Non sei autorizzato a modificare questo titolo"));
                    }
                    
                    // Importa il titolo da Borsa Italiana (questo aggiornerà i dati)
                    TitoloDTO updatedTitolo = titoloService.importaTitoloDaBorsaItaliana(codiceIsin, tipoTitolo);
                    
                    // Assicurati che il titolo sia associato all'utente corrente
                    updatedTitolo.setUtenteId(utenteId);
                    updatedTitolo = titoloService.saveTitolo(updatedTitolo);
                    
                    // Restituisci una risposta di successo con il titolo aggiornato e un messaggio
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("titolo", updatedTitolo);
                    response.put("message", AppMessages.TITOLO_AGGIORNATO);
                    
                    log.info("Risposta per POST /api/titolo/importa (aggiornamento): {}", response);
                    return ResponseEntity.ok(response);
                }
                
                // Se non esiste, importa e crea un nuovo titolo
                TitoloDTO savedTitolo = titoloService.importaTitoloDaBorsaItaliana(codiceIsin, tipoTitolo);
                
                // Assicurati che il titolo sia associato all'utente corrente
                savedTitolo.setUtenteId(utenteId);
                savedTitolo = titoloService.saveTitolo(savedTitolo);
                
                log.info("Risposta per POST /api/titolo/importa (creazione): {}", savedTitolo);
                return ResponseEntity.status(HttpStatus.CREATED).body(savedTitolo);
                
            } catch (IllegalArgumentException e) {
                log.error("Errore nella richiesta POST /api/titolo/importa: {}", e.getMessage());
                Map<String, String> errorResponse = Collections.singletonMap("error", e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            } catch (Exception e) {
                log.error("Errore interno nella richiesta POST /api/titolo/importa: {}", e.getMessage());
                Map<String, String> errorResponse = Collections.singletonMap("error", "Errore durante l'importazione del titolo");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
        
        /**
         * Importa più titoli contemporaneamente da Borsa Italiana.
         * 
         * @param titoliImport lista di DTO contenenti codice ISIN e tipo titolo
         * @return lista dei titoli importati e salvati
         */
        @PostMapping("/importa-multipli")
        public ResponseEntity<?> importaTitoliMultipli(@RequestBody List<TitoloImportDTO> titoliImport) {
            log.info("Ricevuta richiesta POST /api/titolo/importa-multipli con {} titoli", titoliImport.size());
            
            try {
                // Ottieni l'utente corrente
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                
                // Ottieni l'ID dell'utente corrente
                Integer utenteId = utenteService.findByUsername(username)
                        .map(UtenteResponseDTO::getIdUtente)
                        .orElseThrow(() -> new IllegalStateException("Utente non autenticato"));
                
                List<TitoloDTO> titoliSalvati = new ArrayList<>();
                List<String> errori = new ArrayList<>();
                
                // Importa ogni titolo nella lista
                for (TitoloImportDTO titoloImport : titoliImport) {
                    try {
                        String codiceIsin = titoloImport.getCodiceIsin();
                        String tipoTitolo = titoloImport.getTipoTitolo();
                        
                        log.info("Importazione titolo con ISIN: {}, tipo: {}", codiceIsin, tipoTitolo);
                        
                        // Verifica se esiste già un titolo con lo stesso codice ISIN
                        if (titoloService.existsByCodiceIsin(codiceIsin)) {
                            // Recupera il titolo esistente
                            TitoloDTO esistente = titoloService.findByCodiceIsin(codiceIsin);
                            
                            // Verifica che il titolo appartenga all'utente corrente
                            if (!utenteId.equals(esistente.getUtenteId())) {
                                errori.add("Non sei autorizzato a modificare il titolo con ISIN: " + codiceIsin);
                                continue;
                            }
                            
                            // Importa il titolo da Borsa Italiana (questo aggiornerà i dati)
                            TitoloDTO updatedTitolo = titoloService.importaTitoloDaBorsaItaliana(codiceIsin, tipoTitolo);
                            
                            // Assicurati che il titolo sia associato all'utente corrente
                            updatedTitolo.setUtenteId(utenteId);
                            updatedTitolo = titoloService.saveTitolo(updatedTitolo);
                            
                            titoliSalvati.add(updatedTitolo);
                        } else {
                            // Se non esiste, importa e crea un nuovo titolo
                            TitoloDTO savedTitolo = titoloService.importaTitoloDaBorsaItaliana(codiceIsin, tipoTitolo);
                            
                            // Assicurati che il titolo sia associato all'utente corrente
                            savedTitolo.setUtenteId(utenteId);
                            savedTitolo = titoloService.saveTitolo(savedTitolo);
                            
                            titoliSalvati.add(savedTitolo);
                        }
                    } catch (Exception e) {
                        log.error("Errore nell'importazione del titolo con ISIN: {}: {}", 
                                titoloImport.getCodiceIsin(), e.getMessage());
                        errori.add("Errore nell'importazione del titolo con ISIN: " + 
                                titoloImport.getCodiceIsin() + ": " + e.getMessage());
                    }
                }
                
                // Prepara la risposta
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("titoli", titoliSalvati);
                response.put("totale", titoliSalvati.size());
                
                if (!errori.isEmpty()) {
                    response.put("errori", errori);
                }
                
                log.info("Risposta per POST /api/titolo/importa-multipli: {} titoli importati, {} errori", 
                        titoliSalvati.size(), errori.size());
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                log.error("Errore interno nella richiesta POST /api/titolo/importa-multipli: {}", e.getMessage());
                Map<String, String> errorResponse = Collections.singletonMap("error", 
                        "Errore durante l'importazione multipla dei titoli: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
}
