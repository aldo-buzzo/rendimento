package com.example.rendimento.dto;

/**
 * DTO per l'importazione di un titolo da Borsa Italiana.
 * Contiene solo i campi necessari per l'importazione: codice ISIN e tipo titolo.
 */
public class TitoloImportDTO {
    
    private String codiceIsin;
    private String tipoTitolo;
    
    // Costruttori
    public TitoloImportDTO() {
    }
    
    public TitoloImportDTO(String codiceIsin, String tipoTitolo) {
        this.codiceIsin = codiceIsin;
        this.tipoTitolo = tipoTitolo;
    }
    
    // Getter e setter
    public String getCodiceIsin() {
        return codiceIsin;
    }
    
    public void setCodiceIsin(String codiceIsin) {
        this.codiceIsin = codiceIsin;
    }
    
    public String getTipoTitolo() {
        return tipoTitolo;
    }
    
    public void setTipoTitolo(String tipoTitolo) {
        this.tipoTitolo = tipoTitolo;
    }
    
    @Override
    public String toString() {
        return "TitoloImportDTO [codiceIsin=" + codiceIsin + ", tipoTitolo=" + tipoTitolo + "]";
    }
}
