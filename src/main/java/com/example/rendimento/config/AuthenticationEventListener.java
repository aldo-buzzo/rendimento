package com.example.rendimento.config;

import com.example.rendimento.context.UserContext;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.model.ProfiloCalcolo;
import com.example.rendimento.service.ProfiloCalcoloService;
import com.example.rendimento.service.UtenteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Listener per gli eventi di autenticazione riuscita.
 * Si occupa di caricare i profili dell'utente e valorizzare UserContext al login.
 */
@Component
public class AuthenticationEventListener implements ApplicationListener<AuthenticationSuccessEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(AuthenticationEventListener.class);
    
    @Autowired
    private UtenteService utenteService;
    
    @Autowired
    private ProfiloCalcoloService profiloCalcoloService;
    
    /**
     * Gestisce l'evento di autenticazione riuscita.
     * Carica i profili dell'utente e li memorizza in UserContext.
     * 
     * @param event l'evento di autenticazione riuscita
     */
    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            log.info("Utente autenticato con successo: {}", username);
            
            try {
                // Ottieni l'ID dell'utente dal servizio
                Optional<UtenteResponseDTO> utenteOpt = utenteService.findByUsername(username);
                
                if (utenteOpt.isPresent()) {
                    Integer userId = utenteOpt.get().getIdUtente();
                    
                    // Carica i profili dell'utente
                    List<ProfiloCalcolo> profili = profiloCalcoloService.getProfiliCalcoloByUtenteId(userId);
                    
                    // Memorizza i profili in UserContext
                    UserContext.setProfiles(userId, profili);
                    
                    log.info("Profili caricati per l'utente {}: {} profili trovati", username, profili.size());
                } else {
                    log.warn("Impossibile trovare l'utente con username: {}", username);
                }
            } catch (Exception e) {
                log.error("Errore durante il caricamento dei profili per l'utente {}: {}", username, e.getMessage(), e);
            }
        }
    }
}