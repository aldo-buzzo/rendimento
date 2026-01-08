package com.example.rendimento.dto;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) per l'entità Titolo.
 * Utilizzato per trasferire dati tra il livello di servizio e il livello di presentazione.
 */
public class TitoloDTO {

    private Integer idTitolo;
    private String nome;
    private String codiceIsin;
    private LocalDate dataScadenza;
    private Double tassoNominale;
    private String periodicitaCedole;
    private String periodicitaBollo;

    /**
     * Costruttore predefinito.
     */
    public TitoloDTO() {
    }

    /**
     * Costruttore con parametri.
     *
     * @param idTitolo l'ID del titolo
     * @param nome il nome del titolo
     * @param codiceIsin il codice ISIN del titolo
     * @param dataScadenza la data di scadenza del titolo
     * @param tassoNominale il tasso nominale annuale (%)
     * @param periodicitaCedole la frequenza delle cedole
     * @param periodicitaBollo la frequenza del bollo
     */
    public TitoloDTO(Integer idTitolo, String nome, String codiceIsin, LocalDate dataScadenza,
                    Double tassoNominale, String periodicitaCedole, String periodicitaBollo) {
        this.idTitolo = idTitolo;
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

    public Double getTassoNominale() {
        return tassoNominale;
    }

    public void setTassoNominale(Double tassoNominale) {
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
        return "TitoloDTO{" +
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