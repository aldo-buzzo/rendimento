package com.example.rendimento.dto;

import java.math.BigDecimal;

/**
 * DTO per l'entità ProfiloCalcolo.
 * Utilizzato per trasferire i dati dei profili di calcolo tra il frontend e il backend.
 */
public class ProfiloCalcoloDTO {

    private Integer idProfilo;
    
    private Integer idUtente;
    
    private String nome;
    
    private String periodicitaBollo;
    
    private BigDecimal percentualeBollo;
    
    private BigDecimal commissioneBtp;
    
    private BigDecimal commissioneBot120gg;
    
    private BigDecimal commissioneBot240gg;
    
    private BigDecimal commissioneBotOltre;
    
    private BigDecimal commissioneCct;
    
    private BigDecimal commissioneCtz;
    
    private Boolean isDefault;
    
    private Boolean plusvalenzaEsente;
    
    private Long version;

    // Getter e Setter

    public Integer getIdProfilo() {
        return idProfilo;
    }

    public void setIdProfilo(Integer idProfilo) {
        this.idProfilo = idProfilo;
    }

    public Integer getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(Integer idUtente) {
        this.idUtente = idUtente;
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
        return "ProfiloCalcoloDTO{" +
                "idProfilo=" + idProfilo +
                ", idUtente=" + idUtente +
                ", nome='" + nome + '\'' +
                ", periodicitaBollo='" + periodicitaBollo + '\'' +
                ", percentualeBollo=" + percentualeBollo +
                ", commissioneBtp=" + commissioneBtp +
                ", commissioneBot120gg=" + commissioneBot120gg +
                ", commissioneBot240gg=" + commissioneBot240gg +
                ", commissioneBotOltre=" + commissioneBotOltre +
                ", commissioneCct=" + commissioneCct +
                ", commissioneCtz=" + commissioneCtz +
                ", isDefault=" + isDefault +
                ", plusvalenzaEsente=" + plusvalenzaEsente +
                ", version=" + version +
                '}';
    }
}