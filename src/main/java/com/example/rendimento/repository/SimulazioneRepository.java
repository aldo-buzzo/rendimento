package com.example.rendimento.repository;

import com.example.rendimento.model.Simulazione;
import com.example.rendimento.model.Titolo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository per l'entità Simulazione.
 * Fornisce metodi per operazioni CRUD e query personalizzate sulla tabella simulazione.
 */
@Repository
public interface SimulazioneRepository extends JpaRepository<Simulazione, Integer> {
    
    /**
     * Trova tutte le simulazioni associate a un titolo specifico.
     * 
     * @param titolo il titolo per cui cercare le simulazioni
     * @return lista di simulazioni associate al titolo
     */
    List<Simulazione> findByTitolo(Titolo titolo);
    
    /**
     * Trova tutte le simulazioni associate a un titolo specifico tramite il suo ID.
     * 
     * @param idTitolo l'ID del titolo per cui cercare le simulazioni
     * @return lista di simulazioni associate al titolo
     */
    List<Simulazione> findByTitolo_IdTitolo(Integer idTitolo);
    
    /**
     * Trova tutte le simulazioni con data di acquisto compresa in un intervallo.
     * 
     * @param dataInizio la data di inizio dell'intervallo
     * @param dataFine la data di fine dell'intervallo
     * @return lista di simulazioni con data di acquisto nell'intervallo specificato
     */
    List<Simulazione> findByDataAcquistoBetween(LocalDate dataInizio, LocalDate dataFine);
    
    /**
     * Trova tutte le simulazioni con rendimento netto al netto del bollo superiore a un valore specificato.
     * 
     * @param rendimentoMinimo il valore minimo del rendimento netto
     * @return lista di simulazioni con rendimento netto superiore al valore specificato
     */
    List<Simulazione> findByRendimentoNettoBolloGreaterThanEqual(BigDecimal rendimentoMinimo);
    
    /**
     * Trova tutti gli ID dei titoli distinti presenti nelle simulazioni.
     * 
     * @return lista degli ID dei titoli distinti
     */
    @Query("SELECT DISTINCT s.titolo.idTitolo FROM Simulazione s")
    List<Integer> findDistinctTitoloIds();
    
    /**
     * Trova le simulazioni per un titolo specifico, ordinate per data di acquisto in ordine decrescente.
     * 
     * @param titoloId l'ID del titolo
     * @param pageable oggetto per la paginazione e l'ordinamento
     * @return lista di simulazioni ordinate per data di acquisto
     */
    @Query("SELECT s FROM Simulazione s WHERE s.titolo.idTitolo = :titoloId ORDER BY s.dataAcquisto DESC")
    List<Simulazione> findByTitoloIdOrderByDataAcquistoDesc(@Param("titoloId") Integer titoloId, Pageable pageable);
    
    /**
     * Trova le simulazioni per un titolo specifico e una data di acquisto specifica.
     * Questo metodo è utile per verificare se esiste già una simulazione per lo stesso titolo nella stessa giornata.
     * 
     * @param idTitolo l'ID del titolo
     * @param dataAcquisto la data di acquisto
     * @return lista di simulazioni per il titolo e la data specificati
     */
    List<Simulazione> findByTitolo_IdTitoloAndDataAcquisto(Integer idTitolo, LocalDate dataAcquisto);
}
