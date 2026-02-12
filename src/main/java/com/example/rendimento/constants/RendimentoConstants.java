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
     * Prefisso per le costanti di profilo.
     */
    public static final String PREFIX_PROFILE = "PROFILE_";
    
    /**
     * Numero di giorni in un anno.
     * <p>
     * Utilizzato per calcoli di annualizzazione.
     * Usa 365.25 per tenere conto degli anni bisestili.
     */
    public static final BigDecimal TIME_DAYS_IN_YEAR = new BigDecimal("365.25");
    
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
     * Periodicità del bollo di default.
     * <p>
     * Utilizzato quando non viene specificata una periodicità personalizzata.
     * Valori possibili: "ANNUALE" o "MENSILE"
     * Corrisponde al campo periodicitaBollo in ProfiloCalcolo.
     */
    public static final String PROFILE_DEFAULT_PERIODICITA_BOLLO = "ANNUALE";
    
    /**
     * Tasso di imposta di bollo di default (0.2%).
     * <p>
     * Applicato al valore nominale dei titoli.
     * Corrisponde al campo percentualeBollo in ProfiloCalcolo.
     */
    public static final BigDecimal PROFILE_DEFAULT_PERCENTUALE_BOLLO = new BigDecimal("0.002");
    
    /**
     * Tasso di commissione di default per BTP (0.09%).
     * <p>
     * Utilizzato quando non viene specificato un tasso di commissione personalizzato.
     * Corrisponde al campo commissioneBtp in ProfiloCalcolo.
     */
    public static final BigDecimal PROFILE_DEFAULT_COMMISSIONE_BTP = new BigDecimal("0.0009");
    
    /**
     * Tasso di commissione di default per BOT con scadenza fino a 120 giorni (0.09%).
     * <p>
     * Utilizzato quando non viene specificato un tasso di commissione personalizzato.
     * Corrisponde al campo commissioneBot120gg in ProfiloCalcolo.
     */
    public static final BigDecimal PROFILE_DEFAULT_COMMISSIONE_BOT_120GG = new BigDecimal("0.0009");
    
    /**
     * Tasso di commissione di default per BOT con scadenza tra 121 e 240 giorni (0.09%).
     * <p>
     * Utilizzato quando non viene specificato un tasso di commissione personalizzato.
     * Corrisponde al campo commissioneBot240gg in ProfiloCalcolo.
     */
    public static final BigDecimal PROFILE_DEFAULT_COMMISSIONE_BOT_240GG = new BigDecimal("0.0009");
    
    /**
     * Tasso di commissione di default per BOT con scadenza oltre 240 giorni (0.09%).
     * <p>
     * Utilizzato quando non viene specificato un tasso di commissione personalizzato.
     * Corrisponde al campo commissioneBotOltre in ProfiloCalcolo.
     */
    public static final BigDecimal PROFILE_DEFAULT_COMMISSIONE_BOT_OLTRE = new BigDecimal("0.0009");
    
    /**
     * Tasso di commissione di default per CCT (0.09%).
     * <p>
     * Utilizzato quando non viene specificato un tasso di commissione personalizzato.
     * Corrisponde al campo commissioneCct in ProfiloCalcolo.
     */
    public static final BigDecimal PROFILE_DEFAULT_COMMISSIONE_CCT = new BigDecimal("0.0009");
    
    /**
     * Tasso di commissione di default per CTZ (0.09%).
     * <p>
     * Utilizzato quando non viene specificato un tasso di commissione personalizzato.
     * Corrisponde al campo commissioneCtz in ProfiloCalcolo.
     */
    public static final BigDecimal PROFILE_DEFAULT_COMMISSIONE_CTZ = new BigDecimal("0.0009");
    
    /**
     * Valore di default per il flag plusvalenzaEsente (false).
     * <p>
     * Utilizzato quando non viene specificato un valore personalizzato.
     * Corrisponde al campo plusvalenzaEsente in ProfiloCalcolo.
     */
    public static final Boolean PROFILE_DEFAULT_PLUSVALENZA_ESENTE = false;
    
    /**
     * Mese minimo per il range di visualizzazione dei titoli semestrali.
     * <p>
     * Utilizzato per filtrare i titoli nella pagina trends.html.
     */
    public static final int RANGE_SEMESTRALE_MIN_MESI = 5;
    
    /**
     * Mese massimo per il range di visualizzazione dei titoli semestrali.
     * <p>
     * Utilizzato per filtrare i titoli nella pagina trends.html.
     */
    public static final int RANGE_SEMESTRALE_MAX_MESI = 7;
    
    /**
     * Mese minimo per il range di visualizzazione dei titoli annuali.
     * <p>
     * Utilizzato per filtrare i titoli nella pagina trends.html.
     */
    public static final int RANGE_ANNUALE_MIN_MESI = 11;
    
    /**
     * Mese massimo per il range di visualizzazione dei titoli annuali.
     * <p>
     * Utilizzato per filtrare i titoli nella pagina trends.html.
     */
    public static final int RANGE_ANNUALE_MAX_MESI = 13;
    
    /**
     * Offset in mesi per il range di visualizzazione dei titoli pluriennali.
     * <p>
     * Utilizzato per filtrare i titoli nella pagina trends.html.
     * Rappresenta il numero di mesi prima e dopo la scadenza esatta.
     */
    public static final int RANGE_PLURIENNALE_OFFSET_MESI = 6;
    
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
