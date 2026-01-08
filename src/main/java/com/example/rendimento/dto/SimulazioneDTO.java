package com.example.rendimento.dto;

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
    private Double prezzoAcquisto;
    private Double rendimentoLordo;
    private Double rendimentoTassato;
    private Double commissioniAcquisto;
    private Double rendimentoNettoCedole;
    private Double impostaBollo;
    private Double rendimentoNettoBollo;
    private Double plusMinusValenza;

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
                         Double prezzoAcquisto, Double rendimentoLordo, Double rendimentoTassato,
                         Double commissioniAcquisto, Double rendimentoNettoCedole,
                         Double impostaBollo, Double rendimentoNettoBollo, Double plusMinusValenza) {
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
                         Double prezzoAcquisto, Double rendimentoLordo, Double rendimentoTassato,
                         Double commissioniAcquisto, Double rendimentoNettoCedole,
                         Double impostaBollo, Double rendimentoNettoBollo, Double plusMinusValenza) {
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

    // Getter e Setter

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

    public Double getPrezzoAcquisto() {
        return prezzoAcquisto;
    }

    public void setPrezzoAcquisto(Double prezzoAcquisto) {
        this.prezzoAcquisto = prezzoAcquisto;
    }

    public Double getRendimentoLordo() {
        return rendimentoLordo;
    }

    public void setRendimentoLordo(Double rendimentoLordo) {
        this.rendimentoLordo = rendimentoLordo;
    }

    public Double getRendimentoTassato() {
        return rendimentoTassato;
    }

    public void setRendimentoTassato(Double rendimentoTassato) {
        this.rendimentoTassato = rendimentoTassato;
    }

    public Double getCommissioniAcquisto() {
        return commissioniAcquisto;
    }

    public void setCommissioniAcquisto(Double commissioniAcquisto) {
        this.commissioniAcquisto = commissioniAcquisto;
    }

    public Double getRendimentoNettoCedole() {
        return rendimentoNettoCedole;
    }

    public void setRendimentoNettoCedole(Double rendimentoNettoCedole) {
        this.rendimentoNettoCedole = rendimentoNettoCedole;
    }

    public Double getImpostaBollo() {
        return impostaBollo;
    }

    public void setImpostaBollo(Double impostaBollo) {
        this.impostaBollo = impostaBollo;
    }

    public Double getRendimentoNettoBollo() {
        return rendimentoNettoBollo;
    }

    public void setRendimentoNettoBollo(Double rendimentoNettoBollo) {
        this.rendimentoNettoBollo = rendimentoNettoBollo;
    }

    public Double getPlusMinusValenza() {
        return plusMinusValenza;
    }

    public void setPlusMinusValenza(Double plusMinusValenza) {
        this.plusMinusValenza = plusMinusValenza;
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
                '}';
    }
}