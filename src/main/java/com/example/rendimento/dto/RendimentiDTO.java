package com.example.rendimento.dto;

import java.util.List;

/**
 * DTO per rappresentare i rendimenti calcolati
 */
public class RendimentiDTO {
    
    private double rendimentoMinimo;
    private double rendimentoMedio;
    private double rendimentoMassimo;
    private List<TitoloRendimentoDTO> titoli;
    
    public RendimentiDTO() {
    }
    
    public RendimentiDTO(double rendimentoMinimo, double rendimentoMedio, double rendimentoMassimo, List<TitoloRendimentoDTO> titoli) {
        this.rendimentoMinimo = rendimentoMinimo;
        this.rendimentoMedio = rendimentoMedio;
        this.rendimentoMassimo = rendimentoMassimo;
        this.titoli = titoli;
    }
    
    public double getRendimentoMinimo() {
        return rendimentoMinimo;
    }
    
    public void setRendimentoMinimo(double rendimentoMinimo) {
        this.rendimentoMinimo = rendimentoMinimo;
    }
    
    public double getRendimentoMedio() {
        return rendimentoMedio;
    }
    
    public void setRendimentoMedio(double rendimentoMedio) {
        this.rendimentoMedio = rendimentoMedio;
    }
    
    public double getRendimentoMassimo() {
        return rendimentoMassimo;
    }
    
    public void setRendimentoMassimo(double rendimentoMassimo) {
        this.rendimentoMassimo = rendimentoMassimo;
    }
    
    public List<TitoloRendimentoDTO> getTitoli() {
        return titoli;
    }
    
    public void setTitoli(List<TitoloRendimentoDTO> titoli) {
        this.titoli = titoli;
    }
    
    /**
     * DTO interno per rappresentare un titolo con i suoi rendimenti
     */
    public static class TitoloRendimentoDTO {
        private String nome;
        private double rendimentoTrimestrale;
        private double rendimentoAnnuale;
        private double rendimentoBolloMensile;
        private double rendimentoBolloAnnuale;
        
        public TitoloRendimentoDTO() {
        }
        
        public TitoloRendimentoDTO(String nome, double rendimentoTrimestrale, double rendimentoAnnuale, 
                                  double rendimentoBolloMensile, double rendimentoBolloAnnuale) {
            this.nome = nome;
            this.rendimentoTrimestrale = rendimentoTrimestrale;
            this.rendimentoAnnuale = rendimentoAnnuale;
            this.rendimentoBolloMensile = rendimentoBolloMensile;
            this.rendimentoBolloAnnuale = rendimentoBolloAnnuale;
        }
        
        public String getNome() {
            return nome;
        }
        
        public void setNome(String nome) {
            this.nome = nome;
        }
        
        public double getRendimentoTrimestrale() {
            return rendimentoTrimestrale;
        }
        
        public void setRendimentoTrimestrale(double rendimentoTrimestrale) {
            this.rendimentoTrimestrale = rendimentoTrimestrale;
        }
        
        public double getRendimentoAnnuale() {
            return rendimentoAnnuale;
        }
        
        public void setRendimentoAnnuale(double rendimentoAnnuale) {
            this.rendimentoAnnuale = rendimentoAnnuale;
        }
        
        public double getRendimentoBolloMensile() {
            return rendimentoBolloMensile;
        }
        
        public void setRendimentoBolloMensile(double rendimentoBolloMensile) {
            this.rendimentoBolloMensile = rendimentoBolloMensile;
        }
        
        public double getRendimentoBolloAnnuale() {
            return rendimentoBolloAnnuale;
        }
        
        public void setRendimentoBolloAnnuale(double rendimentoBolloAnnuale) {
            this.rendimentoBolloAnnuale = rendimentoBolloAnnuale;
        }
    }
}
