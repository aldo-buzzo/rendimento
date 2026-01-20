package com.example.rendimento.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Classe entità JPA che rappresenta la tabella Simulazione nel database.
 * Implementa il controllo ottimistico della concorrenza tramite il campo version.
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
    private BigDecimal prezzoAcquisto;

    @Column(name = "rendimento_lordo", nullable = false, precision = 10, scale = 4)
    private BigDecimal rendimentoLordo;

    @Column(name = "rendimento_tassato", nullable = false, precision = 10, scale = 4)
    private BigDecimal rendimentoTassato;

    @Column(name = "commissioni_acquisto", nullable = false, precision = 10, scale = 6)
    private BigDecimal commissioniAcquisto;

    @Column(name = "rendimento_netto_cedole", nullable = false, precision = 10, scale = 4)
    private BigDecimal rendimentoNettoCedole;

    @Column(name = "imposta_bollo", nullable = false, precision = 10, scale = 6)
    private BigDecimal impostaBollo;

    @Column(name = "rendimento_netto_bollo", nullable = false, precision = 10, scale = 4)
    private BigDecimal rendimentoNettoBollo;

    @Column(name = "plus_minus_valenza", nullable = false, precision = 10, scale = 4)
    private BigDecimal plusMinusValenza;
    
    @Version
    @Column(name = "version")
    private Long version;

    // Nuovi campi per il calcolo avanzato dei rendimenti
    
    @Column(name = "nominale", precision = 10, scale = 2)
    private BigDecimal nominale;

    @Column(name = "prezzo_riferimento_bollo", precision = 10, scale = 4)
    private BigDecimal prezzoRiferimentoBollo;

    @Column(name = "capitale_investito", precision = 10, scale = 2)
    private BigDecimal capitaleInvestito;

    @Column(name = "capitale_con_commissioni", precision = 10, scale = 2)
    private BigDecimal capitaleConCommissioni;

    @Column(name = "cedole_nette_annue", precision = 10, scale = 4)
    private BigDecimal cedoleNetteAnnue;

    @Column(name = "guadagno_netto_senza_costi", precision = 10, scale = 4)
    private BigDecimal guadagnoNettoSenzaCosti;

    @Column(name = "rendimento_senza_costi", precision = 10, scale = 6)
    private BigDecimal rendimentoSenzaCosti;

    @Column(name = "rendimento_con_commissioni", precision = 10, scale = 6)
    private BigDecimal rendimentoConCommissioni;

    @Column(name = "rendimento_con_bollo_mensile", precision = 10, scale = 6)
    private BigDecimal rendimentoConBolloMensile;

    @Column(name = "rendimento_con_bollo_annuale", precision = 10, scale = 6)
    private BigDecimal rendimentoConBolloAnnuale;

    @Column(name = "bollo_totale_mensile", precision = 10, scale = 4)
    private BigDecimal bolloTotaleMensile;

    @Column(name = "bollo_totale_annuale", precision = 10, scale = 4)
    private BigDecimal bolloTotaleAnnuale;

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
    public Simulazione(Titolo titolo, LocalDate dataAcquisto, BigDecimal prezzoAcquisto,
                      BigDecimal rendimentoLordo, BigDecimal rendimentoTassato, BigDecimal commissioniAcquisto,
                      BigDecimal rendimentoNettoCedole, BigDecimal impostaBollo, BigDecimal rendimentoNettoBollo,
                      BigDecimal plusMinusValenza) {
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

    // Getter e Setter per i campi esistenti

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

    public BigDecimal getRendimentoConBolloAnnuale() {
        return rendimentoConBolloAnnuale;
    }

    public void setRendimentoConBolloAnnuale(BigDecimal rendimentoConBolloAnnuale) {
        this.rendimentoConBolloAnnuale = rendimentoConBolloAnnuale;
    }

    public BigDecimal getBolloTotaleMensile() {
        return bolloTotaleMensile;
    }

    public void setBolloTotaleMensile(BigDecimal bolloTotaleMensile) {
        this.bolloTotaleMensile = bolloTotaleMensile;
    }

    public BigDecimal getBolloTotaleAnnuale() {
        return bolloTotaleAnnuale;
    }

    public void setBolloTotaleAnnuale(BigDecimal bolloTotaleAnnuale) {
        this.bolloTotaleAnnuale = bolloTotaleAnnuale;
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
                ", nominale=" + nominale +
                ", prezzoRiferimentoBollo=" + prezzoRiferimentoBollo +
                ", capitaleInvestito=" + capitaleInvestito +
                ", capitaleConCommissioni=" + capitaleConCommissioni +
                ", cedoleNetteAnnue=" + cedoleNetteAnnue +
                ", guadagnoNettoSenzaCosti=" + guadagnoNettoSenzaCosti +
                ", rendimentoSenzaCosti=" + rendimentoSenzaCosti +
                ", rendimentoConCommissioni=" + rendimentoConCommissioni +
                ", rendimentoConBolloMensile=" + rendimentoConBolloMensile +
                ", rendimentoConBolloAnnuale=" + rendimentoConBolloAnnuale +
                ", bolloTotaleMensile=" + bolloTotaleMensile +
                ", bolloTotaleAnnuale=" + bolloTotaleAnnuale +
                ", version=" + version +
                '}';
    }
}
