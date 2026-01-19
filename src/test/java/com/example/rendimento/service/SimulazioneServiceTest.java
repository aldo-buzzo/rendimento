package com.example.rendimento.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.enums.ModalitaCalcoloBollo;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.mapper.SimulazioneMapper;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.SimulazioneRepository;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.service.impl.SimulazioneServiceImpl;

/**
 * Test per la classe SimulazioneServiceImpl.
 * Verifica il corretto calcolo del rendimento secondo le specifiche.
 */
public class SimulazioneServiceTest {

    @Mock
    private TitoloRepository titoloRepository;

    @Mock
    private SimulazioneRepository simulazioneRepository;

    @Mock
    private SimulazioneMapper simulazioneMapper;

    @InjectMocks
    private SimulazioneServiceImpl simulazioneService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test calcolo rendimento BTP-1nv29 5,25% (IT0001278511)")
    public void testCalcoloRendimentoBTP() {
        // Arrange
        Integer idTitolo = 1;
        BigDecimal prezzoAcquisto = new BigDecimal("109.8887");
        BigDecimal importo = new BigDecimal("10000");
        ModalitaCalcoloBollo modalitaBollo = ModalitaCalcoloBollo.ANNUALE;

        // Creazione del titolo di test
        Titolo titolo = new Titolo();
        titolo.setIdTitolo(idTitolo);
        titolo.setNome("BTP-1nv29 5,25%");
        titolo.setCodiceIsin("IT0001278511");
        titolo.setDataScadenza(LocalDate.of(2029, 11, 1)); // 1 novembre 2029
        titolo.setTassoNominale(new BigDecimal("5.25"));
        titolo.setPeriodicitaCedole("SEMESTRALE");
        titolo.setPeriodicitaBollo("ANNUALE");
        titolo.setTipoTitolo(TipoTitolo.BTP);

        // Mock del repository
        when(titoloRepository.findById(idTitolo)).thenReturn(Optional.of(titolo));

        // Act
        RisultatoSimulazioneDTO risultato = simulazioneService.calcolaRendimento(
                idTitolo, prezzoAcquisto, importo, modalitaBollo);

        // Assert
        assertNotNull(risultato, "Il risultato non dovrebbe essere null");
        
        // Verifica dei valori calcolati
        // Calcolo manuale dei valori attesi secondo la formula specificata
        
        // 1. Calcolo importo pagato (importo * prezzoAcquisto / 100)
        BigDecimal importoPagato = importo.multiply(prezzoAcquisto)
                                 .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        
        // 2. Calcolo plusvalenza (importo - importoPagato)
        BigDecimal plusvalenza = importo.subtract(importoPagato);
        
        // 3. Calcolo plusvalenza netta (plusvalenza * 0,875) - tassazione al 12.5%
        BigDecimal plusvalenzaNetta = plusvalenza.multiply(new BigDecimal("0.875"))
                                    .setScale(4, RoundingMode.HALF_UP);
        
        // 4. Calcolo giorni alla scadenza
        LocalDate dataAcquisto = LocalDate.now();
        LocalDate dataScadenza = titolo.getDataScadenza();
        long giorniAllaScadenza = java.time.temporal.ChronoUnit.DAYS.between(dataAcquisto, dataScadenza);
        
        // 5. Calcolo interessi netti (importo * 0,875 * tasso) * giorni/360
        BigDecimal tassoDecimale = titolo.getTassoNominale().divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
        BigDecimal interessiNetti = importo.multiply(new BigDecimal("0.875"))
                                  .multiply(tassoDecimale)
                                  .multiply(new BigDecimal(giorniAllaScadenza))
                                  .divide(new BigDecimal("360"), 4, RoundingMode.HALF_UP);
        
        // 6. Calcolo commissioni (importoPagato * 0,9/1000)
        BigDecimal commissioni = importoPagato.multiply(new BigDecimal("0.0009"))
                               .setScale(4, RoundingMode.HALF_UP);
        
        // 7. Calcolo guadagno totale (plusvalenza + interessi)
        BigDecimal guadagnoTotale = plusvalenzaNetta.add(interessiNetti)
                                   .setScale(4, RoundingMode.HALF_UP);
        
        // 8. Calcolo guadagno al netto commissioni (guadagno totale - commissioni)
        BigDecimal guadagnoNettoCommissioni = guadagnoTotale.subtract(commissioni)
                                            .setScale(4, RoundingMode.HALF_UP);
        
        // 9. Calcolo imposta di bollo (2/1000 del valore nominale)
        BigDecimal impostaBollo = importo.multiply(new BigDecimal("0.002"))
                                .setScale(4, RoundingMode.HALF_UP);
        
        // 10. Calcolo guadagno al netto bollo (guadagno netto commissioni - imposta bollo)
        BigDecimal guadagnoNettoBollo = guadagnoNettoCommissioni.subtract(impostaBollo)
                                      .setScale(4, RoundingMode.HALF_UP);
        
        // 11. Calcolo tasso netto bollo ((guadagno netto bollo / importo pagato) * 360/giorni * 100)
        BigDecimal tassoNettoBollo = guadagnoNettoBollo.divide(importoPagato, 8, RoundingMode.HALF_UP)
                                   .multiply(new BigDecimal("360"))
                                   .divide(new BigDecimal(giorniAllaScadenza), 8, RoundingMode.HALF_UP)
                                   .multiply(new BigDecimal("100"))
                                   .setScale(4, RoundingMode.HALF_UP);
        
        // Verifica che il tasso netto bollo calcolato dal servizio corrisponda al valore atteso
        assertEquals(tassoNettoBollo.doubleValue(), risultato.getTassoNettoBollo().doubleValue(), 0.0001,
                "Il tasso netto bollo calcolato non corrisponde al valore atteso");
        
        // Stampa dei valori per debug
        System.out.println("Importo pagato: " + importoPagato);
        System.out.println("Plusvalenza: " + plusvalenza);
        System.out.println("Plusvalenza netta: " + plusvalenzaNetta);
        System.out.println("Giorni alla scadenza: " + giorniAllaScadenza);
        System.out.println("Interessi netti: " + interessiNetti);
        System.out.println("Commissioni: " + commissioni);
        System.out.println("Guadagno totale: " + guadagnoTotale);
        System.out.println("Guadagno netto commissioni: " + guadagnoNettoCommissioni);
        System.out.println("Imposta di bollo: " + impostaBollo);
        System.out.println("Guadagno netto bollo: " + guadagnoNettoBollo);
        System.out.println("Tasso netto bollo atteso: " + tassoNettoBollo);
        System.out.println("Tasso netto bollo calcolato: " + risultato.getTassoNettoBollo());
        
        // Verifica degli altri valori calcolati
        assertEquals(plusvalenzaNetta.doubleValue(), risultato.getPlusvalenzaNetta().doubleValue(), 0.0001,
                "La plusvalenza netta calcolata non corrisponde al valore atteso");
        assertEquals(interessiNetti.doubleValue(), risultato.getInteressiNetti().doubleValue(), 0.0001,
                "Gli interessi netti calcolati non corrispondono al valore atteso");
        assertEquals(commissioni.doubleValue(), risultato.getCommissioni().doubleValue(), 0.0001,
                "Le commissioni calcolate non corrispondono al valore atteso");
        assertEquals(guadagnoTotale.doubleValue(), risultato.getGuadagnoTotale().doubleValue(), 0.0001,
                "Il guadagno totale calcolato non corrisponde al valore atteso");
        assertEquals(guadagnoNettoCommissioni.doubleValue(), risultato.getGuadagnoNettoCommissioni().doubleValue(), 0.0001,
                "Il guadagno netto commissioni calcolato non corrisponde al valore atteso");
        assertEquals(impostaBollo.doubleValue(), risultato.getImpostaBollo().doubleValue(), 0.0001,
                "L'imposta di bollo calcolata non corrisponde al valore atteso");
        assertEquals(guadagnoNettoBollo.doubleValue(), risultato.getGuadagnoNettoBollo().doubleValue(), 0.0001,
                "Il guadagno netto bollo calcolato non corrisponde al valore atteso");
    }
}
