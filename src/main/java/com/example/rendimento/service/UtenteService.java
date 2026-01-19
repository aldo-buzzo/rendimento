package com.example.rendimento.service;

import com.example.rendimento.dto.UtenteDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.model.Utente;

import java.util.List;
import java.util.Optional;

/**
 * Interfaccia per il servizio di gestione degli utenti.
 */
public interface UtenteService {
    
    /**
     * Registra un nuovo utente.
     *
     * @param utenteDTO i dati dell'utente da registrare
     * @return il DTO di risposta dell'utente registrato
     * @throws RuntimeException se l'username o l'email sono già in uso
     */
    UtenteResponseDTO registraUtente(UtenteDTO utenteDTO);
    
    /**
     * Trova un utente per ID.
     *
     * @param id l'ID dell'utente da trovare
     * @return un Optional contenente il DTO di risposta dell'utente trovato, o vuoto se non esiste
     */
    Optional<UtenteResponseDTO> findById(Integer id);
    
    /**
     * Trova un utente per username.
     *
     * @param username il nome utente da cercare
     * @return un Optional contenente il DTO di risposta dell'utente trovato, o vuoto se non esiste
     */
    Optional<UtenteResponseDTO> findByUsername(String username);
    
    /**
     * Trova un utente per indirizzo email.
     *
     * @param email l'indirizzo email da cercare
     * @return un Optional contenente il DTO di risposta dell'utente trovato, o vuoto se non esiste
     */
    Optional<UtenteResponseDTO> findByEmail(String email);
    
    /**
     * Trova l'utente di sistema.
     *
     * @return un Optional contenente il DTO di risposta dell'utente di sistema, o vuoto se non esiste
     */
    Optional<UtenteResponseDTO> findSystemUser();
    
    /**
     * Verifica se esiste un utente con il nome utente specificato.
     *
     * @param username il nome utente da verificare
     * @return true se esiste un utente con quel nome utente, false altrimenti
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica se esiste un utente con l'indirizzo email specificato.
     *
     * @param email l'indirizzo email da verificare
     * @return true se esiste un utente con quell'indirizzo email, false altrimenti
     */
    boolean existsByEmail(String email);
    
    /**
     * Ottiene tutti gli utenti.
     *
     * @return una lista di DTO di risposta degli utenti
     */
    List<UtenteResponseDTO> findAll();
    
    /**
     * Aggiorna un utente esistente.
     *
     * @param id l'ID dell'utente da aggiornare
     * @param utenteDTO i nuovi dati dell'utente
     * @return il DTO di risposta dell'utente aggiornato
     * @throws RuntimeException se l'utente non esiste
     */
    UtenteResponseDTO updateUtente(Integer id, UtenteDTO utenteDTO);
    
    /**
     * Elimina un utente.
     *
     * @param id l'ID dell'utente da eliminare
     * @throws RuntimeException se l'utente non esiste
     */
    void deleteUtente(Integer id);
    
    /**
     * Cambia la password di un utente.
     *
     * @param id l'ID dell'utente
     * @param oldPassword la vecchia password
     * @param newPassword la nuova password
     * @return true se la password è stata cambiata con successo, false altrimenti
     * @throws RuntimeException se l'utente non esiste o la vecchia password non è corretta
     */
    boolean changePassword(Integer id, String oldPassword, String newPassword);
    
    /**
     * Ottiene l'entità Utente per username.
     * Questo metodo è utilizzato internamente per l'autenticazione.
     *
     * @param username il nome utente da cercare
     * @return l'entità Utente trovata
     * @throws RuntimeException se l'utente non esiste
     */
    Utente getUtenteEntityByUsername(String username);
}
