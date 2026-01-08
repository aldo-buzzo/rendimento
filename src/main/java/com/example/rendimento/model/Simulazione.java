package com.example.rendimento.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Classe entità JPA che rappresenta la tabella Simulazione nel database.
 */
@Entity
@Table(name = "simulazione")
public class Simulazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simulazione")
    private Integer idSimulazione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_titolo", nullable = false)
    private Titolo titolo;

    @Column(name = "data_acquisto", nullable = false)
    private LocalDate dataAcquisto;

    @Column(name = "prezzo_acquisto", nullable = false, precision = 10, scale = 4)
    private Double prezzoAcquisto;

    @Column(name = "rendimento_lordo", nullable = false, precision = 10, scale = 4)
    private Double rendimentoLordo;

    @Column(name = "rendimento_tassato", nullable = false, precision = 10, scale = 4)
    private Double rendimentoTassato;

    @Column(name = "commissioni_acquisto", nullable = false, precision = 10, scale = 6)
    private Double commissioniAcquisto;

    @Column(name = "rendimento_netto_cedole", nullable = false, precision = 10, scale = 4)
    private Double rendimentoNettoCedole;

    @Column(name = "imposta_bollo", nullable = false, precision = 10, scale = 6)
    private Double impostaBollo;

    @Column(name = "rendimento_netto_bollo", nullable = false, precision = 10, scale = 4)
    private Double rendimentoNettoBollo;

    @Column(name = "plus_minus_valenza", nullable = false, precision = 10, scale = 4)
    private Double plusMinusValenza;

    /**
     * Costruttore predefinito richiesto da JPA.
     */
    public Simulazione() {
    }

    /**
     * Costruttore con parametri per creare una nuova istanza di Simulazione.
     *
     * @param titolo il titolo associato alla simulazione
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
    public Simulazione(Titolo titolo, LocalDate dataAcquisto, Double prezzoAcquisto,
                      Double rendimentoLordo, Double rendimentoTassato, Double commissioniAcquisto,
                      Double rendimentoNettoCedole, Double impostaBollo, Double rendimentoNettoBollo,
                      Double plusMinusValenza) {
        this.titolo = titolo;
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

    public Titolo getTitolo() {
        return titolo;
    }

    public void setTitolo(Titolo titolo) {
        this.titolo = titolo;
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
        return "Simulazione{" +
                "idSimulazione=" + idSimulazione +
                ", titolo=" + (titolo != null ? titolo.getIdTitolo() : null) +
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