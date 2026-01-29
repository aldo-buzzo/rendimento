package com.example.rendimento.repository;

import com.example.rendimento.model.ProfiloCalcolo;
import com.example.rendimento.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per l'entità ProfiloCalcolo.
 * Fornisce metodi per accedere ai dati dei profili di calcolo nel database.
 */
@Repository
public interface ProfiloCalcoloRepository extends JpaRepository<ProfiloCalcolo, Integer> {
    
    /**
     * Trova tutti i profili di calcolo di un utente.
     * 
     * @param utente l'utente di cui trovare i profili
     * @return la lista dei profili di calcolo dell'utente
     */
    List<ProfiloCalcolo> findByUtente(Utente utente);
    
    /**
     * Trova tutti i profili di calcolo di un utente per ID.
     * 
     * @param idUtente l'ID dell'utente
     * @return la lista dei profili di calcolo dell'utente
     */
    List<ProfiloCalcolo> findByUtenteIdUtente(Integer idUtente);
    
    /**
     * Trova il profilo di calcolo predefinito di un utente.
     * 
     * @param utente l'utente di cui trovare il profilo predefinito
     * @return il profilo di calcolo predefinito dell'utente, se esiste
     */
    Optional<ProfiloCalcolo> findByUtenteAndIsDefaultTrue(Utente utente);
    
    /**
     * Trova il profilo di calcolo predefinito di un utente per ID.
     * 
     * @param idUtente l'ID dell'utente
     * @return il profilo di calcolo predefinito dell'utente, se esiste
     */
    @Query("SELECT p FROM ProfiloCalcolo p WHERE p.utente.idUtente = :idUtente AND p.isDefault = true")
    Optional<ProfiloCalcolo> findDefaultByUtenteId(@Param("idUtente") Integer idUtente);
}