package com.example.rendimento.dto;

import java.math.BigDecimal;

/**
 * DTO unificato che contiene i risultati del calcolo del rendimento di un titolo.
 * Sostituisce sia RisultatoSimulazioneDTO che RisultatoRendimentoAdvancedDTO.
 */
public class RisultatoCalcoloDTO {
    
    // Campi originali da RisultatoSimulazioneDTO
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
    
    // Campi aggiuntivi da RisultatoRendimentoAdvancedDTO
    private BigDecimal nominale;
    private BigDecimal prezzoAcquistoPercentuale;
    private BigDecimal cedolaAnnua;
    private BigDecimal anniDurata;
    private BigDecimal commissionRate;
    private BigDecimal prezzoRiferimentoBollo;
    private BigDecimal valoreRimborso;
    private BigDecimal fattoreTempo;
    private BigDecimal fattoreAnnualizzazione;

    private BigDecimal capitaleInvestito;
    private BigDecimal capitaleConCommissioni;
    private BigDecimal cedoleNetteAnnue;
    private BigDecimal guadagnoNettoSenzaCosti;

    private BigDecimal rendimentoSenzaCosti;
    private BigDecimal rendimentoConCommissioni;
    private BigDecimal rendimentoConBollo;
    private String periodicitaBollo;
    private BigDecimal rendimentoPlusvalenzaEsente;
    
    // Valori finali teorici per le diverse combinazioni
    private BigDecimal valoreBolloAnnualePlusvalenzaNonEsente;  // Valore finale con bollo annuale e plusvalenza non esente
    private BigDecimal valoreBolloAnnualePlusvalenzaEsente;     // Valore finale con bollo annuale e plusvalenza esente (solo per BTP)
    private BigDecimal valoreBolloMensilePlusvalenzaNonEsente;  // Valore finale con bollo mensile e plusvalenza non esente
    private BigDecimal valoreBolloMensilePlusvalenzaEsente;     // Valore finale con bollo mensile e plusvalenza esente (solo per BTP)

    /**
     * Costruttore predefinito.
     */
    public RisultatoCalcoloDTO() {
    }
    
