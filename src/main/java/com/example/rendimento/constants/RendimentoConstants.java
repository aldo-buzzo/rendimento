package com.example.rendimento.constants;

import java.math.BigDecimal;

/**
 * Costanti utilizzate nel calcolo dei rendimenti.
 * <p>
 * Questa classe contiene costanti immutabili utilizzate nei calcoli finanziari
 * relativi ai rendimenti dei titoli di Stato italiani.
 * <p>
 * Tutte le costanti numeriche sono definite come {@link BigDecimal} per garantire
 * la massima precisione nei calcoli finanziari.
 */
public final class RendimentoConstants {
    
    /**
     * Prefisso per le costanti di tempo.
     */
    public static final String PREFIX_TIME = "TIME_";
    
    /**
     * Prefisso per le costanti fiscali.
     */
    public static final String PREFIX_TAX = "TAX_";
    
    /**
     * Prefisso per le costanti di commissione.
     */
    public static final String PREFIX_COMMISSION = "COMMISSION_";
    
    /**
     * Numero di giorni in un anno.
     * <p>
     * Utilizzato per calcoli di annualizzazione.
     */
    public static final BigDecimal TIME_DAYS_IN_YEAR = new BigDecimal("365");
    
    /**
     * Numero di mesi in un anno.
     * <p>
     * Utilizzato per calcoli mensili.
     */
    public static final BigDecimal TIME_MONTHS_IN_YEAR = new BigDecimal("12");
    
    /**
     * Tasso di imposta di bollo (0.2%).
     * <p>
     * Applicato al valore nominale dei titoli.
     */
    public static final BigDecimal TAX_BOLLO_RATE = new BigDecimal("0.002");
    
    /**
     * Aliquota fiscale per i titoli di Stato italiani (12.5%).
     * <p>
     * Applicata a cedole e plusvalenze.
     */
    public static final BigDecimal TAX_RATE = new BigDecimal("0.125");
    
    /**
     * Fattore di tassazione (1 - TAX_RATE = 0.875).
     * <p>
     * Utilizzato per calcolare l'importo netto dopo la tassazione.
     */
    public static final BigDecimal TAX_FACTOR = BigDecimal.ONE.subtract(TAX_RATE);
    
    /**
     * Costante per il valore 100 (usata per conversioni percentuali).
     * <p>
     * Utilizzata per convertire tra valori percentuali e decimali.
     */
    public static final BigDecimal PERCENT_100 = new BigDecimal("100");
    
    /**
     * Tasso di commissione di default (0.09%).
     * <p>
     * Utilizzato quando non viene specificato un tasso di commissione personalizzato.
     */
    public static final BigDecimal COMMISSION_DEFAULT_RATE = new BigDecimal("0.0009");
    
    /**
     * Importo fisso per le simulazioni automatiche (10.000 euro).
     * <p>
     * Utilizzato nel calcolo automatico dei rendimenti di tutti i titoli.
     */
    public static final BigDecimal IMPORTO_FISSO_SIMULAZIONE = new BigDecimal("10000");
    
    /**
     * Costruttore privato per impedire l'istanziazione.
     * <p>
     * Questa classe contiene solo costanti statiche e non deve essere istanziata.
     * 
     * @throws UnsupportedOperationException se si tenta di istanziare questa classe
     */
    private RendimentoConstants() {
        throw new UnsupportedOperationException("Questa classe di utilità non può essere istanziata");
    }
}
