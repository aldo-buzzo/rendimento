package com.example.rendimento.dto;

import java.math.BigDecimal;

/**
 * DTO che contiene i risultati del calcolo del rendimento di un titolo.
 */
public class RisultatoSimulazioneDTO {
    
    private BigDecimal plusvalenzaNetta;
    private BigDecimal interessiNetti;
    private BigDecimal commissioni;
    private BigDecimal guadagnoTotale;
    private BigDecimal guadagnoNettoCommissioni;
    private BigDecimal impostaBollo;
    private BigDecimal guadagnoNettoBollo;
    private BigDecimal tasso;
    private BigDecimal tassoNettoCommissioni;
    private BigDecimal tassoNettoBollo;
    private BigDecimal importoScadenza;
    private BigDecimal rendimentoNettoBollo; // Rendimento netto bollo non annualizzato
    
    /**
     * Costruttore predefinito.
     */
    public RisultatoSimulazioneDTO() {
    }
    
    /**
     * Costruttore con tutti i parametri.
     * 
     * @param plusvalenzaNetta la plusvalenza netta
     * @param interessiNetti gli interessi netti
     * @param commissioni le commissioni
     * @param guadagnoTotale il guadagno totale
     * @param guadagnoNettoCommissioni il guadagno al netto delle commissioni
     * @param impostaBollo l'imposta di bollo
     * @param guadagnoNettoBollo il guadagno al netto del bollo
     * @param tasso il tasso di rendimento
     * @param tassoNettoCommissioni il tasso di rendimento al netto delle commissioni
     * @param tassoNettoBollo il tasso di rendimento al netto del bollo
     * @param importoScadenza l'importo a scadenza
     * @param rendimentoNettoBollo il rendimento netto bollo non annualizzato
     */
    public RisultatoSimulazioneDTO(BigDecimal plusvalenzaNetta, BigDecimal interessiNetti, BigDecimal commissioni,
                                  BigDecimal guadagnoTotale, BigDecimal guadagnoNettoCommissioni, BigDecimal impostaBollo,
                                  BigDecimal guadagnoNettoBollo, BigDecimal tasso, BigDecimal tassoNettoCommissioni,
                                  BigDecimal tassoNettoBollo, BigDecimal importoScadenza, BigDecimal rendimentoNettoBollo) {
        this.plusvalenzaNetta = plusvalenzaNetta;
        this.interessiNetti = interessiNetti;
        this.commissioni = commissioni;
        this.guadagnoTotale = guadagnoTotale;
        this.guadagnoNettoCommissioni = guadagnoNettoCommissioni;
        this.impostaBollo = impostaBollo;
        this.guadagnoNettoBollo = guadagnoNettoBollo;
        this.tasso = tasso;
        this.tassoNettoCommissioni = tassoNettoCommissioni;
        this.tassoNettoBollo = tassoNettoBollo;
        this.importoScadenza = importoScadenza;
        this.rendimentoNettoBollo = rendimentoNettoBollo;
    }
    
    // Getter e Setter
    
    public BigDecimal getPlusvalenzaNetta() {
        return plusvalenzaNetta;
    }
    
    public void setPlusvalenzaNetta(BigDecimal plusvalenzaNetta) {
        this.plusvalenzaNetta = plusvalenzaNetta;
    }
    
    public BigDecimal getInteressiNetti() {
        return interessiNetti;
    }
    
    public void setInteressiNetti(BigDecimal interessiNetti) {
        this.interessiNetti = interessiNetti;
    }
    
    public BigDecimal getCommissioni() {
        return commissioni;
    }
    
    public void setCommissioni(BigDecimal commissioni) {
        this.commissioni = commissioni;
    }
    
    public BigDecimal getGuadagnoTotale() {
        return guadagnoTotale;
    }
    
    public void setGuadagnoTotale(BigDecimal guadagnoTotale) {
        this.guadagnoTotale = guadagnoTotale;
    }
    
    public BigDecimal getGuadagnoNettoCommissioni() {
        return guadagnoNettoCommissioni;
    }
    
    public void setGuadagnoNettoCommissioni(BigDecimal guadagnoNettoCommissioni) {
        this.guadagnoNettoCommissioni = guadagnoNettoCommissioni;
    }
    
    public BigDecimal getImpostaBollo() {
        return impostaBollo;
    }
    
    public void setImpostaBollo(BigDecimal impostaBollo) {
        this.impostaBollo = impostaBollo;
    }
    
    public BigDecimal getGuadagnoNettoBollo() {
        return guadagnoNettoBollo;
    }
    
    public void setGuadagnoNettoBollo(BigDecimal guadagnoNettoBollo) {
        this.guadagnoNettoBollo = guadagnoNettoBollo;
    }
    
    public BigDecimal getTasso() {
        return tasso;
    }
    
    public void setTasso(BigDecimal tasso) {
        this.tasso = tasso;
    }
    
    public BigDecimal getTassoNettoCommissioni() {
        return tassoNettoCommissioni;
    }
    
    public void setTassoNettoCommissioni(BigDecimal tassoNettoCommissioni) {
        this.tassoNettoCommissioni = tassoNettoCommissioni;
    }
    
    public BigDecimal getTassoNettoBollo() {
        return tassoNettoBollo;
    }
    
    public void setTassoNettoBollo(BigDecimal tassoNettoBollo) {
        this.tassoNettoBollo = tassoNettoBollo;
    }
    
    public BigDecimal getImportoScadenza() {
        return importoScadenza;
    }
    
    public void setImportoScadenza(BigDecimal importoScadenza) {
        this.importoScadenza = importoScadenza;
    }
    
    public BigDecimal getRendimentoNettoBollo() {
        return rendimentoNettoBollo;
    }
    
    public void setRendimentoNettoBollo(BigDecimal rendimentoNettoBollo) {
        this.rendimentoNettoBollo = rendimentoNettoBollo;
    }
    
    @Override
    public String toString() {
        return "RisultatoSimulazioneDTO{" +
                "plusvalenzaNetta=" + plusvalenzaNetta +
                ", interessiNetti=" + interessiNetti +
                ", commissioni=" + commissioni +
                ", guadagnoTotale=" + guadagnoTotale +
                ", guadagnoNettoCommissioni=" + guadagnoNettoCommissioni +
                ", impostaBollo=" + impostaBollo +
                ", guadagnoNettoBollo=" + guadagnoNettoBollo +
                ", tasso=" + tasso +
                ", tassoNettoCommissioni=" + tassoNettoCommissioni +
                ", tassoNettoBollo=" + tassoNettoBollo +
                ", importoScadenza=" + importoScadenza +
                ", rendimentoNettoBollo=" + rendimentoNettoBollo +
                '}';
    }
}
