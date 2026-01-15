package com.example.rendimento.controllers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.rendimento.dto.PaginatedResponseDTO;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.service.BorsaItalianaService;
import com.example.rendimento.service.factory.BorsaItalianaServiceFactory;

@RestController
@RequestMapping("/api/borsa-italiana")
public class BorsaItalianaController {
    
    private static final Logger log = LoggerFactory.getLogger(BorsaItalianaController.class);
    
    private final BorsaItalianaServiceFactory borsaItalianaServiceFactory;

    // Dependency injection tramite costruttore
    public BorsaItalianaController(BorsaItalianaServiceFactory borsaItalianaServiceFactory) {
        this.borsaItalianaServiceFactory = borsaItalianaServiceFactory;
    }

    /**
     * Restituisce il corso ufficiale di un BTP dato l'ISIN.
     * 
     * @param isin codice ISIN del titolo
     * @return corso ufficiale
     */
    @GetMapping("/corso/btp/{isin}")
    public BigDecimal getCorsoBtp(@PathVariable String isin) {
        log.info("Ricevuta richiesta GET /api/borsa-italiana/corso/btp/{} con ISIN: {}", "isin", isin);
        BorsaItalianaService service = borsaItalianaServiceFactory.getBorsaItalianaService(TipoTitolo.BTP);
        BigDecimal result = service.getCorsoByIsin(isin);
        log.info("Risposta per GET /api/borsa-italiana/corso/btp/{}: {}", isin, result);
        return result;
    }
    
    /**
     * Restituisce il corso ufficiale di un BOT dato l'ISIN.
     * 
     * @param isin codice ISIN del titolo
     * @return corso ufficiale
     */
    @GetMapping("/corso/bot/{isin}")
    public BigDecimal getCorsoBot(@PathVariable String isin) {
        log.info("Ricevuta richiesta GET /api/borsa-italiana/corso/bot/{} con ISIN: {}", "isin", isin);
        BorsaItalianaService service = borsaItalianaServiceFactory.getBorsaItalianaService(TipoTitolo.BOT);
        BigDecimal result = service.getCorsoByIsin(isin);
        log.info("Risposta per GET /api/borsa-italiana/corso/bot/{}: {}", isin, result);
        return result;
    }
    
    /**
     * Restituisce il corso ufficiale di un titolo dato l'ISIN e il tipo.
     * 
     * @param tipo tipo del titolo (BTP o BOT)
     * @param isin codice ISIN del titolo
     * @return corso ufficiale
     */
    @GetMapping("/corso/{tipo}/{isin}")
    public BigDecimal getCorso(@PathVariable String tipo, @PathVariable String isin) {
        log.info("Ricevuta richiesta GET /api/borsa-italiana/corso/{}/{} con tipo: {}, ISIN: {}", "tipo", "isin", tipo, isin);
        TipoTitolo tipoTitolo = TipoTitolo.valueOf(tipo.toUpperCase());
        BorsaItalianaService service = borsaItalianaServiceFactory.getBorsaItalianaService(tipoTitolo);
        BigDecimal result = service.getCorsoByIsin(isin);
        log.info("Risposta per GET /api/borsa-italiana/corso/{}/{}: {}", tipo, isin, result);
        return result;
    }
    
    /**
     * Restituisce tutte le informazioni di un titolo dato l'ISIN e il tipo.
     * 
     * @param tipo tipo del titolo (BTP o BOT)
     * @param isin codice ISIN del titolo
     * @return oggetto TitoloDTO contenente tutte le informazioni del titolo
     */
    @GetMapping("/{tipo}/{isin}")
    public TitoloDTO getTitoloInfo(@PathVariable String tipo, @PathVariable String isin) {
        log.info("Ricevuta richiesta GET /api/borsa-italiana/{}/{} con tipo: {}, ISIN: {}", "tipo", "isin", tipo, isin);
        TipoTitolo tipoTitolo = TipoTitolo.valueOf(tipo.toUpperCase());
        BorsaItalianaService service = borsaItalianaServiceFactory.getBorsaItalianaService(tipoTitolo);
        
        TitoloDTO result;
        if (tipoTitolo == TipoTitolo.BTP) {
            result = service.getTitoloByIsin(isin);
        } else if (tipoTitolo == TipoTitolo.BOT) {
            result = service.getTitoloBotByIsin(isin);
        } else {
            throw new IllegalArgumentException("Tipo titolo non supportato: " + tipoTitolo);
        }
        
        log.info("Risposta per GET /api/borsa-italiana/{}/{}: {}", tipo, isin, result);
        return result;
    }
    
