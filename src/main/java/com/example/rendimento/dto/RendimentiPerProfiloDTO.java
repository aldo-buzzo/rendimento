package com.example.rendimento.dto;

import java.math.BigDecimal;

/**
 * DTO che contiene i rendimenti calcolati per un profilo specifico.
 */
public class RendimentiPerProfiloDTO {
    
    // Riferimento al profilo utilizzato per il calcolo
    private Integer idProfilo;
    private String nomeProfilo;
    
    // Commissioni applicate (dipendono dal profilo)
    private BigDecimal commissionRate;
    private BigDecimal commissioni;
    
    // Bollo (dipende dal profilo)
    private String periodicitaBollo;
    private BigDecimal percentualeBollo;
    private BigDecimal impostaBollo; // Bollo effettivamente applicato in base alla periodicità
    
    // Guadagni netti (dipendono dal profilo)
    private BigDecimal plusvalenzaNetta;
    private BigDecimal guadagnoNettoSenzaCosti;
    private BigDecimal guadagnoNettoCommissioni;
    private BigDecimal guadagnoNettoBollo;
    
    // Rendimenti (dipendono dal profilo)
    private BigDecimal rendimentoLordo;
    private BigDecimal rendimentoNetto;
    private BigDecimal rendimentoConCommissioni;
    private BigDecimal rendimentoConBollo;
    private BigDecimal rendimentoPlusvalenzaEsente;
    
    // Flag che indica se la plusvalenza è esente per questo profilo
    private Boolean isPlusvalenzaEsente;
    
    // Getter e setter
    public Integer getIdProfilo() {
        return idProfilo;
    }
    
    public void setIdProfilo(Integer idProfilo) {
        this.idProfilo = idProfilo;
    }
    
    public String getNomeProfilo() {
        return nomeProfilo;
    }
    
    public void setNomeProfilo(String nomeProfilo) {
        this.nomeProfilo = nomeProfilo;
    }
    
    public BigDecimal getCommissionRate() {
        return commissionRate;
    }
    
    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }
    
    public BigDecimal getCommissioni() {
        return commissioni;
    }
    
    public void setCommissioni(BigDecimal commissioni) {
        this.commissioni = commissioni;
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
    
    
    public BigDecimal getImpostaBollo() {
        return impostaBollo;
    }
    
    public void setImpostaBollo(BigDecimal impostaBollo) {
        this.impostaBollo = impostaBollo;
    }
    
    public BigDecimal getPlusvalenzaNetta() {
        return plusvalenzaNetta;
    }
    
    public void setPlusvalenzaNetta(BigDecimal plusvalenzaNetta) {
        this.plusvalenzaNetta = plusvalenzaNetta;
    }
    
    public BigDecimal getGuadagnoNettoSenzaCosti() {
        return guadagnoNettoSenzaCosti;
    }
    
    public void setGuadagnoNettoSenzaCosti(BigDecimal guadagnoNettoSenzaCosti) {
        this.guadagnoNettoSenzaCosti = guadagnoNettoSenzaCosti;
    }
    
    public BigDecimal getGuadagnoNettoCommissioni() {
        return guadagnoNettoCommissioni;
    }
    
    public void setGuadagnoNettoCommissioni(BigDecimal guadagnoNettoCommissioni) {
        this.guadagnoNettoCommissioni = guadagnoNettoCommissioni;
    }
    
    public BigDecimal getGuadagnoNettoBollo() {
        return guadagnoNettoBollo;
    }
    
    public void setGuadagnoNettoBollo(BigDecimal guadagnoNettoBollo) {
        this.guadagnoNettoBollo = guadagnoNettoBollo;
    }
    
    public BigDecimal getRendimentoLordo() {
        return rendimentoLordo;
    }
    
    public void setRendimentoLordo(BigDecimal rendimentoLordo) {
        this.rendimentoLordo = rendimentoLordo;
    }
    
    public BigDecimal getRendimentoNetto() {
        return rendimentoNetto;
    }
    
    public void setRendimentoNetto(BigDecimal rendimentoNetto) {
        this.rendimentoNetto = rendimentoNetto;
    }
    
    public BigDecimal getRendimentoConCommissioni() {
        return rendimentoConCommissioni;
    }
    
    public void setRendimentoConCommissioni(BigDecimal rendimentoConCommissioni) {
        this.rendimentoConCommissioni = rendimentoConCommissioni;
    }
    
    public BigDecimal getRendimentoConBollo() {
        return rendimentoConBollo;
    }
    
    public void setRendimentoConBollo(BigDecimal rendimentoConBollo) {
        this.rendimentoConBollo = rendimentoConBollo;
    }
    
    public BigDecimal getRendimentoPlusvalenzaEsente() {
        return rendimentoPlusvalenzaEsente;
    }
    
    public void setRendimentoPlusvalenzaEsente(BigDecimal rendimentoPlusvalenzaEsente) {
        this.rendimentoPlusvalenzaEsente = rendimentoPlusvalenzaEsente;
    }
    
    public Boolean getIsPlusvalenzaEsente() {
        return isPlusvalenzaEsente;
    }
    
    public void setIsPlusvalenzaEsente(Boolean isPlusvalenzaEsente) {
        this.isPlusvalenzaEsente = isPlusvalenzaEsente;
    }
}