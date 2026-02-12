/**
 * Controller specifico per la pagina nuova-simulazione.html
 * Gestisce la logica e le interazioni della pagina di creazione di una nuova simulazione
 */

// Namespace per il modulo NuovaSimulazioneController
window.NuovaSimulazioneController = {
    
    /**
     * Inizializza il controller
     */
    init: function() {
        console.log('Inizializzazione NuovaSimulazioneController');
        
        // Inizializza i datepicker
        this.initDatepickers();
        
        // Aggiungi gli event listeners
        this.setupEventListeners();
    },
    
    /**
     * Inizializza i datepicker
     */
    initDatepickers: function() {
        const dataAcquistoInput = document.getElementById('data-acquisto');
        if (dataAcquistoInput) {
            // Inizializza il datepicker con jQuery
            if (window.jQuery && $.fn && $.fn.datepicker) {
                $(dataAcquistoInput).datepicker({
                    format: 'dd-mm-yyyy',
                    language: 'it',
                    autoclose: true
                });
                
                // Imposta la data di oggi come default
                const today = new Date();
                $(dataAcquistoInput).datepicker('setDate', today);
                
                // Salva la data in formato ISO come attributo data-iso-date
                dataAcquistoInput.setAttribute('data-iso-date', today.toISOString().split('T')[0]);
                
                // Aggiungi event listener per aggiornare l'attributo data-iso-date quando la data cambia
                $(dataAcquistoInput).on('changeDate', function(e) {
                    const selectedDate = e.date;
                    const isoDate = selectedDate.toISOString().split('T')[0];
                    dataAcquistoInput.setAttribute('data-iso-date', isoDate);
                    
                    // Aggiorna i giorni alla scadenza
                    NuovaSimulazioneController.updateGiorniAllaScadenza();
                });
            } else {
                console.error('jQuery o datepicker non disponibili');
            }
        }
    },
    
    /**
     * Configura gli event listeners
     */
    setupEventListeners: function() {
        console.log('Configurazione event listeners per nuova-simulazione.html');
        
        // Event listener per il form di simulazione
        const simulazioneForm = document.getElementById('simulazione-form');
        if (simulazioneForm) {
            simulazioneForm.addEventListener('submit', function(e) {
                e.preventDefault();
                NuovaSimulazioneController.calcolaRendimento();
            });
        } else {
            console.error('Elemento simulazione-form non trovato');
        }
        
        // Event listener per il pulsante "Prezzo Corrente"
        const prezzoCorrenteBtn = document.getElementById('prezzo-corrente-btn');
        if (prezzoCorrenteBtn) {
            prezzoCorrenteBtn.addEventListener('click', function() {
                console.log('Pulsante Prezzo Corrente cliccato');
                NuovaSimulazioneController.getPrezzoCorrente();
            });
        } else {
            console.error('Elemento prezzo-corrente-btn non trovato');
        }
        
        // Event listener per il pulsante "Salva Simulazione"
        const salvaSimulazioneBtn = document.getElementById('salva-simulazione-btn');
        if (salvaSimulazioneBtn) {
            salvaSimulazioneBtn.addEventListener('click', function() {
                NuovaSimulazioneController.salvaSimulazione();
            });
        } else {
            console.error('Elemento salva-simulazione-btn non trovato');
        }
        
        // Event listener per il cambio di titolo
        const titoloSelect = document.getElementById('titolo-select');
        if (titoloSelect) {
            titoloSelect.addEventListener('change', function() {
                NuovaSimulazioneController.onTitoloChange();
            });
        } else {
            console.error('Elemento titolo-select non trovato');
        }
        
        // Event listener per il cambio di data acquisto
        const dataAcquistoInput = document.getElementById('data-acquisto');
        if (dataAcquistoInput) {
            dataAcquistoInput.addEventListener('change', function() {
                NuovaSimulazioneController.updateGiorniAllaScadenza();
            });
        } else {
            console.error('Elemento data-acquisto non trovato');
        }
    },
    
    /**
     * Gestisce il cambio di titolo
     */
    onTitoloChange: function() {
        const titoloId = document.getElementById('titolo-select').value;
        
        if (!titoloId) {
            return;
        }
        
        // Trova il titolo selezionato
        const titolo = window.titoli.find(t => t.id == titoloId);
        
        if (titolo) {
            // Imposta il tasso di interesse
            if (titolo.tassoNominale) {
                document.getElementById('tasso-interesse').value = Formatters.formatDecimal(titolo.tassoNominale);
            }
            
            // Calcola i giorni alla scadenza
            this.updateGiorniAllaScadenza();
        }
    },
    
    /**
     * Aggiorna i giorni alla scadenza
     */
    updateGiorniAllaScadenza: function() {
        const titoloSelect = document.getElementById('titolo-select');
        const dataAcquistoInput = document.getElementById('data-acquisto');
        const giorniAllaScadenzaInput = document.getElementById('giorni-alla-scadenza');
        
        if (!titoloSelect || !dataAcquistoInput || !giorniAllaScadenzaInput) {
            console.error('Elementi necessari per il calcolo dei giorni alla scadenza non trovati');
            return;
        }
        
        const titoloId = titoloSelect.value;
        if (!titoloId) {
            giorniAllaScadenzaInput.value = '';
            return;
        }
        
        // Trova il titolo selezionato
        const titolo = window.titoli.find(t => t.id == titoloId);
        if (!titolo || !titolo.dataScadenza) {
            giorniAllaScadenzaInput.value = '';
            return;
        }
        
        // Recupera la data di acquisto
        const dataAcquistoIso = dataAcquistoInput.getAttribute('data-iso-date');
        if (!dataAcquistoIso) {
            giorniAllaScadenzaInput.value = '';
            return;
        }
        
        // Calcola i giorni alla scadenza
        const dataAcquisto = new Date(dataAcquistoIso);
        const dataScadenza = new Date(titolo.dataScadenza);
        
        // Calcola la differenza in millisecondi
        const diffTime = dataScadenza.getTime() - dataAcquisto.getTime();
        
        // Converti in giorni
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        // Aggiorna il campo
        giorniAllaScadenzaInput.value = diffDays > 0 ? diffDays : '0';
    },
    
    /**
     * Ottiene il prezzo corrente del titolo
     */
    getPrezzoCorrente: function() {
        console.log('Recupero prezzo corrente...');
        
        const titoloId = document.getElementById('titolo-select').value;
        
        if (!titoloId) {
            alert('Seleziona prima un titolo');
            return;
        }
        
        // Mostra un indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        // Trova il titolo selezionato
        const titolo = window.titoli.find(t => t.id == titoloId);
        
        if (titolo && titolo.prezzo) {
            // Imposta il prezzo corrente nel campo prezzo acquisto
            document.getElementById('prezzo-acquisto').value = Formatters.formatDecimal(titolo.prezzo);
            DomUtils.toggleLoading(false);
        } else {
            // Se il prezzo non è disponibile, prova a recuperarlo dal server
            // Prima otteniamo il tipo di titolo e il codice ISIN
            if (titolo && titolo.tipoTitolo && titolo.codiceIsin) {
                console.log(`Recupero prezzo corrente per ${titolo.tipoTitolo} ${titolo.codiceIsin}`);
                
                // Usa Titolo.getPrezzoCorrente che è un wrapper per ApiService.getPrezzoTitolo
                console.log(`Chiamata a Titolo.getPrezzoCorrente con tipo=${titolo.tipoTitolo}, isin=${titolo.codiceIsin}`);
                Titolo.getPrezzoCorrente(titolo.tipoTitolo, titolo.codiceIsin)
                    .then(prezzoData => {
                        console.log('Prezzo recuperato:', prezzoData);
                        console.log('Tipo di prezzoData:', typeof prezzoData);
                        
                        // Verifica se prezzoData è un oggetto con una proprietà prezzo
                        if (prezzoData && typeof prezzoData === 'object' && prezzoData.prezzo !== undefined) {
                            console.log('prezzoData è un oggetto con prezzo:', prezzoData.prezzo);
                            document.getElementById('prezzo-acquisto').value = Formatters.formatDecimal(prezzoData.prezzo);
                        } 
                        // Verifica se prezzoData è direttamente il valore del prezzo
                        else if (prezzoData !== null && prezzoData !== undefined && typeof prezzoData !== 'object') {
                            console.log('prezzoData è direttamente il valore:', prezzoData);
                            document.getElementById('prezzo-acquisto').value = Formatters.formatDecimal(prezzoData);
                        }
                        // Se prezzoData è un oggetto ma non ha la proprietà prezzo, cerca altre proprietà che potrebbero contenere il prezzo
                        else if (prezzoData && typeof prezzoData === 'object') {
                            console.log('prezzoData è un oggetto senza prezzo, cerco altre proprietà:', Object.keys(prezzoData));
                            // Cerca proprietà che potrebbero contenere il prezzo
                            const possiblePriceProps = ['corso', 'valore', 'price', 'value'];
                            let found = false;
                            
                            for (const prop of possiblePriceProps) {
                                if (prezzoData[prop] !== undefined) {
                                    console.log(`Trovata proprietà ${prop} con valore:`, prezzoData[prop]);
                                    document.getElementById('prezzo-acquisto').value = Formatters.formatDecimal(prezzoData[prop]);
                                    found = true;
                                    break;
                                }
                            }
                            
                            if (!found) {
                                console.error('Nessuna proprietà di prezzo trovata nell\'oggetto:', prezzoData);
                                alert('Prezzo non disponibile per questo titolo');
                            }
                        } else {
                            console.error('Prezzo non disponibile:', prezzoData);
                            alert('Prezzo non disponibile per questo titolo');
                        }
                        
                        DomUtils.toggleLoading(false);
                    })
                    .catch(error => {
                        console.error('Errore nel recupero del prezzo:', error);
                        DomUtils.toggleLoading(false);
                        alert('Si è verificato un errore nel recupero del prezzo');
                    });
            } else {
                // Fallback alla vecchia funzione getTitolo
                console.log('Fallback a getTitolo per recuperare il prezzo');
                ApiService.getTitolo(titoloId)
                    .then(titoloData => {
                        if (titoloData && titoloData.prezzo) {
                            document.getElementById('prezzo-acquisto').value = Formatters.formatDecimal(titoloData.prezzo);
                        } else {
                            alert('Prezzo non disponibile per questo titolo');
                        }
                        DomUtils.toggleLoading(false);
                    })
                    .catch(error => {
                        console.error('Errore nel recupero del prezzo:', error);
                        DomUtils.toggleLoading(false);
                        alert('Si è verificato un errore nel recupero del prezzo');
                    });
            }
        }
    },
    
    /**
     * Calcola il rendimento
     */
    calcolaRendimento: function() {
        console.log('Calcolo rendimento...');
        
        const titoloId = document.getElementById('titolo-select').value;
        const prezzoAcquisto = document.getElementById('prezzo-acquisto').value.replace(',', '.');
        const importo = document.getElementById('importo-nominale').value.replace(',', '.');
        
        if (!titoloId || !prezzoAcquisto || !importo) {
            alert('Compila tutti i campi richiesti');
            return;
        }
        
        // Mostra un indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        // Chiama l'API per calcolare il rendimento
        ApiService.calcolaRendimento(titoloId, prezzoAcquisto, importo)
            .then(data => {
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
                
                // Popola i campi dei risultati
                document.getElementById('plusvalenza-netta').value = Formatters.formatCurrency(data.plusvalenzaNetta);
                document.getElementById('interessi-netti').value = Formatters.formatCurrency(data.interessiNetti);
                document.getElementById('commissioni').value = Formatters.formatCurrency(data.commissioni);
                document.getElementById('imposta-bollo').value = Formatters.formatCurrency(data.impostaBollo);
                document.getElementById('guadagno-totale').value = Formatters.formatCurrency(data.guadagnoTotale);
                document.getElementById('guadagno-netto-commissioni').value = Formatters.formatCurrency(data.guadagnoNettoCommissioni);
                document.getElementById('tasso').value = Formatters.formatPercentage(data.tasso);
                document.getElementById('tasso-netto-commissioni').value = Formatters.formatPercentage(data.tassoNettoCommissioni);
                document.getElementById('guadagno-netto-bollo').value = Formatters.formatCurrency(data.guadagnoNettoBollo);
                document.getElementById('tasso-netto-bollo').value = Formatters.formatPercentage(data.tassoNettoBollo);
                document.getElementById('importo-scadenza').value = Formatters.formatCurrency(data.importoScadenza);
                
                // Abilita il pulsante per salvare la simulazione
                document.getElementById('salva-simulazione-btn').disabled = false;
                
                // Salva i dati del risultato
                this.ultimoRisultatoCalcolo = data;
            })
            .catch(error => {
                console.error('Errore nel calcolo del rendimento:', error);
                DomUtils.toggleLoading(false);
                alert('Si è verificato un errore nel calcolo del rendimento');
            });
    },
    
    /**
     * Salva la simulazione
     */
    salvaSimulazione: function() {
        console.log('Salvataggio simulazione...');
        
        const titoloId = document.getElementById('titolo-select').value;
        const prezzoAcquisto = document.getElementById('prezzo-acquisto').value.replace(',', '.');
        const importo = document.getElementById('importo-nominale').value.replace(',', '.');
        const dataAcquisto = document.getElementById('data-acquisto').getAttribute('data-iso-date');
        
        if (!titoloId || !prezzoAcquisto || !importo || !dataAcquisto) {
            alert('Compila tutti i campi richiesti');
            return;
        }
        
        // Mostra un indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        // Prepara i dati per la richiesta
        const simulazione = {
            titoloId: titoloId,
            prezzoAcquisto: parseFloat(prezzoAcquisto),
            importoNominale: parseFloat(importo),
            dataAcquisto: dataAcquisto
        };
        
        // Chiama l'API per salvare la simulazione
        ApiService.saveSimulazione(simulazione)
            .then(data => {
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
                
                // Mostra un messaggio di successo
                alert('Simulazione salvata con successo!');
                
                // Disabilita il pulsante per salvare la simulazione
                document.getElementById('salva-simulazione-btn').disabled = true;
            })
            .catch(error => {
                console.error('Errore nel salvataggio della simulazione:', error);
                DomUtils.toggleLoading(false);
                alert('Si è verificato un errore nel salvataggio della simulazione');
            });
    }
};

// Inizializza il controller quando il DOM è pronto
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM caricato, inizializzazione NuovaSimulazioneController');
    NuovaSimulazioneController.init();
});