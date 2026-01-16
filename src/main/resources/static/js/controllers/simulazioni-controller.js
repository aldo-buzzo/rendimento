/**
 * Controller per la gestione delle simulazioni
 */
class SimulazioniController {
    constructor() {
        // Variabili di stato
        this.simulazioni = [];
        this.ultimoRisultatoCalcolo = null;
        this.simulazioniCaricate = false;
        
        // Inizializzazione
        this.init();
    }
    
    /**
     * Inizializza il controller
     */
    init() {
        // Carica le simulazioni dal server
        this.loadSimulazioniFromServer();
        
        // Imposta le date di default
        this.setDefaultDates();
        
        // Aggiungi event listeners
        this.setupEventListeners();
    }
    
    /**
     * Configura gli event listeners
     */
    setupEventListeners() {
        // Determina in quale pagina ci troviamo
        const isIndexPage = !!document.getElementById('simulazione-form');
        const isDetailPage = window.location.href.includes('dettaglio-simulazione.html');
        
        console.log('Configurazione event listeners. Pagina index:', isIndexPage, 'Pagina dettaglio:', isDetailPage);
        
        // Se siamo nella pagina index, configura gli event listener specifici
        if (isIndexPage) {
            // Event listener per il calcolo dei giorni alla scadenza
            const titoloSelect = document.getElementById('titolo-select');
            if (titoloSelect) {
                titoloSelect.addEventListener('change', this.updateGiorniAllaScadenza.bind(this));
            }
            
            const dataAcquistoInput = document.getElementById('data-acquisto');
            if (dataAcquistoInput) {
                dataAcquistoInput.addEventListener('change', this.updateGiorniAllaScadenza.bind(this));
                
                // Inizializza il datepicker e imposta la data di oggi
                DomUtils.initDatepicker(dataAcquistoInput);
                DomUtils.setDatepickerDate(dataAcquistoInput);
                
                // Aggiungi event listener per aggiornare i giorni alla scadenza quando la data cambia
                if (window.jQuery && $.fn && $.fn.datepicker) {
                    $(dataAcquistoInput).on('changeDate', this.updateGiorniAllaScadenza.bind(this));
                }
            }
            
            // Event listener per il pulsante di prezzo corrente
            const prezzoCorrenteBtn = document.getElementById('prezzo-corrente-btn');
            if (prezzoCorrenteBtn) {
                prezzoCorrenteBtn.addEventListener('click', this.getPrezzoCorrente.bind(this));
            }
            
            // Event listener per il pulsante di calcolo rendimenti
            const calcolaRendimentiBtn = document.getElementById('calcola-rendimenti-btn');
            if (calcolaRendimentiBtn) {
                calcolaRendimentiBtn.addEventListener('click', this.calcolaRendimentiTuttiTitoli.bind(this));
            }
            
            // Event listener per il form di simulazione
            const simulazioneForm = document.getElementById('simulazione-form');
            if (simulazioneForm) {
                simulazioneForm.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.createSimulazione();
                });
            }
            
