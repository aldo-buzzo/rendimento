package com.example.rendimento.utility;

import com.example.rendimento.constants.RendimentoConstants;
import com.example.rendimento.dto.RendimentiPerProfiloDTO;
import com.example.rendimento.dto.ValoriFinaliPerProfiloDTO;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.model.Titolo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utility per il calcolo dei valori finali teorici per un profilo specifico.
 */
public class CalcolatoreValoriFinaliPerProfilo {
    
    private static final Logger log = LoggerFactory.getLogger(CalcolatoreValoriFinaliPerProfilo.class);
    
    /**
     * Calcola i valori finali teorici per un profilo specifico.
     * 
     * @param titolo il titolo per cui calcolare i valori finali
     * @param rendimentiProfilo i rendimenti calcolati per il profilo
     * @param nominale il valore nominale dell'investimento
     * @param interessiNetti gli interessi netti
     * @param giorni i giorni alla scadenza
     * @param isPlusvalenzaEsente flag che indica se la plusvalenza è esente
     * @return un ValoriFinaliPerProfiloDTO con i valori finali calcolati
     */
    public static ValoriFinaliPerProfiloDTO calcolaValoriFinaliPerProfilo(
            Titolo titolo,
            RendimentiPerProfiloDTO rendimentiProfilo,
            BigDecimal nominale,
            BigDecimal interessiNetti,
            BigDecimal giorni,
            Boolean isPlusvalenzaEsente) {
        
        ValoriFinaliPerProfiloDTO result = new ValoriFinaliPerProfiloDTO();
        
        // Imposta i dati del profilo
        result.setIdProfilo(rendimentiProfilo.getIdProfilo());
        result.setNomeProfilo(rendimentiProfilo.getNomeProfilo());
        
        // Calcolo degli anni residui
        BigDecimal anniResidui = giorni.divide(RendimentoConstants.TIME_DAYS_IN_YEAR, 10, RoundingMode.HALF_UP);
        
        // Determina se la durata è inferiore a un anno
        boolean durataInferioreAnno = giorni.compareTo(new BigDecimal("365")) < 0;
        
        // Calcola la plusvalenza esente (solo per BTP)
        BigDecimal plusvalenzaEsenteValue = null;
        if (TipoTitolo.BTP.equals(titolo.getTipoTitolo()) && rendimentiProfilo.getRendimentoPlusvalenzaEsente() != null) {
            // Per i BTP, la plusvalenza esente è la plusvalenza lorda (non tassata)
            // Possiamo calcolarla come la differenza tra il guadagno netto senza costi e gli interessi netti
            plusvalenzaEsenteValue = rendimentiProfilo.getGuadagnoNettoSenzaCosti().subtract(interessiNetti)
                    .divide(RendimentoConstants.TAX_FACTOR, 8, RoundingMode.HALF_UP);
        }
        
        // Calcola i valori di bollo annuale e mensile
        BigDecimal bolloAnnuale = BigDecimal.ZERO;
        BigDecimal bolloMensile = BigDecimal.ZERO;
        
        // Calcola il bollo annuale e mensile in base alla percentuale di bollo
        BigDecimal percentualeBollo = rendimentiProfilo.getPercentualeBollo();
        
        // Utilizziamo la data corrente come riferimento per il calcolo del bollo
        LocalDate dataRiferimento = LocalDate.now();
        
        // Bollo annuale (una volta se scadenza > 31/12)
        if (titolo.getDataScadenza().isAfter(dataRiferimento.withMonth(12).withDayOfMonth(31))) {
            bolloAnnuale = nominale.multiply(percentualeBollo)
                            .setScale(8, RoundingMode.HALF_UP);
        }
        
        // Bollo mensile proporzionale ai mesi residui
        long mesiAllaScadenza = java.time.temporal.ChronoUnit.MONTHS.between(
                dataRiferimento, titolo.getDataScadenza());
        if (mesiAllaScadenza > 0) {
            bolloMensile = nominale.multiply(percentualeBollo)
                            .multiply(BigDecimal.valueOf(mesiAllaScadenza))
                            .divide(RendimentoConstants.TIME_MONTHS_IN_YEAR, 8, RoundingMode.HALF_UP);
        }
        
        // Imposta il flag isPlusvalenzaEsente in base al valore passato dal profilo
        // La plusvalenza può essere esente solo per i BTP, mai per i BOT o altri tipi di titoli
        boolean useEsenzione = TipoTitolo.BTP.equals(titolo.getTipoTitolo()) && 
                              isPlusvalenzaEsente != null && 
                              isPlusvalenzaEsente;
        result.setPlusvalenzaEsente(useEsenzione);
        
        // Determina quale plusvalenza e rendimento utilizzare in base al flag
        BigDecimal plusvalenza = useEsenzione ? plusvalenzaEsenteValue : rendimentiProfilo.getPlusvalenzaNetta();
        BigDecimal rendimento = useEsenzione ? rendimentiProfilo.getRendimentoPlusvalenzaEsente() : rendimentiProfilo.getRendimentoConBollo();
        
        // Log per debug
        log.debug("CalcolatoreValoriFinaliPerProfilo - Profilo: {}, isPlusvalenzaEsente: {}, plusvalenza: {}, rendimento: {}", 
                 rendimentiProfilo.getNomeProfilo(), useEsenzione, plusvalenza, rendimento);
        
        // Calcola valoreFinaleLordo (senza commissioni e bolli)
        result.setValoreFinaleLordo(calcolaValoreFinale(
            "VALORE_FINALE_LORDO",
            rendimento,
            plusvalenza,
            BigDecimal.ZERO,
            nominale,
            interessiNetti,
            anniResidui,
            durataInferioreAnno,
            giorni
        ));
        
        // Calcola valoreFinaleMenoCommissioni (con commissioni ma senza bolli)
        result.setValoreFinaleMenoCommissioni(calcolaValoreFinale(
            "VALORE_FINALE_MENO_COMMISSIONI",
            rendimento,
            plusvalenza,
            rendimentiProfilo.getCommissioni(),
            nominale,
            interessiNetti,
            anniResidui,
            durataInferioreAnno,
            giorni
        ));
        
        // Calcola valoreFinaleMenoBolli (con commissioni e bolli)
        BigDecimal bollo = "ANNUALE".equals(rendimentiProfilo.getPeriodicitaBollo()) ? bolloAnnuale : bolloMensile;
        result.setValoreFinaleMenoBolli(calcolaValoreFinale(
            "VALORE_FINALE_MENO_BOLLI",
            rendimento,
            plusvalenza,
            rendimentiProfilo.getCommissioni().add(bollo),
            nominale,
            interessiNetti,
            anniResidui,
            durataInferioreAnno,
            giorni
        ));
        
        return result;
    }
    