    /**
     * Costruttore con i parametri base.
     */
    public RisultatoCalcoloDTO(BigDecimal plusvalenzaNetta, BigDecimal interessiNetti, BigDecimal commissioni,
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
    
    /**
     * Costruttore che converte da RisultatoSimulazioneDTO.
     */
    public RisultatoCalcoloDTO(RisultatoSimulazioneDTO legacy) {
        this(
            legacy.getPlusvalenzaNetta(),
            legacy.getInteressiNetti(),
            legacy.getCommissioni(),
            legacy.getGuadagnoTotale(),
            legacy.getGuadagnoNettoCommissioni(),
            legacy.getImpostaBollo(),
            legacy.getGuadagnoNettoBollo(),
            legacy.getTasso(),
            legacy.getTassoNettoCommissioni(),
            legacy.getTassoNettoBollo(),
            legacy.getImportoScadenza(),
            legacy.getRendimentoNettoBollo()
        );
    }
    
    /**
     * Costruttore che converte da RisultatoRendimentoAdvancedDTO.
     */
    public RisultatoCalcoloDTO(RisultatoRendimentoAdvancedDTO advanced) {
        this(
            advanced.getPlusvalenzaNetta(),
            advanced.getInteressiNetti(),
            advanced.getCommissioni(),
            advanced.getGuadagnoTotale(),
            advanced.getGuadagnoNettoCommissioni(),
            advanced.getImpostaBollo(),
            advanced.getGuadagnoNettoBollo(),
            advanced.getTasso(),
            advanced.getTassoNettoCommissioni(),
            advanced.getTassoNettoBollo(),
            advanced.getImportoScadenza(),
            advanced.getRendimentoNettoBollo()
        );
        
        // Copiamo anche i campi aggiuntivi
        this.nominale = advanced.getNominale();
        this.prezzoAcquistoPercentuale = advanced.getPrezzoAcquistoPercentuale();
        this.cedolaAnnua = advanced.getCedolaAnnua();
        this.anniDurata = advanced.getAnniDurata();
        this.commissionRate = advanced.getCommissionRate();
        this.prezzoRiferimentoBollo = advanced.getPrezzoRiferimentoBollo();
        this.valoreRimborso = advanced.getValoreRimborso();
        this.fattoreTempo = advanced.getFattoreTempo();
        this.fattoreAnnualizzazione = advanced.getFattoreAnnualizzazione();
        this.capitaleInvestito = advanced.getCapitaleInvestito();
        this.capitaleConCommissioni = advanced.getCapitaleConCommissioni();
        this.cedoleNetteAnnue = advanced.getCedoleNetteAnnue();
        this.guadagnoNettoSenzaCosti = advanced.getGuadagnoNettoSenzaCosti();
        this.rendimentoSenzaCosti = advanced.getRendimentoSenzaCosti();
        this.rendimentoConCommissioni = advanced.getRendimentoConCommissioni();
        
        // Per retrocompatibilità, utilizziamo il rendimento con bollo annuale come default
        this.rendimentoConBollo = advanced.getRendimentoConCommissioniEBolloAnnuale();
        this.periodicitaBollo = "ANNUALE"; // Default
        
        this.rendimentoPlusvalenzaEsente = advanced.getRendimentoPlusvalenzaEsente();
        this.valoreBolloAnnualePlusvalenzaNonEsente = advanced.getValoreBolloAnnualePlusvalenzaNonEsente();
        this.valoreBolloAnnualePlusvalenzaEsente = advanced.getValoreBolloAnnualePlusvalenzaEsente();
        this.valoreBolloMensilePlusvalenzaNonEsente = advanced.getValoreBolloMensilePlusvalenzaNonEsente();
        this.valoreBolloMensilePlusvalenzaEsente = advanced.getValoreBolloMensilePlusvalenzaEsente();
    }

    // Getter e Setter per tutti i campi
    
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
    
    public BigDecimal getNominale() { 
        return nominale; 
    }
    
    public void setNominale(BigDecimal nominale) { 
        this.nominale = nominale; 
    }

    public BigDecimal getPrezzoAcquistoPercentuale() { 
        return prezzoAcquistoPercentuale; 
    }
    
    public void setPrezzoAcquistoPercentuale(BigDecimal prezzoAcquistoPercentuale) { 
        this.prezzoAcquistoPercentuale = prezzoAcquistoPercentuale; 
    }

    public BigDecimal getCedolaAnnua() { 
        return cedolaAnnua; 
    }
    
    public void setCedolaAnnua(BigDecimal cedolaAnnua) { 
        this.cedolaAnnua = cedolaAnnua; 
    }

    public BigDecimal getAnniDurata() { 
        return anniDurata; 
    }
    
    public void setAnniDurata(BigDecimal anniDurata) { 
        this.anniDurata = anniDurata; 
    }

    public BigDecimal getCommissionRate() { 
        return commissionRate; 
    }
    
    public void setCommissionRate(BigDecimal commissionRate) { 
        this.commissionRate = commissionRate; 
    }

    public BigDecimal getPrezzoRiferimentoBollo() { 
        return prezzoRiferimentoBollo; 
    }
    
    public void setPrezzoRiferimentoBollo(BigDecimal prezzoRiferimentoBollo) { 
        this.prezzoRiferimentoBollo = prezzoRiferimentoBollo; 
    }

    public BigDecimal getCapitaleInvestito() { 
        return capitaleInvestito; 
    }
    
    public void setCapitaleInvestito(BigDecimal capitaleInvestito) { 
        this.capitaleInvestito = capitaleInvestito; 
    }

    public BigDecimal getCapitaleConCommissioni() { 
        return capitaleConCommissioni; 
    }
    
    public void setCapitaleConCommissioni(BigDecimal capitaleConCommissioni) { 
        this.capitaleConCommissioni = capitaleConCommissioni; 
    }

    public BigDecimal getCedoleNetteAnnue() { 
        return cedoleNetteAnnue; 
    }
    
    public void setCedoleNetteAnnue(BigDecimal cedoleNetteAnnue) { 
        this.cedoleNetteAnnue = cedoleNetteAnnue; 
    }

    public BigDecimal getGuadagnoNettoSenzaCosti() { 
        return guadagnoNettoSenzaCosti; 
    }
    
    public void setGuadagnoNettoSenzaCosti(BigDecimal guadagnoNettoSenzaCosti) { 
        this.guadagnoNettoSenzaCosti = guadagnoNettoSenzaCosti; 
    }


    public BigDecimal getRendimentoSenzaCosti() { 
        return rendimentoSenzaCosti; 
    }
    
    public void setRendimentoSenzaCosti(BigDecimal rendimentoSenzaCosti) { 
        this.rendimentoSenzaCosti = rendimentoSenzaCosti; 
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
    
    public String getPeriodicitaBollo() { 
        return periodicitaBollo; 
    }
    
    public void setPeriodicitaBollo(String periodicitaBollo) { 
        this.periodicitaBollo = periodicitaBollo; 
    }
    
    public BigDecimal getRendimentoPlusvalenzaEsente() { 
        return rendimentoPlusvalenzaEsente; 
    }
    
    public void setRendimentoPlusvalenzaEsente(BigDecimal rendimentoPlusvalenzaEsente) { 
        this.rendimentoPlusvalenzaEsente = rendimentoPlusvalenzaEsente; 
    }
    
    public BigDecimal getValoreBolloAnnualePlusvalenzaNonEsente() { 
        return valoreBolloAnnualePlusvalenzaNonEsente; 
    }
    
    public void setValoreBolloAnnualePlusvalenzaNonEsente(BigDecimal valoreBolloAnnualePlusvalenzaNonEsente) { 
        this.valoreBolloAnnualePlusvalenzaNonEsente = valoreBolloAnnualePlusvalenzaNonEsente; 
    }

    public BigDecimal getValoreBolloAnnualePlusvalenzaEsente() { 
        return valoreBolloAnnualePlusvalenzaEsente; 
    }
    
    public void setValoreBolloAnnualePlusvalenzaEsente(BigDecimal valoreBolloAnnualePlusvalenzaEsente) { 
        this.valoreBolloAnnualePlusvalenzaEsente = valoreBolloAnnualePlusvalenzaEsente; 
    }

    public BigDecimal getValoreBolloMensilePlusvalenzaNonEsente() { 
        return valoreBolloMensilePlusvalenzaNonEsente; 
    }
    
    public void setValoreBolloMensilePlusvalenzaNonEsente(BigDecimal valoreBolloMensilePlusvalenzaNonEsente) { 
        this.valoreBolloMensilePlusvalenzaNonEsente = valoreBolloMensilePlusvalenzaNonEsente; 
    }

    public BigDecimal getValoreBolloMensilePlusvalenzaEsente() { 
        return valoreBolloMensilePlusvalenzaEsente; 
    }
    
    public void setValoreBolloMensilePlusvalenzaEsente(BigDecimal valoreBolloMensilePlusvalenzaEsente) { 
        this.valoreBolloMensilePlusvalenzaEsente = valoreBolloMensilePlusvalenzaEsente; 
    }

    public BigDecimal getValoreRimborso() { 
        return valoreRimborso != null ? valoreRimborso : BigDecimal.valueOf(100); 
    }
    
    public void setValoreRimborso(BigDecimal valoreRimborso) { 
        this.valoreRimborso = valoreRimborso; 
    }
    
    public BigDecimal getFattoreTempo() { 
        return fattoreTempo; 
    }
    
    public void setFattoreTempo(BigDecimal fattoreTempo) { 
        this.fattoreTempo = fattoreTempo; 
    }
    
    public BigDecimal getFattoreAnnualizzazione() { 
        return fattoreAnnualizzazione; 
    }
    
    public void setFattoreAnnualizzazione(BigDecimal fattoreAnnualizzazione) { 
        this.fattoreAnnualizzazione = fattoreAnnualizzazione; 
    }
    
    @Override
    public String toString() {
        return "RisultatoCalcoloDTO{" +
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
                ", nominale=" + nominale +
                ", prezzoAcquistoPercentuale=" + prezzoAcquistoPercentuale +
                ", rendimentoSenzaCosti=" + rendimentoSenzaCosti +
                ", rendimentoConCommissioni=" + rendimentoConCommissioni +
                ", rendimentoConBollo=" + rendimentoConBollo +
                ", periodicitaBollo=" + periodicitaBollo +
                ", rendimentoPlusvalenzaEsente=" + rendimentoPlusvalenzaEsente +
                '}';
    }
}