package com.example.rendimento.enums;

/**
 * Enum che rappresenta la periodicità delle cedole.
 */
public enum PeriodicitaCedole {
    ANNUALE("Annuale"),
    SEMESTRALE("Semestrale"),
    TRIMESTRALE("Trimestrale"),
    MENSILE("Mensile");
    
    private final String descrizione;
    
    PeriodicitaCedole(String descrizione) {
        this.descrizione = descrizione;
    }
    
    public String getDescrizione() {
        return descrizione;
    }
}