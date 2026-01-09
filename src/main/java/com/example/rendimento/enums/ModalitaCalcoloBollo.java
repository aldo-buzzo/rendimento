package com.example.rendimento.enums;

/**
 * Enum che rappresenta le modalità di calcolo dell'imposta di bollo.
 */
public enum ModalitaCalcoloBollo {
    /**
     * Calcolo annuale: l'imposta si calcola solo se la scadenza è successiva al 31 dicembre dell'anno corrente.
     */
    ANNUALE,
    
    /**
     * Calcolo mensile: l'imposta si calcola proporzionalmente per ogni mese mancante alla scadenza.
     */
    MENSILE
}