            // Event listener per il pulsante di salvataggio della simulazione
            const salvaSimulazioneBtn = document.getElementById('salva-simulazione-btn');
            if (salvaSimulazioneBtn) {
                salvaSimulazioneBtn.addEventListener('click', this.salvaSimulazione.bind(this));
            }
        }
        
        // Se siamo nella pagina dettaglio, configura gli event listener specifici
        if (isDetailPage) {
            console.log('Configurazione event listeners per la pagina dettaglio');
            // Qui puoi aggiungere event listener specifici per la pagina dettaglio-simulazione.html
            // Ad esempio, per il pulsante "Ricalcola"
            const btnRicalcola = document.getElementById('btn-ricalcola');
            if (btnRicalcola) {
                btnRicalcola.addEventListener('click', () => {
                    console.log('Pulsante Ricalcola cliccato');
                    // Implementa la logica di ricalcolo se necessario
                });
            }
        }
    }
    
    /**
     * Carica le simulazioni dal server
     */
    loadSimulazioniFromServer() {
        // Assicurati che window.titoli esista
        if (!window.titoli) {
            console.log('window.titoli non è ancora definito, inizializzazione come array vuoto');
            window.titoli = [];
        }
        
        Simulazione.load(true)
            .then(data => {
                // Assegna le simulazioni convertite alla variabile di stato
                this.simulazioni = data;
                
                this.simulazioniCaricate = true;
                
                // Aggiorna la vista delle simulazioni
                this.updateSimulazioniTable();
            })
            .catch(error => {
                console.error('Errore nel caricamento delle simulazioni:', error);
                // In caso di errore, se non ci sono già simulazioni caricate, carica i dati di esempio
                if (!this.simulazioniCaricate && this.simulazioni.length === 0) {
                    this.loadSampleData();
                    this.updateSimulazioniTable();
                }
            });
    }
    
    /**
     * Carica dati di esempio
     */
    loadSampleData() {
        // Simulazioni di esempio
        const dataAcquisto1 = new Date();
        dataAcquisto1.setMonth(dataAcquisto1.getMonth() - 6);
        const dataAcquisto2 = new Date();
        dataAcquisto2.setMonth(dataAcquisto2.getMonth() - 3);
        
        this.simulazioni = [
            { 
                id: 1, 
                titoloId: 1, 
                prezzoAcquisto: 98.25,
                dataAcquisto: dataAcquisto1.toISOString().split('T')[0],
                importoNominale: 10000,
                commissioniAcquisto: 0.25,
                rendimentoLordo: 2.75,
                rendimentoTassato: 2.31,
                rendimentoNettoCedole: 2.25,
                impostaBollo: 0.20,
                rendimentoNettoBollo: 2.05,
                plusMinusValenza: 0.50
            },
            { 
                id: 2, 
                titoloId: 2, 
                prezzoAcquisto: 99.10,
                dataAcquisto: dataAcquisto2.toISOString().split('T')[0],
                importoNominale: 20000,
                commissioniAcquisto: 0.20,
                rendimentoLordo: 1.95,
                rendimentoTassato: 1.64,
                rendimentoNettoCedole: 1.60,
                impostaBollo: 0.20,
                rendimentoNettoBollo: 1.40,
                plusMinusValenza: 0.40
            }
        ];
    }
    
    /**
     * Aggiorna la tabella delle simulazioni
     */
    updateSimulazioniTable() {
        const tbody = document.getElementById('simulazioni-list');
        if (!tbody) return;
        
        tbody.innerHTML = '';
        
        // Verifica che window.titoli esista prima di usarlo
        if (!window.titoli || !Array.isArray(window.titoli)) {
            console.error('window.titoli non è definito o non è un array');
            return;
        }
        
        this.simulazioni.forEach(simulazione => {
            // Verifica che simulazione.titoloId esista
            if (!simulazione.titoloId) return;
            
            const titolo = window.titoli.find(t => t && t.id == simulazione.titoloId);
            if (!titolo) return;
            
            const row = document.createElement('tr');
            
            // Aggiungi attributo data-titolo-id per il doppio click
            row.setAttribute('data-titolo-id', simulazione.titoloId);
            
            // Aggiungi classe per indicare che la riga è cliccabile
            row.classList.add('simulazione-row');
            
            // Determina la classe CSS in base al rendimento netto
            const rendimentoClass = (simulazione.rendimentoNettoBollo || 0) >= 0 ? 'rendimento-positivo' : 'rendimento-negativo';
            
            // Calcola il valore finale
            const importoNominale = simulazione.importoNominale || 0;
            const rendimentoNettoBollo = simulazione.rendimentoNettoBollo || 0;
            const valoreFinaleTeorico = importoNominale * (1 + (rendimentoNettoBollo / 100));
            
            // Usa Formatters.formatDecimal e Formatters.formatDate per la formattazione
            row.innerHTML = `
                <td>${titolo.nome || ''} (${titolo.codiceIsin || ''})</td>
                <td>${Formatters.formatDecimal(importoNominale)} €</td>
                <td>${Formatters.formatDecimal(simulazione.prezzoAcquisto || 0)} €</td>
                <td>${Formatters.formatDate(simulazione.dataAcquisto || '')}</td>
                <td>${Formatters.formatDate(titolo.dataScadenza || '')}</td>
                <td>${Formatters.formatDecimal(simulazione.commissioniAcquisto || 0)}%</td>
                <td>${Formatters.formatDecimal(simulazione.impostaBollo || 0)} €</td>
                            <td>${Formatters.formatDecimal(simulazione.rendimentoLordo || 0)}%</td>
                            <td>${Formatters.formatDecimal(simulazione.rendimentoNettoCedole || 0)}%</td>
                            <td class="${rendimentoClass}">${Formatters.formatDecimal(rendimentoNettoBollo)}%</td>
                <td>${Formatters.formatDecimal(valoreFinaleTeorico)} €</td>
            `;
            
            // Aggiungi event listener per il doppio click
            row.addEventListener('dblclick', function() {
                const titoloId = this.getAttribute('data-titolo-id');
                if (titoloId) {
                    window.location.href = `dettaglio-simulazione.html?titoloId=${titoloId}`;
                }
            });
            
            tbody.appendChild(row);
        });
    }
    
    /**
     * Imposta le date di default per il form di simulazione
     */
    setDefaultDates() {
        const today = new Date();
        const dataAcquistoInput = document.getElementById('data-acquisto');
        
        if (dataAcquistoInput) {
            // Imposta la data di oggi utilizzando le funzioni di utilità DomUtils
            DomUtils.setDatepickerDate(dataAcquistoInput, today);
            
            // Memorizza il formato ISO per l'invio al server
            dataAcquistoInput.setAttribute('data-iso-date', today.toISOString().split('T')[0]);
        }
        
        // Imposta il valore di default per le commissioni (0,9/1000 = 0.09%)
        const commissioniInput = document.getElementById('commissioni-acquisto');
        if (commissioniInput) {
            commissioniInput.value = "0.09";
        }
        
        // Imposta il valore di default per il tasso d'interesse a vuoto
        const tassoInteresseInput = document.getElementById('tasso-interesse');
        if (tassoInteresseInput) {
            tassoInteresseInput.value = "";
        }
    }
    
    /**
     * Calcola e aggiorna i giorni mancanti alla scadenza e imposta il tasso d'interesse
     */
    updateGiorniAllaScadenza() {
        const titoloSelect = document.getElementById('titolo-select');
        const dataAcquistoInput = document.getElementById('data-acquisto');
        const giorniAllaScadenzaInput = document.getElementById('giorni-alla-scadenza');
        const tassoInteresseInput = document.getElementById('tasso-interesse');
        
        // Verifica che tutti gli elementi esistano
        if (!titoloSelect || !dataAcquistoInput || !giorniAllaScadenzaInput || !tassoInteresseInput) {
            console.error('Elementi DOM mancanti per updateGiorniAllaScadenza');
            return;
        }
        
        const titoloId = titoloSelect.value;
        
        if (!titoloId || !dataAcquistoInput.value) {
            giorniAllaScadenzaInput.value = '';
            tassoInteresseInput.value = '';
            return;
        }
        
        // Verifica che window.titoli esista prima di usarlo
        if (!window.titoli || !Array.isArray(window.titoli)) {
            console.error('window.titoli non è definito o non è un array');
            giorniAllaScadenzaInput.value = '';
            tassoInteresseInput.value = '';
            return;
        }
        
        const titolo = window.titoli.find(t => t && t.id == titoloId);
        if (!titolo || !titolo.dataScadenza) {
            giorniAllaScadenzaInput.value = '';
            tassoInteresseInput.value = '';
            return;
        }
        
        // Usa la data ISO memorizzata nell'attributo data-iso-date
        const dataAcquistoISO = dataAcquistoInput.getAttribute('data-iso-date');
        
        // Calcola i giorni tra la data di acquisto e la data di scadenza
        const acquisto = dataAcquistoISO ? new Date(dataAcquistoISO) : new Date();
        const scadenza = new Date(titolo.dataScadenza);
        
        // Calcola la differenza in millisecondi e converti in giorni
        const diffTime = scadenza - acquisto;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        giorniAllaScadenzaInput.value = diffDays > 0 ? diffDays : 0;
        
        // Imposta il tasso d'interesse con il valore del titolo selezionato
        if (titolo.tassoNominale) {
            tassoInteresseInput.value = Formatters.formatDecimal(titolo.tassoNominale);
        }
    }
    
    /**
     * Ottiene il prezzo corrente di un titolo
     */
    getPrezzoCorrente() {
        const titoloId = document.getElementById('titolo-select').value;
        
        if (!titoloId) {
            alert('Seleziona un titolo');
            return;
        }
        
        const titolo = window.titoli.find(t => t.id == titoloId);
        if (!titolo) {
            alert('Titolo non trovato');
            return;
        }
        
        // Ottieni il codice ISIN e il tipo di titolo
        const codiceIsin = titolo.codiceIsin;
        const tipoTitolo = titolo.tipoTitolo;
        
        if (!codiceIsin || !tipoTitolo) {
            alert('Informazioni sul titolo incomplete');
            return;
        }
        
        Titolo.getPrezzoCorrente(tipoTitolo, codiceIsin)
            .then(prezzo => {
                console.log("Prezzo corrente:", prezzo);
                
                // Aggiorna il prezzo nel campo
                document.getElementById('prezzo-acquisto').value = Formatters.formatDecimal(prezzo);
            })
            .catch(error => {
                console.error('Errore:', error);
                alert('Si è verificato un errore nel recupero del prezzo corrente.');
            });
    }
    
    /**
     * Calcola i rendimenti di tutti i titoli
     */
    calcolaRendimentiTuttiTitoli() {
        Simulazione.calcolaRendimentiTuttiTitoli()
            .then(data => {
                console.log("Rendimenti calcolati:", data);
                
                // Ricarica le simulazioni dal server
                this.loadSimulazioniFromServer();
                
                // Mostra un messaggio di successo
                alert('Rendimenti calcolati con successo!');
            })
            .catch(error => {
                console.error('Errore:', error);
                alert('Si è verificato un errore nel calcolo dei rendimenti.');
            });
    }
    
    /**
     * Crea una nuova simulazione
     */
    createSimulazione() {
        const titoloId = document.getElementById('titolo-select').value;
        const prezzoAcquistoText = document.getElementById('prezzo-acquisto').value;
        const importoText = document.getElementById('importo-nominale').value;
        const dataAcquistoInput = document.getElementById('data-acquisto');
        const commissioniAcquistoText = document.getElementById('commissioni-acquisto').value;
        const modalitaBollo = document.querySelector('input[name="modalita-bollo"]:checked').value;
        
        // Verifica che tutti i campi siano valorizzati
        if (!titoloId || !prezzoAcquistoText || !importoText || !dataAcquistoInput.value || !commissioniAcquistoText || !modalitaBollo) {
            alert('Compila tutti i campi');
            return;
        }
        
        // Converti i valori
        const prezzoAcquisto = Validators.parseNumericValue(prezzoAcquistoText);
        const importo = Validators.parseNumericValue(importoText);
        const dataAcquistoISO = dataAcquistoInput.getAttribute('data-iso-date');
        const commissioniAcquisto = Validators.parseNumericValue(commissioniAcquistoText);
        
        // Verifica che i valori siano validi
        if (prezzoAcquisto <= 0 || importo <= 0 || commissioniAcquisto < 0) {
            alert('I valori devono essere positivi');
            return;
        }
        
        // Mostra un indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        Simulazione.calcolaRendimento(titoloId, prezzoAcquisto, importo, modalitaBollo)
            .then(data => {
                console.log("Rendimento calcolato:", data);
                
                // Memorizza l'ultimo risultato del calcolo
                this.ultimoRisultatoCalcolo = data;
                
                // Visualizza i risultati nei campi appropriati
                document.getElementById('plusvalenza-netta').value = Formatters.formatDecimal(data.plusvalenzaNetta);
                document.getElementById('interessi-netti').value = Formatters.formatDecimal(data.interessiNetti);
                document.getElementById('commissioni').value = Formatters.formatDecimal(data.commissioni);
                document.getElementById('imposta-bollo').value = Formatters.formatDecimal(data.impostaBollo);
                document.getElementById('guadagno-totale').value = Formatters.formatDecimal(data.guadagnoTotale);
                document.getElementById('guadagno-netto-commissioni').value = Formatters.formatDecimal(data.guadagnoNettoCommissioni);
                document.getElementById('tasso').value = Formatters.formatDecimal(data.tasso) + '%';
                document.getElementById('tasso-netto-commissioni').value = Formatters.formatDecimal(data.tassoNettoCommissioni) + '%';
                document.getElementById('guadagno-netto-bollo').value = Formatters.formatDecimal(data.guadagnoNettoBollo);
                document.getElementById('tasso-netto-bollo').value = Formatters.formatDecimal(data.tassoNettoBollo) + '%';
                document.getElementById('importo-scadenza').value = Formatters.formatDecimal(data.importoScadenza);
                
                // Abilita il pulsante "Salva Simulazione"
                document.getElementById('salva-simulazione-btn').disabled = false;
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
                
                // Mostra un messaggio di successo
                DomUtils.showAlert('Rendimento calcolato con successo!', 'success');
            })
            .catch(error => {
                console.error('Errore:', error);
                DomUtils.showAlert('Si è verificato un errore nel calcolo del rendimento.', 'danger');
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
            });
    }
    
    /**
     * Salva una simulazione
     */
    salvaSimulazione() {
        // Verifica che ci sia un risultato di calcolo
        if (!this.ultimoRisultatoCalcolo) {
            DomUtils.showAlert('Calcola prima il rendimento', 'warning');
            return;
        }
        
        const titoloId = document.getElementById('titolo-select').value;
        const prezzoAcquistoText = document.getElementById('prezzo-acquisto').value;
        const importoText = document.getElementById('importo-nominale').value;
        const dataAcquistoInput = document.getElementById('data-acquisto');
        const commissioniAcquistoText = document.getElementById('commissioni-acquisto').value;
        
        // Verifica che tutti i campi siano valorizzati
        if (!titoloId || !prezzoAcquistoText || !importoText || !dataAcquistoInput.value || !commissioniAcquistoText) {
            DomUtils.showAlert('Compila tutti i campi', 'warning');
            return;
        }
        
        // Converti i valori
        const prezzoAcquisto = Validators.parseNumericValue(prezzoAcquistoText);
        const importo = Validators.parseNumericValue(importoText);
        const dataAcquistoISO = dataAcquistoInput.getAttribute('data-iso-date');
        const commissioniAcquisto = Validators.parseNumericValue(commissioniAcquistoText);
        
        // Crea l'oggetto simulazione utilizzando il metodo del modello
        const simulazione = Simulazione.creaSimulazione(
            titoloId, 
            prezzoAcquisto, 
            dataAcquistoISO, 
            commissioniAcquisto, 
            this.ultimoRisultatoCalcolo
        );
        
        // Mostra un indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        Simulazione.save(simulazione)
            .then(data => {
                console.log("Simulazione salvata:", data);
                
                // Ricarica le simulazioni dal server
                this.loadSimulazioniFromServer();
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
                
                // Mostra un messaggio di successo
                DomUtils.showAlert('Simulazione salvata con successo!', 'success');
            })
            .catch(error => {
                console.error('Errore:', error);
                DomUtils.showAlert('Si è verificato un errore nel salvataggio della simulazione.', 'danger');
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
            });
    }
}

