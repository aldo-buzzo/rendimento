package com.example.rendimento.utility;

import com.example.rendimento.constants.RendimentoConstants;
import com.example.rendimento.dto.RendimentiPerProfiloDTO;
import com.example.rendimento.dto.ValoriFinaliPerProfiloDTO;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.model.ProfiloCalcolo;
import com.example.rendimento.model.Titolo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe per il calcolo dei rendimenti e valori finali per profili specifici.
 * Questa classe mantiene liste di rendimenti e valori finali calcolati.
 */
public class CalcolatoreRendimentiPerProfilo {

    private static final Logger log = LoggerFactory.getLogger(CalcolatoreRendimentiPerProfilo.class);

    // Attributi per memorizzare i risultati dei calcoli
    private List<RendimentiPerProfiloDTO> rendimentiPerProfili;
    private List<ValoriFinaliPerProfiloDTO> valoriFinaliPerProfili;

    /**
     * Costruttore che inizializza le liste per memorizzare i risultati.
     */
    public CalcolatoreRendimentiPerProfilo() {
        this.rendimentiPerProfili = new ArrayList<>();
        this.valoriFinaliPerProfili = new ArrayList<>();
    }

    /**
     * Calcola sia i rendimenti che i valori finali per un profilo specifico.
     * Questo metodo unifica le funzionalità dei precedenti metodi statici.
     * 
     * @param titolo                 il titolo per cui calcolare i rendimenti
     * @param profilo                il profilo per cui calcolare i rendimenti
     * @param nominale               il valore nominale dell'investimento
     * @param capitaleInvestito      il capitale investito
     * @param plusvalenzaLorda       la plusvalenza lorda
     * @param interessiNetti         gli interessi netti
     * @param giorni                 i giorni alla scadenza
     * @param fattoreAnnualizzazione il fattore di annualizzazione
     * @param dataAcquisto           la data di acquisto
     * @return il DTO con i rendimenti calcolati (anche i valori finali vengono
     *         calcolati e memorizzati internamente)
     */
    public RendimentiPerProfiloDTO calcolaRendimentiEValoriFinali(
            Titolo titolo,
            ProfiloCalcolo profilo,
            BigDecimal nominale,
            BigDecimal capitaleInvestito,
            BigDecimal plusvalenzaLorda,
            BigDecimal interessiNetti,
            BigDecimal giorni,
            BigDecimal fattoreAnnualizzazione,
            LocalDate dataAcquisto) {

        // Calcola i rendimenti
        RendimentiPerProfiloDTO rendimentiProfilo = calcolaRendimentiInterno(
                titolo, profilo, nominale, capitaleInvestito, plusvalenzaLorda,
                interessiNetti, giorni, fattoreAnnualizzazione, dataAcquisto);

        // Calcola i valori finali
        ValoriFinaliPerProfiloDTO valoriFinaliProfilo = calcolaValoriFinaliInterno(
                titolo, rendimentiProfilo, nominale, interessiNetti, giorni,
                profilo.getPlusvalenzaEsente(), dataAcquisto);

        // Memorizza i risultati nelle liste
        rendimentiPerProfili.add(rendimentiProfilo);
        valoriFinaliPerProfili.add(valoriFinaliProfilo);

        // Restituisce i rendimenti calcolati
        return rendimentiProfilo;
    }

