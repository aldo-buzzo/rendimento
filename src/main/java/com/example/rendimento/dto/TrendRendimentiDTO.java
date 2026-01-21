package com.example.rendimento.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO per rappresentare i dati dei trend di rendimento per un determinato periodo.
 * Contiene sia le statistiche (rendimento minimo, medio, massimo) che la lista dei titoli
 * con i loro rendimenti.
 */
public class TrendRendimentiDTO {
    
    private BigDecimal rendimentoMinimo;
    private BigDecimal rendimentoMedio;
    private BigDecimal rendimentoMassimo;
    private List<TitoloRendimentoDTO> titoli;
    
    /**
     * Costruttore predefinito.
     */
    public TrendRendimentiDTO() {
    }
    
    /**
     * Costruttore con tutti i parametri.
     * 
     * @param rendimentoMinimo il rendimento minimo
     * @param rendimentoMedio il rendimento medio
     * @param rendimentoMassimo il rendimento massimo
     * @param titoli la lista dei titoli con i loro rendimenti
     */
    public TrendRendimentiDTO(BigDecimal rendimentoMinimo, BigDecimal rendimentoMedio, 
                             BigDecimal rendimentoMassimo, List<TitoloRendimentoDTO> titoli) {
        this.rendimentoMinimo = rendimentoMinimo;
        this.rendimentoMedio = rendimentoMedio;
        this.rendimentoMassimo = rendimentoMassimo;
        this.titoli = titoli;
    }
    
    // Getter e Setter
    
    public BigDecimal getRendimentoMinimo() {
        return rendimentoMinimo;
    }
    
    public void setRendimentoMinimo(BigDecimal rendimentoMinimo) {
        this.rendimentoMinimo = rendimentoMinimo;
    }
    
    public BigDecimal getRendimentoMedio() {
        return rendimentoMedio;
    }
    
    public void setRendimentoMedio(BigDecimal rendimentoMedio) {
        this.rendimentoMedio = rendimentoMedio;
    }
    
    public BigDecimal getRendimentoMassimo() {
        return rendimentoMassimo;
    }
    
    public void setRendimentoMassimo(BigDecimal rendimentoMassimo) {
        this.rendimentoMassimo = rendimentoMassimo;
    }
    
    public List<TitoloRendimentoDTO> getTitoli() {
        return titoli;
    }
    
    public void setTitoli(List<TitoloRendimentoDTO> titoli) {
        this.titoli = titoli;
    }
    
    /**
     * DTO interno per rappresentare un titolo con i suoi rendimenti.
     */
    public static class TitoloRendimentoDTO {
        
        private Integer idTitolo;
        private String nome;
        private String codiceIsin;
        private BigDecimal rendimentoBolloMensile;
        private BigDecimal rendimentoBolloAnnuale;
        
        /**
         * Costruttore predefinito.
         */
        public TitoloRendimentoDTO() {
        }
        
        /**
         * Costruttore con tutti i parametri.
         * 
         * @param idTitolo l'ID del titolo
         * @param nome il nome del titolo
         * @param codiceIsin il codice ISIN del titolo
         * @param rendimentoBolloMensile il rendimento con bollo mensile
         * @param rendimentoBolloAnnuale il rendimento con bollo annuale
         */
        public TitoloRendimentoDTO(Integer idTitolo, String nome, String codiceIsin, 
                                  BigDecimal rendimentoBolloMensile, BigDecimal rendimentoBolloAnnuale) {
            this.idTitolo = idTitolo;
            this.nome = nome;
            this.codiceIsin = codiceIsin;
            this.rendimentoBolloMensile = rendimentoBolloMensile;
            this.rendimentoBolloAnnuale = rendimentoBolloAnnuale;
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
        
        public BigDecimal getRendimentoBolloMensile() {
            return rendimentoBolloMensile;
        }
        
        public void setRendimentoBolloMensile(BigDecimal rendimentoBolloMensile) {
            this.rendimentoBolloMensile = rendimentoBolloMensile;
        }
        
        public BigDecimal getRendimentoBolloAnnuale() {
            return rendimentoBolloAnnuale;
        }
        
        public void setRendimentoBolloAnnuale(BigDecimal rendimentoBolloAnnuale) {
            this.rendimentoBolloAnnuale = rendimentoBolloAnnuale;
        }
    }
}
