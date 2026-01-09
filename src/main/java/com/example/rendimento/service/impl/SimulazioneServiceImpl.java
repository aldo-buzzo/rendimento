package com.example.rendimento.service.impl;

import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.enums.ModalitaCalcoloBollo;
import com.example.rendimento.mapper.SimulazioneMapper;
import com.example.rendimento.model.Simulazione;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.SimulazioneRepository;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.service.SimulazioneService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione del servizio che gestisce le operazioni sulle simulazioni.
 */
@Service
public class SimulazioneServiceImpl implements SimulazioneService {

    private final SimulazioneRepository simulazioneRepository;
    private final TitoloRepository titoloRepository;
    private final SimulazioneMapper simulazioneMapper;

    /**
     * Costruttore con parametri per l'iniezione delle dipendenze.
     *
     * @param simulazioneRepository repository per le operazioni CRUD sulle simulazioni
     * @param titoloRepository repository per le operazioni CRUD sui titoli
     * @param simulazioneMapper mapper per la conversione tra entità e DTO
     */
    @Autowired
    public SimulazioneServiceImpl(SimulazioneRepository simulazioneRepository, 
                                 TitoloRepository titoloRepository,
                                 SimulazioneMapper simulazioneMapper) {
        this.simulazioneRepository = simulazioneRepository;
        this.titoloRepository = titoloRepository;
        this.simulazioneMapper = simulazioneMapper;
    }

