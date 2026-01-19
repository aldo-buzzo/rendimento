package com.example.rendimento.dto;

import java.math.BigDecimal;

/**
 * DTO che contiene i risultati del calcolo avanzato del rendimento di un titolo di Stato.
 * Implementa un modello lineare annualizzato per il calcolo dei rendimenti di titoli di Stato italiani.
 */
public class RisultatoRendimentoAdvancedDTO {
    
    // Parametri di input (per riferimento)
    private BigDecimal nominale;
    private BigDecimal prezzoAcquistoPercentuale;
    private BigDecimal cedolaAnnua;
    private BigDecimal anniDurata;
    private BigDecimal commissionRate;
    private BigDecimal prezzoRiferimentoBollo;
    
    // Calcoli base
    private BigDecimal capitaleInvestito;
    private BigDecimal capitaleConCommissioni;
    private BigDecimal cedoleNetteAnnue;
    private BigDecimal plusvalenzaNetta;
    private BigDecimal guadagnoNettoSenzaCosti;
    
    // Commissioni
    private BigDecimal commissioni;
    
    // Bollo
    private BigDecimal bolloTotaleAnnuale;
    private BigDecimal bolloTotaleMensile;
    
    // I quattro rendimenti richiesti
    private BigDecimal rendimentoSenzaCosti;
    private BigDecimal rendimentoConCommissioni;
    private BigDecimal rendimentoConCommissioniEBolloAnnuale;
    private BigDecimal rendimentoConCommissioniEBolloMensile;
    
    // Altri campi per compatibilità
    private BigDecimal interessiNetti;
    private BigDecimal guadagnoTotale;
    private BigDecimal impostaBollo;
    
    /**
     * Costruttore predefinito.
     */
    public RisultatoRendimentoAdvancedDTO() {
    }
    
    /**
     * Converte questo DTO in un RisultatoSimulazioneDTO standard per compatibilità con il sistema esistente.
     * 
     * @return un RisultatoSimulazioneDTO con i campi compatibili popolati
     */
    public RisultatoSimulazioneDTO toRisultatoSimulazioneDTO() {
        RisultatoSimulazioneDTO risultato = new RisultatoSimulazioneDTO();
        
        risultato.setPlusvalenzaNetta(this.plusvalenzaNetta);
        risultato.setInteressiNetti(this.interessiNetti);
        risultato.setCommissioni(this.commissioni);
        risultato.setGuadagnoTotale(this.guadagnoTotale);
        risultato.setGuadagnoNettoCommissioni(this.guadagnoTotale.subtract(this.commissioni));
        risultato.setImpostaBollo(this.impostaBollo);
        risultato.setGuadagnoNettoBollo(this.guadagnoTotale.subtract(this.impostaBollo));
        risultato.setTasso(this.rendimentoSenzaCosti.multiply(new BigDecimal("100")));
        risultato.setTassoNettoCommissioni(this.rendimentoConCommissioni.multiply(new BigDecimal("100")));
        risultato.setTassoNettoBollo(this.rendimentoConCommissioniEBolloAnnuale.multiply(new BigDecimal("100")));
        risultato.setImportoScadenza(this.nominale.add(this.guadagnoTotale.subtract(this.impostaBollo)));
        risultato.setRendimentoNettoBollo(this.rendimentoConCommissioniEBolloAnnuale.multiply(new BigDecimal("100")));
        
        return risultato;
    }

    // Getter e Setter
    
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

    public BigDecimal getCommissioni() {
        return commissioni;
    }

    public void setCommissioni(BigDecimal commissioni) {
        this.commissioni = commissioni;
    }

    public BigDecimal getBolloTotaleAnnuale() {
        return bolloTotaleAnnuale;
    }

    public void setBolloTotaleAnnuale(BigDecimal bolloTotaleAnnuale) {
        this.bolloTotaleAnnuale = bolloTotaleAnnuale;
    }

    public BigDecimal getBolloTotaleMensile() {
        return bolloTotaleMensile;
    }

    public void setBolloTotaleMensile(BigDecimal bolloTotaleMensile) {
        this.bolloTotaleMensile = bolloTotaleMensile;
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

    public BigDecimal getRendimentoConCommissioniEBolloAnnuale() {
        return rendimentoConCommissioniEBolloAnnuale;
    }

    public void setRendimentoConCommissioniEBolloAnnuale(BigDecimal rendimentoConCommissioniEBolloAnnuale) {
        this.rendimentoConCommissioniEBolloAnnuale = rendimentoConCommissioniEBolloAnnuale;
    }

    public BigDecimal getRendimentoConCommissioniEBolloMensile() {
        return rendimentoConCommissioniEBolloMensile;
    }

    public void setRendimentoConCommissioniEBolloMensile(BigDecimal rendimentoConCommissioniEBolloMensile) {
        this.rendimentoConCommissioniEBolloMensile = rendimentoConCommissioniEBolloMensile;
    }

    public BigDecimal getInteressiNetti() {
        return interessiNetti;
    }

    public void setInteressiNetti(BigDecimal interessiNetti) {
        this.interessiNetti = interessiNetti;
    }

    public BigDecimal getGuadagnoTotale() {
        return guadagnoTotale;
    }

    public void setGuadagnoTotale(BigDecimal guadagnoTotale) {
        this.guadagnoTotale = guadagnoTotale;
    }

    public BigDecimal getImpostaBollo() {
        return impostaBollo;
    }

    public void setImpostaBollo(BigDecimal impostaBollo) {
        this.impostaBollo = impostaBollo;
    }

    @Override
    public String toString() {
        return "RisultatoRendimentoAdvancedDTO{" +
                "nominale=" + nominale +
                ", prezzoAcquistoPercentuale=" + prezzoAcquistoPercentuale +
                ", cedolaAnnua=" + cedolaAnnua +
                ", anniDurata=" + anniDurata +
                ", commissionRate=" + commissionRate +
                ", prezzoRiferimentoBollo=" + prezzoRiferimentoBollo +
                ", capitaleInvestito=" + capitaleInvestito +
                ", capitaleConCommissioni=" + capitaleConCommissioni +
                ", cedoleNetteAnnue=" + cedoleNetteAnnue +
                ", plusvalenzaNetta=" + plusvalenzaNetta +
                ", guadagnoNettoSenzaCosti=" + guadagnoNettoSenzaCosti +
                ", commissioni=" + commissioni +
                ", bolloTotaleAnnuale=" + bolloTotaleAnnuale +
                ", bolloTotaleMensile=" + bolloTotaleMensile +
                ", rendimentoSenzaCosti=" + rendimentoSenzaCosti +
                ", rendimentoConCommissioni=" + rendimentoConCommissioni +
                ", rendimentoConCommissioniEBolloAnnuale=" + rendimentoConCommissioniEBolloAnnuale +
                ", rendimentoConCommissioniEBolloMensile=" + rendimentoConCommissioniEBolloMensile +
                '}';
    }
}
