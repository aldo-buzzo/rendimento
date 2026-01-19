package com.example.rendimento.service.impl;

import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

/**
 * Implementazione del servizio UserDetailsService per l'autenticazione con Spring Security.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    private final UtenteRepository utenteRepository;
    
    /**
     * Costruttore con parametri per l'iniezione delle dipendenze.
     *
     * @param utenteRepository repository per le operazioni CRUD sugli utenti
     */
    @Autowired
    public UserDetailsServiceImpl(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }
    
    /**
     * Carica un utente per username.
     * Questo metodo è utilizzato da Spring Security per l'autenticazione.
     *
     * @param username il nome utente da cercare
     * @return i dettagli dell'utente per l'autenticazione
     * @throws UsernameNotFoundException se l'utente non esiste
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Tentativo di autenticazione per l'utente: {}", username);
        
        try {
            log.debug("Ricerca utente nel database con username: {}", username);
            Optional<Utente> utenteOpt = utenteRepository.findByUsername(username);
            
            if (utenteOpt.isEmpty()) {
                log.error("Autenticazione fallita: utente {} non trovato nel database", username);
                throw new UsernameNotFoundException("Utente non trovato con username: " + username);
            }
            
            Utente utente = utenteOpt.get();
            log.info("Utente trovato nel database: {} (ID: {})", username, utente.getIdUtente());
            log.debug("Password hash dell'utente {}: {}", username, utente.getPassword());
            
            // Assegna il ruolo USER a tutti gli utenti normali e ADMIN agli utenti di sistema
            String role = utente.getIsSystemUser() ? "ADMIN" : "USER";
            log.info("Ruolo assegnato all'utente {}: {}", username, role);
            
            UserDetails userDetails = new User(
                    utente.getUsername(),
                    utente.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
            );
            
            log.info("UserDetails creato con successo per l'utente: {}", username);
            log.debug("UserDetails: username={}, password=[PROTECTED], authorities={}", 
                    userDetails.getUsername(), 
                    userDetails.getAuthorities());
            
            return userDetails;
        } catch (UsernameNotFoundException e) {
            log.error("Autenticazione fallita: utente {} non trovato", username);
            throw e;
        } catch (Exception e) {
            log.error("Errore durante il caricamento dell'utente {}: {}", username, e.getMessage(), e);
            log.error("Stack trace completo:", e);
            throw new UsernameNotFoundException("Errore durante il caricamento dell'utente: " + e.getMessage(), e);
        }
    }
}
