package com.example.rendimento.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.rendimento.model.TrendEntity;
import com.example.rendimento.repository.projection.TrendAggregatoProjection;

@Repository
public interface TrendRepository extends JpaRepository<TrendEntity, Long> {

    /**
     * Trova tutti gli snapshot di un determinato titolo (ISIN)
     * ordinati per data crescente.
     */
    List<TrendEntity> findByIsinOrderByDataSnapshotAsc(String isin);

    /**
     * Trova tutti gli snapshot per una data specifica.
     */
    List<TrendEntity> findByDataSnapshot(LocalDate dataSnapshot);

    /**
     * Trova tutti gli snapshot di un titolo in un intervallo di date.
     */
    List<TrendEntity> findByIsinAndDataSnapshotBetweenOrderByDataSnapshotAsc(
            String isin,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * Cancella tutti gli snapshot precedenti a una certa data
     * (utile per pulizia o aggiornamenti).
     */
    void deleteByDataSnapshotBefore(LocalDate date);
    
    /**
     * Trova un trend specifico per ISIN e data snapshot.
     * Utilizzato per verificare se esiste già un trend per un titolo in una data specifica.
     */
    java.util.Optional<TrendEntity> findByIsinAndDataSnapshot(String isin, LocalDate dataSnapshot);

  @Query("""
        SELECT
            t.dataSnapshot as dataSnapshot,
            t.anniAllaScadenza as anniAllaScadenza,
            AVG(t.rendimentoAnnuo) as rendimentoMedio,
            MIN(t.rendimentoAnnuo) as rendimentoMinimo,
            MAX(t.rendimentoAnnuo) as rendimentoMassimo,
            COUNT(t) as numeroTitoli
        FROM TrendEntity t
        WHERE t.dataSnapshot BETWEEN :dataInizio AND :dataFine
        GROUP BY t.dataSnapshot, t.anniAllaScadenza
    """)
    List<TrendAggregatoProjection> findTrendAggregati(
        @Param("dataInizio") LocalDate dataInizio,
        @Param("dataFine") LocalDate dataFine
    );
    
    /**
     * Trova tutti i trend aggregati a partire da una data specifica
     */
    @Query("""
        SELECT
            t.dataSnapshot as dataSnapshot,
            t.anniAllaScadenza as anniAllaScadenza,
            AVG(t.rendimentoAnnuo) as rendimentoMedio,
            MIN(t.rendimentoAnnuo) as rendimentoMinimo,
            MAX(t.rendimentoAnnuo) as rendimentoMassimo,
            COUNT(t) as numeroTitoli
        FROM TrendEntity t
        WHERE t.dataSnapshot >= :dataInizio
        GROUP BY t.dataSnapshot, t.anniAllaScadenza
    """)
    List<TrendAggregatoProjection> findTrendAggregatiFromDate(
        @Param("dataInizio") LocalDate dataInizio
    );
    
    /**
     * Trova tutti i trend aggregati senza filtro per data
     */
    @Query("""
        SELECT
            t.dataSnapshot as dataSnapshot,
            t.anniAllaScadenza as anniAllaScadenza,
            AVG(t.rendimentoAnnuo) as rendimentoMedio,
            MIN(t.rendimentoAnnuo) as rendimentoMinimo,
            MAX(t.rendimentoAnnuo) as rendimentoMassimo,
            COUNT(t) as numeroTitoli
        FROM TrendEntity t
        GROUP BY t.dataSnapshot, t.anniAllaScadenza
    """)
    List<TrendAggregatoProjection> findAllTrendAggregati();

}