    /**
     * Metodo interno per calcolare i rendimenti.
     * Contiene la logica dell'originale metodo statico calcolaRendimentiPerProfilo.
     */
    private RendimentiPerProfiloDTO calcolaRendimentiInterno(
            Titolo titolo,
            ProfiloCalcolo profilo,
            BigDecimal nominale,
            BigDecimal capitaleInvestito,
            BigDecimal plusvalenzaLorda,
            BigDecimal interessiNetti,
            BigDecimal giorni,
            BigDecimal fattoreAnnualizzazione,
            LocalDate dataAcquisto) {

        RendimentiPerProfiloDTO result = new RendimentiPerProfiloDTO();

        // Imposta i dati del profilo (il profilo non sarà mai null, viene impostato a
        // monte)
        result.setIdProfilo(profilo.getIdProfilo());
        result.setNomeProfilo(profilo.getNome());
        result.setIsPlusvalenzaEsente(profilo.getPlusvalenzaEsente());

        // ===============================
        // 1. PLUSVALENZA NETTA (dipende dal profilo)
        // ===============================
        BigDecimal plusvalenzaNetta;

        // Ottieni il valore del flag isPlusvalenzaEsente dal profilo
        Boolean isPlusvalenzaEsente = profilo.getPlusvalenzaEsente();

        // La plusvalenza lorda viene tassata se:
        // 1. È positiva E
        // 2. (È un BOT OPPURE isPlusvalenzaEsente è false)
        // Nota: per i BOT la plusvalenza non può essere esente
        if (plusvalenzaLorda.signum() > 0 &&
                (TipoTitolo.BOT.equals(titolo.getTipoTitolo()) || isPlusvalenzaEsente == null
                        || !isPlusvalenzaEsente)) {
            plusvalenzaNetta = plusvalenzaLorda
                    .multiply(RendimentoConstants.TAX_FACTOR);
        } else {
            plusvalenzaNetta = plusvalenzaLorda; // minusvalenza NON tassata o plusvalenza esente (solo per BTP)
        }

        plusvalenzaNetta = plusvalenzaNetta.setScale(8, RoundingMode.HALF_UP);
        result.setPlusvalenzaNetta(plusvalenzaNetta);

        BigDecimal guadagnoBase = plusvalenzaNetta.add(interessiNetti)
                .setScale(8, RoundingMode.HALF_UP);

        // ===============================
        // 2. GUADAGNO SENZA COSTI (dipende dal profilo)
        // ===============================
        BigDecimal guadagnoNettoSenzaCosti = plusvalenzaNetta.add(interessiNetti)
                .setScale(8, RoundingMode.HALF_UP);
        result.setGuadagnoNettoSenzaCosti(guadagnoNettoSenzaCosti);

        // ===============================
        // 3. COMMISSIONI (ONE-SHOT) (dipende dal profilo)
        // ===============================
        BigDecimal commissionRate;

        // Usa le commissioni dal profilo in base al tipo di titolo (il titolo è sempre
        // valorizzato)
        switch (titolo.getTipoTitolo()) {
            case BTP:
                commissionRate = profilo.getCommissioneBtp();
                break;
            case BOT:
                // Calcola i giorni alla scadenza per determinare quale commissione BOT usare
                long giorniAllaScadenza = giorni.longValue();
                if (giorniAllaScadenza <= 120) {
                    commissionRate = profilo.getCommissioneBot120gg();
                } else if (giorniAllaScadenza <= 240) {
                    commissionRate = profilo.getCommissioneBot240gg();
                } else {
                    commissionRate = profilo.getCommissioneBotOltre();
                }
                break;
            default:
                commissionRate = profilo.getCommissioneBtp(); // Default
        }

        result.setCommissionRate(commissionRate);

        BigDecimal commissioni = capitaleInvestito
                .multiply(commissionRate)
                .setScale(8, RoundingMode.HALF_UP);
        result.setCommissioni(commissioni);

        BigDecimal guadagnoConCommissioni = guadagnoNettoSenzaCosti.subtract(commissioni);
        result.setGuadagnoNettoCommissioni(guadagnoConCommissioni);

        // ===============================
        // 4. BOLLO (dipende dal profilo)
        // ===============================
        String periodicitaBollo = profilo.getPeriodicitaBollo();
        BigDecimal percentualeBollo = profilo.getPercentualeBollo();

        result.setPeriodicitaBollo(periodicitaBollo);
        result.setPercentualeBollo(percentualeBollo);

        // Calcola il bollo appropriato in base alla periodicità configurata nel profilo
        BigDecimal impostaBollo = BigDecimal.ZERO;

        if ("ANNUALE".equals(periodicitaBollo)) {
            // Bollo annuale (una volta se scadenza > 31/12)
            LocalDate fineAnno = LocalDate.of(dataAcquisto.getYear(), 12, 31);
            if (titolo.getDataScadenza().isAfter(fineAnno)) {
                impostaBollo = nominale.multiply(percentualeBollo)
                        .setScale(8, RoundingMode.HALF_UP);
            }
        } else {
            // Bollo mensile proporzionale ai mesi residui
            long mesiAllaScadenza = ChronoUnit.MONTHS.between(dataAcquisto, titolo.getDataScadenza());
            if (mesiAllaScadenza > 0) {
                impostaBollo = nominale.multiply(percentualeBollo)
                        .multiply(BigDecimal.valueOf(mesiAllaScadenza))
                        .divide(RendimentoConstants.TIME_MONTHS_IN_YEAR, 8, RoundingMode.HALF_UP);
            }
        }

        // Calcola il guadagno netto dopo l'applicazione del bollo
        BigDecimal guadagnoNettoBollo = guadagnoConCommissioni.subtract(impostaBollo);

        // Imposta i valori nel risultato
        result.setGuadagnoNettoBollo(guadagnoNettoBollo);
        result.setImpostaBollo(impostaBollo);

        // ===============================
        // 5. RENDIMENTI ANNUALIZZATI (dipendono dal profilo)
        // ===============================
        result.setRendimentoNetto(guadagnoBase.divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                .multiply(fattoreAnnualizzazione));
        result.setRendimentoConCommissioni(
                guadagnoConCommissioni.divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                        .multiply(fattoreAnnualizzazione));

        // Calcola il rendimento con bollo
        result.setRendimentoConBollo(
                guadagnoNettoBollo.divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                        .multiply(fattoreAnnualizzazione));

        // Rendimento con plusvalenza esente (non tassata) - solo per BTP
        if (TipoTitolo.BTP.equals(titolo.getTipoTitolo())) {
            // Calcolo del guadagno con plusvalenza esente (non tassata) e interessi netti
            // (tassati)
            BigDecimal guadagnoConPlusvalenzaEsente = plusvalenzaLorda.add(interessiNetti)
                    .setScale(8, RoundingMode.HALF_UP);

            // Calcolo del guadagno con plusvalenza esente e commissioni
            BigDecimal guadagnoConPlusvalenzaEsenteECommissioni = guadagnoConPlusvalenzaEsente.subtract(commissioni)
                    .setScale(8, RoundingMode.HALF_UP);

            // Calcolo del guadagno con plusvalenza esente, commissioni e bollo
            BigDecimal guadagnoConPlusvalenzaEsenteCommissioniEBollo = guadagnoConPlusvalenzaEsenteECommissioni
                    .subtract(impostaBollo)
                    .setScale(8, RoundingMode.HALF_UP);

            result.setRendimentoPlusvalenzaEsente(
                    guadagnoConPlusvalenzaEsenteCommissioniEBollo
                            .divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                            .multiply(fattoreAnnualizzazione));
        } else {
            // Per i BOT e altri tipi di titoli, il rendimentoPlusvalenzaEsente è uguale al
            // rendimentoConBollo
            // Questo garantisce che il campo sia sempre valorizzato
            result.setRendimentoPlusvalenzaEsente(result.getRendimentoConBollo());
        }

        return result;
    }

