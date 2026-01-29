package com.example.rendimento.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Classe entità JPA che rappresenta un profilo di calcolo per le commissioni e i bolli.
 * Ogni utente può avere più profili di calcolo per gestire diverse condizioni.
 * Implementa il controllo ottimistico della concorrenza tramite il campo version.
 */
@Entity
@Table(name = "profilo_calcolo")
public class ProfiloCalcolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profilo")
    private Integer idProfilo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utente", nullable = false)
    private Utente utente;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    // Impostazioni bollo
    @Column(name = "periodicita_bollo", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ANNUALE'")
    private String periodicitaBollo;

    @Column(name = "percentuale_bollo", nullable = false, precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.0020")
    private BigDecimal percentualeBollo;

    // Commissioni BTP - default 0.9/1000 (0.0009)
    @Column(name = "commissione_btp", nullable = false, precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.0009")
    private BigDecimal commissioneBtp;

    // Commissioni BOT per fasce di scadenza - default 0.9/1000 (0.0009)
    @Column(name = "commissione_bot_120gg", nullable = false, precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.0009")
    private BigDecimal commissioneBot120gg;

    @Column(name = "commissione_bot_240gg", nullable = false, precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.0009")
    private BigDecimal commissioneBot240gg;

    @Column(name = "commissione_bot_oltre", nullable = false, precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.0009")
    private BigDecimal commissioneBotOltre;

    // Commissioni per altri tipi di titoli (opzionali) - default 0.9/1000 (0.0009)
    @Column(name = "commissione_cct", precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.0009")
    private BigDecimal commissioneCct;

    @Column(name = "commissione_ctz", precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.0009")
    private BigDecimal commissioneCtz;

    // Flag per profilo predefinito
    @Column(name = "is_default", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDefault;

    // Controllo versione
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Costruttore predefinito richiesto da JPA.
     * Inizializza i valori di default per le commissioni.
     */
    public ProfiloCalcolo() {
        // Imposta i valori di default anche nel costruttore per garantire coerenza
        this.periodicitaBollo = "ANNUALE";
        this.percentualeBollo = new BigDecimal("0.0020");
        this.commissioneBtp = new BigDecimal("0.0009");
        this.commissioneBot120gg = new BigDecimal("0.0009");
        this.commissioneBot240gg = new BigDecimal("0.0009");
        this.commissioneBotOltre = new BigDecimal("0.0009");
        this.commissioneCct = new BigDecimal("0.0009");
        this.commissioneCtz = new BigDecimal("0.0009");
        this.isDefault = false;
    }

    /**
     * Costruttore con parametri per creare una nuova istanza di ProfiloCalcolo.
     *
     * @param utente l'utente proprietario del profilo
     * @param nome il nome del profilo
     * @param periodicitaBollo la periodicità del bollo (MENSILE o ANNUALE)
     * @param percentualeBollo la percentuale del bollo
     * @param commissioneBtp la commissione per i BTP
     * @param commissioneBot120gg la commissione per i BOT con scadenza fino a 120 giorni
     * @param commissioneBot240gg la commissione per i BOT con scadenza tra 121 e 240 giorni
     * @param commissioneBotOltre la commissione per i BOT con scadenza oltre 240 giorni
     * @param isDefault se questo è il profilo predefinito dell'utente
     */
    public ProfiloCalcolo(Utente utente, String nome, 
                         String periodicitaBollo, BigDecimal percentualeBollo,
                         BigDecimal commissioneBtp, BigDecimal commissioneBot120gg,
                         BigDecimal commissioneBot240gg, BigDecimal commissioneBotOltre,
                         Boolean isDefault) {
        this.utente = utente;
        this.nome = nome;
        this.periodicitaBollo = periodicitaBollo;
        this.percentualeBollo = percentualeBollo;
        this.commissioneBtp = commissioneBtp;
        this.commissioneBot120gg = commissioneBot120gg;
        this.commissioneBot240gg = commissioneBot240gg;
        this.commissioneBotOltre = commissioneBotOltre;
        this.isDefault = isDefault;
    }

    /**
     * Crea un nuovo profilo di calcolo con valori di default.
     *
     * @param utente l'utente proprietario del profilo
     * @param nome il nome del profilo
     * @param isDefault se questo è il profilo predefinito dell'utente
     * @return un nuovo profilo di calcolo con valori di default
     */
    public static ProfiloCalcolo createDefault(Utente utente, String nome, Boolean isDefault) {
        return new ProfiloCalcolo(
            utente,
            nome,
            "ANNUALE",
            new BigDecimal("0.0020"),
            new BigDecimal("0.0009"),
            new BigDecimal("0.0009"),
            new BigDecimal("0.0009"),
            new BigDecimal("0.0009"),
            isDefault
        );
    }

    // Getter e Setter

    public Integer getIdProfilo() {
        return idProfilo;
    }

    public void setIdProfilo(Integer idProfilo) {
        this.idProfilo = idProfilo;
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPeriodicitaBollo() {
        return periodicitaBollo;
    }

    public void setPeriodicitaBollo(String periodicitaBollo) {
        this.periodicitaBollo = periodicitaBollo;
    }

    public BigDecimal getPercentualeBollo() {
        return percentualeBollo;
    }

    public void setPercentualeBollo(BigDecimal percentualeBollo) {
        this.percentualeBollo = percentualeBollo;
    }

    public BigDecimal getCommissioneBtp() {
        return commissioneBtp;
    }

    public void setCommissioneBtp(BigDecimal commissioneBtp) {
        this.commissioneBtp = commissioneBtp;
    }

    public BigDecimal getCommissioneBot120gg() {
        return commissioneBot120gg;
    }

    public void setCommissioneBot120gg(BigDecimal commissioneBot120gg) {
        this.commissioneBot120gg = commissioneBot120gg;
    }

    public BigDecimal getCommissioneBot240gg() {
        return commissioneBot240gg;
    }

    public void setCommissioneBot240gg(BigDecimal commissioneBot240gg) {
        this.commissioneBot240gg = commissioneBot240gg;
    }

    public BigDecimal getCommissioneBotOltre() {
        return commissioneBotOltre;
    }

    public void setCommissioneBotOltre(BigDecimal commissioneBotOltre) {
        this.commissioneBotOltre = commissioneBotOltre;
    }

    public BigDecimal getCommissioneCct() {
        return commissioneCct;
    }

    public void setCommissioneCct(BigDecimal commissioneCct) {
        this.commissioneCct = commissioneCct;
    }

    public BigDecimal getCommissioneCtz() {
        return commissioneCtz;
    }

    public void setCommissioneCtz(BigDecimal commissioneCtz) {
        this.commissioneCtz = commissioneCtz;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ProfiloCalcolo{" +
                "idProfilo=" + idProfilo +
                ", utente=" + (utente != null ? utente.getIdUtente() : null) +
                ", nome='" + nome + '\'' +
                ", periodicitaBollo='" + periodicitaBollo + '\'' +
                ", percentualeBollo=" + percentualeBollo +
                ", commissioneBtp=" + commissioneBtp +
                ", commissioneBot120gg=" + commissioneBot120gg +
                ", commissioneBot240gg=" + commissioneBot240gg +
                ", commissioneBotOltre=" + commissioneBotOltre +
                ", isDefault=" + isDefault +
                ", version=" + version +
                '}';
    }
}