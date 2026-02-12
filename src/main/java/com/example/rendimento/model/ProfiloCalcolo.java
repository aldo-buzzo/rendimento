package com.example.rendimento.model;

import com.example.rendimento.constants.RendimentoConstants;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

    @Column(name = "percentuale_bollo", nullable = false, precision = 7, scale = 6, columnDefinition = "DECIMAL(7,6) DEFAULT 0.0020")
    private BigDecimal percentualeBollo;

    // Commissioni BTP - default 0.9/1000 (0.0009)
    @Column(name = "commissione_btp", nullable = false, precision = 7, scale = 6, columnDefinition = "DECIMAL(7,6) DEFAULT 0.0009")
    private BigDecimal commissioneBtp;

    // Commissioni BOT per fasce di scadenza - default 0.9/1000 (0.0009)
    @Column(name = "commissione_bot_120gg", nullable = false, precision = 7, scale = 6, columnDefinition = "DECIMAL(7,6) DEFAULT 0.0009")
    private BigDecimal commissioneBot120gg;

    @Column(name = "commissione_bot_240gg", nullable = false, precision = 7, scale = 6, columnDefinition = "DECIMAL(7,6) DEFAULT 0.0009")
    private BigDecimal commissioneBot240gg;

    @Column(name = "commissione_bot_oltre", nullable = false, precision = 7, scale = 6, columnDefinition = "DECIMAL(7,6) DEFAULT 0.0009")
    private BigDecimal commissioneBotOltre;

    // Commissioni per altri tipi di titoli (opzionali) - default 0.9/1000 (0.0009)
    @Column(name = "commissione_cct", precision = 7, scale = 6, columnDefinition = "DECIMAL(7,6) DEFAULT 0.0009")
    private BigDecimal commissioneCct;

    @Column(name = "commissione_ctz", precision = 7, scale = 6, columnDefinition = "DECIMAL(7,6) DEFAULT 0.0009")
    private BigDecimal commissioneCtz;

    // Flag per profilo predefinito
    @Column(name = "is_default", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDefault;
    
    // Flag per indicare se la plusvalenza è esente
    @Column(name = "plusvalenza_esente", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean plusvalenzaEsente;

    // Controllo versione
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Costruttore predefinito richiesto da JPA.
     * Inizializza i valori di default per le commissioni utilizzando le costanti definite in RendimentoConstants.
     */
    public ProfiloCalcolo() {
        // Imposta i valori di default anche nel costruttore per garantire coerenza
        this.periodicitaBollo = RendimentoConstants.PROFILE_DEFAULT_PERIODICITA_BOLLO;
        this.percentualeBollo = RendimentoConstants.PROFILE_DEFAULT_PERCENTUALE_BOLLO;
        this.commissioneBtp = RendimentoConstants.PROFILE_DEFAULT_COMMISSIONE_BTP;
        this.commissioneBot120gg = RendimentoConstants.PROFILE_DEFAULT_COMMISSIONE_BOT_120GG;
        this.commissioneBot240gg = RendimentoConstants.PROFILE_DEFAULT_COMMISSIONE_BOT_240GG;
        this.commissioneBotOltre = RendimentoConstants.PROFILE_DEFAULT_COMMISSIONE_BOT_OLTRE;
        this.commissioneCct = RendimentoConstants.PROFILE_DEFAULT_COMMISSIONE_CCT;
        this.commissioneCtz = RendimentoConstants.PROFILE_DEFAULT_COMMISSIONE_CTZ;
        this.isDefault = false;
        this.plusvalenzaEsente = RendimentoConstants.PROFILE_DEFAULT_PLUSVALENZA_ESENTE;
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
                         Boolean isDefault, Boolean plusvalenzaEsente) {
        this.utente = utente;
        this.nome = nome;
        this.periodicitaBollo = periodicitaBollo;
        this.percentualeBollo = percentualeBollo;
        this.commissioneBtp = commissioneBtp;
        this.commissioneBot120gg = commissioneBot120gg;
        this.commissioneBot240gg = commissioneBot240gg;
        this.commissioneBotOltre = commissioneBotOltre;
        this.isDefault = isDefault;
        this.plusvalenzaEsente = plusvalenzaEsente;
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
            RendimentoConstants.PROFILE_DEFAULT_PERIODICITA_BOLLO,
            RendimentoConstants.PROFILE_DEFAULT_PERCENTUALE_BOLLO,
            RendimentoConstants.PROFILE_DEFAULT_COMMISSIONE_BTP,
            RendimentoConstants.PROFILE_DEFAULT_COMMISSIONE_BOT_120GG,
            RendimentoConstants.PROFILE_DEFAULT_COMMISSIONE_BOT_240GG,
            RendimentoConstants.PROFILE_DEFAULT_COMMISSIONE_BOT_OLTRE,
            isDefault,
            RendimentoConstants.PROFILE_DEFAULT_PLUSVALENZA_ESENTE
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
        this.percentualeBollo = percentualeBollo != null ? percentualeBollo.setScale(6, RoundingMode.HALF_UP) : null;
    }

    public BigDecimal getCommissioneBtp() {
        return commissioneBtp;
    }

    public void setCommissioneBtp(BigDecimal commissioneBtp) {
        this.commissioneBtp = commissioneBtp != null ? commissioneBtp.setScale(6, RoundingMode.HALF_UP) : null;
    }

    public BigDecimal getCommissioneBot120gg() {
        return commissioneBot120gg;
    }

    public void setCommissioneBot120gg(BigDecimal commissioneBot120gg) {
        this.commissioneBot120gg = commissioneBot120gg != null ? commissioneBot120gg.setScale(6, RoundingMode.HALF_UP) : null;
    }

    public BigDecimal getCommissioneBot240gg() {
        return commissioneBot240gg;
    }

    public void setCommissioneBot240gg(BigDecimal commissioneBot240gg) {
        this.commissioneBot240gg = commissioneBot240gg != null ? commissioneBot240gg.setScale(6, RoundingMode.HALF_UP) : null;
    }

    public BigDecimal getCommissioneBotOltre() {
        return commissioneBotOltre;
    }

    public void setCommissioneBotOltre(BigDecimal commissioneBotOltre) {
        this.commissioneBotOltre = commissioneBotOltre != null ? commissioneBotOltre.setScale(6, RoundingMode.HALF_UP) : null;
    }

    public BigDecimal getCommissioneCct() {
        return commissioneCct;
    }

    public void setCommissioneCct(BigDecimal commissioneCct) {
        this.commissioneCct = commissioneCct != null ? commissioneCct.setScale(6, RoundingMode.HALF_UP) : null;
    }

    public BigDecimal getCommissioneCtz() {
        return commissioneCtz;
    }

    public void setCommissioneCtz(BigDecimal commissioneCtz) {
        this.commissioneCtz = commissioneCtz != null ? commissioneCtz.setScale(6, RoundingMode.HALF_UP) : null;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public Boolean getPlusvalenzaEsente() {
        return plusvalenzaEsente;
    }
    
    public void setPlusvalenzaEsente(Boolean plusvalenzaEsente) {
        this.plusvalenzaEsente = plusvalenzaEsente;
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
                ", plusvalenzaEsente=" + plusvalenzaEsente +
                ", version=" + version +
                '}';
    }
}