    /**
     * Metodo interno per calcolare i valori finali.
     * Contiene la logica dell'originale metodo statico calcolaValoriFinali.
     * Distingue tra titoli con scadenza entro l'anno e titoli pluriennali.
     */
    private ValoriFinaliPerProfiloDTO calcolaValoriFinaliInterno(
            Titolo titolo,
            RendimentiPerProfiloDTO rendimentiProfilo,
            BigDecimal nominale,
            BigDecimal interessiNetti,
            BigDecimal giorni,
            Boolean isPlusvalenzaEsente,
            LocalDate dataAcquisto) {

        ValoriFinaliPerProfiloDTO result = new ValoriFinaliPerProfiloDTO();

        // Imposta i dati del profilo
        result.setIdProfilo(rendimentiProfilo.getIdProfilo());
        result.setNomeProfilo(rendimentiProfilo.getNomeProfilo());

        // Imposta il flag isPlusvalenzaEsente in base al valore passato dal profilo
        // La plusvalenza può essere esente solo per i BTP, mai per i BOT o altri tipi
        // di titoli
        boolean useEsenzione = TipoTitolo.BTP.equals(titolo.getTipoTitolo()) &&
                isPlusvalenzaEsente != null &&
                isPlusvalenzaEsente;
        result.setPlusvalenzaEsente(useEsenzione);

        // Verifica se il titolo ha scadenza oltre l'anno
        boolean isTitoloPluriannuale = giorni.compareTo(BigDecimal.valueOf(365)) > 0;

        if (isTitoloPluriannuale) {
            // Logica per titoli pluriennali
            calcolaValoriFinaliTitoloPluriannuale(result, titolo, rendimentiProfilo, nominale,
                    interessiNetti, giorni, useEsenzione, dataAcquisto);
        } else {
            // Logica per titoli con scadenza entro l'anno
            calcolaValoriFinaliTitoloAnnuale(result, rendimentiProfilo, interessiNetti);
        }

        return result;
    }