    @Override
    public RisultatoSimulazioneDTO calcolaRendimento(Integer idTitolo, BigDecimal prezzoAcquisto, 
                                                   BigDecimal importo, ModalitaCalcoloBollo modalitaBollo) {
        // Validazione input
        if (idTitolo == null || prezzoAcquisto == null || importo == null || modalitaBollo == null) {
            throw new IllegalArgumentException("Tutti i parametri devono essere valorizzati");
        }

        // Recupero titolo
        Titolo titolo = titoloRepository.findById(idTitolo)
            .orElseThrow(() -> new EntityNotFoundException("Titolo non trovato con ID: " + idTitolo));

        // Calcolo giorni alla scadenza
        LocalDate oggi = LocalDate.now();
        LocalDate dataScadenza = titolo.getDataScadenza();
        long giorniAllaScadenza = ChronoUnit.DAYS.between(oggi, dataScadenza);
        
        if (giorniAllaScadenza <= 0) {
            throw new IllegalArgumentException("La data di scadenza deve essere successiva alla data odierna");
        }

        // Calcolo importo pagato (importo * prezzoAcquisto / 100)
        BigDecimal importoPagato = importo.multiply(prezzoAcquisto)
                                 .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Calcolo plusvalenza (importo - importoPagato)
        BigDecimal plusvalenza = importo.subtract(importoPagato);

        // Calcolo plusvalenza netta (plusvalenza * 0,875) - tassazione al 12.5%
        BigDecimal plusvalenzaNetta = plusvalenza.multiply(new BigDecimal("0.875"))
                                    .setScale(4, RoundingMode.HALF_UP);

        // Calcolo interessi netti (importo * 0,875 * tasso) * giorni/360
        BigDecimal tassoDecimale = titolo.getTassoNominale().divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
        BigDecimal interessiNetti = importo.multiply(new BigDecimal("0.875"))
                                  .multiply(tassoDecimale)
                                  .multiply(new BigDecimal(giorniAllaScadenza))
                                  .divide(new BigDecimal("360"), 4, RoundingMode.HALF_UP);

        // Calcolo commissioni (importo * 0,9/1000)
        BigDecimal commissioni = importo.multiply(new BigDecimal("0.0009"))
                               .setScale(4, RoundingMode.HALF_UP);

        // Calcolo guadagno totale (plusvalenza + interessi)
        BigDecimal guadagnoTotale = plusvalenzaNetta.add(interessiNetti)
                                   .setScale(4, RoundingMode.HALF_UP);

        // Calcolo guadagno al netto commissioni (guadagno totale - commissioni)
        BigDecimal guadagnoNettoCommissioni = guadagnoTotale.subtract(commissioni)
                                            .setScale(4, RoundingMode.HALF_UP);

        // Calcolo imposta di bollo (2/1000 del valore nominale)
        BigDecimal impostaBollo = BigDecimal.ZERO;

        if (modalitaBollo == ModalitaCalcoloBollo.ANNUALE) {
            // Verifica se la scadenza è successiva al 31 dicembre dell'anno corrente
            LocalDate fineDellAnno = LocalDate.of(oggi.getYear(), 12, 31);
            if (dataScadenza.isAfter(fineDellAnno)) {
                impostaBollo = importo.multiply(new BigDecimal("0.002"))
                             .setScale(4, RoundingMode.HALF_UP);
            }
        } else { // MENSILE
            // Calcolo proporzionale per ogni mese mancante alla scadenza
            long mesiAllaScadenza = ChronoUnit.MONTHS.between(oggi, dataScadenza);
            if (mesiAllaScadenza > 0) {
                impostaBollo = importo.multiply(new BigDecimal("0.002"))
                             .multiply(new BigDecimal(mesiAllaScadenza))
                             .divide(new BigDecimal("12"), 4, RoundingMode.HALF_UP);
            }
        }

        // Calcolo guadagno al netto bollo (guadagno netto commissioni - imposta bollo)
        BigDecimal guadagnoNettoBollo = guadagnoNettoCommissioni.subtract(impostaBollo)
                                      .setScale(4, RoundingMode.HALF_UP);

        // Calcolo tasso ((guadagno totale / importo pagato) * 360/giorni * 100)
        BigDecimal tasso = guadagnoTotale.divide(importoPagato, 8, RoundingMode.HALF_UP)
                         .multiply(new BigDecimal("360"))
                         .divide(new BigDecimal(giorniAllaScadenza), 8, RoundingMode.HALF_UP)
                         .multiply(new BigDecimal("100"))
                         .setScale(4, RoundingMode.HALF_UP);

        // Calcolo tasso netto commissioni ((guadagno netto commissioni / importo pagato) * 360/giorni * 100)
        BigDecimal tassoNettoCommissioni = guadagnoNettoCommissioni.divide(importoPagato, 8, RoundingMode.HALF_UP)
                                         .multiply(new BigDecimal("360"))
                                         .divide(new BigDecimal(giorniAllaScadenza), 8, RoundingMode.HALF_UP)
                                         .multiply(new BigDecimal("100"))
                                         .setScale(4, RoundingMode.HALF_UP);

        // Calcolo importo a scadenza
        BigDecimal importoScadenza = importo.add(guadagnoNettoBollo)
                                   .setScale(4, RoundingMode.HALF_UP);

        // Creazione e restituzione del DTO con i risultati
        return new RisultatoSimulazioneDTO(
            plusvalenzaNetta,
            interessiNetti,
            commissioni,
            guadagnoTotale,
            guadagnoNettoCommissioni,
            impostaBollo,
            guadagnoNettoBollo,
            tasso,
            tassoNettoCommissioni,
            importoScadenza
        );
    }

    @Override
    public SimulazioneDTO salvaSimulazione(SimulazioneDTO simulazioneDTO) {
        Simulazione simulazione = simulazioneMapper.toEntity(simulazioneDTO);
        Simulazione savedSimulazione = simulazioneRepository.save(simulazione);
        return simulazioneMapper.toDTO(savedSimulazione);
    }

    @Override
    public List<SimulazioneDTO> getAllSimulazioni() {
        List<Simulazione> simulazioni = simulazioneRepository.findAll();
        return simulazioni.stream()
                .map(simulazioneMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SimulazioneDTO findById(Integer id) {
        Simulazione simulazione = simulazioneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Simulazione non trovata con ID: " + id));
        return simulazioneMapper.toDTO(simulazione);
    }

    @Override
    public void deleteSimulazione(Integer id) {
        if (!simulazioneRepository.existsById(id)) {
            throw new EntityNotFoundException("Simulazione non trovata con ID: " + id);
        }
        simulazioneRepository.deleteById(id);
    }
}