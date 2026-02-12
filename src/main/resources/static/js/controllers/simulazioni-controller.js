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
        
        // Log per debug
        console.log('SimulazioniController costruito');
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
     * Configura gli event listener per le icone di informazioni
     */
    setupInfoIconListeners() {
        // Aggiungi event listener per le icone di informazioni
        document.querySelectorAll('.info-icon').forEach(icon => {
            icon.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation(); // Previene la propagazione dell'evento al gestore di doppio click della riga
                const simulazioneId = icon.getAttribute('data-simulazione-id');
                const rendimentoTipo = icon.getAttribute('data-rendimento-tipo');
                this.showRendimentoInfo(simulazioneId, rendimentoTipo);
            });
        });
    }
    
    /**
     * Mostra le informazioni dettagliate sul rendimento
     * @param {number} simulazioneId - ID della simulazione
     * @param {string} tipoRendimento - Tipo di rendimento (netto, commissioni, bolloMensile, bolloAnnuale)
     */
    showRendimentoInfo(simulazioneId, tipoRendimento) {
        window.location.href = `info-titolo-rendimenti.html?simulazioneId=${simulazioneId}&tipoRendimento=${tipoRendimento}`;
    }
    
    /**
     * Carica le simulazioni dal server
     */
    loadSimulazioniFromServer() {
        console.log('Caricamento simulazioni dal server...');
        
        // Assicurati che window.titoli esista
        if (!window.titoli) {
            console.log('window.titoli non è ancora definito, inizializzazione come array vuoto');
            window.titoli = [];
        }
        
        // Verifica se la tabella simulazioni-list esiste
        const tbody = document.getElementById('simulazioni-list');
        if (!tbody) {
            console.log('Elemento simulazioni-list non trovato nel DOM, potrebbe essere una pagina diversa');
            return;
        }
        
        Simulazione.load(true)
            .then(data => {
                console.log('Simulazioni caricate con successo:', data);
                
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
                    console.log('Caricamento dati di esempio per le simulazioni...');
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
                rendimentoSenzaCosti: 2.75,
                rendimentoConCommissioni: 2.31,
                rendimentoConBolloMensile: 2.25,
                rendimentoConBolloAnnuale: 2.05,
                // Aggiungiamo i valori finali per i dati di esempio
                valoreBolloAnnualePlusvalenzaNonEsente: 10175.50,
                valoreBolloMensilePlusvalenzaNonEsente: 10180.25,
                valoreBolloAnnualePlusvalenzaEsente: 10200.75,
                valoreBolloMensilePlusvalenzaEsente: 10205.50
            },
            { 
                id: 2, 
                titoloId: 2, 
                prezzoAcquisto: 99.10,
                dataAcquisto: dataAcquisto2.toISOString().split('T')[0],
                importoNominale: 20000,
                commissioniAcquisto: 0.20,
                rendimentoSenzaCosti: 1.95,
                rendimentoConCommissioni: 1.64,
                rendimentoConBolloMensile: 1.60,
                rendimentoConBolloAnnuale: 1.40,
                // Aggiungiamo i valori finali per i dati di esempio
                valoreBolloAnnualePlusvalenzaNonEsente: 20280.00,
                valoreBolloMensilePlusvalenzaNonEsente: 20320.00,
                valoreBolloAnnualePlusvalenzaEsente: null,
                valoreBolloMensilePlusvalenzaEsente: null
            }
        ];
        
        // Aggiungiamo un log per verificare i dati di esempio
        console.log("Dati di esempio caricati:", this.simulazioni);
    }
    
    /**
     * Aggiorna la tabella delle simulazioni
     */
    updateSimulazioniTable() {
        console.log('Aggiornamento tabella simulazioni...');
        
        const tbody = document.getElementById('simulazioni-list');
        if (!tbody) {
            console.log('Elemento simulazioni-list non trovato nel DOM, potrebbe essere una pagina diversa');
            return;
        }
        
        tbody.innerHTML = '';
        
        // Aggiorna la data di acquisto nel titolo della sezione
        const dataRendimentiElement = document.getElementById('data-rendimenti');
        if (dataRendimentiElement && this.simulazioni.length > 0) {
            // Prendi la data di acquisto dalla prima simulazione (sono tutte uguali)
            const dataAcquisto = this.simulazioni[0].dataAcquisto;
            if (dataAcquisto) {
                dataRendimentiElement.textContent = `Rendimenti al: ${Formatters.formatDate(dataAcquisto)}`;
            }
        }
        
        // Verifica che ci siano simulazioni da mostrare
        if (!this.simulazioni || this.simulazioni.length === 0) {
            console.log('Nessuna simulazione da mostrare');
            return;
        }
        
        console.log(`Aggiornamento tabella con ${this.simulazioni.length} simulazioni`);
        
        // I dati sono già ordinati per scadenza crescente dal backend
        // Non è necessario ordinare nuovamente qui
        
        // Verifica se this.simulazioni è un array o un oggetto
        // Se è un oggetto, converti in array
        let simulazioniArray = this.simulazioni;
        if (this.simulazioni && !Array.isArray(this.simulazioni)) {
            console.log('this.simulazioni non è un array, tentativo di conversione...');
            // Verifica se è un oggetto con proprietà numeriche (come un oggetto JSON con indici)
            if (typeof this.simulazioni === 'object') {
                simulazioniArray = Object.values(this.simulazioni);
                console.log('Convertito oggetto in array con', simulazioniArray.length, 'elementi');
            } else {
                console.error('Impossibile convertire this.simulazioni in array');
                return;
            }
        }
        
        // Usa l'array (originale o convertito)
        simulazioniArray.forEach(simulazione => {
            // Verifica se la simulazione ha un oggetto titolo associato
            let titolo = simulazione.titolo;
            
            // Se non c'è un oggetto titolo ma c'è un titoloId, cerca il titolo nell'array window.titoli
            if (!titolo && simulazione.titoloId && window.titoli) {
                console.log(`Cercando titolo con ID ${simulazione.titoloId} in window.titoli`);
                titolo = window.titoli.find(t => t.id == simulazione.titoloId);
                
                if (titolo) {
                    console.log(`Trovato titolo con ID ${simulazione.titoloId}:`, titolo);
                    // Associa il titolo alla simulazione per usi futuri
                    simulazione.titolo = titolo;
                } else {
                    console.warn(`Titolo con ID ${simulazione.titoloId} non trovato in window.titoli`);
                }
            }
            
            // Se ancora non abbiamo un titolo, salta questa simulazione
            if (!titolo) {
                console.warn('Simulazione senza titolo associato:', simulazione);
                return;
            }
            
            // Prima riga: dati principali del titolo
            const row = document.createElement('tr');
            
            // Aggiungi attributo data-titolo-id per il doppio click
            row.setAttribute('data-titolo-id', simulazione.titoloId);
            row.setAttribute('data-simulazione-id', simulazione.id);
            
            // Aggiungi classe per indicare che la riga è cliccabile
            row.classList.add('simulazione-row');
            
            // Determina la classe CSS in base al rendimento netto
            const rendimentoClass = (simulazione.rendimentoConBolloAnnuale || 0) >= 0 ? 'rendimento-positivo' : 'rendimento-negativo';
            
            // Crea l'icona di informazioni
            const infoIconSvg = `
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" class="bi bi-info-circle" viewBox="0 0 16 16">
                    <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                    <path d="m8.93 6.588-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 3.468c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246-.686.246-.275 0-.375-.193-.304-.533L8.93 6.588zM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
                </svg>
            `;
            
            // Funzione per normalizzare i valori dei rendimenti
            // I dati dopo la funzione "ricalcola" sono divisi per cento rispetto a quelli presenti al caricamento della pagina
            // Quindi moltiplichiamo sempre per 100 i valori provenienti dal ricalcolo
            const normalizeRendimento = (value) => {
                if (value === null || value === undefined) return 0;
                const numValue = parseFloat(value);
                if (isNaN(numValue)) return 0;
                
                // Se il valore è molto piccolo (es. 0.025 invece di 2.5), lo moltiplichiamo per 100
                // Questo è un indicatore più affidabile che i dati sono in formato decimale (0.025 per 2.5%)
                return (numValue < 0.1 && numValue > 0) ? numValue * 100 : numValue;
            };
            
            // Usa Formatters.formatDecimal e Formatters.formatDate per la formattazione
            row.innerHTML = `
                <td>${titolo.nome || ''} (${titolo.codiceIsin || ''})</td>
                <td>${Formatters.formatDecimal(simulazione.prezzoAcquisto || 0)}</td>
                <td>${Formatters.formatDate(titolo.dataScadenza || '')}</td>
                <td>${Formatters.formatDecimal(normalizeRendimento(simulazione.rendimentoSenzaCosti))}%</td>
                <td>${Formatters.formatDecimal(normalizeRendimento(simulazione.rendimentoConCommissioni))}%</td>
                <td>${Formatters.formatDecimal(normalizeRendimento(simulazione.rendimentoConBolloMensile))}%</td>
                <td>${Formatters.formatDecimal(normalizeRendimento(simulazione.rendimentoConBolloAnnuale))}%</td>
                <td>${titolo.tipoTitolo === 'BTP' && simulazione.rendimentoPlusvalenzaEsente != null ? 
                    Formatters.formatDecimal(normalizeRendimento(simulazione.rendimentoPlusvalenzaEsente)) + '%' : 
                    '-'}</td>
                <td>
                    <span 
                        class="value-with-popover"
                        data-simulazione-id="${simulazione.id}"
                        data-valore-bollo-annuale-plusvalenza-non-esente="${simulazione.valoreBolloAnnualePlusvalenzaNonEsente || 0}"
                        data-valore-bollo-mensile-plusvalenza-non-esente="${simulazione.valoreBolloMensilePlusvalenzaNonEsente || 0}"
                        data-valore-bollo-annuale-plusvalenza-esente="${simulazione.valoreBolloAnnualePlusvalenzaEsente || 0}"
                        data-valore-bollo-mensile-plusvalenza-esente="${simulazione.valoreBolloMensilePlusvalenzaEsente || 0}"
                        data-tipo-titolo="${titolo.tipoTitolo || ''}"
                        data-titolo-nome="${titolo.nome || ''}"
                        data-titolo-isin="${titolo.codiceIsin || ''}"
                    >
                        ${Formatters.formatDecimal(simulazione.valoreBolloAnnualePlusvalenzaNonEsente || 0)}
                    </span>
                    <a href="#" class="info-icon" data-simulazione-id="${simulazione.id}" data-rendimento-tipo="bolloAnnuale">
                        ${infoIconSvg}
                    </a>
                </td>
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
        
        // Aggiungi event listener per le icone di informazioni
        this.setupInfoIconListeners();
        
        // Aggiungi stile CSS per la tabella dei valori finali
        this.addValoriFinaliStyles();
        
        // Inizializza i popover
        this.initializePopovers();
    }
    
    /**
     * Inizializza i popover per la tabella dei valori finali
     */
    initializePopovers() {
        console.log('Inizializzazione popover con approccio manuale...');
        
        // Rimuovi eventuali listener precedenti
        document.querySelectorAll('.value-with-popover').forEach(el => {
            el.removeEventListener('click', this.showPopupTable);
        });
        
        // Aggiungi nuovi listener per il click
        const self = this; // Salva il riferimento a this
        document.querySelectorAll('.value-with-popover').forEach(el => {
            el.addEventListener('click', function(event) {
                self.showPopupTable(event, self); // Passa sia l'evento che il riferimento a this
            });
            // Aggiungi stile per indicare che è cliccabile
            el.style.cursor = 'pointer';
            el.style.textDecoration = 'underline';
            el.title = 'Clicca per vedere i dettagli';
        });
        
        // Aggiungi listener per chiudere il popup quando si clicca altrove
        document.addEventListener('click', (event) => {
            const popup = document.getElementById('valori-popup');
            if (popup && !popup.contains(event.target) && !event.target.classList.contains('value-with-popover')) {
                popup.remove();
            }
        });
        
        console.log('Listener popover aggiunti a', document.querySelectorAll('.value-with-popover').length, 'elementi');
    }
    
    /**
     * Mostra la tabella popup al click
     * @param {Event} event - L'evento click
     * @param {SimulazioniController} controller - Il controller
     */
    showPopupTable(event, controller) {
        event.stopPropagation(); // Previene la propagazione dell'evento click
        
        // Rimuovi eventuali popup esistenti
        const existingPopup = document.getElementById('valori-popup');
        if (existingPopup) {
            existingPopup.remove();
            return; // Se c'era già un popup, lo rimuove e basta (toggle)
        }
        
        const target = event.currentTarget;
        const simulazioneId = target.getAttribute('data-simulazione-id');
        
        if (!simulazioneId) {
            console.error('ID simulazione non trovato');
            return;
        }
        
        // Recupera i dati dal target per il titolo
        const titoloNome = target.getAttribute('data-titolo-nome') || '';
        const titoloIsin = target.getAttribute('data-titolo-isin') || '';
        const tipoTitolo = target.getAttribute('data-tipo-titolo') || '';
        
        // Mostra un indicatore di caricamento
        const loadingPopup = document.createElement('div');
        loadingPopup.id = 'valori-popup';
        loadingPopup.className = 'modal-style-popup popup-visible';
        loadingPopup.innerHTML = `
            <div class="popup-header">
                <h5>Caricamento dati...</h5>
            </div>
            <div class="popup-body">
                <div class="text-center">
                    <div class="spinner-border text-primary" role="status">
                        <span class="sr-only">Caricamento...</span>
                    </div>
                </div>
            </div>
        `;
        
        // Posiziona il popup al centro della pagina
        loadingPopup.style.position = 'fixed';
        loadingPopup.style.top = '50%';
        loadingPopup.style.left = '50%';
        loadingPopup.style.transform = 'translate(-50%, -50%)';
        
        // Aggiungi il popup di caricamento al DOM
        document.body.appendChild(loadingPopup);
        
        // Recupera i dati dettagliati della simulazione
        fetch(`/api/simulazioni/${simulazioneId}/calcolo-dettagliato`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Errore nella richiesta: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log("Dati dettagliati ricevuti:", data);
                
                // Rimuovi il popup di caricamento
                loadingPopup.remove();
                
                // Crea il popup con i dati ricevuti
                controller.createValoriFinaliPopup(data, titoloNome, titoloIsin, tipoTitolo);
            })
            .catch(error => {
                console.error('Errore nel recupero dei dati dettagliati:', error);
                
                // Rimuovi il popup di caricamento
                loadingPopup.remove();
                
                // Mostra un messaggio di errore
                const errorPopup = document.createElement('div');
                errorPopup.id = 'valori-popup';
                errorPopup.className = 'modal-style-popup popup-visible';
                errorPopup.innerHTML = `
                    <div class="popup-header">
                        <h5>Errore</h5>
                        <button type="button" class="close-button" aria-label="Chiudi">×</button>
                    </div>
                    <div class="popup-body">
                        <div class="alert alert-danger">
                            Si è verificato un errore nel recupero dei dati dettagliati.
                        </div>
                    </div>
                `;
                
                // Posiziona il popup al centro della pagina
                errorPopup.style.position = 'fixed';
                errorPopup.style.top = '50%';
                errorPopup.style.left = '50%';
                errorPopup.style.transform = 'translate(-50%, -50%)';
                
                // Aggiungi il popup di errore al DOM
                document.body.appendChild(errorPopup);
                
                // Aggiungi event listener per il pulsante di chiusura
                const closeButton = errorPopup.querySelector('.close-button');
                if (closeButton) {
                    closeButton.addEventListener('click', (e) => {
                        e.stopPropagation();
                        errorPopup.remove();
                    });
                }
            });
    }
    
    /**
     * Crea il popup con i dati dei valori finali per profilo
     * @param {Object} data - I dati dettagliati della simulazione
     * @param {string} titoloNome - Il nome del titolo
     * @param {string} titoloIsin - Il codice ISIN del titolo
     * @param {string} tipoTitolo - Il tipo di titolo
     */
    createValoriFinaliPopup(data, titoloNome, titoloIsin, tipoTitolo) {
        // Verifica che ci siano dati dei profili
        if (!data || (!data.rendimentiPerProfili && !data.valoriFinaliPerProfili) || 
            (data.rendimentiPerProfili && data.rendimentiPerProfili.length === 0 && 
             data.valoriFinaliPerProfili && data.valoriFinaliPerProfili.length === 0)) {
            console.error('Dati dei profili non disponibili');
            return;
        }
        
        // Crea il popup
        const popup = document.createElement('div');
        popup.id = 'valori-popup';
        popup.className = 'modal-style-popup';
        
        // Crea l'intestazione del popup
        let contentHtml = `
            <div class="popup-header" id="valori-popup-header">
                <h5>Dettaglio Valori Finali - ${titoloNome} (${titoloIsin})</h5>
                <button type="button" class="close-button" aria-label="Chiudi">×</button>
            </div>
            <div class="popup-body">
        `;
        
        // Aggiungi la tabella dei rendimenti per profilo
        if (data.rendimentiPerProfili && data.rendimentiPerProfili.length > 0) {
            contentHtml += `
        <h6>Rendimenti per Profilo</h6>
        <table id="rendimenti-profilo-table" class="table table-sm popup-table">
            <thead>
                <tr>
                    <th class="scenario-column">Profilo</th>
                    <th>Netto</th>
                    <th>Commiss.</th>
                    <th>Bolli</th>
                    <th>Plusvalenza</th>
                    <th>Esente</th>
                </tr>
            </thead>
                    <tbody>
            `;
            
            // Aggiungi una riga per ogni profilo
            data.rendimentiPerProfili.forEach(profilo => {
                const nomeProfilo = profilo.nomeProfilo || 'Profilo';
                const rendimentoConCommissioni = profilo.rendimentoConCommissioni || 0;
                const rendimentoConBollo = profilo.rendimentoConBollo || 0;
                const rendimentoPlusvalenzaEsente = profilo.rendimentoPlusvalenzaEsente || null;
                
                // Usa il campo rendimentoNetto del DTO per la colonna "Netto"
                const rendimentoNettoValue = profilo.rendimentoNetto ? (profilo.rendimentoNetto * 100) : 0;
                
                // Aggiungi la riga alla tabella
                contentHtml += `
                    <tr>
                        <td class="scenario-column">${nomeProfilo}</td>
                        <td>${Formatters.formatDecimal(rendimentoNettoValue)}%</td>
                        <td>${Formatters.formatDecimal(rendimentoConCommissioni * 100)}%</td>
                        <td>${Formatters.formatDecimal(rendimentoConBollo * 100)}%</td>
                        <td>${rendimentoPlusvalenzaEsente !== null ? Formatters.formatDecimal(rendimentoPlusvalenzaEsente * 100) + '%' : 'N/A'}</td>
                        <td>${profilo.isPlusvalenzaEsente ? 'Sì' : 'No'}</td>
                    </tr>
                `;
            });
            
            contentHtml += `
                    </tbody>
                </table>
                <br>
            `;
        }
        
        // Aggiungi la tabella dei valori finali per profilo
        if (data.valoriFinaliPerProfili && data.valoriFinaliPerProfili.length > 0) {
            contentHtml += `
                <h6>Valori Finali per Profilo</h6>
                <table id="valori-finali-table" class="table table-sm popup-table">
                    <thead>
                        <tr>
                            <th class="scenario-column">Profilo</th>
                            <th>10.000€</th>
                            <th>30.000€</th>
                            <th>50.000€</th>
                            <th>100.000€</th>
                        </tr>
                    </thead>
                    <tbody>
            `;
            
            // Aggiungi una riga per ogni profilo
            data.valoriFinaliPerProfili.forEach(profilo => {
                const nomeProfilo = profilo.nomeProfilo || 'Profilo';
                const isPlusvalenzaEsente = profilo.plusvalenzaEsente;
                const valoreFinaleLordo = profilo.valoreFinaleLordo || 0;
                const valoreFinaleMenoCommissioni = profilo.valoreFinaleMenoCommissioni || 0;
                const valoreFinaleMenoBolli = profilo.valoreFinaleMenoBolli || 0;
                
                // Calcola il valore per diversi importi di capitale
                const valore10k = valoreFinaleMenoBolli;
                const valore30k = valoreFinaleMenoBolli * 3;
                const valore50k = valoreFinaleMenoBolli * 5;
                const valore100k = valoreFinaleMenoBolli * 10;
                
                // Aggiungi la riga alla tabella
                contentHtml += `
                    <tr>
                        <td class="scenario-column">${nomeProfilo} (Plusv. ${isPlusvalenzaEsente ? 'Esente' : 'Tassata'})</td>
                        <td>${Formatters.formatDecimal(valore10k)}€</td>
                        <td>${Formatters.formatDecimal(valore30k)}€</td>
                        <td>${Formatters.formatDecimal(valore50k)}€</td>
                        <td>${Formatters.formatDecimal(valore100k)}€</td>
                    </tr>
                `;
            });
            
            contentHtml += `
                    </tbody>
                </table>
            `;
        }
        
        // Chiudi il popup
        contentHtml += `
            </div>
        `;
        
        popup.innerHTML = contentHtml;
        
        // Posiziona il popup al centro della pagina
        popup.style.position = 'fixed';
        popup.style.top = '50%';
        popup.style.left = '50%';
        popup.style.transform = 'translate(-50%, -50%)';
        
        // Aggiungi il popup al DOM
        document.body.appendChild(popup);
        
        // Aggiungi event listener per il pulsante di chiusura
        const closeButton = popup.querySelector('.close-button');
        if (closeButton) {
            closeButton.addEventListener('click', (e) => {
                e.stopPropagation();
                popup.remove();
            });
        }
    }
    
    /**
     * Aggiunge stili CSS per la tabella dei valori finali
     */
    addValoriFinaliStyles() {
        // Verifica se gli stili sono già stati aggiunti
        if (document.getElementById('valori-finali-styles')) {
            return;
        }
        
        // Crea un elemento style
        const style = document.createElement('style');
        style.id = 'valori-finali-styles';
        
        // Aggiungi gli stili CSS
        style.innerHTML = `
            .modal-style-popup {
                position: fixed;
                z-index: 1050;
                background-color: white;
                border-radius: 5px;
                box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
                max-width: 80%;
                max-height: 80%;
                overflow: auto;
            }
            
            .popup-header {
                padding: 10px 15px;
                border-bottom: 1px solid #e9ecef;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }
            
            .popup-body {
                padding: 15px;
            }
            
            .close-button {
                background: none;
                border: none;
                font-size: 1.5rem;
                font-weight: bold;
                line-height: 1;
                color: #000;
                text-shadow: 0 1px 0 #fff;
                opacity: 0.5;
                cursor: pointer;
            }
            
            .close-button:hover {
                opacity: 0.75;
            }
            
            .popup-table {
                width: 100%;
                margin-bottom: 1rem;
                color: #212529;
                border-collapse: collapse;
            }
            
            .popup-table th,
            .popup-table td {
                padding: 0.3rem;
                vertical-align: top;
                border-top: 1px solid #dee2e6;
            }
            
            .popup-table thead th {
                vertical-align: bottom;
                border-bottom: 2px solid #dee2e6;
                background-color: #f8f9fa;
            }
            
            .scenario-column {
                font-weight: bold;
                background-color: #f8f9fa;
            }
        `;
        
        // Aggiungi lo stile al DOM
        document.head.appendChild(style);
    }
    
    /**
     * Imposta le date di default
     */
    setDefaultDates() {
        const dataAcquistoInput = document.getElementById('data-acquisto');
        if (dataAcquistoInput) {
            // Imposta la data di oggi come data di acquisto di default
            const today = new Date();
            const formattedDate = today.toISOString().split('T')[0];
            dataAcquistoInput.value = formattedDate;
        }
    }
    
    /**
     * Aggiorna i giorni alla scadenza
     */
    updateGiorniAllaScadenza() {
        const titoloSelect = document.getElementById('titolo-select');
        const dataAcquistoInput = document.getElementById('data-acquisto');
        const giorniScadenzaSpan = document.getElementById('giorni-scadenza');
        
        if (!titoloSelect || !dataAcquistoInput || !giorniScadenzaSpan) {
            console.log('Elementi necessari per il calcolo dei giorni alla scadenza non trovati');
            return;
        }
        
        const titoloId = titoloSelect.value;
        if (!titoloId) {
            giorniScadenzaSpan.textContent = '0';
            return;
        }
        
        // Trova il titolo selezionato
        const titolo = window.titoli.find(t => t.id == titoloId);
        if (!titolo || !titolo.dataScadenza) {
            giorniScadenzaSpan.textContent = '0';
            return;
        }
        
        // Calcola i giorni alla scadenza
        const dataAcquisto = new Date(dataAcquistoInput.value);
        const dataScadenza = new Date(titolo.dataScadenza);
        
        // Calcola la differenza in millisecondi
        const diffTime = dataScadenza - dataAcquisto;
        
        // Converti in giorni
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        // Aggiorna il testo
        giorniScadenzaSpan.textContent = diffDays > 0 ? diffDays : '0';
    }
    
    /**
     * Ottiene il prezzo corrente del titolo
     */
    getPrezzoCorrente() {
        const titoloSelect = document.getElementById('titolo-select');
        const prezzoAcquistoInput = document.getElementById('prezzo-acquisto');
        
        if (!titoloSelect || !prezzoAcquistoInput) {
            console.log('Elementi necessari per il recupero del prezzo corrente non trovati');
            return;
        }
        
        const titoloId = titoloSelect.value;
        if (!titoloId) {
            alert('Seleziona un titolo prima di recuperare il prezzo corrente');
            return;
        }
        
        // Mostra un indicatore di caricamento
        const loadingIndicator = document.createElement('div');
        loadingIndicator.id = 'loading-indicator';
        loadingIndicator.className = 'spinner-border text-primary';
        loadingIndicator.setAttribute('role', 'status');
        loadingIndicator.innerHTML = '<span class="sr-only">Caricamento...</span>';
        
        // Aggiungi l'indicatore vicino al campo del prezzo
        prezzoAcquistoInput.parentNode.appendChild(loadingIndicator);
        
        // Disabilita il pulsante durante il caricamento
        const prezzoCorrenteBtn = document.getElementById('prezzo-corrente-btn');
        if (prezzoCorrenteBtn) {
            prezzoCorrenteBtn.disabled = true;
        }
        
        // Recupera il prezzo corrente dal server
        fetch(`/api/titoli/${titoloId}/prezzo-corrente`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Errore nella richiesta: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log('Prezzo corrente ricevuto:', data);
                
                // Aggiorna il campo del prezzo di acquisto
                if (data && data.prezzo) {
                    prezzoAcquistoInput.value = data.prezzo;
                } else {
                    alert('Prezzo corrente non disponibile');
                }
            })
            .catch(error => {
                console.error('Errore nel recupero del prezzo corrente:', error);
                alert('Si è verificato un errore nel recupero del prezzo corrente');
            })
            .finally(() => {
                // Rimuovi l'indicatore di caricamento
                const loadingIndicator = document.getElementById('loading-indicator');
                if (loadingIndicator) {
                    loadingIndicator.remove();
                }
                
                // Riabilita il pulsante
                if (prezzoCorrenteBtn) {
                    prezzoCorrenteBtn.disabled = false;
                }
            });
    }
    
    /**
     * Calcola i rendimenti per tutti i titoli
     */
    calcolaRendimentiTuttiTitoli() {
        console.log('Calcolo rendimenti per tutti i titoli...');
        
        // Verifica che ci siano titoli disponibili
        if (!window.titoli || window.titoli.length === 0) {
            alert('Nessun titolo disponibile per il calcolo dei rendimenti');
            return;
        }
        
        // Mostra un indicatore di caricamento
        const loadingIndicator = document.createElement('div');
        loadingIndicator.id = 'loading-indicator';
        loadingIndicator.className = 'spinner-border text-primary';
        loadingIndicator.setAttribute('role', 'status');
        loadingIndicator.innerHTML = '<span class="sr-only">Caricamento...</span>';
        
        // Aggiungi l'indicatore al DOM
        const calcolaRendimentiBtn = document.getElementById('calcola-rendimenti-btn');
        const calcolaRendimentiBtnRendimenti = document.getElementById('calcola-rendimenti-btn-rendimenti');
        const buttonToUse = calcolaRendimentiBtn || calcolaRendimentiBtnRendimenti;
        
        if (buttonToUse) {
            buttonToUse.parentNode.appendChild(loadingIndicator);
            buttonToUse.disabled = true;
        }
        
        // Valori predefiniti
        let dataAcquisto = new Date().toISOString().split('T')[0]; // Data odierna
        let importoNominale = 10000;
        let commissioniAcquisto = 0.25;
        
        // Recupera i dati dal form se disponibili
        const dataAcquistoInput = document.getElementById('data-acquisto');
        const importoNominaleInput = document.getElementById('importo-nominale');
        const commissioniAcquistoInput = document.getElementById('commissioni-acquisto');
        
        // Se gli elementi del form esistono, usa i loro valori
        if (dataAcquistoInput) {
            dataAcquisto = dataAcquistoInput.value || dataAcquisto;
        }
        
        if (importoNominaleInput) {
            importoNominale = parseFloat(importoNominaleInput.value) || importoNominale;
        }
        
        if (commissioniAcquistoInput) {
            commissioniAcquisto = parseFloat(commissioniAcquistoInput.value) || commissioniAcquisto;
        }
        
        // Prepara i dati per la richiesta
        const requestData = {
            dataAcquisto: dataAcquisto,
            importoNominale: importoNominale,
            commissioniAcquisto: commissioniAcquisto
        };
        
        // Invia la richiesta al server per calcolare i rendimenti
        fetch('/api/simulazioni/calcola-rendimenti-tutti-titoli', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Errore nella richiesta: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log('Risultati del calcolo ricevuti:', data);
                
                // Salva i risultati temporaneamente
                if (data && data.simulazioniAggiornate) {
                    this.ultimoRisultatoCalcolo = data.simulazioniAggiornate;
                } else if (Array.isArray(data)) {
                    this.ultimoRisultatoCalcolo = data;
                } else {
                    console.warn('Formato dati non riconosciuto:', data);
                    this.ultimoRisultatoCalcolo = [];
                }
                
                // Dopo aver calcolato i rendimenti, carica le simulazioni aggiornate dal server
                // Questo garantisce che i dati siano nel formato corretto con tutti i campi necessari
                console.log('Caricamento delle simulazioni aggiornate dal server...');
                return fetch('/api/simulazioni?latest=true');
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Errore nella richiesta: ' + response.status);
                }
                return response.json();
            })
            .then(simulazioni => {
                console.log('Simulazioni aggiornate caricate dal server:', simulazioni);
                
                // Aggiorna la vista delle simulazioni con i dati completi
                this.simulazioni = simulazioni;
                this.updateSimulazioniTable();
                
                // Mostra la sezione dei risultati
                const risultatiSection = document.getElementById('risultati-section');
                if (risultatiSection) {
                    risultatiSection.style.display = 'block';
                }
            })
            .catch(error => {
                console.error('Errore nel calcolo dei rendimenti:', error);
                alert('Si è verificato un errore nel calcolo dei rendimenti');
            })
            .finally(() => {
                // Rimuovi l'indicatore di caricamento
                const loadingIndicator = document.getElementById('loading-indicator');
                if (loadingIndicator) {
                    loadingIndicator.remove();
                }
                
                // Riabilita il pulsante
                const calcolaRendimentiBtn = document.getElementById('calcola-rendimenti-btn');
                const calcolaRendimentiBtnRendimenti = document.getElementById('calcola-rendimenti-btn-rendimenti');
                
                if (calcolaRendimentiBtn) {
                    calcolaRendimentiBtn.disabled = false;
                }
                
                if (calcolaRendimentiBtnRendimenti) {
                    calcolaRendimentiBtnRendimenti.disabled = false;
                }
            });
    }
    
    /**
     * Crea una nuova simulazione
     */
    createSimulazione() {
        console.log('Creazione nuova simulazione...');
        
        // Recupera i dati dal form
        const titoloSelect = document.getElementById('titolo-select');
        const dataAcquistoInput = document.getElementById('data-acquisto');
        const prezzoAcquistoInput = document.getElementById('prezzo-acquisto');
        const importoNominaleInput = document.getElementById('importo-nominale');
        const commissioniAcquistoInput = document.getElementById('commissioni-acquisto');
        
        if (!titoloSelect || !dataAcquistoInput || !prezzoAcquistoInput || !importoNominaleInput || !commissioniAcquistoInput) {
            console.error('Elementi del form non trovati');
            return;
        }
        
        const titoloId = titoloSelect.value;
        const dataAcquisto = dataAcquistoInput.value;
        const prezzoAcquisto = parseFloat(prezzoAcquistoInput.value);
        const importoNominale = parseFloat(importoNominaleInput.value);
        const commissioniAcquisto = parseFloat(commissioniAcquistoInput.value);
        
        // Validazione dei dati
        if (!titoloId) {
            alert('Seleziona un titolo');
            return;
        }
        
        if (!dataAcquisto) {
            alert('Inserisci la data di acquisto');
            return;
        }
        
        if (isNaN(prezzoAcquisto) || prezzoAcquisto <= 0) {
            alert('Inserisci un prezzo di acquisto valido');
            return;
        }
        
        if (isNaN(importoNominale) || importoNominale <= 0) {
            alert('Inserisci un importo nominale valido');
            return;
        }
        
        if (isNaN(commissioniAcquisto) || commissioniAcquisto < 0) {
            alert('Inserisci un valore valido per le commissioni di acquisto');
            return;
        }
        
        // Prepara i dati per la richiesta
        const requestData = {
            titoloId: titoloId,
            dataAcquisto: dataAcquisto,
            prezzoAcquisto: prezzoAcquisto,
            importoNominale: importoNominale,
            commissioniAcquisto: commissioniAcquisto
        };
        
        // Mostra un indicatore di caricamento
        const loadingIndicator = document.createElement('div');
        loadingIndicator.id = 'loading-indicator';
        loadingIndicator.className = 'spinner-border text-primary';
        loadingIndicator.setAttribute('role', 'status');
        loadingIndicator.innerHTML = '<span class="sr-only">Caricamento...</span>';
        
        // Aggiungi l'indicatore al DOM
        const submitButton = document.querySelector('#simulazione-form button[type="submit"]');
        if (submitButton) {
            submitButton.parentNode.appendChild(loadingIndicator);
            submitButton.disabled = true;
        }
        
        // Invia la richiesta al server
        fetch('/api/simulazioni', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Errore nella richiesta: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log('Simulazione creata con successo:', data);
                
                // Reindirizza alla pagina di dettaglio della simulazione
                window.location.href = `dettaglio-simulazione.html?id=${data.id}`;
            })
            .catch(error => {
                console.error('Errore nella creazione della simulazione:', error);
                alert('Si è verificato un errore nella creazione della simulazione');
            })
            .finally(() => {
                // Rimuovi l'indicatore di caricamento
                const loadingIndicator = document.getElementById('loading-indicator');
                if (loadingIndicator) {
                    loadingIndicator.remove();
                }
                
                // Riabilita il pulsante
                if (submitButton) {
                    submitButton.disabled = false;
                }
            });
    }
    
    /**
     * Salva una simulazione
     */
    salvaSimulazione() {
        console.log('Salvataggio simulazione...');
        
        // Verifica che ci sia un risultato di calcolo
        if (!this.ultimoRisultatoCalcolo || this.ultimoRisultatoCalcolo.length === 0) {
            alert('Nessun risultato di calcolo disponibile da salvare');
            return;
        }
        
        // Mostra un indicatore di caricamento
        const loadingIndicator = document.createElement('div');
        loadingIndicator.id = 'loading-indicator';
        loadingIndicator.className = 'spinner-border text-primary';
        loadingIndicator.setAttribute('role', 'status');
        loadingIndicator.innerHTML = '<span class="sr-only">Caricamento...</span>';
        
        // Aggiungi l'indicatore al DOM
        const salvaSimulazioneBtn = document.getElementById('salva-simulazione-btn');
        if (salvaSimulazioneBtn) {
            salvaSimulazioneBtn.parentNode.appendChild(loadingIndicator);
            salvaSimulazioneBtn.disabled = true;
        }
        
        // Prepara i dati per la richiesta
        const requestData = {
            simulazioni: this.ultimoRisultatoCalcolo
        };
        
        // Invia la richiesta al server
        fetch('/api/simulazioni/salva-multiple', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Errore nella richiesta: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log('Simulazioni salvate con successo:', data);
                alert('Simulazioni salvate con successo');
            })
            .catch(error => {
                console.error('Errore nel salvataggio delle simulazioni:', error);
                alert('Si è verificato un errore nel salvataggio delle simulazioni');
            })
            .finally(() => {
                // Rimuovi l'indicatore di caricamento
                const loadingIndicator = document.getElementById('loading-indicator');
                if (loadingIndicator) {
                    loadingIndicator.remove();
                }
                
                // Riabilita il pulsante
                if (salvaSimulazioneBtn) {
                    salvaSimulazioneBtn.disabled = false;
                }
            });
    }
}
