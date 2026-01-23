package com.example.rendimento.enums;

import java.math.BigDecimal;

public enum TrendBucket {

    TRE_MESI("3M", 0.17, 0.33),
    SEI_MESI("6M", 0.42, 0.67),
    DODICI_MESI("12M", 0.90, 1.25),
    VENTIQUATTRO_MESI("24M", 1.75, 2.50),
    TRENTASEI_MESI("36M", 2.75, 3.50),
    SESSANTA_MESI("60M", 4.50, 6.00),
    OLTRE_SESSANTA_MESI(">60M", 6.00, null);

    private final String label;
    private final Double minYears;
    private final Double maxYears;

    TrendBucket(String label, Double minYears, Double maxYears) {
        this.label = label;
        this.minYears = minYears;
        this.maxYears = maxYears;
    }

    public boolean matches(BigDecimal anniAllaScadenza) {
        double years = anniAllaScadenza.doubleValue();
        if (maxYears == null) {
            return years >= minYears;
        }
        return years >= minYears && years < maxYears;
    }

    public String getLabel() {
        return label;
    }
}
