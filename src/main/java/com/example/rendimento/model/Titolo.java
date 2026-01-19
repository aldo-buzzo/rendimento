package com.example.rendimento.model;

import com.example.rendimento.enums.TipoTitolo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Classe entità JPA che rappresenta la tabella Titolo nel database.
 * Implementa il controllo ottimistico della concorrenza tramite il campo version.
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_titolo", length = 50)
    private TipoTitolo tipoTitolo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utente", nullable = false)
    private Utente utente;
    
    @Version
    @Column(name = "version")
    private Long version;

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
     * @param tipoTitolo il tipo di titolo
     */
    public Titolo(String nome, String codiceIsin, LocalDate dataScadenza, 
                 BigDecimal tassoNominale, String periodicitaCedole, String periodicitaBollo,
                 TipoTitolo tipoTitolo) {
        this.nome = nome;
        this.codiceIsin = codiceIsin;
        this.dataScadenza = dataScadenza;
        this.tassoNominale = tassoNominale;
        this.periodicitaCedole = periodicitaCedole;
        this.periodicitaBollo = periodicitaBollo;
        this.tipoTitolo = tipoTitolo;
    }
    
    /**
     * Costruttore con parametri incluso l'utente.
     *
     * @param nome il nome del titolo
     * @param codiceIsin il codice ISIN univoco
     * @param dataScadenza la data di scadenza
     * @param tassoNominale il tasso nominale annuale (%)
     * @param periodicitaCedole la frequenza delle cedole
     * @param periodicitaBollo la frequenza del bollo
     * @param tipoTitolo il tipo di titolo
     * @param utente l'utente proprietario del titolo
     */
    public Titolo(String nome, String codiceIsin, LocalDate dataScadenza, 
                 BigDecimal tassoNominale, String periodicitaCedole, String periodicitaBollo,
                 TipoTitolo tipoTitolo, Utente utente) {
        this.nome = nome;
        this.codiceIsin = codiceIsin;
        this.dataScadenza = dataScadenza;
        this.tassoNominale = tassoNominale;
        this.periodicitaCedole = periodicitaCedole;
        this.periodicitaBollo = periodicitaBollo;
        this.tipoTitolo = tipoTitolo;
        this.utente = utente;
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
    
    public Utente getUtente() {
        return utente;
    }
    
    public void setUtente(Utente utente) {
        this.utente = utente;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
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
                ", tipoTitolo='" + tipoTitolo + '\'' +
                ", utente=" + (utente != null ? utente.getIdUtente() : null) +
                ", version=" + version +
                '}';
    }
}
