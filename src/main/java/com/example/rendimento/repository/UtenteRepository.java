package com.example.rendimento.repository;

import com.example.rendimento.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository per l'entità Utente.
 * Fornisce metodi per l'accesso ai dati degli utenti nel database.
 */
@Repository
public interface UtenteRepository extends JpaRepository<Utente, Integer> {
    
    /**
     * Trova un utente per username.
     *
     * @param username il nome utente da cercare
     * @return un Optional contenente l'utente trovato, o vuoto se non esiste
     */
    Optional<Utente> findByUsername(String username);
    
    /**
     * Trova un utente per indirizzo email.
     *
     * @param email l'indirizzo email da cercare
     * @return un Optional contenente l'utente trovato, o vuoto se non esiste
     */
    Optional<Utente> findByEmail(String email);
    
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
     * Trova l'utente di sistema.
     *
     * @return un Optional contenente l'utente di sistema, o vuoto se non esiste
     */
    Optional<Utente> findByIsSystemUserTrue();
}