    /**
     * Metodo interno per calcolare i valori finali per titoli con scadenza entro
     * l'anno.
     * Contiene la logica originale del metodo calcolaValoriFinaliInterno.
     */
    private void calcolaValoriFinaliTitoloAnnuale(
            ValoriFinaliPerProfiloDTO result,
            RendimentiPerProfiloDTO rendimentiProfilo,
            BigDecimal interessiNetti) {

        // Calcolo diretto degli importi guadagnati
        // 1. Importo guadagnato lordo (plusvalenza + interessi)
        BigDecimal importoGuadagnatoLordo = rendimentiProfilo.getPlusvalenzaNetta().add(interessiNetti)
                .setScale(2, RoundingMode.HALF_UP);

        // 2. Importo guadagnato meno commissioni
        BigDecimal importoGuadagnatoMenoCommissioni = importoGuadagnatoLordo
                .subtract(rendimentiProfilo.getCommissioni())
                .setScale(2, RoundingMode.HALF_UP);

        // 3. Importo guadagnato meno commissioni e bolli
        BigDecimal importoGuadagnatoMenoBolli = importoGuadagnatoMenoCommissioni
                .subtract(rendimentiProfilo.getImpostaBollo())
                .setScale(2, RoundingMode.HALF_UP);

        // Log per debug
        log.debug(
                "CalcolaValoriFinali (Annuale) - Profilo: {}, importoGuadagnatoLordo: {}, importoGuadagnatoMenoCommissioni: {}, importoGuadagnatoMenoBolli: {}",
                result.getNomeProfilo(), importoGuadagnatoLordo, importoGuadagnatoMenoCommissioni,
                importoGuadagnatoMenoBolli);

        // Impostare i valori finali (importi guadagnati)
        result.setValoreFinaleLordo(importoGuadagnatoLordo);
        result.setValoreFinaleMenoCommissioni(importoGuadagnatoMenoCommissioni);
        result.setValoreFinaleMenoBolli(importoGuadagnatoMenoBolli);
    }

