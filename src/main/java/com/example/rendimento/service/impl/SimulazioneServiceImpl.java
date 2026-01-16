package com.example.rendimento.service.impl;

import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.enums.ModalitaCalcoloBollo;
import com.example.rendimento.exception.ConflittoModificaException;
import com.example.rendimento.mapper.SimulazioneMapper;
import com.example.rendimento.model.Simulazione;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.SimulazioneRepository;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.service.SimulazioneService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

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

    /**
     * Metodo privato che implementa la logica di calcolo del rendimento.
     * Questo metodo è utilizzato sia da calcolaRendimento che da calcolaESalvaSimulazione.
     *
     * @param titolo il titolo per cui calcolare il rendimento
     * @param prezzoAcquisto il prezzo di acquisto
     * @param importo l'importo nominale
     * @param dataAcquisto la data di acquisto
     * @param modalitaBollo la modalità di calcolo del bollo
     * @return il risultato del calcolo del rendimento
     */
    private RisultatoSimulazioneDTO calcolaRendimentoInternal(
            Titolo titolo, 
            BigDecimal prezzoAcquisto, 
            BigDecimal importo, 
            LocalDate dataAcquisto,
            ModalitaCalcoloBollo modalitaBollo) {
        
        // Calcolo giorni alla scadenza
        LocalDate dataScadenza = titolo.getDataScadenza();
        long giorniAllaScadenza = ChronoUnit.DAYS.between(dataAcquisto, dataScadenza);
        
        if (giorniAllaScadenza <= 0) {
            throw new IllegalArgumentException("La data di scadenza deve essere successiva alla data di acquisto");
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

        // Calcolo commissioni (importoPagato * 0,9/1000)
        BigDecimal commissioni = importoPagato.multiply(new BigDecimal("0.0009"))
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
            LocalDate fineDellAnno = LocalDate.of(dataAcquisto.getYear(), 12, 31);
            if (dataScadenza.isAfter(fineDellAnno)) {
                impostaBollo = importo.multiply(new BigDecimal("0.002"))
                             .setScale(4, RoundingMode.HALF_UP);
            }
        } else { // MENSILE
            // Calcolo proporzionale per ogni mese mancante alla scadenza
            long mesiAllaScadenza = ChronoUnit.MONTHS.between(dataAcquisto, dataScadenza);
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
        
        // Calcolo tasso netto bollo ((guadagno netto bollo / importo pagato) * 360/giorni * 100)
        BigDecimal tassoNettoBollo = guadagnoNettoBollo.divide(importoPagato, 8, RoundingMode.HALF_UP)
                                   .multiply(new BigDecimal("360"))
                                   .divide(new BigDecimal(giorniAllaScadenza), 8, RoundingMode.HALF_UP)
                                   .multiply(new BigDecimal("100"))
                                   .setScale(4, RoundingMode.HALF_UP);

        // Calcolo rendimento netto bollo non annualizzato (guadagno netto bollo / importo * 100)
        BigDecimal rendimentoNettoBollo = guadagnoNettoBollo.divide(importo, 4, RoundingMode.HALF_UP)
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
            tassoNettoBollo,
            importoScadenza,
            rendimentoNettoBollo
        );
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

        // Usa il metodo interno per calcolare il rendimento
        return calcolaRendimentoInternal(titolo, prezzoAcquisto, importo, LocalDate.now(), modalitaBollo);
    }

    @Override
    @Transactional
    public SimulazioneDTO salvaSimulazione(SimulazioneDTO simulazioneDTO) {
        try {
            Simulazione simulazione = simulazioneMapper.toEntity(simulazioneDTO);
            Simulazione savedSimulazione = simulazioneRepository.save(simulazione);
            return simulazioneMapper.toDTO(savedSimulazione);
        } catch (ObjectOptimisticLockingFailureException e) {
            // Gestione dell'eccezione di concorrenza ottimistica
            throw new ConflittoModificaException(
                "La simulazione è stata modificata da un altro utente. Ricarica e riprova.", e);
        }
    }
    
    @Override
    @Transactional
    public SimulazioneDTO calcolaESalvaSimulazione(Integer idTitolo, BigDecimal prezzoAcquisto, 
                                                BigDecimal importo, LocalDate dataAcquisto,
                                                ModalitaCalcoloBollo modalitaBollo, BigDecimal commissioniAcquisto) {
        // Recupera il titolo
        Titolo titolo = titoloRepository.findById(idTitolo)
            .orElseThrow(() -> new EntityNotFoundException("Titolo non trovato con ID: " + idTitolo));
        
        // Usa il metodo interno per calcolare il rendimento
        RisultatoSimulazioneDTO risultato = calcolaRendimentoInternal(
            titolo, prezzoAcquisto, importo, dataAcquisto, modalitaBollo);
        
        // Crea l'oggetto SimulazioneDTO con i risultati del calcolo
        SimulazioneDTO simulazioneDTO = new SimulazioneDTO();
        simulazioneDTO.setIdTitolo(idTitolo);
        simulazioneDTO.setDataAcquisto(dataAcquisto);
        simulazioneDTO.setPrezzoAcquisto(prezzoAcquisto);
        simulazioneDTO.setCommissioniAcquisto(commissioniAcquisto);
        
        // Imposta i valori calcolati
        simulazioneDTO.setRendimentoLordo(risultato.getTasso().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        simulazioneDTO.setRendimentoTassato(risultato.getTasso().multiply(new BigDecimal("0.875"))
                                          .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        simulazioneDTO.setRendimentoNettoCedole(risultato.getTassoNettoCommissioni()
                                              .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        simulazioneDTO.setImpostaBollo(risultato.getImpostaBollo());
        simulazioneDTO.setRendimentoNettoBollo(risultato.getGuadagnoNettoBollo()
                                             .divide(importo, 4, RoundingMode.HALF_UP)
                                             .multiply(new BigDecimal("100")));
        simulazioneDTO.setPlusMinusValenza(risultato.getPlusvalenzaNetta());
        
        // Salva la simulazione
        return salvaSimulazione(simulazioneDTO);
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
    
    @Override
    public List<SimulazioneDTO> getLatestSimulazioneForEachTitolo() {
        List<Integer> titoloIds = simulazioneRepository.findDistinctTitoloIds();
        List<Simulazione> latestSimulazioni = new ArrayList<>();
        
        for (Integer titoloId : titoloIds) {
            List<Simulazione> simulazioni = simulazioneRepository.findByTitoloIdOrderByDataAcquistoDesc(
                titoloId, PageRequest.of(0, 1));
            if (!simulazioni.isEmpty()) {
                latestSimulazioni.add(simulazioni.get(0));
            }
        }
        
        return latestSimulazioni.stream()
                .map(simulazioneMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SimulazioneDTO> findByTitoloId(Integer idTitolo) {
        List<Simulazione> simulazioni = simulazioneRepository.findByTitolo_IdTitolo(idTitolo);
        return simulazioni.stream()
                .map(simulazioneMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public SimulazioneDTO getLatestSimulazioneByTitoloId(Integer idTitolo) {
        // Verifica se il titolo esiste
        if (!titoloRepository.existsById(idTitolo)) {
            throw new EntityNotFoundException("Titolo non trovato con ID: " + idTitolo);
        }
        
        // Recupera la simulazione più recente per il titolo specificato
        List<Simulazione> simulazioni = simulazioneRepository.findByTitoloIdOrderByDataAcquistoDesc(
            idTitolo, PageRequest.of(0, 1));
            
        if (simulazioni.isEmpty()) {
            throw new EntityNotFoundException("Nessuna simulazione trovata per il titolo con ID: " + idTitolo);
        }
        
        // Restituisce la simulazione più recente
        return simulazioneMapper.toDTO(simulazioni.get(0));
    }
    
    @Override
    public List<SimulazioneDTO> findByTitoloIdAndDataAcquisto(Integer idTitolo, LocalDate dataAcquisto) {
        // Recupera tutte le simulazioni per il titolo e la data specificati
        List<Simulazione> simulazioni = simulazioneRepository.findByTitolo_IdTitoloAndDataAcquisto(idTitolo, dataAcquisto);
        
        // Converte le entità in DTO e restituisce la lista
        return simulazioni.stream()
                .map(simulazioneMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public RisultatoSimulazioneDTO ricalcolaValoriSimulazione(SimulazioneDTO simulazione) {
        // Recupera il titolo
        Titolo titolo = titoloRepository.findById(simulazione.getIdTitolo())
            .orElseThrow(() -> new EntityNotFoundException("Titolo non trovato con ID: " + simulazione.getIdTitolo()));
        
        // Usa la modalità di calcolo del bollo predefinita (ANNUALE)
        ModalitaCalcoloBollo modalitaBollo = ModalitaCalcoloBollo.ANNUALE;
        
        // Usa il metodo interno per ricalcolare tutti i valori
        return calcolaRendimentoInternal(
            titolo, 
            simulazione.getPrezzoAcquisto(), 
            new BigDecimal("10000"), // Importo fisso di 10.000 euro
            simulazione.getDataAcquisto(), 
            modalitaBollo
        );
    }
}
