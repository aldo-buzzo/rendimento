package com.example.rendimento.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Classe entità JPA che rappresenta la tabella Titolo nel database.
 */
@Entity
@Table(name = "titolo")
public class Titolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_titolo")
    private Integer idTitolo;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "codice_isin", nullable = false, length = 20, unique = true)
    private String codiceIsin;

    @Column(name = "data_scadenza", nullable = false)
    private LocalDate dataScadenza;

    @Column(name = "tasso_nominale", nullable = false, precision = 5, scale = 2)
    private BigDecimal tassoNominale;

    @Column(name = "periodicita_cedole", nullable = false, length = 20)
    private String periodicitaCedole;

    @Column(name = "periodicita_bollo", nullable = false, length = 20)
    private String periodicitaBollo;

    /**
     * Costruttore predefinito richiesto da JPA.
     */
    public Titolo() {
    }

    /**
     * Costruttore con parametri per creare una nuova istanza di Titolo.
     *
     * @param nome il nome del titolo
     * @param codiceIsin il codice ISIN univoco
     * @param dataScadenza la data di scadenza
     * @param tassoNominale il tasso nominale annuale (%)
     * @param periodicitaCedole la frequenza delle cedole
     * @param periodicitaBollo la frequenza del bollo
     */
    public Titolo(String nome, String codiceIsin, LocalDate dataScadenza, 
                 BigDecimal tassoNominale, String periodicitaCedole, String periodicitaBollo) {
        this.nome = nome;
        this.codiceIsin = codiceIsin;
        this.dataScadenza = dataScadenza;
        this.tassoNominale = tassoNominale;
        this.periodicitaCedole = periodicitaCedole;
        this.periodicitaBollo = periodicitaBollo;
    }

    // Getter e Setter

    public Integer getIdTitolo() {
        return idTitolo;
    }

    public void setIdTitolo(Integer idTitolo) {
        this.idTitolo = idTitolo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodiceIsin() {
        return codiceIsin;
    }

    public void setCodiceIsin(String codiceIsin) {
        this.codiceIsin = codiceIsin;
    }

    public LocalDate getDataScadenza() {
        return dataScadenza;
    }

    public void setDataScadenza(LocalDate dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    public BigDecimal getTassoNominale() {
        return tassoNominale;
    }

    public void setTassoNominale(BigDecimal tassoNominale) {
        this.tassoNominale = tassoNominale;
    }

    public String getPeriodicitaCedole() {
        return periodicitaCedole;
    }

    public void setPeriodicitaCedole(String periodicitaCedole) {
        this.periodicitaCedole = periodicitaCedole;
    }

    public String getPeriodicitaBollo() {
        return periodicitaBollo;
    }

    public void setPeriodicitaBollo(String periodicitaBollo) {
        this.periodicitaBollo = periodicitaBollo;
    }

    @Override
    public String toString() {
        return "Titolo{" +
                "idTitolo=" + idTitolo +
                ", nome='" + nome + '\'' +
                ", codiceIsin='" + codiceIsin + '\'' +
                ", dataScadenza=" + dataScadenza +
                ", tassoNominale=" + tassoNominale +
                ", periodicitaCedole='" + periodicitaCedole + '\'' +
                ", periodicitaBollo='" + periodicitaBollo + '\'' +
                '}';
    }
}