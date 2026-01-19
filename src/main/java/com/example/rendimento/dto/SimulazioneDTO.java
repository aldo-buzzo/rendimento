package com.example.rendimento.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) per l'entità Simulazione.
 * Utilizzato per trasferire dati tra il livello di servizio e il livello di presentazione.
 */
public class SimulazioneDTO {

    private Integer idSimulazione;
    private Integer idTitolo;
    private TitoloDTO titolo;
    private LocalDate dataAcquisto;
    private BigDecimal prezzoAcquisto;
    private BigDecimal rendimentoLordo;
    private BigDecimal rendimentoTassato;
    private BigDecimal commissioniAcquisto;
    private BigDecimal rendimentoNettoCedole;
    private BigDecimal impostaBollo;
    private BigDecimal rendimentoNettoBollo;
    private BigDecimal plusMinusValenza;
    private Long version;
    
    // Nuovi campi per il calcolo avanzato dei rendimenti
    private BigDecimal nominale;
    private BigDecimal prezzoRiferimentoBollo;
    private BigDecimal capitaleInvestito;
    private BigDecimal capitaleConCommissioni;
    private BigDecimal cedoleNetteAnnue;
    private BigDecimal guadagnoNettoSenzaCosti;
    private BigDecimal rendimentoSenzaCosti;
    private BigDecimal rendimentoConCommissioni;
    private BigDecimal rendimentoConBolloMensile;
    private BigDecimal bolloTotaleMensile;

    /**
     * Costruttore predefinito.
     */
    public SimulazioneDTO() {
    }

    /**
     * Costruttore con parametri.
     *
     * @param idSimulazione l'ID della simulazione
     * @param idTitolo l'ID del titolo associato
     * @param dataAcquisto la data ipotetica di acquisto
     * @param prezzoAcquisto il prezzo di acquisto per unità nominale
     * @param rendimentoLordo il rendimento lordo (cedole + plusvalenza/minusvalenza)
     * @param rendimentoTassato il rendimento netto dopo tassazione
     * @param commissioniAcquisto le commissioni di acquisto
     * @param rendimentoNettoCedole il rendimento netto dopo tasse e commissioni
     * @param impostaBollo l'imposta di bollo
     * @param rendimentoNettoBollo il rendimento netto finale al netto del bollo
     * @param plusMinusValenza la differenza tra prezzo di acquisto e 100
     */
    public SimulazioneDTO(Integer idSimulazione, Integer idTitolo, LocalDate dataAcquisto,
                         BigDecimal prezzoAcquisto, BigDecimal rendimentoLordo, BigDecimal rendimentoTassato,
                         BigDecimal commissioniAcquisto, BigDecimal rendimentoNettoCedole,
                         BigDecimal impostaBollo, BigDecimal rendimentoNettoBollo, BigDecimal plusMinusValenza) {
        this.idSimulazione = idSimulazione;
        this.idTitolo = idTitolo;
        this.dataAcquisto = dataAcquisto;
        this.prezzoAcquisto = prezzoAcquisto;
        this.rendimentoLordo = rendimentoLordo;
        this.rendimentoTassato = rendimentoTassato;
        this.commissioniAcquisto = commissioniAcquisto;
        this.rendimentoNettoCedole = rendimentoNettoCedole;
        this.impostaBollo = impostaBollo;
        this.rendimentoNettoBollo = rendimentoNettoBollo;
        this.plusMinusValenza = plusMinusValenza;
    }

    /**
     * Costruttore con parametri incluso l'oggetto TitoloDTO.
     *
     * @param idSimulazione l'ID della simulazione
     * @param titolo l'oggetto TitoloDTO associato
     * @param dataAcquisto la data ipotetica di acquisto
     * @param prezzoAcquisto il prezzo di acquisto per unità nominale
     * @param rendimentoLordo il rendimento lordo (cedole + plusvalenza/minusvalenza)
     * @param rendimentoTassato il rendimento netto dopo tassazione
     * @param commissioniAcquisto le commissioni di acquisto
     * @param rendimentoNettoCedole il rendimento netto dopo tasse e commissioni
     * @param impostaBollo l'imposta di bollo
     * @param rendimentoNettoBollo il rendimento netto finale al netto del bollo
     * @param plusMinusValenza la differenza tra prezzo di acquisto e 100
     */
    public SimulazioneDTO(Integer idSimulazione, TitoloDTO titolo, LocalDate dataAcquisto,
                         BigDecimal prezzoAcquisto, BigDecimal rendimentoLordo, BigDecimal rendimentoTassato,
                         BigDecimal commissioniAcquisto, BigDecimal rendimentoNettoCedole,
                         BigDecimal impostaBollo, BigDecimal rendimentoNettoBollo, BigDecimal plusMinusValenza) {
        this.idSimulazione = idSimulazione;
        this.titolo = titolo;
        this.idTitolo = titolo != null ? titolo.getIdTitolo() : null;
        this.dataAcquisto = dataAcquisto;
        this.prezzoAcquisto = prezzoAcquisto;
        this.rendimentoLordo = rendimentoLordo;
        this.rendimentoTassato = rendimentoTassato;
        this.commissioniAcquisto = commissioniAcquisto;
        this.rendimentoNettoCedole = rendimentoNettoCedole;
        this.impostaBollo = impostaBollo;
        this.rendimentoNettoBollo = rendimentoNettoBollo;
        this.plusMinusValenza = plusMinusValenza;
    }

