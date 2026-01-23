package com.example.rendimento.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Classe entità JPA che rappresenta la tabella trends nel database.
 * Implementa il controllo ottimistico della concorrenza tramite il campo version.
 */
@Entity
@Table(
    name = "trends",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"isin", "data_snapshot"})
    }
)
public class TrendEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Data dello snapshot (es. 2026-01-21)
     */
    @Column(name = "data_snapshot", nullable = false)
    private LocalDate dataSnapshot;

    /**
     * ISIN del titolo
     */
    @Column(name = "isin", length = 12, nullable = false)
    private String isin;

    /**
     * Data di scadenza del titolo
     */
    @Column(name = "data_scadenza", nullable = false)
    private LocalDate dataScadenza;

    /**
     * Giorni residui alla scadenza (per query veloci)
     */
    @Column(name = "giorni_alla_scadenza", precision = 10, scale = 2, nullable = false)
    private BigDecimal giorniAllaScadenza;

    /**
     * Anni residui alla scadenza (chiave concettuale delle categorie)
     */
    @Column(name = "anni_alla_scadenza", precision = 10, scale = 6, nullable = false)
    private BigDecimal anniAllaScadenza;

    /**
     * Prezzo di mercato usato per il calcolo
     */
    @Column(name = "prezzo", precision = 10, scale = 4, nullable = false)
    private BigDecimal prezzo;

    /**
     * Rendimento annuo scelto come riferimento
     * (es. netto commissioni + bollo mensile)
     */
    @Column(name = "rendimento_annuo", precision = 10, scale = 6, nullable = false)
    private BigDecimal rendimentoAnnuo;

    /**
     * Campo per il controllo ottimistico della concorrenza.
     * Viene incrementato automaticamente ad ogni aggiornamento dell'entità.
     */
    @Version
    @Column(name = "version")
    private Long version;

    // ===============================
    // Getter & Setter
    // ===============================

    public Long getId() {
        return id;
    }

    public LocalDate getDataSnapshot() {
        return dataSnapshot;
    }

    public void setDataSnapshot(LocalDate dataSnapshot) {
        this.dataSnapshot = dataSnapshot;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public LocalDate getDataScadenza() {
        return dataScadenza;
    }

    public void setDataScadenza(LocalDate dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    public BigDecimal getGiorniAllaScadenza() {
        return giorniAllaScadenza;
    }

    public void setGiorniAllaScadenza(BigDecimal giorniAllaScadenza) {
        this.giorniAllaScadenza = giorniAllaScadenza;
    }

    public BigDecimal getAnniAllaScadenza() {
        return anniAllaScadenza;
    }

    public void setAnniAllaScadenza(BigDecimal anniAllaScadenza) {
        this.anniAllaScadenza = anniAllaScadenza;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public BigDecimal getRendimentoAnnuo() {
        return rendimentoAnnuo;
    }

    public void setRendimentoAnnuo(BigDecimal rendimentoAnnuo) {
        this.rendimentoAnnuo = rendimentoAnnuo;
    }

    /**
     * Restituisce il valore del campo version per il controllo ottimistico della concorrenza.
     * 
     * @return il valore del campo version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Imposta il valore del campo version per il controllo ottimistico della concorrenza.
     * 
     * @param version il nuovo valore del campo version
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "TrendEntity{" +
                "id=" + id +
                ", dataSnapshot=" + dataSnapshot +
                ", isin='" + isin + '\'' +
                ", dataScadenza=" + dataScadenza +
                ", giorniAllaScadenza=" + giorniAllaScadenza +
                ", anniAllaScadenza=" + anniAllaScadenza +
                ", prezzo=" + prezzo +
                ", rendimentoAnnuo=" + rendimentoAnnuo +
                ", version=" + version +
                '}';
    }
}