    /**
     * Metodo che implementa la logica di calcolo del valore finale
     */
    private static BigDecimal calcolaValoreFinale(
            String tipoValore,
            BigDecimal rendimentoPercentuale, 
            BigDecimal plusvalenza, 
            BigDecimal costiTotali,
            BigDecimal nominale,
            BigDecimal interessiNetti,
            BigDecimal anniResidui,
            boolean durataInferioreAnno,
            BigDecimal giorni) {
        
        BigDecimal valoreFinale;
        
        // Utilizziamo direttamente il rendimento percentuale per calcolare il valore finale
        // Il rendimento è già annualizzato, quindi dobbiamo de-annualizzarlo per il periodo specifico
        BigDecimal rendimentoPerPeriodo = rendimentoPercentuale.divide(RendimentoConstants.PERCENT_100, 10, RoundingMode.HALF_UP)
                                         .multiply(anniResidui);
        
        // Calcolo del valore finale utilizzando il rendimento
        valoreFinale = nominale.multiply(BigDecimal.ONE.add(rendimentoPerPeriodo));
        
        log.debug("Calcolo valore finale {} utilizzando rendimento: nominale={}, rendimentoPercentuale={}, rendimentoPerPeriodo={}, anniResidui={}, valoreFinale={}",
            tipoValore, nominale, rendimentoPercentuale, rendimentoPerPeriodo, anniResidui, valoreFinale);
        
        // Arrotonda a 2 decimali
        return valoreFinale.setScale(2, RoundingMode.HALF_UP);
    }
}