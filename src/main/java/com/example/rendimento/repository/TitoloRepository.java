package com.example.rendimento.repository;

import com.example.rendimento.model.Titolo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository per l'entità Titolo.
 * Fornisce metodi per operazioni CRUD e query personalizzate sulla tabella titolo.
 */
@Repository
public interface TitoloRepository extends JpaRepository<Titolo, Integer> {
    
    /**
     * Trova un titolo per codice ISIN.
     * 
     * @param codiceIsin il codice ISIN del titolo da cercare
     * @return l'entità Titolo corrispondente, se esiste
     */
    Titolo findByCodiceIsin(String codiceIsin);
    
    /**
     * Verifica se esiste un titolo con il codice ISIN specificato.
     * 
     * @param codiceIsin il codice ISIN del titolo da verificare
     * @return true se esiste, false altrimenti
     */
    boolean existsByCodiceIsin(String codiceIsin);
    
    /**
     * Trova titoli per nome (ricerca parziale, case-insensitive).
     * 
     * @param nome il nome o parte del nome del titolo da cercare
     * @return lista di titoli che corrispondono al criterio di ricerca
     */
    java.util.List<Titolo> findByNomeContainingIgnoreCase(String nome);
}