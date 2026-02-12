package com.example.rendimento.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private BigDecimal rendimentoTassato;
    private BigDecimal commissioniAcquisto;
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
    private BigDecimal rendimentoConBollo;
    private BigDecimal rendimentoPlusvalenzaEsente;
    private String periodicitaBollo;
    
    // Valori finali teorici per le diverse combinazioni
    private BigDecimal valoreBolloAnnualePlusvalenzaNonEsente;  // Valore finale con bollo annuale e plusvalenza non esente
    private BigDecimal valoreBolloAnnualePlusvalenzaEsente;     // Valore finale con bollo annuale e plusvalenza esente (solo per BTP)
    private BigDecimal valoreBolloMensilePlusvalenzaNonEsente;  // Valore finale con bollo mensile e plusvalenza non esente
    private BigDecimal valoreBolloMensilePlusvalenzaEsente;     // Valore finale con bollo mensile e plusvalenza esente (solo per BTP)
    
    // Liste di rendimenti e valori finali per profili
    private List<RendimentiPerProfiloDTO> rendimentiPerProfili = new ArrayList<>();
    private List<ValoriFinaliPerProfiloDTO> valoriFinaliPerProfili = new ArrayList<>();

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
     * @param rendimentoTassato il rendimento netto dopo tassazione
     * @param commissioniAcquisto le commissioni di acquisto
     * @param impostaBollo l'imposta di bollo
     * @param rendimentoNettoBollo il rendimento netto finale al netto del bollo
     * @param plusMinusValenza la differenza tra prezzo di acquisto e 100
     */
    public SimulazioneDTO(Integer idSimulazione, Integer idTitolo, LocalDate dataAcquisto,
                         BigDecimal prezzoAcquisto, BigDecimal rendimentoTassato,
                         BigDecimal commissioniAcquisto, BigDecimal impostaBollo, 
                         BigDecimal rendimentoNettoBollo, BigDecimal plusMinusValenza) {
        this.idSimulazione = idSimulazione;
        this.idTitolo = idTitolo;
        this.dataAcquisto = dataAcquisto;
        this.prezzoAcquisto = prezzoAcquisto;
        this.rendimentoTassato = rendimentoTassato;
        this.commissioniAcquisto = commissioniAcquisto;
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
     * @param rendimentoTassato il rendimento netto dopo tassazione
     * @param commissioniAcquisto le commissioni di acquisto
     * @param impostaBollo l'imposta di bollo
     * @param rendimentoNettoBollo il rendimento netto finale al netto del bollo
     * @param plusMinusValenza la differenza tra prezzo di acquisto e 100
     */
    public SimulazioneDTO(Integer idSimulazione, TitoloDTO titolo, LocalDate dataAcquisto,
                         BigDecimal prezzoAcquisto, BigDecimal rendimentoTassato,
                         BigDecimal commissioniAcquisto, BigDecimal impostaBollo, 
                         BigDecimal rendimentoNettoBollo, BigDecimal plusMinusValenza) {
        this.idSimulazione = idSimulazione;
        this.titolo = titolo;
        this.idTitolo = titolo != null ? titolo.getIdTitolo() : null;
        this.dataAcquisto = dataAcquisto;
        this.prezzoAcquisto = prezzoAcquisto;
        this.rendimentoTassato = rendimentoTassato;
        this.commissioniAcquisto = commissioniAcquisto;
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

    public BigDecimal getRendimentoConBollo() {
        return rendimentoConBollo;
    }

    public void setRendimentoConBollo(BigDecimal rendimentoConBollo) {
        this.rendimentoConBollo = rendimentoConBollo;
    }
    
    public String getPeriodicitaBollo() {
        return periodicitaBollo;
    }
    
    public void setPeriodicitaBollo(String periodicitaBollo) {
        this.periodicitaBollo = periodicitaBollo;
    }
    
    public BigDecimal getRendimentoPlusvalenzaEsente() {
        return rendimentoPlusvalenzaEsente;
    }

    public void setRendimentoPlusvalenzaEsente(BigDecimal rendimentoPlusvalenzaEsente) {
        this.rendimentoPlusvalenzaEsente = rendimentoPlusvalenzaEsente;
    }
    
    public BigDecimal getValoreBolloAnnualePlusvalenzaNonEsente() {
        return valoreBolloAnnualePlusvalenzaNonEsente;
    }

    public void setValoreBolloAnnualePlusvalenzaNonEsente(BigDecimal valoreBolloAnnualePlusvalenzaNonEsente) {
        this.valoreBolloAnnualePlusvalenzaNonEsente = valoreBolloAnnualePlusvalenzaNonEsente;
    }

    public BigDecimal getValoreBolloAnnualePlusvalenzaEsente() {
        return valoreBolloAnnualePlusvalenzaEsente;
    }

    public void setValoreBolloAnnualePlusvalenzaEsente(BigDecimal valoreBolloAnnualePlusvalenzaEsente) {
        this.valoreBolloAnnualePlusvalenzaEsente = valoreBolloAnnualePlusvalenzaEsente;
    }

    public BigDecimal getValoreBolloMensilePlusvalenzaNonEsente() {
        return valoreBolloMensilePlusvalenzaNonEsente;
    }

    public void setValoreBolloMensilePlusvalenzaNonEsente(BigDecimal valoreBolloMensilePlusvalenzaNonEsente) {
        this.valoreBolloMensilePlusvalenzaNonEsente = valoreBolloMensilePlusvalenzaNonEsente;
    }

    public BigDecimal getValoreBolloMensilePlusvalenzaEsente() {
        return valoreBolloMensilePlusvalenzaEsente;
    }

    public void setValoreBolloMensilePlusvalenzaEsente(BigDecimal valoreBolloMensilePlusvalenzaEsente) {
        this.valoreBolloMensilePlusvalenzaEsente = valoreBolloMensilePlusvalenzaEsente;
    }
    
    // Getter e setter per le liste di rendimenti e valori finali per profili
    
    public List<RendimentiPerProfiloDTO> getRendimentiPerProfili() {
        return rendimentiPerProfili;
    }
    
    public void setRendimentiPerProfili(List<RendimentiPerProfiloDTO> rendimentiPerProfili) {
        this.rendimentiPerProfili = rendimentiPerProfili != null ? rendimentiPerProfili : new ArrayList<>();
    }
    
    public List<ValoriFinaliPerProfiloDTO> getValoriFinaliPerProfili() {
        return valoriFinaliPerProfili;
    }
    
    public void setValoriFinaliPerProfili(List<ValoriFinaliPerProfiloDTO> valoriFinaliPerProfili) {
        this.valoriFinaliPerProfili = valoriFinaliPerProfili != null ? valoriFinaliPerProfili : new ArrayList<>();
    }
    
    // Metodi di utilità per aggiungere elementi alle liste
    
    public void addRendimentiPerProfilo(RendimentiPerProfiloDTO rendimentiPerProfilo) {
        if (rendimentiPerProfilo != null) {
            this.rendimentiPerProfili.add(rendimentiPerProfilo);
        }
    }
    
    public void addValoriFinaliPerProfilo(ValoriFinaliPerProfiloDTO valoriFinaliPerProfilo) {
        if (valoriFinaliPerProfilo != null) {
            this.valoriFinaliPerProfili.add(valoriFinaliPerProfilo);
        }
    }

    @Override
    public String toString() {
        return "SimulazioneDTO{" +
                "idSimulazione=" + idSimulazione +
                ", idTitolo=" + idTitolo +
                ", dataAcquisto=" + dataAcquisto +
                ", prezzoAcquisto=" + prezzoAcquisto +
                ", rendimentoTassato=" + rendimentoTassato +
                ", commissioniAcquisto=" + commissioniAcquisto +
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
                ", rendimentoConBollo=" + rendimentoConBollo +
                ", rendimentoPlusvalenzaEsente=" + rendimentoPlusvalenzaEsente +
                ", periodicitaBollo=" + periodicitaBollo +
                ", valoreBolloAnnualePlusvalenzaNonEsente=" + valoreBolloAnnualePlusvalenzaNonEsente +
                ", valoreBolloAnnualePlusvalenzaEsente=" + valoreBolloAnnualePlusvalenzaEsente +
                ", valoreBolloMensilePlusvalenzaNonEsente=" + valoreBolloMensilePlusvalenzaNonEsente +
                ", valoreBolloMensilePlusvalenzaEsente=" + valoreBolloMensilePlusvalenzaEsente +
                ", rendimentiPerProfili=" + (rendimentiPerProfili != null ? rendimentiPerProfili.size() : 0) +
                ", valoriFinaliPerProfili=" + (valoriFinaliPerProfili != null ? valoriFinaliPerProfili.size() : 0) +
                ", version=" + version +
                '}';
    }
}
