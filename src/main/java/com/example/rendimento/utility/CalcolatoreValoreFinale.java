package com.example.rendimento.utility;

import com.example.rendimento.constants.RendimentoConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Classe utility per il calcolo dei valori finali teorici di un investimento.
 * Questa classe è indipendente e può essere utilizzata senza accedere all'oggetto Titolo.
 */
public class CalcolatoreValoreFinale {
    
    private static final Logger log = LoggerFactory.getLogger(CalcolatoreValoreFinale.class);
    
    // Campi calcolati dalla durata
    private final BigDecimal giorni;
    private final BigDecimal anniResidui;
    private final boolean durataInferioreAnno;
    
    // Altri campi necessari per i calcoli
    private final BigDecimal nominale;
    private final BigDecimal interessiNetti;
    private final BigDecimal plusvalenzaNonEsente;
    private final BigDecimal plusvalenzaEsente;
    private final BigDecimal commissioni;
    private final BigDecimal bolloAnnuale;
    private final BigDecimal bolloMensile;
    private final BigDecimal rendimentoConBolloAnnuale;
    private final BigDecimal rendimentoConBolloMensile;
    private final BigDecimal rendimentoPlusvalenzaEsente;
    
    /**
     * Costruttore che inizializza tutti i parametri necessari per i calcoli
     */
    public CalcolatoreValoreFinale(
            BigDecimal giorni,
            BigDecimal nominale, 
            BigDecimal interessiNetti,
            BigDecimal anniResidui,
            BigDecimal plusvalenzaNonEsente,
            BigDecimal plusvalenzaEsente,
            BigDecimal commissioni,
            BigDecimal bolloAnnuale,
            BigDecimal bolloMensile,
            BigDecimal rendimentoConBolloAnnuale,
            BigDecimal rendimentoConBolloMensile,
            BigDecimal rendimentoPlusvalenzaEsente) {
        
        // Inizializza i campi calcolati dalla durata
        this.giorni = giorni;
        this.anniResidui = anniResidui;
        this.durataInferioreAnno = giorni.compareTo(new BigDecimal("365")) < 0;
        
        // Inizializza gli altri campi
        this.nominale = nominale;
        this.interessiNetti = interessiNetti;
        this.plusvalenzaNonEsente = plusvalenzaNonEsente;
        this.plusvalenzaEsente = plusvalenzaEsente;
        this.commissioni = commissioni;
        this.bolloAnnuale = bolloAnnuale;
        this.bolloMensile = bolloMensile;
        this.rendimentoConBolloAnnuale = rendimentoConBolloAnnuale;
        this.rendimentoConBolloMensile = rendimentoConBolloMensile;
        this.rendimentoPlusvalenzaEsente = rendimentoPlusvalenzaEsente;
    }
    
    
    /**
     * Calcola il valore finale con bollo annuale e plusvalenza non esente
     */
    public BigDecimal getValoreBolloAnnualePlusvalenzaNonEsente() {
        return calcolaValoreFinale(
            "BOLLO_ANNUALE_PLUSVALENZA_NON_ESENTE",
            rendimentoConBolloAnnuale, 
            plusvalenzaNonEsente, 
            commissioni.add(bolloAnnuale)
        );
    }
    
    /**
     * Calcola il valore finale con bollo mensile e plusvalenza non esente
     */
    public BigDecimal getValoreBolloMensilePlusvalenzaNonEsente() {
        return calcolaValoreFinale(
            "BOLLO_MENSILE_PLUSVALENZA_NON_ESENTE",
            rendimentoConBolloMensile, 
            plusvalenzaNonEsente, 
            commissioni.add(bolloMensile)
        );
    }
    
    /**
     * Calcola il valore finale con bollo annuale e plusvalenza esente
     */
    public BigDecimal getValoreBolloAnnualePlusvalenzaEsente() {
        if (rendimentoPlusvalenzaEsente == null) {
            return null;
        }
        
        return calcolaValoreFinale(
            "BOLLO_ANNUALE_PLUSVALENZA_ESENTE",
            rendimentoPlusvalenzaEsente, 
            plusvalenzaEsente, 
            commissioni.add(bolloAnnuale)
        );
    }
    
    /**
     * Calcola il valore finale con bollo mensile e plusvalenza esente
     */
    public BigDecimal getValoreBolloMensilePlusvalenzaEsente() {
        if (rendimentoPlusvalenzaEsente == null) {
            return null;
        }
        
        return calcolaValoreFinale(
            "BOLLO_MENSILE_PLUSVALENZA_ESENTE",
            rendimentoPlusvalenzaEsente, 
            plusvalenzaEsente, 
            commissioni.add(bolloMensile)
        );
    }
    
    /**
     * Metodo privato che implementa la logica di calcolo del valore finale
     */
    private BigDecimal calcolaValoreFinale(
            String tipoValore,
            BigDecimal rendimentoPercentuale, 
            BigDecimal plusvalenza, 
            BigDecimal costiTotali) {
        
        BigDecimal valoreFinale;
        
        if (durataInferioreAnno) {
            // Formula per titoli con durata inferiore a 365 giorni
            valoreFinale = nominale
                .add(interessiNetti)
                .add(plusvalenza)
                .subtract(costiTotali);
            
            log.debug("Calcolo valore finale {} per titolo con scadenza < 1 anno: nominale={}, interessiNetti={}, plusvalenza={}, costiTotali={}, valoreFinale={}",
                tipoValore, nominale, interessiNetti, plusvalenza, costiTotali, valoreFinale);
        } else {
            // Formula per titoli con durata superiore o uguale a 365 giorni
            // Nuova formula: ValoreFinaleEquivalente = Nominale × (1 + (RendimentoTotale / anni))
            // dove RendimentoTotale = CedoleNetteTotali + PlusvalenzaNetta − CostiTotali
            
            // Calcolo del rendimento totale in valore assoluto
            BigDecimal rendimentoTotale = interessiNetti.add(plusvalenza).subtract(costiTotali);
            
            // Calcolo del fattore (1 + (RendimentoTotale / anni))
            BigDecimal fattore = BigDecimal.ONE.add(
                rendimentoTotale.divide(nominale.multiply(anniResidui), 10, RoundingMode.HALF_UP)
            );
            
            // Calcolo del valore finale
            valoreFinale = nominale.multiply(fattore);
            
            log.debug("Calcolo valore finale {} per titolo con scadenza >= 1 anno: nominale={}, interessiNetti={}, plusvalenza={}, costiTotali={}, anniResidui={}, rendimentoTotale={}, valoreFinale={}",
                tipoValore, nominale, interessiNetti, plusvalenza, costiTotali, anniResidui, rendimentoTotale, valoreFinale);
        }
        
        // Arrotonda a 2 decimali
        return valoreFinale.setScale(2, RoundingMode.HALF_UP);
    }
    
    // Getter per i campi calcolati dalla durata
    public BigDecimal getGiorni() { return giorni; }
    public BigDecimal getAnniResidui() { return anniResidui; }
    public boolean isDurataInferioreAnno() { return durataInferioreAnno; }
}