    /**
     * Metodo interno per calcolare i valori finali per titoli pluriennali (scadenza
     * > 365 giorni).
     * Implementa la nuova logica per il calcolo dei valori finali dei titoli
     * pluriennali.
     */
    private void calcolaValoriFinaliTitoloPluriannuale(
            ValoriFinaliPerProfiloDTO result,
            Titolo titolo,
            RendimentiPerProfiloDTO rendimentiProfilo,
            BigDecimal nominale,
            BigDecimal interessiNetti,
            BigDecimal giorni,
            boolean useEsenzione,
            LocalDate dataAcquisto) {

        // 1. Calcolare gli interessi netti che maturano in tutta la durata del titolo
        // Gli interessi netti sono già calcolati per l'intera durata del titolo

        // 2. Aggiungere o sottrarre la plusvalenza calcolata secondo i profili
        BigDecimal plusvalenza;
        if (useEsenzione) {
            // Se la plusvalenza è esente, usiamo la plusvalenza lorda
            // Dobbiamo ricavare la plusvalenza lorda dalla plusvalenza netta
            // Se la plusvalenza è positiva, è stata tassata al 12.5%
            if (rendimentiProfilo.getPlusvalenzaNetta().signum() > 0) {
                plusvalenza = rendimentiProfilo.getPlusvalenzaNetta()
                        .divide(RendimentoConstants.TAX_FACTOR, 8, RoundingMode.HALF_UP);
            } else {
                // Se è negativa, non è stata tassata
                plusvalenza = rendimentiProfilo.getPlusvalenzaNetta();
            }
        } else {
            // Se la plusvalenza non è esente, usiamo la plusvalenza netta
            plusvalenza = rendimentiProfilo.getPlusvalenzaNetta();
        }

        // Importo guadagnato lordo (plusvalenza + interessi)
        BigDecimal importoGuadagnatoLordo = plusvalenza.add(interessiNetti)
                .setScale(2, RoundingMode.HALF_UP);

        // 3. Sottrarre i costi di acquisto secondo i vari profili
        BigDecimal importoGuadagnatoMenoCommissioni = importoGuadagnatoLordo
                .subtract(rendimentiProfilo.getCommissioni())
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Sottrarre i bolli calcolati in funzione del profilo
        // Per i profili con bolli annuali, il bollo dell'ultimo anno non viene
        // applicato
        BigDecimal impostaBollo;
        String periodicitaBollo = rendimentiProfilo.getPeriodicitaBollo();
        BigDecimal percentualeBollo = rendimentiProfilo.getPercentualeBollo();

        if ("ANNUALE".equals(periodicitaBollo)) {
            // Calcola il numero di anni completi fino alla scadenza
            long anniCompleti = ChronoUnit.YEARS.between(dataAcquisto, titolo.getDataScadenza());
            // Per i titoli pluriennali con bollo annuale, non applichiamo il bollo
            // dell'ultimo anno
            impostaBollo = nominale.multiply(percentualeBollo)
                    .multiply(BigDecimal.valueOf(anniCompleti))
                    .setScale(8, RoundingMode.HALF_UP);
        } else {
            // Bollo mensile proporzionale ai mesi residui
            long mesiAllaScadenza = ChronoUnit.MONTHS.between(dataAcquisto, titolo.getDataScadenza());
            impostaBollo = nominale.multiply(percentualeBollo)
                    .multiply(BigDecimal.valueOf(mesiAllaScadenza))
                    .divide(RendimentoConstants.TIME_MONTHS_IN_YEAR, 8, RoundingMode.HALF_UP);
        }

        BigDecimal importoGuadagnatoMenoBolli = importoGuadagnatoMenoCommissioni.subtract(impostaBollo)
                .setScale(2, RoundingMode.HALF_UP);

        // Impostare i valori finali (importi guadagnati)
        result.setValoreFinaleLordo(importoGuadagnatoLordo);
        result.setValoreFinaleMenoCommissioni(importoGuadagnatoMenoCommissioni);
        result.setValoreFinaleMenoBolli(importoGuadagnatoMenoBolli);

        // Log per debug
        log.debug(
                "CalcolaValoriFinali (Pluriennale) - Profilo: {}, useEsenzione: {}, importoGuadagnatoLordo: {}, importoGuadagnatoMenoCommissioni: {}, importoGuadagnatoMenoBolli: {}",
                result.getNomeProfilo(), useEsenzione, importoGuadagnatoLordo, importoGuadagnatoMenoCommissioni,
                importoGuadagnatoMenoBolli);
    }

    /**
     * Restituisce la lista dei rendimenti calcolati per i profili.
     * 
     * @return la lista dei rendimenti per profilo
     */
    public List<RendimentiPerProfiloDTO> getRendimentiPerProfili() {
        return rendimentiPerProfili;
    }

    /**
     * Restituisce la lista dei valori finali calcolati per i profili.
     * 
     * @return la lista dei valori finali per profilo
     */
    public List<ValoriFinaliPerProfiloDTO> getValoriFinaliPerProfili() {
        return valoriFinaliPerProfili;
    }
}
