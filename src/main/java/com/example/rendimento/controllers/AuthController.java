package com.example.rendimento.controllers;

import com.example.rendimento.dto.UtenteDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.UtenteRepository;
import com.example.rendimento.service.UtenteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller per la gestione dell'autenticazione e della registrazione degli utenti.
 */
@Controller
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final UtenteService utenteService;
    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Costruttore con parametri per l'iniezione delle dipendenze.
     *
     * @param utenteService servizio per la gestione degli utenti
     * @param utenteRepository repository per le operazioni CRUD sugli utenti
     * @param passwordEncoder encoder per la codifica delle password
     */
    @Autowired
    public AuthController(UtenteService utenteService, UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteService = utenteService;
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Mostra la pagina di login.
     *
     * @return la vista della pagina di login
     */
    @GetMapping("/login")
    public ModelAndView showLoginPage() {
        log.info("Ricevuta richiesta GET /login");
        // Se l'utente è già autenticato, reindirizza alla home
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            log.info("Utente già autenticato, reindirizzamento a /");
            return new ModelAndView("redirect:/");
        }
        
        log.info("Utente non autenticato, inoltro a /login.html");
        return new ModelAndView("forward:/login.html");
    }
    
    /**
     * Mostra la pagina di registrazione.
     *
     * @return la vista della pagina di registrazione
     */
    @GetMapping("/registrazione")
    public ModelAndView showRegistrationPage() {
        log.info("Ricevuta richiesta GET /registrazione");
        // Se l'utente è già autenticato, reindirizza alla home
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            log.info("Utente già autenticato, reindirizzamento a /");
            return new ModelAndView("redirect:/");
        }
        
        log.info("Utente non autenticato, inoltro a /registrazione.html");
        return new ModelAndView("forward:/registrazione.html");
    }
    
    /**
     * Registra un nuovo utente.
     *
     * @param utenteDTO i dati dell'utente da registrare
     * @return la risposta con i dati dell'utente registrato
     */
    @PostMapping("/api/auth/registrazione")
    @ResponseBody
    public ResponseEntity<?> registraUtente(@RequestBody UtenteDTO utenteDTO) {
        log.info("Ricevuta richiesta POST /api/auth/registrazione con dati: {}", utenteDTO);
        try {
            UtenteResponseDTO nuovoUtente = utenteService.registraUtente(utenteDTO);
            log.info("Registrazione completata con successo per l'utente: {}", nuovoUtente.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(nuovoUtente);
        } catch (Exception e) {
            log.error("Errore durante la registrazione dell'utente: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errore", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Ottiene i dati dell'utente corrente.
     *
     * @param authentication l'oggetto di autenticazione
     * @return la risposta con i dati dell'utente corrente
     */
    @GetMapping("/api/auth/utente-corrente")
    @ResponseBody
    public ResponseEntity<?> getUtenteCorrente(Authentication authentication) {
        log.info("Ricevuta richiesta GET /api/auth/utente-corrente");
        if (authentication == null || !authentication.isAuthenticated() || 
                authentication instanceof AnonymousAuthenticationToken) {
            log.info("Utente non autenticato o autenticazione anonima");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String username = authentication.getName();
        log.info("Recupero informazioni per l'utente autenticato: {}", username);
        return utenteService.findByUsername(username)
                .map(utente -> {
                    log.info("Utente trovato: {}", utente.getUsername());
                    return ResponseEntity.ok(utente);
                })
                .orElseGet(() -> {
                    log.warn("Utente autenticato {} non trovato nel database", username);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }
    
    /**
     * Effettua il logout dell'utente corrente.
     *
     * @param request la richiesta HTTP
     * @param response la risposta HTTP
     * @param authentication l'oggetto di autenticazione
     * @return la risposta di successo
     */
    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, 
                                   Authentication authentication) {
        log.info("Ricevuta richiesta POST /api/auth/logout");
        if (authentication != null) {
            log.info("Esecuzione logout per l'utente: {}", authentication.getName());
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        } else {
            log.warn("Tentativo di logout senza autenticazione attiva");
        }
        
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("messaggio", "Logout effettuato con successo");
        return ResponseEntity.ok(successResponse);
    }
    
    /**
     * Cambia la password dell'utente corrente.
     *
     * @param passwordMap la mappa contenente la vecchia e la nuova password
     * @param authentication l'oggetto di autenticazione
     * @return la risposta di successo o errore
     */
    @PostMapping("/api/auth/cambia-password")
    @ResponseBody
    public ResponseEntity<?> cambiaPassword(@RequestBody Map<String, String> passwordMap, 
                                           Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || 
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String username = authentication.getName();
        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errore", "Vecchia e nuova password sono richieste");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            Integer userId = utenteService.findByUsername(username)
                    .map(UtenteResponseDTO::getIdUtente)
                    .orElseThrow(() -> new RuntimeException("Utente non trovato"));
            
            boolean success = utenteService.changePassword(userId, oldPassword, newPassword);
            
            if (success) {
                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("messaggio", "Password cambiata con successo");
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("errore", "La vecchia password non è corretta");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errore", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Endpoint di test per verificare se l'utente admin esiste e se la password è corretta.
     * Questo endpoint è solo per scopi di debug e dovrebbe essere rimosso in produzione.
     *
     * @return la risposta con i dati dell'utente admin
     */
    @GetMapping("/api/auth/test-admin")
    @ResponseBody
    public ResponseEntity<?> testAdmin() {
        log.info("Ricevuta richiesta GET /api/auth/test-admin");
        
        try {
            // Verifica se l'utente admin esiste
            var adminOpt = utenteRepository.findByUsername("admin");
            
            if (adminOpt.isEmpty()) {
                log.error("Utente admin non trovato nel database");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("errore", "Utente admin non trovato nel database");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Utente admin = adminOpt.get();
            log.info("Utente admin trovato nel database con ID: {}", admin.getIdUtente());
            log.info("Password hash dell'utente admin: {}", admin.getPassword());
            
            // Verifica se la password è corretta
            boolean passwordMatches = passwordEncoder.matches("password", admin.getPassword());
            log.info("La password 'password' corrisponde all'hash nel database: {}", passwordMatches);
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", admin.getUsername());
            response.put("email", admin.getEmail());
            response.put("isSystemUser", admin.getIsSystemUser());
            response.put("passwordMatches", passwordMatches);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Errore durante il test dell'utente admin: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errore", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Endpoint di test per creare un nuovo utente admin nel database.
     * Questo endpoint è solo per scopi di debug e dovrebbe essere rimosso in produzione.
     *
     * @return la risposta con i dati del nuovo utente admin
     */
    @GetMapping("/api/auth/create-test-admin")
    @ResponseBody
    public ResponseEntity<?> createTestAdmin() {
        log.info("Ricevuta richiesta GET /api/auth/create-test-admin");
        
        try {
            // Verifica se l'utente test-admin esiste già
            var adminOpt = utenteRepository.findByUsername("test-admin");
            
            if (adminOpt.isPresent()) {
                log.info("Utente test-admin già esistente nel database con ID: {}", adminOpt.get().getIdUtente());
                
                // Aggiorna la password dell'utente esistente
                Utente admin = adminOpt.get();
                admin.setPassword(passwordEncoder.encode("test-password"));
                admin.setIsSystemUser(true);
                admin = utenteRepository.save(admin);
                
                log.info("Password dell'utente test-admin aggiornata");
                
                Map<String, Object> response = new HashMap<>();
                response.put("username", admin.getUsername());
                response.put("email", admin.getEmail());
                response.put("isSystemUser", admin.getIsSystemUser());
                response.put("message", "Utente test-admin aggiornato con successo");
                
                return ResponseEntity.ok(response);
            }
            
            // Crea un nuovo utente test-admin
            Utente newAdmin = new Utente();
            newAdmin.setUsername("test-admin");
            newAdmin.setPassword(passwordEncoder.encode("test-password"));
            newAdmin.setEmail("test-admin@example.com");
            newAdmin.setNome("Test");
            newAdmin.setCognome("Admin");
            newAdmin.setDataRegistrazione(java.time.LocalDateTime.now());
            newAdmin.setIsSystemUser(true);
            
            Utente savedAdmin = utenteRepository.save(newAdmin);
            log.info("Nuovo utente test-admin creato con ID: {}", savedAdmin.getIdUtente());
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", savedAdmin.getUsername());
            response.put("email", savedAdmin.getEmail());
            response.put("isSystemUser", savedAdmin.getIsSystemUser());
            response.put("message", "Utente test-admin creato con successo");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Errore durante la creazione dell'utente test-admin: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errore", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