    // Getter e Setter per i campi esistenti

    public Integer getIdSimulazione() {
        return idSimulazione;
    }

    public void setIdSimulazione(Integer idSimulazione) {
        this.idSimulazione = idSimulazione;
    }

    public Integer getIdTitolo() {
        return idTitolo;
    }

    public void setIdTitolo(Integer idTitolo) {
        this.idTitolo = idTitolo;
    }

    public TitoloDTO getTitolo() {
        return titolo;
    }

    public void setTitolo(TitoloDTO titolo) {
        this.titolo = titolo;
        this.idTitolo = titolo != null ? titolo.getIdTitolo() : null;
    }

    public LocalDate getDataAcquisto() {
        return dataAcquisto;
    }

    public void setDataAcquisto(LocalDate dataAcquisto) {
        this.dataAcquisto = dataAcquisto;
    }

    public BigDecimal getPrezzoAcquisto() {
        return prezzoAcquisto;
    }

    public void setPrezzoAcquisto(BigDecimal prezzoAcquisto) {
        this.prezzoAcquisto = prezzoAcquisto;
    }

    public BigDecimal getRendimentoLordo() {
        return rendimentoLordo;
    }

    public void setRendimentoLordo(BigDecimal rendimentoLordo) {
        this.rendimentoLordo = rendimentoLordo;
    }

    public BigDecimal getRendimentoTassato() {
        return rendimentoTassato;
    }

    public void setRendimentoTassato(BigDecimal rendimentoTassato) {
        this.rendimentoTassato = rendimentoTassato;
    }

    public BigDecimal getCommissioniAcquisto() {
        return commissioniAcquisto;
    }

    public void setCommissioniAcquisto(BigDecimal commissioniAcquisto) {
        this.commissioniAcquisto = commissioniAcquisto;
    }

    public BigDecimal getRendimentoNettoCedole() {
        return rendimentoNettoCedole;
    }

    public void setRendimentoNettoCedole(BigDecimal rendimentoNettoCedole) {
        this.rendimentoNettoCedole = rendimentoNettoCedole;
    }

    public BigDecimal getImpostaBollo() {
        return impostaBollo;
    }

    public void setImpostaBollo(BigDecimal impostaBollo) {
        this.impostaBollo = impostaBollo;
    }

    public BigDecimal getRendimentoNettoBollo() {
        return rendimentoNettoBollo;
    }

    public void setRendimentoNettoBollo(BigDecimal rendimentoNettoBollo) {
        this.rendimentoNettoBollo = rendimentoNettoBollo;
    }

    public BigDecimal getPlusMinusValenza() {
        return plusMinusValenza;
    }

    public void setPlusMinusValenza(BigDecimal plusMinusValenza) {
        this.plusMinusValenza = plusMinusValenza;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    // Getter e Setter per i nuovi campi
    
    public BigDecimal getNominale() {
        return nominale;
    }

    public void setNominale(BigDecimal nominale) {
        this.nominale = nominale;
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

    public BigDecimal getRendimentoConBolloMensile() {
        return rendimentoConBolloMensile;
    }

    public void setRendimentoConBolloMensile(BigDecimal rendimentoConBolloMensile) {
        this.rendimentoConBolloMensile = rendimentoConBolloMensile;
    }

    public BigDecimal getBolloTotaleMensile() {
        return bolloTotaleMensile;
    }

    public void setBolloTotaleMensile(BigDecimal bolloTotaleMensile) {
        this.bolloTotaleMensile = bolloTotaleMensile;
    }

    @Override
    public String toString() {
        return "SimulazioneDTO{" +
                "idSimulazione=" + idSimulazione +
                ", idTitolo=" + idTitolo +
                ", dataAcquisto=" + dataAcquisto +
                ", prezzoAcquisto=" + prezzoAcquisto +
                ", rendimentoLordo=" + rendimentoLordo +
                ", rendimentoTassato=" + rendimentoTassato +
                ", commissioniAcquisto=" + commissioniAcquisto +
                ", rendimentoNettoCedole=" + rendimentoNettoCedole +
                ", impostaBollo=" + impostaBollo +
                ", rendimentoNettoBollo=" + rendimentoNettoBollo +
                ", plusMinusValenza=" + plusMinusValenza +
                ", nominale=" + nominale +
                ", prezzoRiferimentoBollo=" + prezzoRiferimentoBollo +
                ", capitaleInvestito=" + capitaleInvestito +
                ", capitaleConCommissioni=" + capitaleConCommissioni +
                ", cedoleNetteAnnue=" + cedoleNetteAnnue +
                ", guadagnoNettoSenzaCosti=" + guadagnoNettoSenzaCosti +
                ", rendimentoSenzaCosti=" + rendimentoSenzaCosti +
                ", rendimentoConCommissioni=" + rendimentoConCommissioni +
                ", rendimentoConBolloMensile=" + rendimentoConBolloMensile +
                ", bolloTotaleMensile=" + bolloTotaleMensile +
                ", version=" + version +
                '}';
    }
}
