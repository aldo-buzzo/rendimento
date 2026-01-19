package com.example.rendimento.dto;

import com.example.rendimento.enums.TipoTitolo;
import java.math.BigDecimal;
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
    private BigDecimal tassoNominale;
    private String periodicitaCedole;
    private String periodicitaBollo;
    private TipoTitolo tipoTitolo;
    private BigDecimal corso;
    private Integer utenteId;

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
                    BigDecimal tassoNominale, String periodicitaCedole, String periodicitaBollo,
                    TipoTitolo tipoTitolo, BigDecimal corso, Integer utenteId) {
        this.idTitolo = idTitolo;
        this.nome = nome;
        this.codiceIsin = codiceIsin;
        this.dataScadenza = dataScadenza;
        this.tassoNominale = tassoNominale;
        this.periodicitaCedole = periodicitaCedole;
        this.periodicitaBollo = periodicitaBollo;
        this.tipoTitolo = tipoTitolo;
        this.corso = corso;
        this.utenteId = utenteId;
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
    
    public TipoTitolo getTipoTitolo() {
        return tipoTitolo;
    }
    
    public void setTipoTitolo(TipoTitolo tipoTitolo) {
        this.tipoTitolo = tipoTitolo;
    }
    
    public BigDecimal getCorso() {
        return corso;
    }
    
    public void setCorso(BigDecimal corso) {
        this.corso = corso;
    }
    
    public Integer getUtenteId() {
        return utenteId;
    }
    
    public void setUtenteId(Integer utenteId) {
        this.utenteId = utenteId;
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
                ", tipoTitolo='" + tipoTitolo + '\'' +
                ", corso=" + corso +
                ", utenteId=" + utenteId +
                '}';
    }
}
