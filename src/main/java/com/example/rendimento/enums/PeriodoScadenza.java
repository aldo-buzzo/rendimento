package com.example.rendimento.enums;

/**
 * Enumerazione per i periodi di scadenza dei titoli
 */
public enum PeriodoScadenza {
    TRIMESTRALI(2, 3, TrendBucket.TRE_MESI),
    SEMESTRALI(5, 6, TrendBucket.SEI_MESI),
    ANNUALI(11, 12, TrendBucket.DODICI_MESI),
    TRIENNALI(30, 36, TrendBucket.TRENTASEI_MESI),
    TUTTI(0, Integer.MAX_VALUE, null);

    private final int mesiMin;
    private final int mesiMax;
    private final TrendBucket bucket;

    PeriodoScadenza(int mesiMin, int mesiMax, TrendBucket bucket) {
        this.mesiMin = mesiMin;
        this.mesiMax = mesiMax;
        this.bucket = bucket;
    }

    public int getMesiMin() {
        return mesiMin;
    }

    public int getMesiMax() {
        return mesiMax;
    }
    
    /**
     * Restituisce il TrendBucket corrispondente a questo periodo di scadenza
     * 
     * @return TrendBucket corrispondente, o null se il periodo è TUTTI
     */
    public TrendBucket getTrendBucket() {
        return bucket;
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
