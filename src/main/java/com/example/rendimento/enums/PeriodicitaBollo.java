package com.example.rendimento.enums;

/**
 * Enum che rappresenta la periodicità del bollo.
 */
public enum PeriodicitaBollo {
    ANNUALE("Annuale"),
    SEMESTRALE("Semestrale"),
    TRIMESTRALE("Trimestrale");
    
    private final String descrizione;
    
    PeriodicitaBollo(String descrizione) {
        this.descrizione = descrizione;
    }
    
    public String getDescrizione() {
        return descrizione;
    }
}