    /**
     * Recupera la lista dei titoli da Borsa Italiana in base al tipo.
     * 
     * @param tipoTitolo il tipo di titolo (BTP, BOT, ecc.)
     * @return lista dei titoli recuperati da Borsa Italiana
     */
    @GetMapping("/lista/{tipoTitolo}")
    public ResponseEntity<List<TitoloDTO>> getListaTitoli(@PathVariable String tipoTitolo) {
        log.info("Ricevuta richiesta GET /api/borsa-italiana/lista/{} con tipoTitolo: {}", tipoTitolo, tipoTitolo);
        try {
            BorsaItalianaService service = borsaItalianaServiceFactory.getBorsaItalianaService(tipoTitolo);
            List<TitoloDTO> titoli = service.getListaTitoli();
            
            // Log dettagliato dei dati ricevuti e spediti
            log.info("Dati ricevuti - tipoTitolo: {}", tipoTitolo);
            log.info("Dati spediti - Numero titoli: {}", titoli.size());
            if (!titoli.isEmpty()) {
                log.info("Dati spediti - Primo titolo: {}", titoli.get(0));
                log.info("Dati spediti - Ultimo titolo: {}", titoli.get(titoli.size() - 1));
            }
            
            log.info("Risposta per GET /api/borsa-italiana/lista/{}: {} titoli trovati", tipoTitolo, titoli.size());
            return ResponseEntity.ok(titoli);
        } catch (IllegalArgumentException e) {
            log.error("Errore nella richiesta GET /api/borsa-italiana/lista/{}: {}", tipoTitolo, e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            log.error("Errore interno nella richiesta GET /api/borsa-italiana/lista/{}: {}", tipoTitolo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    
    /**
     * Recupera la lista paginata dei titoli da Borsa Italiana in base al tipo.
     * 
     * @param tipoTitolo il tipo di titolo (BTP, BOT, ecc.)
     * @param page il numero di pagina (0-based, default 0)
     * @param size la dimensione della pagina (default 10)
     * @return risposta paginata contenente i titoli recuperati da Borsa Italiana
     */
    @GetMapping("/lista-paginata/{tipoTitolo}")
    public ResponseEntity<PaginatedResponseDTO<TitoloDTO>> getListaTitoliPaginata(
            @PathVariable String tipoTitolo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Ricevuta richiesta GET /api/borsa-italiana/lista-paginata/{} con tipoTitolo: {}, page: {}, size: {}", 
                tipoTitolo, tipoTitolo, page, size);
        
        try {
            BorsaItalianaService service = borsaItalianaServiceFactory.getBorsaItalianaService(tipoTitolo);
            List<TitoloDTO> allTitoli = service.getListaTitoli();
            
            // Calcola gli indici per la paginazione
            int totalElements = allTitoli.size();
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalElements);
            
            // Verifica che gli indici siano validi
            if (fromIndex >= totalElements) {
                // Se la pagina richiesta è oltre il numero totale di elementi, restituisci una pagina vuota
                log.warn("Pagina richiesta ({}) oltre il numero totale di elementi ({})", page, totalElements);
                return ResponseEntity.ok(new PaginatedResponseDTO<>(
                        Collections.emptyList(), page, size, totalElements));
            }
            
            // Estrai la sottolista per la pagina corrente
            List<TitoloDTO> paginatedTitoli = allTitoli.subList(fromIndex, toIndex);
            
            // Crea la risposta paginata
            PaginatedResponseDTO<TitoloDTO> response = new PaginatedResponseDTO<>(
                    paginatedTitoli, page, size, totalElements);
            
            // Log dettagliato dei dati ricevuti e spediti
            log.info("Dati ricevuti - tipoTitolo: {}, page: {}, size: {}", tipoTitolo, page, size);
            log.info("Dati spediti - Numero titoli nella pagina: {}, Totale titoli: {}, Totale pagine: {}", 
                    paginatedTitoli.size(), totalElements, response.getTotalPages());
            
            if (!paginatedTitoli.isEmpty()) {
                log.info("Dati spediti - Primo titolo nella pagina: {}", paginatedTitoli.get(0));
                log.info("Dati spediti - Ultimo titolo nella pagina: {}", paginatedTitoli.get(paginatedTitoli.size() - 1));
            }
            
            log.info("Risposta per GET /api/borsa-italiana/lista-paginata/{}: {} titoli trovati, pagina {}/{}", 
                    tipoTitolo, totalElements, page + 1, response.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Errore nella richiesta GET /api/borsa-italiana/lista-paginata/{}: {}", tipoTitolo, e.getMessage());
            return ResponseEntity.badRequest().body(new PaginatedResponseDTO<>(Collections.emptyList(), page, size, 0));
        } catch (Exception e) {
            log.error("Errore interno nella richiesta GET /api/borsa-italiana/lista-paginata/{}: {}", tipoTitolo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PaginatedResponseDTO<>(Collections.emptyList(), page, size, 0));
        }
    }
}
