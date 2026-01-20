package com.example.rendimento.dto;
import java.math.BigDecimal;

public class RisultatoRendimentoAdvancedDTO extends RisultatoSimulazioneDTO {

    private BigDecimal nominale;
    private BigDecimal prezzoAcquistoPercentuale;
    private BigDecimal cedolaAnnua;
    private BigDecimal anniDurata;
    private BigDecimal commissionRate;
    private BigDecimal prezzoRiferimentoBollo;

    private BigDecimal capitaleInvestito;
    private BigDecimal capitaleConCommissioni;
    private BigDecimal cedoleNetteAnnue;
    private BigDecimal guadagnoNettoSenzaCosti;

    private BigDecimal bolloTotaleAnnuale;
    private BigDecimal bolloTotaleMensile;

    private BigDecimal rendimentoSenzaCosti;
    private BigDecimal rendimentoConCommissioni;
    private BigDecimal rendimentoConCommissioniEBolloAnnuale;
    private BigDecimal rendimentoConCommissioniEBolloMensile;

    public RisultatoRendimentoAdvancedDTO() {
        super();
    }

    public RisultatoRendimentoAdvancedDTO(RisultatoSimulazioneDTO legacy) {
        super(
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

    // Getter / Setter dei campi nuovi (advanced)
    public BigDecimal getNominale() { return nominale; }
    public void setNominale(BigDecimal nominale) { this.nominale = nominale; }

    public BigDecimal getPrezzoAcquistoPercentuale() { return prezzoAcquistoPercentuale; }
    public void setPrezzoAcquistoPercentuale(BigDecimal prezzoAcquistoPercentuale) { this.prezzoAcquistoPercentuale = prezzoAcquistoPercentuale; }

    public BigDecimal getCedolaAnnua() { return cedolaAnnua; }
    public void setCedolaAnnua(BigDecimal cedolaAnnua) { this.cedolaAnnua = cedolaAnnua; }

    public BigDecimal getAnniDurata() { return anniDurata; }
    public void setAnniDurata(BigDecimal anniDurata) { this.anniDurata = anniDurata; }

    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }

    public BigDecimal getPrezzoRiferimentoBollo() { return prezzoRiferimentoBollo; }
    public void setPrezzoRiferimentoBollo(BigDecimal prezzoRiferimentoBollo) { this.prezzoRiferimentoBollo = prezzoRiferimentoBollo; }

    public BigDecimal getCapitaleInvestito() { return capitaleInvestito; }
    public void setCapitaleInvestito(BigDecimal capitaleInvestito) { this.capitaleInvestito = capitaleInvestito; }

    public BigDecimal getCapitaleConCommissioni() { return capitaleConCommissioni; }
    public void setCapitaleConCommissioni(BigDecimal capitaleConCommissioni) { this.capitaleConCommissioni = capitaleConCommissioni; }

    public BigDecimal getCedoleNetteAnnue() { return cedoleNetteAnnue; }
    public void setCedoleNetteAnnue(BigDecimal cedoleNetteAnnue) { this.cedoleNetteAnnue = cedoleNetteAnnue; }

    public BigDecimal getGuadagnoNettoSenzaCosti() { return guadagnoNettoSenzaCosti; }
    public void setGuadagnoNettoSenzaCosti(BigDecimal guadagnoNettoSenzaCosti) { this.guadagnoNettoSenzaCosti = guadagnoNettoSenzaCosti; }

    public BigDecimal getBolloTotaleAnnuale() { return bolloTotaleAnnuale; }
    public void setBolloTotaleAnnuale(BigDecimal bolloTotaleAnnuale) { this.bolloTotaleAnnuale = bolloTotaleAnnuale; }

    public BigDecimal getBolloTotaleMensile() { return bolloTotaleMensile; }
    public void setBolloTotaleMensile(BigDecimal bolloTotaleMensile) { this.bolloTotaleMensile = bolloTotaleMensile; }

    public BigDecimal getRendimentoSenzaCosti() { return rendimentoSenzaCosti; }
    public void setRendimentoSenzaCosti(BigDecimal rendimentoSenzaCosti) { this.rendimentoSenzaCosti = rendimentoSenzaCosti; }

    public BigDecimal getRendimentoConCommissioni() { return rendimentoConCommissioni; }
    public void setRendimentoConCommissioni(BigDecimal rendimentoConCommissioni) { this.rendimentoConCommissioni = rendimentoConCommissioni; }

    public BigDecimal getRendimentoConCommissioniEBolloAnnuale() { return rendimentoConCommissioniEBolloAnnuale; }
    public void setRendimentoConCommissioniEBolloAnnuale(BigDecimal rendimentoConCommissioniEBolloAnnuale) { this.rendimentoConCommissioniEBolloAnnuale = rendimentoConCommissioniEBolloAnnuale; }

    public BigDecimal getRendimentoConCommissioniEBolloMensile() { return rendimentoConCommissioniEBolloMensile; }
    public void setRendimentoConCommissioniEBolloMensile(BigDecimal rendimentoConCommissioniEBolloMensile) { this.rendimentoConCommissioniEBolloMensile = rendimentoConCommissioniEBolloMensile; }

}
