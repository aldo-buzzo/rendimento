package com.example.rendimento.controllers;

import com.example.rendimento.constants.RendimentoConstants;
import com.example.rendimento.dto.ProfiloCalcoloDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.mapper.ProfiloCalcoloMapper;
import com.example.rendimento.model.ProfiloCalcolo;
import com.example.rendimento.service.ProfiloCalcoloService;
import com.example.rendimento.service.UtenteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller REST che gestisce le operazioni relative ai profili di calcolo.
 */
@RestController
@RequestMapping("/api/profili-calcolo")
public class ProfiloCalcoloController {

    private static final Logger log = LoggerFactory.getLogger(ProfiloCalcoloController.class);

    @Autowired
    private ProfiloCalcoloService profiloCalcoloService;

    @Autowired
    private ProfiloCalcoloMapper profiloCalcoloMapper;
    
    @Autowired
    private UtenteService utenteService;

    /**
     * Recupera tutti i profili di calcolo dell'utente autenticato.
     * Se l'utente non ha profili, ne viene creato uno predefinito.
     * Se l'utente ha almeno un profilo attivo, il profilo predefinito non viene incluso.
     *
     * @return lista dei profili di calcolo dell'utente
     */
    @GetMapping
    public ResponseEntity<List<ProfiloCalcoloDTO>> getProfiliCalcolo() {
        log.info("Ricevuta richiesta GET /api/profili-calcolo");
        
        // Ottieni l'utente autenticato
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Utente autenticato: {}", username);
        
        // Ottieni l'ID dell'utente corrente
        Integer utenteId = utenteService.findByUsername(username)
                .map(UtenteResponseDTO::getIdUtente)
                .orElse(null);
        
        if (utenteId == null) {
            log.warn("Impossibile trovare l'utente con username: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Recupera i profili di calcolo dell'utente
        List<ProfiloCalcolo> profili = profiloCalcoloService.getProfiliCalcoloByUtenteId(utenteId);
        
        // Se l'utente ha profili, rimuovi i profili predefiniti dalla lista
        if (!profili.isEmpty()) {
            final List<ProfiloCalcolo> profiliOriginali = profili;
            profili = profiliOriginali.stream()
                    .filter(p -> !Boolean.TRUE.equals(p.getIsDefault()))
                    .collect(Collectors.toList());
            
            // Se dopo il filtraggio non ci sono più profili, mantieni il primo profilo originale
            if (profili.isEmpty() && !profiliOriginali.isEmpty()) {
                profili = Collections.singletonList(profiliOriginali.get(0));
            }
        } else {
            // Se l'utente non ha profili, crea un profilo predefinito in memoria (non persistito)
            log.info("L'utente {} non ha profili di calcolo. Creazione di un profilo predefinito in memoria.", username);
            ProfiloCalcolo profiloPredefinito = new ProfiloCalcolo();
            profiloPredefinito.setNome("Profilo Predefinito");
            profiloPredefinito.setPeriodicitaBollo("ANNUALE");
            profiloPredefinito.setPercentualeBollo(RendimentoConstants.TAX_BOLLO_RATE); // 0.2%
            profiloPredefinito.setCommissioneBtp(RendimentoConstants.COMMISSION_DEFAULT_RATE); // 0.09%
            profiloPredefinito.setCommissioneBot120gg(RendimentoConstants.COMMISSION_DEFAULT_RATE); // 0.09%
            profiloPredefinito.setCommissioneBot240gg(RendimentoConstants.COMMISSION_DEFAULT_RATE); // 0.09%
            profiloPredefinito.setCommissioneBotOltre(RendimentoConstants.COMMISSION_DEFAULT_RATE); // 0.09%
            profiloPredefinito.setCommissioneCct(RendimentoConstants.COMMISSION_DEFAULT_RATE); // 0.09%
            profiloPredefinito.setCommissioneCtz(RendimentoConstants.COMMISSION_DEFAULT_RATE); // 0.09%
            profiloPredefinito.setIsDefault(true);
            // Non impostiamo l'utente perché questo profilo è solo in memoria e non verrà persistito
            
            profili = Collections.singletonList(profiloPredefinito);
        }
        
        List<ProfiloCalcoloDTO> result = profiloCalcoloMapper.toDtoList(profili);
        log.info("Risposta per GET /api/profili-calcolo: {} profili trovati", result.size());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Recupera un profilo di calcolo specifico.
     *
     * @param id l'ID del profilo di calcolo
     * @return il profilo di calcolo richiesto
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfiloCalcoloDTO> getProfiloCalcolo(@PathVariable Integer id) {
        log.info("Ricevuta richiesta GET /api/profili-calcolo/{}", id);
        
        Optional<ProfiloCalcolo> profiloOpt = profiloCalcoloService.getProfiloCalcoloById(id);
        
        if (profiloOpt.isPresent()) {
            ProfiloCalcoloDTO result = profiloCalcoloMapper.toDto(profiloOpt.get());
            log.info("Risposta per GET /api/profili-calcolo/{}: {}", id, result);
            return ResponseEntity.ok(result);
        } else {
            log.warn("Profilo di calcolo non trovato con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Crea un nuovo profilo di calcolo.
     *
     * @param dto il DTO con i dati del profilo di calcolo
     * @return il profilo di calcolo creato
     */
    @PostMapping
    public ResponseEntity<?> createProfiloCalcolo(@RequestBody ProfiloCalcoloDTO dto) {
        log.info("Ricevuta richiesta POST /api/profili-calcolo: {}", dto);
        
        // Valida il DTO
        Map<String, String> validationErrors = validateProfiloCalcoloDTO(dto);
        if (!validationErrors.isEmpty()) {
            log.warn("Errori di validazione: {}", validationErrors);
            return ResponseEntity.badRequest().body(validationErrors);
        }
        
        try {
            // Ottieni l'utente autenticato
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.debug("Utente autenticato: {}", username);
            
            // Ottieni l'ID dell'utente corrente
            Integer utenteId = utenteService.findByUsername(username)
                    .map(UtenteResponseDTO::getIdUtente)
                    .orElse(null);
            
            if (utenteId == null) {
                log.warn("Impossibile trovare l'utente con username: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Imposta l'ID dell'utente nel DTO
            dto.setIdUtente(utenteId);
            
            // Imposta i valori predefiniti per le commissioni non valorizzate
            setDefaultCommissionValues(dto);
            
            // Converti il DTO in entità
            ProfiloCalcolo profilo = profiloCalcoloMapper.toEntity(dto);
            
            // Salva il profilo
            ProfiloCalcolo saved = profiloCalcoloService.saveProfiloCalcolo(profilo);
            
            // Converti l'entità salvata in DTO
            ProfiloCalcoloDTO result = profiloCalcoloMapper.toDto(saved);
            
            log.info("Risposta per POST /api/profili-calcolo: {}", result);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Errore nella creazione del profilo di calcolo", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Aggiorna un profilo di calcolo esistente.
     *
     * @param id l'ID del profilo di calcolo da aggiornare
     * @param dto il DTO con i nuovi dati
     * @return il profilo di calcolo aggiornato
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfiloCalcolo(@PathVariable Integer id, @RequestBody ProfiloCalcoloDTO dto) {
        log.info("Ricevuta richiesta PUT /api/profili-calcolo/{}: {}", id, dto);
        
        // Verifica che l'ID nel path corrisponda all'ID nel DTO
        if (!id.equals(dto.getIdProfilo())) {
            log.warn("ID nel path ({}) non corrisponde all'ID nel DTO ({})", id, dto.getIdProfilo());
            return ResponseEntity.badRequest().build();
        }
        
        // Valida il DTO
        Map<String, String> validationErrors = validateProfiloCalcoloDTO(dto);
        if (!validationErrors.isEmpty()) {
            log.warn("Errori di validazione: {}", validationErrors);
            return ResponseEntity.badRequest().body(validationErrors);
        }
        
        try {
            // Imposta i valori predefiniti per le commissioni non valorizzate
            setDefaultCommissionValues(dto);
            
            // Recupera il profilo esistente
            Optional<ProfiloCalcolo> profiloOpt = profiloCalcoloService.getProfiloCalcoloById(id);
            
            if (profiloOpt.isPresent()) {
                // Aggiorna il profilo con i dati del DTO
                ProfiloCalcolo profilo = profiloCalcoloMapper.updateEntityFromDto(profiloOpt.get(), dto);
                
                // Salva il profilo aggiornato
                ProfiloCalcolo updated = profiloCalcoloService.saveProfiloCalcolo(profilo);
                
                // Converti l'entità aggiornata in DTO
                ProfiloCalcoloDTO result = profiloCalcoloMapper.toDto(updated);
                
                log.info("Risposta per PUT /api/profili-calcolo/{}: {}", id, result);
                return ResponseEntity.ok(result);
            } else {
                log.warn("Profilo di calcolo non trovato con ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Errore nell'aggiornamento del profilo di calcolo", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Imposta un profilo di calcolo come predefinito.
     *
     * @param id l'ID del profilo di calcolo da impostare come predefinito
     * @return il profilo di calcolo aggiornato
     */
    @PutMapping("/{id}/predefinito")
    public ResponseEntity<ProfiloCalcoloDTO> setProfiloCalcoloPredefinito(@PathVariable Integer id) {
        log.info("Ricevuta richiesta PUT /api/profili-calcolo/{}/predefinito", id);
        
        try {
            // Ottieni l'utente autenticato
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Ottieni l'ID dell'utente corrente
            Integer utenteId = utenteService.findByUsername(username)
                    .map(UtenteResponseDTO::getIdUtente)
                    .orElse(null);
            
            if (utenteId == null) {
                log.warn("Impossibile trovare l'utente con username: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Imposta il profilo come predefinito
            ProfiloCalcolo profilo = profiloCalcoloService.setProfiloCalcoloPredefinito(id, utenteId);
            
            // Converti l'entità in DTO
            ProfiloCalcoloDTO result = profiloCalcoloMapper.toDto(profilo);
            
            log.info("Risposta per PUT /api/profili-calcolo/{}/predefinito: {}", id, result);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Errore nell'impostazione del profilo predefinito: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Errore nell'impostazione del profilo predefinito", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina un profilo di calcolo.
     *
     * @param id l'ID del profilo di calcolo da eliminare
     * @return risposta vuota con stato HTTP
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfiloCalcolo(@PathVariable Integer id) {
        log.info("Ricevuta richiesta DELETE /api/profili-calcolo/{}", id);
        
        try {
            profiloCalcoloService.deleteProfiloCalcolo(id);
            log.info("Profilo di calcolo eliminato con ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            log.warn("Impossibile eliminare il profilo di calcolo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Errore nell'eliminazione del profilo di calcolo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Valida il DTO del profilo di calcolo.
     * 
     * @param dto il DTO da validare
     * @return una mappa di errori di validazione (vuota se non ci sono errori)
     */
    private Map<String, String> validateProfiloCalcoloDTO(ProfiloCalcoloDTO dto) {
        Map<String, String> errors = new HashMap<>();
        
        // Valida il nome
        if (dto.getNome() == null || !StringUtils.hasText(dto.getNome())) {
            errors.put("nome", "Il nome del profilo è obbligatorio");
        }
        
        // Valida la periodicità del bollo
        if (dto.getPeriodicitaBollo() == null || !StringUtils.hasText(dto.getPeriodicitaBollo())) {
            errors.put("periodicitaBollo", "La periodicità del bollo è obbligatoria");
        }
        
        // Valida la percentuale del bollo
        if (dto.getPercentualeBollo() == null) {
            errors.put("percentualeBollo", "La percentuale del bollo è obbligatoria");
        }
        
        // Valida la commissione BTP
        if (dto.getCommissioneBtp() == null) {
            errors.put("commissioneBtp", "La commissione BTP è obbligatoria");
        }
        
        return errors;
    }
    
    /**
     * Imposta i valori predefiniti per le commissioni non valorizzate.
     * 
     * @param dto il DTO da aggiornare
     */
    private void setDefaultCommissionValues(ProfiloCalcoloDTO dto) {
        BigDecimal commissioneBtp = dto.getCommissioneBtp();
        
        if (commissioneBtp != null) {
            // Imposta le commissioni non valorizzate con il valore della commissione BTP
            if (dto.getCommissioneBot120gg() == null) {
                dto.setCommissioneBot120gg(commissioneBtp);
            }
            
            if (dto.getCommissioneBot240gg() == null) {
                dto.setCommissioneBot240gg(commissioneBtp);
            }
            
            if (dto.getCommissioneBotOltre() == null) {
                dto.setCommissioneBotOltre(commissioneBtp);
            }
            
            if (dto.getCommissioneCct() == null) {
                dto.setCommissioneCct(commissioneBtp);
            }
            
            if (dto.getCommissioneCtz() == null) {
                dto.setCommissioneCtz(commissioneBtp);
            }
        }
    }
}