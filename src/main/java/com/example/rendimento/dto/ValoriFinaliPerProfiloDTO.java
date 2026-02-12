package com.example.rendimento.dto;

import java.math.BigDecimal;

/**
 * DTO che contiene i valori finali teorici calcolati per un profilo specifico.
 */
public class ValoriFinaliPerProfiloDTO {
    
    // Riferimento al profilo utilizzato per il calcolo
    private Integer idProfilo;
    private String nomeProfilo;
    
    // Flag per indicare se la plusvalenza è esente
    private boolean isPlusvalenzaEsente;
    private boolean plusvalenzaEsente;
    
    // Valori finali teorici
    private BigDecimal valoreFinaleLordo;
    private BigDecimal valoreFinaleMenoCommissioni;
    private BigDecimal valoreFinaleMenoBolli;
    
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
    
    public boolean isPlusvalenzaEsente() {
        return isPlusvalenzaEsente;
    }
    
    public void setPlusvalenzaEsente(boolean value) {
        this.isPlusvalenzaEsente = value;
        this.plusvalenzaEsente = value;
    }
    
    public boolean getPlusvalenzaEsente() {
        return plusvalenzaEsente;
    }
    
    public BigDecimal getValoreFinaleLordo() {
        return valoreFinaleLordo;
    }
    
    public void setValoreFinaleLordo(BigDecimal valoreFinaleLordo) {
        this.valoreFinaleLordo = valoreFinaleLordo;
    }
    
    public BigDecimal getValoreFinaleMenoCommissioni() {
        return valoreFinaleMenoCommissioni;
    }
    
    public void setValoreFinaleMenoCommissioni(BigDecimal valoreFinaleMenoCommissioni) {
        this.valoreFinaleMenoCommissioni = valoreFinaleMenoCommissioni;
    }
    
    public BigDecimal getValoreFinaleMenoBolli() {
        return valoreFinaleMenoBolli;
    }
    
    public void setValoreFinaleMenoBolli(BigDecimal valoreFinaleMenoBolli) {
        this.valoreFinaleMenoBolli = valoreFinaleMenoBolli;
    }
}
