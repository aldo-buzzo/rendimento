package com.example.rendimento.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.rendimento.constants.RendimentoConstants;
import com.example.rendimento.context.UserContext;
import com.example.rendimento.dto.ElaborazioneRisultatoDTO;
import com.example.rendimento.dto.RisultatoRendimentoAdvancedDTO;
import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.exception.ConflittoModificaException;
import com.example.rendimento.mapper.SimulazioneMapper;
import com.example.rendimento.model.ProfiloCalcolo;
import com.example.rendimento.model.Simulazione;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.SimulazioneRepository;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.service.SimulazioneService;
import com.example.rendimento.utility.CalcolatoreRendimentoAdvanced;

import jakarta.persistence.EntityNotFoundException;

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
         * Metodo helper per recuperare il profilo predefinito da un titolo.
         * Questo metodo centralizza la logica di recupero del profilo predefinito
         * per evitare duplicazione di codice.
         *
         * @param titolo il titolo da cui recuperare l'ID utente
         * @return il profilo predefinito dell'utente o null se non disponibile
         */
        private ProfiloCalcolo getDefaultProfileFromTitolo(Titolo titolo) {
                ProfiloCalcolo profiloPredefinito = null;
                try {
                        // Ottieni l'ID dell'utente dal titolo
                        Integer utenteId = titolo.getUtente() != null ? titolo.getUtente().getIdUtente() : null;
                        if (utenteId != null) {
                                profiloPredefinito = UserContext.getDefaultProfile(utenteId);
                        }
                } catch (Exception e) {
                        log.warn("Impossibile ottenere il profilo predefinito: {}", e.getMessage());
                        // Continua con profiloPredefinito = null
                }
                return profiloPredefinito;
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

                // Ottieni il profilo predefinito dell'utente corrente
                ProfiloCalcolo profiloPredefinito = getDefaultProfileFromTitolo(titolo);

                // Crea una lista con il profilo predefinito
                List<ProfiloCalcolo> profili = new ArrayList<>();
                profili.add(profiloPredefinito);

                return calcolaRendimentoAdvanced(titolo, prezzoAcquisto, importo, LocalDate.now(), profili);
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
                simulazioneDTO.setRendimentoSenzaCosti(rendimentoSenzaCosti);
                simulazioneDTO.setRendimentoTassato(tasso.multiply(new BigDecimal("0.875"))
                                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                simulazioneDTO.setRendimentoConCommissioni(risultato.getRendimentoConCommissioni());
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
                simulazioneDTO.setRendimentoConCommissioni(risultato.getRendimentoConCommissioni());

                // Per retrocompatibilità, utilizziamo il rendimento con bollo annuale come
                // default
                simulazioneDTO.setRendimentoConBollo(risultato.getRendimentoConCommissioniEBolloAnnuale());
                simulazioneDTO.setPeriodicitaBollo("ANNUALE"); // Default

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

                // Copia le liste di rendimenti e valori finali per tutti i profili
                if (risultato.getRendimentiPerProfili() != null) {
                        simulazioneDTO.setRendimentiPerProfili(risultato.getRendimentiPerProfili());
                }

                if (risultato.getValoriFinaliPerProfili() != null) {
                        simulazioneDTO.setValoriFinaliPerProfili(risultato.getValoriFinaliPerProfili());
                }

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

                // Ottieni il profilo predefinito dell'utente corrente
                ProfiloCalcolo profiloPredefinito = getDefaultProfileFromTitolo(titolo);

                // Crea una lista con il profilo predefinito
                List<ProfiloCalcolo> profili = new ArrayList<>();
                profili.add(profiloPredefinito);

                RisultatoRendimentoAdvancedDTO risultatoAdvanced = calcolaRendimentoAdvanced(
                                titolo, prezzoAcquisto, importo, dataAcquisto, profili);

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

                // Ottieni il profilo predefinito dell'utente corrente
                ProfiloCalcolo profiloPredefinito = getDefaultProfileFromTitolo(titolo);

                // Crea una lista con il profilo predefinito
                List<ProfiloCalcolo> profili = new ArrayList<>();
                profili.add(profiloPredefinito);

                return calcolaRendimentoAdvanced(
                                titolo,
                                simulazione.getPrezzoAcquisto(),
                                new BigDecimal("10000"), // Importo fisso di 10.000 euro
                                simulazione.getDataAcquisto(),
                                profili);
        }

        // Il metodo calcolaRendimentoAdvanced senza parametro profilo è stato rimosso
        // Tutte le chiamate a questo metodo devono ora utilizzare il metodo con
        // parametro profilo
        // ottenendo il profilo di default tramite UserContext.getDefaultProfile()

        @Override
        public RisultatoRendimentoAdvancedDTO calcolaRendimentoAdvanced(
                        Titolo titolo,
                        BigDecimal prezzoAcquistoPercentuale,
                        BigDecimal nominale,
                        LocalDate dataAcquisto,
                        List<ProfiloCalcolo> profili) {
                // Delega il calcolo alla classe utility CalcolatoreRendimentoAdvanced
                return CalcolatoreRendimentoAdvanced.calcolaRendimentoAdvanced(
                                titolo,
                                prezzoAcquistoPercentuale,
                                nominale,
                                dataAcquisto,
                                profili);
        }

        @Override
        public RisultatoRendimentoAdvancedDTO getCalcoloDettagliato(Integer id, Integer utenteId) {
                log.info("Recupero dati dettagliati di calcolo per simulazione ID: {}, utente ID: {}", id, utenteId);

                // Recupera la simulazione
                SimulazioneDTO simulazione = findById(id);
                if (simulazione == null) {
                        throw new EntityNotFoundException("Simulazione non trovata con ID: " + id);
                }

                // Verifica che la simulazione appartenga all'utente corrente
                if (simulazione.getTitolo() != null && simulazione.getTitolo().getUtenteId() != null &&
                                utenteId != null && !simulazione.getTitolo().getUtenteId().equals(utenteId)) {
                        throw new EntityNotFoundException("Simulazione non autorizzata con ID: " + id);
                }

                // Recupera il titolo associato
                Titolo titolo = titoloRepository.findById(simulazione.getIdTitolo())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Titolo non trovato con ID: " + simulazione.getIdTitolo()));

                // Recupera tutti i profili dell'utente
                List<ProfiloCalcolo> profiliUtente = UserContext.getProfiles(utenteId);
                log.info("Recuperati {} profili per l'utente ID: {}", profiliUtente.size(), utenteId);

                // Calcola i rendimenti dettagliati utilizzando tutti i profili dell'utente
                RisultatoRendimentoAdvancedDTO risultato = calcolaRendimentoAdvanced(
                                titolo,
                                simulazione.getPrezzoAcquisto(),
                                simulazione.getNominale() != null ? simulazione.getNominale() : new BigDecimal("10000"),
                                simulazione.getDataAcquisto(),
                                profiliUtente);

                log.info("Calcolo dettagliato completato per simulazione ID: {}", id);
                return risultato;
        }

        @Override
        public List<SimulazioneDTO> getSimulazioniByUtenteId(Integer utenteId, boolean latest) {
                log.info("Recupero simulazioni per utente ID: {} (latest: {})", utenteId, latest);

                // Inizia il conteggio del tempo
                long startTime = System.currentTimeMillis();

                // Recupera tutti i profili dell'utente una sola volta
                List<ProfiloCalcolo> profiliUtente = UserContext.getProfiles(utenteId);
                log.info("Recuperati {} profili per l'utente ID: {}", profiliUtente.size(), utenteId);

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

                // Misura il tempo per il recupero delle simulazioni dal database
                long dbQueryTime = System.currentTimeMillis();
                double dbQueryTimeInSeconds = (dbQueryTime - startTime) / 1000.0;
                log.info("Tempo per recupero simulazioni dal database: {} secondi", dbQueryTimeInSeconds);

                // Converti le simulazioni in DTO
                List<SimulazioneDTO> simulazioniDTO = simulazioni.stream()
                                .map(simulazioneMapper::toDTO)
                                .collect(Collectors.toList());

                // Per ogni simulazione, calcola i valori finali utilizzando
                // calcolaRendimentoAdvanced
                for (SimulazioneDTO simulazioneDTO : simulazioniDTO) {
                        if ("IT0005640666".equals(simulazioneDTO.getTitolo().getCodiceIsin())) {
                                log.debug("Calcolo rendimento per simulazione con ISIN :"
                                                + simulazioneDTO.getTitolo().getCodiceIsin());
                        }
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

                        // Calcola i valori finali utilizzando calcolaRendimentoAdvanced con tutti i
                        // profili dell'utente
                        RisultatoRendimentoAdvancedDTO risultato = calcolaRendimentoAdvanced(
                                        titolo,
                                        simulazioneDTO.getPrezzoAcquisto(),
                                        simulazioneDTO.getNominale(),
                                        simulazioneDTO.getDataAcquisto(),
                                        profiliUtente);

                        // Imposta i valori finali nel DTO (questi sono i valori per il profilo
                        // predefinito)
                        simulazioneDTO.setValoreBolloAnnualePlusvalenzaNonEsente(
                                        risultato.getValoreBolloAnnualePlusvalenzaNonEsente());
                        simulazioneDTO.setValoreBolloMensilePlusvalenzaNonEsente(
                                        risultato.getValoreBolloMensilePlusvalenzaNonEsente());
                        simulazioneDTO.setValoreBolloAnnualePlusvalenzaEsente(
                                        risultato.getValoreBolloAnnualePlusvalenzaEsente());
                        simulazioneDTO.setValoreBolloMensilePlusvalenzaEsente(
                                        risultato.getValoreBolloMensilePlusvalenzaEsente());

                        // Popola le liste di rendimenti e valori finali per tutti i profili
                        if (risultato.getRendimentiPerProfili() != null) {
                                simulazioneDTO.setRendimentiPerProfili(risultato.getRendimentiPerProfili());
                        }

                        if (risultato.getValoriFinaliPerProfili() != null) {
                                simulazioneDTO.setValoriFinaliPerProfili(risultato.getValoriFinaliPerProfili());
                        }
                }

                return simulazioniDTO;
        }

        // Metodi mancanti da aggiungere a SimulazioneServiceImpl.java

        @Override
        public List<SimulazioneDTO> getSimulazioniByUtenteIdOrderByScadenzaAsc(Integer utenteId, boolean latest) {
                log.info("Recupero simulazioni per utente ID: {} (latest: {}) ordinate per scadenza", utenteId, latest);

                // Recupera le simulazioni utilizzando il metodo esistente
                List<SimulazioneDTO> simulazioni = getSimulazioniByUtenteId(utenteId, latest);

                // Ordina le simulazioni per data di scadenza crescente
                return simulazioni.stream()
                                .sorted(Comparator.comparing(s -> s.getTitolo().getDataScadenza(),
                                                Comparator.nullsLast(Comparator.naturalOrder())))
                                .collect(Collectors.toList());
        }

        @Override
        public ElaborazioneRisultatoDTO elaboraSimulazionePerTitolo(Titolo titolo, BigDecimal prezzo,
                        LocalDate dataPrezzo) {
                log.info("Elaborazione simulazione per titolo: {}, prezzo: {}, data: {}",
                                titolo.getCodiceIsin(), prezzo, dataPrezzo);

                // Se la data del prezzo non è specificata, usa la data corrente
                LocalDate dataEffettiva = dataPrezzo != null ? dataPrezzo : LocalDate.now();

                // Ottieni il profilo predefinito dell'utente
                ProfiloCalcolo profiloPredefinito = getDefaultProfileFromTitolo(titolo);

                // Crea una lista con il profilo predefinito
                List<ProfiloCalcolo> profili = new ArrayList<>();
                profili.add(profiloPredefinito);

                // Calcola il rendimento avanzato
                BigDecimal importoNominale = new BigDecimal("10000"); // Importo fisso di 10.000 euro
                RisultatoRendimentoAdvancedDTO risultato = calcolaRendimentoAdvanced(
                                titolo, prezzo, importoNominale, dataEffettiva, profili);

                // Crea una simulazione temporanea per il titolo
                SimulazioneDTO simulazioneDTO = convertToSimulazioneDTO(
                                risultato, titolo.getIdTitolo(), dataEffettiva, prezzo, importoNominale);

                // Crea l'oggetto di risposta utilizzando il costruttore con parametri
                return new ElaborazioneRisultatoDTO(simulazioneDTO, risultato);
        }

        @Override
        @Transactional
        public SimulazioneDTO aggiornaSimulazione(SimulazioneDTO simulazioneEsistente, Titolo titolo,
                        BigDecimal prezzo, BigDecimal importo, LocalDate dataPrezzo) {
                log.info("Aggiornamento simulazione ID: {} con prezzo: {}, importo: {}, data: {}",
                                simulazioneEsistente.getIdSimulazione(), prezzo, importo, dataPrezzo);

                // Se la data del prezzo non è specificata, usa la data della simulazione
                // esistente
                LocalDate dataEffettiva = dataPrezzo != null ? dataPrezzo : simulazioneEsistente.getDataAcquisto();

                // Ottieni tutti i profili dell'utente
                Integer utenteId = titolo.getUtente() != null ? titolo.getUtente().getIdUtente() : null;
                List<ProfiloCalcolo> profiliUtente = utenteId != null ? UserContext.getProfiles(utenteId)
                                : new ArrayList<>();

                // Calcola il rendimento avanzato
                RisultatoRendimentoAdvancedDTO risultato = calcolaRendimentoAdvanced(
                                titolo, prezzo, importo, dataEffettiva, profiliUtente);

                // Aggiorna i campi della simulazione esistente
                simulazioneEsistente.setPrezzoAcquisto(prezzo);
                simulazioneEsistente.setDataAcquisto(dataEffettiva);
                simulazioneEsistente.setNominale(importo);

                // Aggiorna i valori calcolati
                simulazioneEsistente.setRendimentoSenzaCosti(risultato.getRendimentoSenzaCosti());
                simulazioneEsistente.setRendimentoTassato(risultato.getTasso().multiply(new BigDecimal("0.875"))
                                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                simulazioneEsistente.setRendimentoConCommissioni(risultato.getRendimentoConCommissioni());
                simulazioneEsistente.setImpostaBollo(risultato.getImpostaBollo());
                simulazioneEsistente.setRendimentoNettoBollo(risultato.getGuadagnoNettoBollo()
                                .divide(importo, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100")));
                simulazioneEsistente.setPlusMinusValenza(risultato.getPlusvalenzaNetta());

                // Aggiorna i valori avanzati
                simulazioneEsistente.setPrezzoRiferimentoBollo(risultato.getPrezzoRiferimentoBollo());
                simulazioneEsistente.setCapitaleInvestito(risultato.getCapitaleInvestito());
                simulazioneEsistente.setCapitaleConCommissioni(risultato.getCapitaleConCommissioni());
                simulazioneEsistente.setCedoleNetteAnnue(risultato.getCedoleNetteAnnue());
                simulazioneEsistente.setGuadagnoNettoSenzaCosti(risultato.getGuadagnoNettoSenzaCosti());
                simulazioneEsistente.setRendimentoConBollo(risultato.getRendimentoConCommissioniEBolloAnnuale());
                simulazioneEsistente.setRendimentoPlusvalenzaEsente(risultato.getRendimentoPlusvalenzaEsente());

                // Aggiorna i valori finali
                simulazioneEsistente.setValoreBolloAnnualePlusvalenzaNonEsente(
                                risultato.getValoreBolloAnnualePlusvalenzaNonEsente());
                simulazioneEsistente.setValoreBolloAnnualePlusvalenzaEsente(
                                risultato.getValoreBolloAnnualePlusvalenzaEsente());
                simulazioneEsistente.setValoreBolloMensilePlusvalenzaNonEsente(
                                risultato.getValoreBolloMensilePlusvalenzaNonEsente());
                simulazioneEsistente.setValoreBolloMensilePlusvalenzaEsente(
                                risultato.getValoreBolloMensilePlusvalenzaEsente());

                // Aggiorna le liste di rendimenti e valori finali per tutti i profili
                if (risultato.getRendimentiPerProfili() != null) {
                        simulazioneEsistente.setRendimentiPerProfili(risultato.getRendimentiPerProfili());
                }

                if (risultato.getValoriFinaliPerProfili() != null) {
                        simulazioneEsistente.setValoriFinaliPerProfili(risultato.getValoriFinaliPerProfili());
                }

                // Salva la simulazione aggiornata
                return salvaSimulazione(simulazioneEsistente);
        }

}