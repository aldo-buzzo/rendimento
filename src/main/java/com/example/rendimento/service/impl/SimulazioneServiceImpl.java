package com.example.rendimento.service.impl;

import com.example.rendimento.constants.RendimentoConstants;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.utility.CalcolatoreValoreFinale;
import com.example.rendimento.dto.RisultatoRendimentoAdvancedDTO;
import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.enums.ModalitaCalcoloBollo;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.exception.ConflittoModificaException;
import com.example.rendimento.mapper.SimulazioneMapper;
import com.example.rendimento.model.Simulazione;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.SimulazioneRepository;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.service.SimulazioneService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        private static final Logger log = LoggerFactory.getLogger(SimulazioneServiceImpl.class);

        private final SimulazioneRepository simulazioneRepository;
        private final TitoloRepository titoloRepository;
        private final SimulazioneMapper simulazioneMapper;

        /**
         * Costruttore con parametri per l'iniezione delle dipendenze.
         *
         * @param simulazioneRepository repository per le operazioni CRUD sulle
         *                              simulazioni
         * @param titoloRepository      repository per le operazioni CRUD sui titoli
         * @param simulazioneMapper     mapper per la conversione tra entità e DTO
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
         * Questo metodo è utilizzato sia da calcolaRendimento che da
         * calcolaESalvaSimulazione.
         *
         * @param titolo         il titolo per cui calcolare il rendimento
         * @param prezzoAcquisto il prezzo di acquisto
         * @param importo        l'importo nominale
         * @param dataAcquisto   la data di acquisto
         * @param modalitaBollo  la modalità di calcolo del bollo
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
                        throw new IllegalArgumentException(
                                        "La data di scadenza deve essere successiva alla data di acquisto");
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
                BigDecimal tassoDecimale = titolo.getTassoNominale().divide(new BigDecimal("100"), 8,
                                RoundingMode.HALF_UP);
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

                // Calcolo tasso netto commissioni ((guadagno netto commissioni / importo
                // pagato) * 360/giorni * 100)
                BigDecimal tassoNettoCommissioni = guadagnoNettoCommissioni
                                .divide(importoPagato, 8, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("360"))
                                .divide(new BigDecimal(giorniAllaScadenza), 8, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(4, RoundingMode.HALF_UP);

                // Calcolo tasso netto bollo ((guadagno netto bollo / importo pagato) *
                // 360/giorni * 100)
                BigDecimal tassoNettoBollo = guadagnoNettoBollo.divide(importoPagato, 8, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("360"))
                                .divide(new BigDecimal(giorniAllaScadenza), 8, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(4, RoundingMode.HALF_UP);

                // Calcolo rendimento netto bollo non annualizzato (guadagno netto bollo /
                // importo * 100)
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
                                rendimentoNettoBollo);
        }

        @Override
        public RisultatoSimulazioneDTO calcolaRendimento(Integer idTitolo, BigDecimal prezzoAcquisto,
                        BigDecimal importo) {
                // Validazione input
                if (idTitolo == null || prezzoAcquisto == null || importo == null) {
                        throw new IllegalArgumentException("Tutti i parametri devono essere valorizzati");
                }

                // Recupero titolo
                Titolo titolo = titoloRepository.findById(idTitolo)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Titolo non trovato con ID: " + idTitolo));

                // Usa il metodo avanzato per calcolare il rendimento
                // Nota: calcolaRendimentoAdvanced calcola sia il bollo mensile che annuale
                // e utilizza il bollo mensile come default
                // Restituisce direttamente RisultatoRendimentoAdvancedDTO per mantenere tutti i
                // campi avanzati
                // inclusi rendimentoConCommissioniEBolloAnnuale e bolloTotaleAnnuale
                return calcolaRendimentoAdvanced(titolo, prezzoAcquisto, importo, LocalDate.now());
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

        /**
         * Converte un RisultatoRendimentoAdvancedDTO in un SimulazioneDTO
         * 
         * @param risultato           il risultato del calcolo avanzato
         * @param idTitolo            l'ID del titolo
         * @param dataAcquisto        la data di acquisto
         * @param prezzoAcquisto      il prezzo di acquisto
         * @param commissioniAcquisto le commissioni di acquisto
         * @return un SimulazioneDTO con tutti i campi impostati
         */
        /**
         * Converte un RisultatoRendimentoAdvancedDTO in un SimulazioneDTO
         * 
         * @param risultato      il risultato del calcolo avanzato
         * @param idTitolo       l'ID del titolo
         * @param dataAcquisto   la data di acquisto
         * @param prezzoAcquisto il prezzo di acquisto
         * @param importo        l'importo nominale
         * @return un SimulazioneDTO con tutti i campi impostati
         */
        private SimulazioneDTO convertToSimulazioneDTO(
                        RisultatoRendimentoAdvancedDTO risultato,
                        Integer idTitolo,
                        LocalDate dataAcquisto,
                        BigDecimal prezzoAcquisto,
                        BigDecimal importo) {

                // Verifica che i campi essenziali non siano null
                if (risultato == null) {
                        throw new IllegalArgumentException("Il risultato non può essere null");
                }

                // Verifica e imposta valori di default per i campi che potrebbero essere null
                BigDecimal tasso = risultato.getTasso();
                if (tasso == null) {
                        tasso = BigDecimal.ZERO;
                        risultato.setTasso(tasso);
                }

                BigDecimal tassoNettoCommissioni = risultato.getTassoNettoCommissioni();
                if (tassoNettoCommissioni == null) {
                        tassoNettoCommissioni = BigDecimal.ZERO;
                        risultato.setTassoNettoCommissioni(tassoNettoCommissioni);
                }

                BigDecimal guadagnoNettoBollo = risultato.getGuadagnoNettoBollo();
                if (guadagnoNettoBollo == null) {
                        guadagnoNettoBollo = BigDecimal.ZERO;
                        risultato.setGuadagnoNettoBollo(guadagnoNettoBollo);
                }

                BigDecimal rendimentoSenzaCosti = risultato.getRendimentoSenzaCosti();
                if (rendimentoSenzaCosti == null) {
                        // Calcola il rendimento senza costi se è null
                        if (risultato.getGuadagnoNettoSenzaCosti() != null && risultato.getCapitaleInvestito() != null
                                        && !risultato.getCapitaleInvestito().equals(BigDecimal.ZERO)) {
                                BigDecimal giorni = BigDecimal.valueOf(ChronoUnit.DAYS.between(dataAcquisto,
                                                risultato.getImportoScadenza() != null ? dataAcquisto.plusYears(1)
                                                                : dataAcquisto.plusYears(1)));
                                BigDecimal fattoreAnnualizzazione = RendimentoConstants.TIME_DAYS_IN_YEAR
                                                .divide(giorni, 10, RoundingMode.HALF_UP);

                                rendimentoSenzaCosti = risultato.getGuadagnoNettoSenzaCosti()
                                                .divide(risultato.getCapitaleInvestito(), 10, RoundingMode.HALF_UP)
                                                .multiply(fattoreAnnualizzazione);
                                risultato.setRendimentoSenzaCosti(rendimentoSenzaCosti);
                        } else {
                                rendimentoSenzaCosti = BigDecimal.ZERO;
                                risultato.setRendimentoSenzaCosti(rendimentoSenzaCosti);
                        }
                }

                SimulazioneDTO simulazioneDTO = new SimulazioneDTO();
                simulazioneDTO.setIdTitolo(idTitolo);
                simulazioneDTO.setDataAcquisto(dataAcquisto);
                simulazioneDTO.setPrezzoAcquisto(prezzoAcquisto);
                // Usa il tasso di commissione calcolato da calcolaRendimentoAdvanced
                simulazioneDTO.setCommissioniAcquisto(risultato.getCommissionRate());

                // Imposta i valori calcolati di base
                simulazioneDTO.setRendimentoLordo(tasso.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                simulazioneDTO.setRendimentoTassato(tasso.multiply(new BigDecimal("0.875"))
                                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                simulazioneDTO.setRendimentoNettoCedole(tassoNettoCommissioni
                                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                simulazioneDTO.setImpostaBollo(risultato.getImpostaBollo());

                // Calcola rendimentoNettoBollo in modo sicuro
                if (importo != null && !importo.equals(BigDecimal.ZERO)) {
                        simulazioneDTO.setRendimentoNettoBollo(guadagnoNettoBollo
                                        .divide(importo, 4, RoundingMode.HALF_UP)
                                        .multiply(new BigDecimal("100")));
                } else {
                        simulazioneDTO.setRendimentoNettoBollo(BigDecimal.ZERO);
                }

                simulazioneDTO.setPlusMinusValenza(risultato.getPlusvalenzaNetta());

                // Imposta i valori avanzati
                simulazioneDTO.setNominale(risultato.getNominale());
                simulazioneDTO.setPrezzoRiferimentoBollo(risultato.getPrezzoRiferimentoBollo());
                simulazioneDTO.setCapitaleInvestito(risultato.getCapitaleInvestito());
                simulazioneDTO.setCapitaleConCommissioni(risultato.getCapitaleConCommissioni());
                simulazioneDTO.setCedoleNetteAnnue(risultato.getCedoleNetteAnnue());
                simulazioneDTO.setGuadagnoNettoSenzaCosti(risultato.getGuadagnoNettoSenzaCosti());
                simulazioneDTO.setRendimentoSenzaCosti(rendimentoSenzaCosti);
                simulazioneDTO.setRendimentoConCommissioni(risultato.getRendimentoConCommissioni());
                simulazioneDTO.setRendimentoConBolloMensile(risultato.getRendimentoConCommissioniEBolloMensile());
                simulazioneDTO.setRendimentoConBolloAnnuale(risultato.getRendimentoConCommissioniEBolloAnnuale());
                simulazioneDTO.setBolloTotaleMensile(risultato.getBolloTotaleMensile());
                simulazioneDTO.setBolloTotaleAnnuale(risultato.getBolloTotaleAnnuale());
                simulazioneDTO.setRendimentoPlusvalenzaEsente(risultato.getRendimentoPlusvalenzaEsente());

                // Copia i valori finali
                simulazioneDTO.setValoreBolloAnnualePlusvalenzaNonEsente(
                                risultato.getValoreBolloAnnualePlusvalenzaNonEsente());
                simulazioneDTO.setValoreBolloAnnualePlusvalenzaEsente(
                                risultato.getValoreBolloAnnualePlusvalenzaEsente());
                simulazioneDTO.setValoreBolloMensilePlusvalenzaNonEsente(
                                risultato.getValoreBolloMensilePlusvalenzaNonEsente());
                simulazioneDTO.setValoreBolloMensilePlusvalenzaEsente(
                                risultato.getValoreBolloMensilePlusvalenzaEsente());

                // I campi aggiuntivi da RisultatoSimulazioneDTO non possono essere copiati
                // direttamente
                // perché non esistono i setter corrispondenti in SimulazioneDTO
                // Questi campi sono già stati utilizzati per calcolare i valori in
                // SimulazioneDTO

                return simulazioneDTO;
        }

        @Override
        @Transactional
        public SimulazioneDTO calcolaESalvaSimulazione(Integer idTitolo, BigDecimal prezzoAcquisto,
                        BigDecimal importo, LocalDate dataAcquisto) {
                // Recupera il titolo
                Titolo titolo = titoloRepository.findById(idTitolo)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Titolo non trovato con ID: " + idTitolo));

                // Usa il metodo avanzato per calcolare il rendimento
                // Nota: calcolaRendimentoAdvanced calcola sia il bollo mensile che annuale
                // e utilizza il bollo mensile come default
                RisultatoRendimentoAdvancedDTO risultatoAdvanced = calcolaRendimentoAdvanced(
                                titolo, prezzoAcquisto, importo, dataAcquisto);

                // Converti il risultato in SimulazioneDTO
                SimulazioneDTO simulazioneDTO = convertToSimulazioneDTO(
                                risultatoAdvanced, idTitolo, dataAcquisto, prezzoAcquisto, importo);

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
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Simulazione non trovata con ID: " + id));
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
                        throw new EntityNotFoundException(
                                        "Nessuna simulazione trovata per il titolo con ID: " + idTitolo);
                }

                // Restituisce la simulazione più recente
                return simulazioneMapper.toDTO(simulazioni.get(0));
        }

        @Override
        public List<SimulazioneDTO> findByTitoloIdAndDataAcquisto(Integer idTitolo, LocalDate dataAcquisto) {
                // Recupera tutte le simulazioni per il titolo e la data specificati
                List<Simulazione> simulazioni = simulazioneRepository.findByTitolo_IdTitoloAndDataAcquisto(idTitolo,
                                dataAcquisto);

                // Converte le entità in DTO e restituisce la lista
                return simulazioni.stream()
                                .map(simulazioneMapper::toDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public RisultatoSimulazioneDTO ricalcolaValoriSimulazione(SimulazioneDTO simulazione) {
                // Recupera il titolo
                Titolo titolo = titoloRepository.findById(simulazione.getIdTitolo())
                                .orElseThrow(
                                                () -> new EntityNotFoundException("Titolo non trovato con ID: "
                                                                + simulazione.getIdTitolo()));

                // Usa il metodo avanzato per ricalcolare tutti i valori
                return calcolaRendimentoAdvanced(
                                titolo,
                                simulazione.getPrezzoAcquisto(),
                                new BigDecimal("10000"), // Importo fisso di 10.000 euro
                                simulazione.getDataAcquisto());
        }

        public RisultatoRendimentoAdvancedDTO calcolaRendimentoAdvanced(
                        Titolo titolo,
                        BigDecimal prezzoAcquistoPercentuale,
                        BigDecimal nominale,
                        LocalDate dataAcquisto) {

                RisultatoRendimentoAdvancedDTO dto = new RisultatoRendimentoAdvancedDTO();
                System.out.println("DEBUG: Titolo con ISIN vuoto rilevato." + titolo.getCodiceIsin());
                if ("IT0005640666".equals(titolo.getCodiceIsin())) {
                        System.out.println("DEBUG: Titolo con ISIN vuoto rilevato." + titolo.getCodiceIsin());
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
                // 6. COMMISSIONI (ONE-SHOT)
                // ===============================
                // Imposta il tasso di commissione
                BigDecimal commissionRate = RendimentoConstants.COMMISSION_DEFAULT_RATE;
                dto.setCommissionRate(commissionRate);

                BigDecimal commissioni = capitaleInvestito
                                .multiply(commissionRate)
                                .setScale(8, RoundingMode.HALF_UP);
                dto.setCommissioni(commissioni);

                BigDecimal guadagnoConCommissioni = guadagnoNettoSenzaCosti.subtract(commissioni);
                dto.setGuadagnoNettoCommissioni(guadagnoConCommissioni);

                // Calcolo del guadagno con plusvalenza esente e commissioni
                BigDecimal guadagnoConPlusvalenzaEsenteECommissioni = guadagnoConPlusvalenzaEsente.subtract(commissioni)
                                .setScale(8, RoundingMode.HALF_UP);

                BigDecimal capitaleConCommissioni = capitaleInvestito.add(commissioni);
                dto.setCapitaleConCommissioni(capitaleConCommissioni);

                // ===============================
                // 7. BOLLO
                // ===============================
                // Bollo annuale (una volta se scadenza > 31/12)
                // Se il titolo ha scadenza entro l'anno corrente, il bollo annuale è zero
                // e il rendimento_con_bollo_annuale sarà uguale al rendimento con le sole
                // commissioni
                BigDecimal bolloAnnuale = BigDecimal.ZERO;
                LocalDate fineAnno = LocalDate.of(dataAcquisto.getYear(), 12, 31);
                if (dataScadenza.isAfter(fineAnno)) {
                        bolloAnnuale = nominale.multiply(RendimentoConstants.TAX_BOLLO_RATE)
                                        .setScale(8, RoundingMode.HALF_UP);
                }

                // Bollo mensile proporzionale ai mesi residui
                long mesiAllaScadenza = ChronoUnit.MONTHS.between(dataAcquisto, dataScadenza);
                BigDecimal bolloMensile = BigDecimal.ZERO;
                if (mesiAllaScadenza > 0) {
                        bolloMensile = nominale.multiply(RendimentoConstants.TAX_BOLLO_RATE)
                                        .multiply(BigDecimal.valueOf(mesiAllaScadenza))
                                        .divide(RendimentoConstants.TIME_MONTHS_IN_YEAR, 8, RoundingMode.HALF_UP);
                }

                dto.setBolloTotaleAnnuale(bolloAnnuale);
                dto.setBolloTotaleMensile(bolloMensile);

                BigDecimal guadagnoConBolloAnnuale = guadagnoConCommissioni.subtract(bolloAnnuale);
                BigDecimal guadagnoConBolloMensile = guadagnoConCommissioni.subtract(bolloMensile);
                dto.setGuadagnoNettoBollo(guadagnoConBolloMensile); // Usiamo il bollo mensile come default

                // Calcolo del guadagno con plusvalenza esente, commissioni e bollo annuale
                BigDecimal guadagnoConPlusvalenzaEsenteCommissioniEBollo = guadagnoConPlusvalenzaEsenteECommissioni
                                .subtract(bolloAnnuale)
                                .setScale(8, RoundingMode.HALF_UP);

                // ===============================
                // 8. RENDIMENTI ANNUALIZZATI
                // ===============================
                // Calcolo tasso di rendimento (guadagno totale / capitale investito * fattore
                // annualizzazione)
                BigDecimal tasso = guadagnoNettoSenzaCosti
                                .divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                .multiply(fattoreAnnualizzazione)
                                .multiply(RendimentoConstants.PERCENT_100)
                                .setScale(4, RoundingMode.HALF_UP);
                dto.setTasso(tasso);

                // Calcolo tasso netto commissioni
                BigDecimal tassoNettoCommissioni = guadagnoConCommissioni
                                .divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                .multiply(fattoreAnnualizzazione)
                                .multiply(RendimentoConstants.PERCENT_100)
                                .setScale(4, RoundingMode.HALF_UP);
                dto.setTassoNettoCommissioni(tassoNettoCommissioni);

                // Calcolo tasso netto bollo
                BigDecimal tassoNettoBollo = guadagnoConBolloMensile
                                .divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                .multiply(fattoreAnnualizzazione)
                                .multiply(RendimentoConstants.PERCENT_100)
                                .setScale(4, RoundingMode.HALF_UP);
                dto.setTassoNettoBollo(tassoNettoBollo);

                // Rendimenti in formato decimale (non percentuale)
                dto.setRendimentoSenzaCosti(
                                guadagnoNettoSenzaCosti.divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                                .multiply(fattoreAnnualizzazione));

                dto.setRendimentoConCommissioni(
                                guadagnoConCommissioni.divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                                .multiply(fattoreAnnualizzazione));

                dto.setRendimentoConCommissioniEBolloAnnuale(
                                guadagnoConBolloAnnuale.divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                                .multiply(fattoreAnnualizzazione));

                dto.setRendimentoConCommissioniEBolloMensile(
                                guadagnoConBolloMensile.divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                                .multiply(fattoreAnnualizzazione));

                // Rendimento con plusvalenza esente (non tassata) - solo per BTP
                if (TipoTitolo.BTP.equals(titolo.getTipoTitolo())) {
                        dto.setRendimentoPlusvalenzaEsente(
                                        guadagnoConPlusvalenzaEsenteCommissioniEBollo
                                                        .divide(capitaleInvestito, 10, RoundingMode.HALF_UP)
                                                        .multiply(fattoreAnnualizzazione));
                } else {
                        dto.setRendimentoPlusvalenzaEsente(null); // Valore null per gli altri tipi di titoli
                }

                // Calcolo rendimento netto bollo non annualizzato
                BigDecimal rendimentoNettoBollo = guadagnoConBolloMensile
                                .divide(nominale, 4, RoundingMode.HALF_UP)
                                .multiply(RendimentoConstants.PERCENT_100)
                                .setScale(4, RoundingMode.HALF_UP);
                dto.setRendimentoNettoBollo(rendimentoNettoBollo);

                // Calcolo importo a scadenza
                BigDecimal importoScadenza = nominale.add(guadagnoConBolloMensile)
                                .setScale(4, RoundingMode.HALF_UP);
                dto.setImportoScadenza(importoScadenza);

                // ===============================
                // 9. CALCOLO VALORI FINALI TEORICI
                // ===============================

                // Calcolo degli anni residui
                BigDecimal anniResidui = giorni.divide(RendimentoConstants.TIME_DAYS_IN_YEAR, 10, RoundingMode.HALF_UP);

                // Crea un'istanza del calcolatore di valori finali
                CalcolatoreValoreFinale calcolatore = new CalcolatoreValoreFinale(
                                giorni,
                                nominale,
                                dto.getInteressiNetti(),
                                anniResidui,
                                dto.getPlusvalenzaNetta(),
                                plusvalenzaEsente,
                                commissioni,
                                bolloAnnuale,
                                bolloMensile,
                                dto.getRendimentoConCommissioniEBolloAnnuale(),
                                dto.getRendimentoConCommissioniEBolloMensile(),
                                dto.getRendimentoPlusvalenzaEsente());

                // 1. Valore finale con bollo annuale e plusvalenza non esente
                BigDecimal valoreBolloAnnualePlusvalenzaNonEsente = calcolatore
                                .getValoreBolloAnnualePlusvalenzaNonEsente();
                dto.setValoreBolloAnnualePlusvalenzaNonEsente(valoreBolloAnnualePlusvalenzaNonEsente);

                // 2. Valore finale con bollo mensile e plusvalenza non esente
                BigDecimal valoreBolloMensilePlusvalenzaNonEsente = calcolatore
                                .getValoreBolloMensilePlusvalenzaNonEsente();
                dto.setValoreBolloMensilePlusvalenzaNonEsente(valoreBolloMensilePlusvalenzaNonEsente);

                // 3. Valori finali con plusvalenza esente (solo per BTP)
                if (TipoTitolo.BTP.equals(titolo.getTipoTitolo()) && dto.getRendimentoPlusvalenzaEsente() != null) {
                        // 3.1 Valore finale con bollo annuale e plusvalenza esente
                        BigDecimal valoreBolloAnnualePlusvalenzaEsente = calcolatore
                                        .getValoreBolloAnnualePlusvalenzaEsente();
                        dto.setValoreBolloAnnualePlusvalenzaEsente(valoreBolloAnnualePlusvalenzaEsente);

                        // 3.2 Valore finale con bollo mensile e plusvalenza esente
                        BigDecimal valoreBolloMensilePlusvalenzaEsente = calcolatore
                                        .getValoreBolloMensilePlusvalenzaEsente();
                        dto.setValoreBolloMensilePlusvalenzaEsente(valoreBolloMensilePlusvalenzaEsente);
                } else {
                        // Se non è un BTP o non ha rendimento con plusvalenza esente, imposta i valori
                        // a null
                        dto.setValoreBolloAnnualePlusvalenzaEsente(null);
                        dto.setValoreBolloMensilePlusvalenzaEsente(null);
                }

                // ===============================
                // 10. CAMPi DI COMPATIBILITÀ / RIEPILOGO
                // ===============================
                dto.setGuadagnoTotale(guadagnoNettoSenzaCosti);
                dto.setImpostaBollo(bolloAnnuale.max(bolloMensile));

                return dto;
        }

        @Override
        public List<SimulazioneDTO> getSimulazioniByUtenteId(Integer utenteId, boolean latest) {
                log.info("Recupero simulazioni per utente ID: {} (latest: {})", utenteId, latest);

                List<Simulazione> simulazioni;
                if (latest) {
                        // Utilizziamo il nuovo metodo che esclude i titoli scaduti
                        LocalDate dataOdierna = LocalDate.now();
                        simulazioni = simulazioneRepository.findLatestByUtenteIdAndNotExpired(utenteId, dataOdierna);
                        log.info("Trovate {} simulazioni più recenti per titoli non scaduti dell'utente ID: {}",
                                        simulazioni.size(),
                                        utenteId);
                } else {
                        simulazioni = simulazioneRepository.findByUtenteId(utenteId);
                        log.info("Trovate {} simulazioni totali per l'utente ID: {}", simulazioni.size(), utenteId);
                }

                // Converti le simulazioni in DTO
                List<SimulazioneDTO> simulazioniDTO = simulazioni.stream()
                                .map(simulazioneMapper::toDTO)
                                .collect(Collectors.toList());

                // Per ogni simulazione, calcola i valori finali utilizzando
                // calcolaRendimentoAdvanced
                for (SimulazioneDTO simulazioneDTO : simulazioniDTO) {
                        // Crea un oggetto Titolo dal TitoloDTO
                        Titolo titolo = new Titolo();
                        titolo.setIdTitolo(simulazioneDTO.getTitolo().getIdTitolo());
                        titolo.setNome(simulazioneDTO.getTitolo().getNome());
                        titolo.setCodiceIsin(simulazioneDTO.getTitolo().getCodiceIsin());
                        titolo.setDataScadenza(simulazioneDTO.getTitolo().getDataScadenza());
                        titolo.setTassoNominale(simulazioneDTO.getTitolo().getTassoNominale());
                        titolo.setPeriodicitaCedole(simulazioneDTO.getTitolo().getPeriodicitaCedole());
                        titolo.setPeriodicitaBollo(simulazioneDTO.getTitolo().getPeriodicitaBollo());
                        titolo.setTipoTitolo(simulazioneDTO.getTitolo().getTipoTitolo());

                        // Calcola i valori finali utilizzando calcolaRendimentoAdvanced
                        RisultatoRendimentoAdvancedDTO risultato = calcolaRendimentoAdvanced(
                                        titolo,
                                        simulazioneDTO.getPrezzoAcquisto(),
                                        simulazioneDTO.getNominale(),
                                        simulazioneDTO.getDataAcquisto());

                        // Imposta i valori finali nel DTO
                        simulazioneDTO.setValoreBolloAnnualePlusvalenzaNonEsente(
                                        risultato.getValoreBolloAnnualePlusvalenzaNonEsente());
                        simulazioneDTO.setValoreBolloMensilePlusvalenzaNonEsente(
                                        risultato.getValoreBolloMensilePlusvalenzaNonEsente());
                        simulazioneDTO.setValoreBolloAnnualePlusvalenzaEsente(
                                        risultato.getValoreBolloAnnualePlusvalenzaEsente());
                        simulazioneDTO.setValoreBolloMensilePlusvalenzaEsente(
                                        risultato.getValoreBolloMensilePlusvalenzaEsente());
                }

                return simulazioniDTO;
        }

        @Override
        public List<SimulazioneDTO> getSimulazioniByUtenteIdOrderByScadenzaAsc(Integer utenteId, boolean latest) {
                log.info("Recupero simulazioni per utente ID: {} (latest: {}) ordinate per data di scadenza crescente",
                                utenteId, latest);

                List<Simulazione> simulazioni;
                if (latest) {
                        // Utilizziamo il nuovo metodo che esclude i titoli scaduti e ordina per data di
                        // scadenza crescente
                        LocalDate dataOdierna = LocalDate.now();
                        simulazioni = simulazioneRepository
                                        .findLatestByUtenteIdAndNotExpiredOrderByScadenzaAsc(utenteId, dataOdierna);
                        log.info("Trovate {} simulazioni più recenti per titoli non scaduti dell'utente ID: {} ordinate per data di scadenza crescente",
                                        simulazioni.size(), utenteId);
                } else {
                        // Per il caso in cui latest è false, dobbiamo ordinare manualmente
                        simulazioni = simulazioneRepository.findByUtenteId(utenteId);
                        // Ordina le simulazioni per data di scadenza crescente
                        simulazioni.sort((s1, s2) -> {
                                LocalDate scadenza1 = s1.getTitolo().getDataScadenza();
                                LocalDate scadenza2 = s2.getTitolo().getDataScadenza();
                                return scadenza1.compareTo(scadenza2);
                        });
                        log.info("Trovate {} simulazioni totali per l'utente ID: {} ordinate per data di scadenza crescente",
                                        simulazioni.size(), utenteId);
                }

                // Converti le simulazioni in DTO
                List<SimulazioneDTO> simulazioniDTO = simulazioni.stream()
                                .map(simulazioneMapper::toDTO)
                                .collect(Collectors.toList());

                // Per ogni simulazione, calcola i valori finali utilizzando
                // calcolaRendimentoAdvanced
                for (SimulazioneDTO simulazioneDTO : simulazioniDTO) {
                        // Crea un oggetto Titolo dal TitoloDTO
                        Titolo titolo = new Titolo();
                        titolo.setIdTitolo(simulazioneDTO.getTitolo().getIdTitolo());
                        titolo.setNome(simulazioneDTO.getTitolo().getNome());
                        titolo.setCodiceIsin(simulazioneDTO.getTitolo().getCodiceIsin());
                        titolo.setDataScadenza(simulazioneDTO.getTitolo().getDataScadenza());
                        titolo.setTassoNominale(simulazioneDTO.getTitolo().getTassoNominale());
                        titolo.setPeriodicitaCedole(simulazioneDTO.getTitolo().getPeriodicitaCedole());
                        titolo.setPeriodicitaBollo(simulazioneDTO.getTitolo().getPeriodicitaBollo());
                        titolo.setTipoTitolo(simulazioneDTO.getTitolo().getTipoTitolo());

                        // Calcola i valori finali utilizzando calcolaRendimentoAdvanced
                        RisultatoRendimentoAdvancedDTO risultato = calcolaRendimentoAdvanced(
                                        titolo,
                                        simulazioneDTO.getPrezzoAcquisto(),
                                        simulazioneDTO.getNominale(),
                                        simulazioneDTO.getDataAcquisto());

                        // Imposta i valori finali nel DTO
                        simulazioneDTO.setValoreBolloAnnualePlusvalenzaNonEsente(
                                        risultato.getValoreBolloAnnualePlusvalenzaNonEsente());
                        simulazioneDTO.setValoreBolloMensilePlusvalenzaNonEsente(
                                        risultato.getValoreBolloMensilePlusvalenzaNonEsente());
                        simulazioneDTO.setValoreBolloAnnualePlusvalenzaEsente(
                                        risultato.getValoreBolloAnnualePlusvalenzaEsente());
                        simulazioneDTO.setValoreBolloMensilePlusvalenzaEsente(
                                        risultato.getValoreBolloMensilePlusvalenzaEsente());
                }

                return simulazioniDTO;
        }

        @Override
        public SimulazioneDTO aggiornaSimulazione(SimulazioneDTO simulazioneEsistente,
                        RisultatoSimulazioneDTO risultato, BigDecimal importo) {
                // Verifica che i parametri non siano null
                if (simulazioneEsistente == null || risultato == null) {
                        throw new IllegalArgumentException(
                                        "La simulazione esistente e il risultato non possono essere null");
                }

                // Converti il risultato in un RisultatoRendimentoAdvancedDTO se necessario
                RisultatoRendimentoAdvancedDTO risultatoAdvanced;
                if (risultato instanceof RisultatoRendimentoAdvancedDTO) {
                        risultatoAdvanced = (RisultatoRendimentoAdvancedDTO) risultato;
                } else {
                        risultatoAdvanced = new RisultatoRendimentoAdvancedDTO(risultato);
                }

                // Utilizza convertToSimulazioneDTO per aggiornare i campi della simulazione
                SimulazioneDTO simulazioneAggiornata = convertToSimulazioneDTO(
                                risultatoAdvanced,
                                simulazioneEsistente.getIdTitolo(),
                                simulazioneEsistente.getDataAcquisto(),
                                simulazioneEsistente.getPrezzoAcquisto(),
                                importo);

                // Mantieni l'ID e la versione della simulazione esistente
                simulazioneAggiornata.setIdSimulazione(simulazioneEsistente.getIdSimulazione());
                simulazioneAggiornata.setVersion(simulazioneEsistente.getVersion());

                // Salva la simulazione aggiornata
                return salvaSimulazione(simulazioneAggiornata);
        }

        /**
         * Metodo di convenienza che accetta direttamente un
         * RisultatoRendimentoAdvancedDTO.
         * Questo evita la conversione inutile quando si ha già un
         * RisultatoRendimentoAdvancedDTO.
         * 
         * @param simulazioneEsistente la simulazione esistente da aggiornare
         * @param risultatoAdvanced    il risultato dettagliato del calcolo di
         *                             rendimento
         * @param importo              l'importo dell'investimento
         * @return la simulazione aggiornata e salvata
         */

        public SimulazioneDTO aggiornaSimulazione(SimulazioneDTO simulazioneEsistente,
                        RisultatoRendimentoAdvancedDTO risultatoAdvanced, BigDecimal importo) {
                // Verifica che i parametri non siano null
                if (simulazioneEsistente == null || risultatoAdvanced == null) {
                        throw new IllegalArgumentException(
                                        "La simulazione esistente e il risultato non possono essere null");
                }

                // Utilizza convertToSimulazioneDTO per aggiornare i campi della simulazione
                SimulazioneDTO simulazioneAggiornata = convertToSimulazioneDTO(
                                risultatoAdvanced,
                                simulazioneEsistente.getIdTitolo(),
                                simulazioneEsistente.getDataAcquisto(),
                                simulazioneEsistente.getPrezzoAcquisto(),
                                importo);

                // Mantieni l'ID e la versione della simulazione esistente
                simulazioneAggiornata.setIdSimulazione(simulazioneEsistente.getIdSimulazione());
                simulazioneAggiornata.setVersion(simulazioneEsistente.getVersion());

                // Salva la simulazione aggiornata
                return salvaSimulazione(simulazioneAggiornata);
        }
}
