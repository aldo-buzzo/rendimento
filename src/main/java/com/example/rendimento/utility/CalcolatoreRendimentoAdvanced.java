package com.example.rendimento.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.rendimento.constants.RendimentoConstants;
import com.example.rendimento.dto.RendimentiPerProfiloDTO;
import com.example.rendimento.dto.RisultatoRendimentoAdvancedDTO;
import com.example.rendimento.dto.ValoriFinaliPerProfiloDTO;
import com.example.rendimento.model.ProfiloCalcolo;
import com.example.rendimento.model.Titolo;

/**
 * Classe utility per il calcolo avanzato dei rendimenti di un titolo.
 * Questa classe centralizza la logica di calcolo dei rendimenti avanzati,
 * precedentemente contenuta nel metodo calcolaRendimentoAdvanced di
 * SimulazioneServiceImpl.
 */
public class CalcolatoreRendimentoAdvanced {

        private static final Logger log = LoggerFactory.getLogger(CalcolatoreRendimentoAdvanced.class);

        /**
         * Calcola i rendimenti avanzati per un titolo, considerando diversi profili di
         * calcolo.
         * 
         * @param titolo                    il titolo per cui calcolare i rendimenti
         * @param prezzoAcquistoPercentuale il prezzo di acquisto in percentuale
         * @param nominale                  l'importo nominale dell'investimento
         * @param dataAcquisto              la data di acquisto
         * @param profili                   la lista dei profili di calcolo da
         *                                  utilizzare
         * @return un oggetto RisultatoRendimentoAdvancedDTO contenente tutti i
         *         risultati del calcolo
         */
        public static RisultatoRendimentoAdvancedDTO calcolaRendimentoAdvanced(
                        Titolo titolo,
                        BigDecimal prezzoAcquistoPercentuale,
                        BigDecimal nominale,
                        LocalDate dataAcquisto,
                        List<ProfiloCalcolo> profili) {


                RisultatoRendimentoAdvancedDTO dto = new RisultatoRendimentoAdvancedDTO();
                log.info("DEBUG: Titolo con ISIN:" + titolo.getCodiceIsin() + " " + titolo.getTipoTitolo());
                if ("IT0005640666".equals(titolo.getCodiceIsin())) {
                        log.info("DEBUG: Titolo con ISIN:" + titolo.getCodiceIsin() + " " + titolo.getTipoTitolo());
                }
                // ===============================
                // 1. TEMPO
                // ===============================
                LocalDate dataScadenza = titolo.getDataScadenza();
                long giorniAllaScadenza = ChronoUnit.DAYS.between(dataAcquisto, dataScadenza);

                if (giorniAllaScadenza <= 0) {
                        throw new IllegalArgumentException(
                                        "La data di scadenza deve essere successiva alla data di acquisto");
                }

                BigDecimal giorni = BigDecimal.valueOf(giorniAllaScadenza);
                BigDecimal fattoreTempo = giorni.divide(new BigDecimal("360"), 10, RoundingMode.HALF_UP);
                BigDecimal fattoreAnnualizzazione = RendimentoConstants.TIME_DAYS_IN_YEAR
                                .divide(giorni, 10, RoundingMode.HALF_UP);

                // Imposta i fattori tempo
                dto.setFattoreTempo(fattoreTempo);
                dto.setFattoreAnnualizzazione(fattoreAnnualizzazione);

                // Calcolo anni durata
                BigDecimal anniDurata = giorni.divide(RendimentoConstants.TIME_DAYS_IN_YEAR, 4, RoundingMode.HALF_UP);
                dto.setAnniDurata(anniDurata);

                // Imposta il valore di rimborso fisso a 100
                dto.setValoreRimborso(BigDecimal.valueOf(100));

                // ===============================
                // 2. CAPITALE INVESTITO
                // ===============================
                BigDecimal capitaleInvestito = nominale
                                .multiply(prezzoAcquistoPercentuale)
                                .divide(RendimentoConstants.PERCENT_100, 8, RoundingMode.HALF_UP);

                dto.setNominale(nominale);
                dto.setPrezzoAcquistoPercentuale(prezzoAcquistoPercentuale);
                dto.setCapitaleInvestito(capitaleInvestito);

                // Imposta il prezzo di riferimento per il bollo (uguale al prezzo di acquisto
                // percentuale)
                dto.setPrezzoRiferimentoBollo(prezzoAcquistoPercentuale);

                // ===============================
                // 3. PLUSVALENZA NETTA
                // ===============================
                // BigDecimal plusvalenzaNetta = nominale
                // .subtract(capitaleInvestito)
                // .multiply(RendimentoConstants.TAX_FACTOR)
                // .setScale(8, RoundingMode.HALF_UP);
                // dto.setPlusvalenzaNetta(plusvalenzaNetta);
                BigDecimal plusvalenzaLorda = nominale.subtract(capitaleInvestito);

                BigDecimal plusvalenzaNetta;
                if (plusvalenzaLorda.signum() > 0) {
                        plusvalenzaNetta = plusvalenzaLorda
                                        .multiply(RendimentoConstants.TAX_FACTOR);
                } else {
                        plusvalenzaNetta = plusvalenzaLorda; // minusvalenza NON tassata
                }

                plusvalenzaNetta = plusvalenzaNetta.setScale(8, RoundingMode.HALF_UP);
                dto.setPlusvalenzaNetta(plusvalenzaNetta);

                // Salviamo la plusvalenza lorda per il calcolo del rendimento con plusvalenza
                // esente
                BigDecimal plusvalenzaEsente = plusvalenzaLorda.setScale(8, RoundingMode.HALF_UP);

                // ===============================
                // 4. INTERESSI NETTI (CEDOLE)
                // ===============================
                BigDecimal tassoCedolare = titolo.getTassoNominale()
                                .divide(RendimentoConstants.PERCENT_100, 10, RoundingMode.HALF_UP);

                // Imposta la cedola annua
                BigDecimal cedolaAnnua = titolo.getTassoNominale();
                dto.setCedolaAnnua(cedolaAnnua);

                BigDecimal cedoleNetteAnnue = nominale
                                .multiply(tassoCedolare)
                                .multiply(RendimentoConstants.TAX_FACTOR)
                                .setScale(8, RoundingMode.HALF_UP);
                dto.setCedoleNetteAnnue(cedoleNetteAnnue);

                BigDecimal interessiNetti = cedoleNetteAnnue.multiply(fattoreTempo)
                                .setScale(8, RoundingMode.HALF_UP);
                dto.setInteressiNetti(interessiNetti);

                // ===============================
                // 5. GUADAGNO SENZA COSTI
                // ===============================
                BigDecimal guadagnoNettoSenzaCosti = plusvalenzaNetta.add(interessiNetti)
                                .setScale(8, RoundingMode.HALF_UP);
                dto.setGuadagnoNettoSenzaCosti(guadagnoNettoSenzaCosti);

                // Calcolo del guadagno con plusvalenza esente (non tassata) e interessi netti
                // (tassati)
                BigDecimal guadagnoConPlusvalenzaEsente = plusvalenzaEsente.add(interessiNetti)
                                .setScale(8, RoundingMode.HALF_UP);

                // ===============================
                // 6. CALCOLO DEI RENDIMENTI PER OGNI PROFILO
                // ===============================
                // Crea un'istanza del calcolatore di rendimenti per profilo
                CalcolatoreRendimentiPerProfilo calcolatorePerProfilo = new CalcolatoreRendimentiPerProfilo();

                // Per ogni profilo, calcola i rendimenti e i valori finali
                for (ProfiloCalcolo profilo : profili) {
                        // Calcola i rendimenti e i valori finali per il profilo corrente
                        calcolatorePerProfilo.calcolaRendimentiEValoriFinali(
                                titolo,
                                profilo,
                                nominale,
                                capitaleInvestito,
                                plusvalenzaLorda,
                                interessiNetti,
                                giorni,
                                fattoreAnnualizzazione,
                                dataAcquisto);
                }

                // Ottieni le liste complete di rendimenti e valori finali
                List<RendimentiPerProfiloDTO> rendimentiPerProfili = calcolatorePerProfilo.getRendimentiPerProfili();
                List<ValoriFinaliPerProfiloDTO> valoriFinaliPerProfili = calcolatorePerProfilo.getValoriFinaliPerProfili();

                // Aggiungi i rendimenti e i valori finali alle liste nel DTO risultato
                for (int i = 0; i < rendimentiPerProfili.size(); i++) {
                        dto.addRendimentiPerProfilo(rendimentiPerProfili.get(i));
                        dto.addValoriFinaliPerProfilo(valoriFinaliPerProfili.get(i));
                }

                // ===============================
                // 7. RETROCOMPATIBILITÀ
                // ===============================
                // Per retrocompatibilità, imposta i campi principali del DTO con i valori del
                // primo profilo
                if (!dto.getRendimentiPerProfili().isEmpty() && !dto.getValoriFinaliPerProfili().isEmpty()) {
                        RendimentiPerProfiloDTO primoProfilo = dto.getRendimentiPerProfili().get(0);
                        ValoriFinaliPerProfiloDTO primiValoriFinali = dto.getValoriFinaliPerProfili().get(0);

                        // Imposta i campi di commissioni
                        dto.setCommissionRate(primoProfilo.getCommissionRate());
                        dto.setCommissioni(primoProfilo.getCommissioni());
                        dto.setGuadagnoNettoCommissioni(primoProfilo.getGuadagnoNettoCommissioni());
                        dto.setCapitaleConCommissioni(capitaleInvestito.add(primoProfilo.getCommissioni()));

                        // Imposta i campi di bollo
                        // Non settiamo più bolloTotaleAnnuale e bolloTotaleMensile perché sono stati
                        // rimossi dal DTO
                        // Calcoliamo i valori direttamente qui per retrocompatibilità
                        String periodicitaBollo = primoProfilo.getPeriodicitaBollo();
                        BigDecimal percentualeBollo = primoProfilo.getPercentualeBollo();

                        // Bollo annuale (una volta se scadenza > 31/12)
                        BigDecimal bolloAnnuale = BigDecimal.ZERO;
                        LocalDate fineAnno = LocalDate.of(dataAcquisto.getYear(), 12, 31);
                        if (titolo.getDataScadenza().isAfter(fineAnno)) {
                                bolloAnnuale = nominale.multiply(percentualeBollo)
                                                .setScale(8, RoundingMode.HALF_UP);
                        }

                        // Bollo mensile proporzionale ai mesi residui
                        long mesiAllaScadenza = ChronoUnit.MONTHS.between(dataAcquisto, titolo.getDataScadenza());
                        BigDecimal bolloMensile = BigDecimal.ZERO;
                        if (mesiAllaScadenza > 0) {
                                bolloMensile = nominale.multiply(percentualeBollo)
                                                .multiply(BigDecimal.valueOf(mesiAllaScadenza))
                                                .divide(RendimentoConstants.TIME_MONTHS_IN_YEAR, 8,
                                                                RoundingMode.HALF_UP);
                        }

                        dto.setBolloTotaleAnnuale(bolloAnnuale);
                        dto.setBolloTotaleMensile(bolloMensile);
                        dto.setGuadagnoNettoBollo(primoProfilo.getGuadagnoNettoBollo());
                        dto.setImpostaBollo(primoProfilo.getImpostaBollo());

                        // Imposta i campi di rendimenti
                        dto.setRendimentoSenzaCosti(
                                        guadagnoNettoSenzaCosti.divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                                        .multiply(fattoreAnnualizzazione));
                        dto.setRendimentoConCommissioni(primoProfilo.getRendimentoConCommissioni());

                        // Usa il rendimento con bollo dal profilo
                        if ("ANNUALE".equals(primoProfilo.getPeriodicitaBollo())) {
                                dto.setRendimentoConCommissioniEBolloAnnuale(primoProfilo.getRendimentoConBollo());
                                // Per retrocompatibilità, impostiamo anche il campo mensile con lo stesso
                                // valore
                                dto.setRendimentoConCommissioniEBolloMensile(primoProfilo.getRendimentoConBollo());
                        } else {
                                dto.setRendimentoConCommissioniEBolloMensile(primoProfilo.getRendimentoConBollo());
                                // Per retrocompatibilità, impostiamo anche il campo annuale con lo stesso
                                // valore
                                dto.setRendimentoConCommissioniEBolloAnnuale(primoProfilo.getRendimentoConBollo());
                        }

                        dto.setRendimentoPlusvalenzaEsente(primoProfilo.getRendimentoPlusvalenzaEsente());

                        // Imposta i campi di valori finali con i valori corretti
                        // Cerchiamo i valori finali per ogni combinazione di bollo e plusvalenza
                        BigDecimal valoreBolloAnnualePlusvalenzaNonEsente = primiValoriFinali.getValoreFinaleMenoBolli();
                        BigDecimal valoreBolloMensilePlusvalenzaNonEsente = primiValoriFinali.getValoreFinaleMenoBolli();
                        BigDecimal valoreBolloAnnualePlusvalenzaEsente = primiValoriFinali.getValoreFinaleMenoBolli();
                        BigDecimal valoreBolloMensilePlusvalenzaEsente = primiValoriFinali.getValoreFinaleMenoBolli();
                        
                        // Cerchiamo i valori finali per ogni profilo
                        for (int i = 0; i < dto.getRendimentiPerProfili().size(); i++) {
                            RendimentiPerProfiloDTO rendimentiProfilo = dto.getRendimentiPerProfili().get(i);
                            ValoriFinaliPerProfiloDTO valoriFinaliProfilo = dto.getValoriFinaliPerProfili().get(i);
                            
                            // Verifichiamo se questo profilo ha rendimenti migliori
                            if ("ANNUALE".equals(rendimentiProfilo.getPeriodicitaBollo())) {
                                if (rendimentiProfilo.getIsPlusvalenzaEsente() != null && rendimentiProfilo.getIsPlusvalenzaEsente()) {
                                    // Bollo annuale, plusvalenza esente
                                    if (rendimentiProfilo.getRendimentoPlusvalenzaEsente().compareTo(primoProfilo.getRendimentoPlusvalenzaEsente()) > 0) {
                                        valoreBolloAnnualePlusvalenzaEsente = valoriFinaliProfilo.getValoreFinaleMenoBolli();
                                    }
                                } else {
                                    // Bollo annuale, plusvalenza non esente
                                    if (rendimentiProfilo.getRendimentoConBollo().compareTo(primoProfilo.getRendimentoConBollo()) > 0) {
                                        valoreBolloAnnualePlusvalenzaNonEsente = valoriFinaliProfilo.getValoreFinaleMenoBolli();
                                    }
                                }
                            } else {
                                if (rendimentiProfilo.getIsPlusvalenzaEsente() != null && rendimentiProfilo.getIsPlusvalenzaEsente()) {
                                    // Bollo mensile, plusvalenza esente
                                    if (rendimentiProfilo.getRendimentoPlusvalenzaEsente().compareTo(primoProfilo.getRendimentoPlusvalenzaEsente()) > 0) {
                                        valoreBolloMensilePlusvalenzaEsente = valoriFinaliProfilo.getValoreFinaleMenoBolli();
                                    }
                                } else {
                                    // Bollo mensile, plusvalenza non esente
                                    if (rendimentiProfilo.getRendimentoConBollo().compareTo(primoProfilo.getRendimentoConBollo()) > 0) {
                                        valoreBolloMensilePlusvalenzaNonEsente = valoriFinaliProfilo.getValoreFinaleMenoBolli();
                                    }
                                }
                            }
                        }
                        
                        // Imposta i valori finali nel DTO
                        dto.setValoreBolloAnnualePlusvalenzaNonEsente(valoreBolloAnnualePlusvalenzaNonEsente);
                        dto.setValoreBolloMensilePlusvalenzaNonEsente(valoreBolloMensilePlusvalenzaNonEsente);
                        dto.setValoreBolloAnnualePlusvalenzaEsente(valoreBolloAnnualePlusvalenzaEsente);
                        dto.setValoreBolloMensilePlusvalenzaEsente(valoreBolloMensilePlusvalenzaEsente);

                        // Calcolo tasso di rendimento (guadagno totale / capitale investito * fattore
                        // annualizzazione)
                        BigDecimal tasso = guadagnoNettoSenzaCosti
                                        .divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                        .multiply(fattoreAnnualizzazione)
                                        .multiply(RendimentoConstants.PERCENT_100)
                                        .setScale(4, RoundingMode.HALF_UP);
                        dto.setTasso(tasso);

                        // Calcolo tasso netto commissioni
                        BigDecimal tassoNettoCommissioni = primoProfilo.getGuadagnoNettoCommissioni()
                                        .divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                        .multiply(fattoreAnnualizzazione)
                                        .multiply(RendimentoConstants.PERCENT_100)
                                        .setScale(4, RoundingMode.HALF_UP);
                        dto.setTassoNettoCommissioni(tassoNettoCommissioni);

                        // Calcolo tasso netto bollo
                        BigDecimal tassoNettoBollo = primoProfilo.getGuadagnoNettoBollo()
                                        .divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                        .multiply(fattoreAnnualizzazione)
                                        .multiply(RendimentoConstants.PERCENT_100)
                                        .setScale(4, RoundingMode.HALF_UP);
                        dto.setTassoNettoBollo(tassoNettoBollo);

                        // Calcolo rendimento netto bollo non annualizzato
                        BigDecimal rendimentoNettoBollo = primoProfilo.getGuadagnoNettoBollo()
                                        .divide(nominale, 4, RoundingMode.HALF_UP)
                                        .multiply(RendimentoConstants.PERCENT_100)
                                        .setScale(4, RoundingMode.HALF_UP);
                        dto.setRendimentoNettoBollo(rendimentoNettoBollo);

                        // Calcolo importo a scadenza
                        BigDecimal importoScadenza = nominale.add(primoProfilo.getGuadagnoNettoBollo())
                                        .setScale(4, RoundingMode.HALF_UP);
                        dto.setImportoScadenza(importoScadenza);
                }

                // ===============================
                // 8. CAMPI DI COMPATIBILITÀ / RIEPILOGO
                // ===============================
                dto.setGuadagnoTotale(guadagnoNettoSenzaCosti);
                dto.setPlusvalenzaNetta(plusvalenzaNetta);

                return dto;
        }
}