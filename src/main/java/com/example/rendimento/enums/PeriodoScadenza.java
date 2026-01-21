package com.example.rendimento.enums;

/**
 * Enumerazione per i periodi di scadenza dei titoli
 */
public enum PeriodoScadenza {
    TRIMESTRALI(2, 3),
    SEMESTRALI(5, 6),
    ANNUALI(11, 12),
    TRIENNALI(30, 36),
    TUTTI(0, Integer.MAX_VALUE);

    private final int mesiMin;
    private final int mesiMax;

    PeriodoScadenza(int mesiMin, int mesiMax) {
        this.mesiMin = mesiMin;
        this.mesiMax = mesiMax;
    }

    public int getMesiMin() {
        return mesiMin;
    }

    public int getMesiMax() {
        return mesiMax;
    }

    /**
     * Converte una stringa in un valore dell'enumerazione
     * 
     * @param periodo Stringa rappresentante il periodo
     * @return Valore dell'enumerazione corrispondente
     */
    public static PeriodoScadenza fromString(String periodo) {
        if (periodo == null) {
            return TUTTI;
        }

        switch (periodo.toLowerCase()) {
            case "trimestrali":
                return TRIMESTRALI;
            case "semestrali":
                return SEMESTRALI;
            case "annuali":
                return ANNUALI;
            case "triennali":
                return TRIENNALI;
            default:
                return TUTTI;
        }
    }
}
