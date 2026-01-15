package com.example.rendimento.enums;

/**
 * Enumerazione che rappresenta i tipi di titolo supportati.
 */
public enum TipoTitolo {
    BTP("Buono del Tesoro Poliennale"),
    BOT("Buono Ordinario del Tesoro");
    
    private final String descrizione;
    
    TipoTitolo(String descrizione) {
        this.descrizione = descrizione;
    }
    
    public String getDescrizione() {
        return descrizione;
    }
}