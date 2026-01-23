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
                rendimentoSenzaCosti: 2.75,
                rendimentoConCommissioni: 2.31,
                rendimentoConBolloMensile: 2.25,
                rendimentoConBolloAnnuale: 2.05
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
                rendimentoConBolloAnnuale: 1.40
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
        
        // Aggiorna la data di acquisto nel titolo della sezione
        const dataRendimentiElement = document.getElementById('data-rendimenti');
        if (dataRendimentiElement && this.simulazioni.length > 0) {
            // Prendi la data di acquisto dalla prima simulazione (sono tutte uguali)
            const dataAcquisto = this.simulazioni[0].dataAcquisto;
            if (dataAcquisto) {
                dataRendimentiElement.textContent = `Rendimenti al: ${Formatters.formatDate(dataAcquisto)}`;
            }
        }
        
        // Verifica che window.titoli esista prima di usarlo
        if (!window.titoli || !Array.isArray(window.titoli)) {
            console.error('window.titoli non è definito o non è un array');
            return;
        }
        
        // Ordina le simulazioni per data di scadenza crescente
        this.simulazioni.sort((a, b) => {
            const titoloA = window.titoli.find(t => t && t.id == a.titoloId);
            const titoloB = window.titoli.find(t => t && t.id == b.titoloId);
            
            if (!titoloA || !titoloA.dataScadenza) return -1;
            if (!titoloB || !titoloB.dataScadenza) return 1;
            
            const scadenzaA = new Date(titoloA.dataScadenza);
            const scadenzaB = new Date(titoloB.dataScadenza);
            
            return scadenzaA - scadenzaB;
        });
        
        this.simulazioni.forEach(simulazione => {
            // Verifica che simulazione.titoloId esista
            if (!simulazione.titoloId) return;
            
            const titolo = window.titoli.find(t => t && t.id == simulazione.titoloId);
            if (!titolo) return;
            
            const row = document.createElement('tr');
            
            // Aggiungi attributo data-titolo-id per il doppio click
            row.setAttribute('data-titolo-id', simulazione.titoloId);
            row.setAttribute('data-simulazione-id', simulazione.id);
            
            // Aggiungi classe per indicare che la riga è cliccabile
            row.classList.add('simulazione-row');
            
            // Determina la classe CSS in base al rendimento netto
            const rendimentoClass = (simulazione.rendimentoConBolloAnnuale || 0) >= 0 ? 'rendimento-positivo' : 'rendimento-negativo';
            
            // Calcola il valore finale
            const importoNominale = simulazione.importoNominale || 0;
            const rendimentoNettoBollo = simulazione.rendimentoConBolloAnnuale || 0;
            const valoreFinaleTeorico = importoNominale * (1 + (rendimentoNettoBollo / 100));
            
            // Crea l'icona di informazioni
            const infoIconSvg = `
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" class="bi bi-info-circle" viewBox="0 0 16 16">
                    <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                    <path d="m8.93 6.588-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 3.468c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246-.686.246-.275 0-.375-.193-.304-.533L8.93 6.588zM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
                </svg>
            `;
            
            // Usa Formatters.formatDecimal e Formatters.formatDate per la formattazione
            row.innerHTML = `
                <td>${titolo.nome || ''} (${titolo.codiceIsin || ''})</td>
                <td>${Formatters.formatDecimal(simulazione.prezzoAcquisto || 0)}</td>
                <td>${Formatters.formatDate(titolo.dataScadenza || '')}</td>
                <td>${Formatters.formatDecimal(simulazione.rendimentoSenzaCosti || 0)}%</td>
                <td>${Formatters.formatDecimal(simulazione.rendimentoConCommissioni || 0)}%</td>
                <td>${Formatters.formatDecimal(simulazione.rendimentoConBolloMensile || 0)}%</td>
                <td>${Formatters.formatDecimal(simulazione.rendimentoConBolloAnnuale || 0)}%</td>
                <td>${titolo.tipoTitolo === 'BTP' && simulazione.rendimentoPlusvalenzaEsente != null ? 
                    Formatters.formatDecimal(simulazione.rendimentoPlusvalenzaEsente) + '%' : 
                    '-'}</td>
                <td>
                    ${Formatters.formatDecimal(valoreFinaleTeorico)}
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
        // Verifica se tassoNominale è definito (anche se è zero)
        if (titolo.tassoNominale != null) {
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
        // Mostra un indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        Simulazione.calcolaRendimentiTuttiTitoli()
            .then(data => {
                console.log("Rendimenti calcolati:", data);
                
                // Ricarica le simulazioni dal server
                this.loadSimulazioniFromServer();
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
                
                // Mostra un messaggio di successo
                DomUtils.showAlert('Rendimenti calcolati con successo!', 'success');
            })
            .catch(error => {
                console.error('Errore:', error);
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
                
                // Mostra un messaggio di errore
                DomUtils.showAlert('Si è verificato un errore nel calcolo dei rendimenti.', 'danger');
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
        
        Simulazione.calcolaRendimento(titoloId, prezzoAcquisto, importo)
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

// Inizializza il controller quando il DOM è pronto
document.addEventListener('DOMContentLoaded', function() {
    // Crea l'istanza del controller e la rende disponibile globalmente
    window.simulazioniController = new SimulazioniController();
});