// Inizializza il controller quando il DOM è pronto e le librerie sono caricate
document.addEventListener('DOMContentLoaded', function() {
    // Funzione per verificare se jQuery e datepicker sono disponibili
    function checkJQueryAndDatepicker() {
        return window.jQuery && $.fn && $.fn.datepicker;
    }
    
    // Funzione per verificare se window.titoli è disponibile
    function checkTitoliAvailable() {
        return window.titoli && Array.isArray(window.titoli);
    }
    
    // Funzione per inizializzare il controller
    function initController(retryCount = 0) {
        const maxRetries = 5; // Numero massimo di tentativi
        const retryDelay = 500; // Ritardo tra i tentativi in ms
        
        // Se window.titoli non è ancora disponibile, inizializzalo come array vuoto
        if (!checkTitoliAvailable()) {
            console.log('window.titoli non è ancora definito, inizializzazione come array vuoto');
            window.titoli = [];
        }
        
        // Verifica se jQuery e datepicker sono disponibili
        if (checkJQueryAndDatepicker()) {
            try {
                // Crea l'istanza del controller e la rende disponibile globalmente
                window.simulazioniController = new SimulazioniController();
                console.log('Controller simulazioni inizializzato con successo' + (retryCount > 0 ? ' (dopo ' + retryCount + ' tentativi)' : ''));
            } catch (error) {
                console.error('Errore durante l\'inizializzazione del controller:', error);
                
                // Se ci sono ancora tentativi disponibili, riprova
                if (retryCount < maxRetries) {
                    console.warn(`Tentativo ${retryCount + 1}/${maxRetries} fallito. Riprovo tra ${retryDelay}ms...`);
                    setTimeout(() => initController(retryCount + 1), retryDelay);
                } else {
                    console.error(`Impossibile inizializzare il controller dopo ${maxRetries} tentativi.`);
                    
                    // Inizializza comunque il controller in modalità fallback
                    initFallbackMode();
                }
            }
        } else {
            console.warn('jQuery o datepicker non disponibili. Tentativo ' + (retryCount + 1) + '/' + maxRetries);
            
            // Se ci sono ancora tentativi disponibili, riprova
            if (retryCount < maxRetries) {
                setTimeout(() => initController(retryCount + 1), retryDelay);
            } else {
                console.error(`jQuery o datepicker non disponibili dopo ${maxRetries} tentativi. Inizializzazione in modalità fallback.`);
                
                // Inizializza il controller in modalità fallback
                initFallbackMode();
            }
        }
    }
    
    // Funzione per inizializzare il controller in modalità fallback
    function initFallbackMode() {
        console.log('Inizializzazione controller in modalità fallback (senza jQuery/datepicker)');
        
        // Assicurati che window.titoli esista
        if (!window.titoli) {
            window.titoli = [];
        }
        
        try {
            // Crea una versione semplificata del controller
            class SimplifiedController {
                constructor() {
                    this.simulazioni = [];
                    this.simulazioniCaricate = false;
                    
                    // Carica le simulazioni dal server
                    this.loadSimulazioniFromServer();
                }
                
                loadSimulazioniFromServer() {
                    Simulazione.load(true)
                        .then(data => {
                            this.simulazioni = data;
                            this.simulazioniCaricate = true;
                            this.updateSimulazioniTable();
                        })
                        .catch(error => {
                            console.error('Errore nel caricamento delle simulazioni:', error);
                            if (!this.simulazioniCaricate && this.simulazioni.length === 0) {
                                this.loadSampleData();
                                this.updateSimulazioniTable();
                            }
                        });
                }
                
                loadSampleData() {
                    // Simulazioni di esempio
                    const dataAcquisto1 = new Date();
                    dataAcquisto1.setMonth(dataAcquisto1.getMonth() - 6);
                    const dataAcquisto2 = new Date();
                    dataAcquisto2.setMonth(dataAcquisto2.getMonth() - 3);
                    
                    this.simulazioni = [
                        { 
                            id: 1, 
                            titoloId: 1, 
                            prezzoAcquisto: 98.25,
                            dataAcquisto: dataAcquisto1.toISOString().split('T')[0],
                            importoNominale: 10000,
                            commissioniAcquisto: 0.25,
                            rendimentoLordo: 2.75,
                            rendimentoTassato: 2.31,
                            rendimentoNettoCedole: 2.25,
                            impostaBollo: 0.20,
                            rendimentoNettoBollo: 2.05,
                            plusMinusValenza: 0.50
                        },
                        { 
                            id: 2, 
                            titoloId: 2, 
                            prezzoAcquisto: 99.10,
                            dataAcquisto: dataAcquisto2.toISOString().split('T')[0],
                            importoNominale: 20000,
                            commissioniAcquisto: 0.20,
                            rendimentoLordo: 1.95,
                            rendimentoTassato: 1.64,
                            rendimentoNettoCedole: 1.60,
                            impostaBollo: 0.20,
                            rendimentoNettoBollo: 1.40,
                            plusMinusValenza: 0.40
                        }
                    ];
                }
                
                updateSimulazioniTable() {
                    const tbody = document.getElementById('simulazioni-list');
                    if (!tbody) return;
                    
                    tbody.innerHTML = '';
                    
                    // Verifica che window.titoli esista prima di usarlo
                    if (!window.titoli || !Array.isArray(window.titoli)) {
                        console.error('window.titoli non è definito o non è un array');
                        return;
                    }
                    
                    this.simulazioni.forEach(simulazione => {
                        // Verifica che simulazione.titoloId esista
                        if (!simulazione.titoloId) return;
                        
                        const titolo = window.titoli.find(t => t && t.id == simulazione.titoloId);
                        if (!titolo) return;
                        
                        const row = document.createElement('tr');
                        
                        // Aggiungi attributo data-titolo-id per il doppio click
                        row.setAttribute('data-titolo-id', simulazione.titoloId);
                        
                        // Aggiungi classe per indicare che la riga è cliccabile
                        row.classList.add('simulazione-row');
                        
                        // Determina la classe CSS in base al rendimento netto
                        const rendimentoClass = (simulazione.rendimentoNettoBollo || 0) >= 0 ? 'rendimento-positivo' : 'rendimento-negativo';
                        
                        // Calcola il valore finale
                        const importoNominale = simulazione.importoNominale || 0;
                        const rendimentoNettoBollo = simulazione.rendimentoNettoBollo || 0;
                        const valoreFinaleTeorico = importoNominale * (1 + (rendimentoNettoBollo / 100));
                        
                        // Usa Formatters.formatDecimal e Formatters.formatDate per la formattazione
                        row.innerHTML = `
                            <td>${titolo.nome || ''} (${titolo.codiceIsin || ''})</td>
                            <td>${Formatters.formatDecimal(importoNominale)} €</td>
                            <td>${Formatters.formatDecimal(simulazione.prezzoAcquisto || 0)} €</td>
                            <td>${Formatters.formatDate(simulazione.dataAcquisto || '')}</td>
                            <td>${Formatters.formatDate(titolo.dataScadenza || '')}</td>
                            <td>${Formatters.formatDecimal(simulazione.commissioniAcquisto || 0)}%</td>
                            <td>${Formatters.formatDecimal(simulazione.impostaBollo || 0)} €</td>
                            <td>${Formatters.formatDecimal(simulazione.rendimentoLordo || 0)}%</td>
                            <td>${Formatters.formatDecimal(simulazione.rendimentoNettoCedole || 0)}%</td>
                            <td class="${rendimentoClass}">${Formatters.formatDecimal(rendimentoNettoBollo)}%</td>
                            <td>${Formatters.formatDecimal(valoreFinaleTeorico)} €</td>
                        `;
                        
                        // Aggiungi event listener per il doppio click
                        row.addEventListener('dblclick', function() {
                            const titoloId = this.getAttribute('data-titolo-id');
                            if (titoloId) {
                                window.location.href = `dettaglio-simulazione.html?titoloId=${titoloId}`;
                            }
                        });
                        
                        tbody.appendChild(row);
                    });
                }
            }
            
            // Crea l'istanza del controller semplificato
            window.simulazioniController = new SimplifiedController();
            console.log('Controller simulazioni inizializzato in modalità fallback');
        } catch (error) {
            console.error('Errore durante l\'inizializzazione del controller in modalità fallback:', error);
        }
    }
    
    // Avvia l'inizializzazione
    initController();
});
