/**
 * Controller per la gestione dei titoli
 * Gestisce la visualizzazione e l'interazione con i titoli
 */

// Namespace per il modulo TitoliController
window.TitoliController = {
    // Variabile per tenere traccia dei titoli selezionati
    titoliSelezionati: [],
    
    /**
     * Inizializza il controller
     */
    init: function() {
        console.log('Inizializzazione TitoliController');
        
        // Inizializza window.titoli come array vuoto se non esiste
        if (!window.titoli) {
            window.titoli = [];
        }
        
        // Carica i titoli dal server
        this.loadTitoli();
        
        // Aggiungi gli event listeners
        this.setupEventListeners();
        
        // Popola i select con i valori degli enum
        this.populateEnumSelects();
    },
    
    /**
     * Configura gli event listeners per le azioni sui titoli
     */
    setupEventListeners: function() {
        // Event listener per il pulsante "Aggiungi Titolo"
        document.getElementById('add-titolo-btn').addEventListener('click', () => this.showTitoloModal());
        
        // Event listener per il pulsante "Salva" nel modal
        document.getElementById('save-titolo-btn').addEventListener('click', () => this.saveTitolo());
        
        // Event listener per il pulsante "Cerca Titolo"
        document.getElementById('cerca-titolo-btn').addEventListener('click', () => this.cercaTitoloByIsin());
        
        // Event listeners per i pulsanti "Lista BTP" e "Lista BOT"
        document.getElementById('lista-btp-btn').addEventListener('click', () => this.showListaTitoli('BTP'));
        document.getElementById('lista-bot-btn').addEventListener('click', () => this.showListaTitoli('BOT'));
        
        // Event listener per il checkbox "Seleziona tutti"
        const selectAllCheckbox = document.getElementById('select-all-titoli');
        if (selectAllCheckbox) {
            selectAllCheckbox.addEventListener('change', () => this.toggleSelectAll());
        }
        
        // Event listener per i pulsanti "Aggiungi selezionati" (in alto e in basso nel modal)
        const aggiungiSelezionatiBtn = document.getElementById('aggiungi-selezionati-btn');
        const aggiungiSelezionatiBtnTop = document.getElementById('aggiungi-selezionati-btn-top');
        
        if (aggiungiSelezionatiBtn) {
            aggiungiSelezionatiBtn.addEventListener('click', () => this.aggiungiTitoliSelezionati());
        }
        
        if (aggiungiSelezionatiBtnTop) {
            aggiungiSelezionatiBtnTop.addEventListener('click', () => this.aggiungiTitoliSelezionati());
        }
        
        // Event listeners per i pulsanti di paginazione
        document.getElementById('prev-page-btn').addEventListener('click', () => {
            if (this.currentPage > 0) {
                this.currentPage--;
                this.loadTitoliPage();
            }
        });
        
        document.getElementById('next-page-btn').addEventListener('click', () => {
            if (this.currentPage < this.totalPages - 1) {
                this.currentPage++;
                this.loadTitoliPage();
            }
        });
        
        // Inizializza i datepicker con validazione per data futura
        this.initDatepickers();
    },
    
    /**
     * Inizializza i datepicker con validazione per data futura
     */
    initDatepickers: function() {
        // Inizializza il datepicker per la data di scadenza con validazione per data futura
        const dataScadenzaInput = document.getElementById('data-scadenza');
        if (dataScadenzaInput) {
            DomUtils.initDatepicker(dataScadenzaInput, {}, true); // true per validare che la data sia nel futuro
        }
    },
    
    /**
     * Carica i titoli dal server
     */
    loadTitoli: function() {
        console.log('Caricamento titoli dal server...');
        Titolo.load()
            .then(data => {
                console.log('Titoli caricati con successo:', data);
                // Memorizza i titoli
                window.titoli = data;
                
                // Aggiorna le viste
                this.updateTitoliTable();
                this.updateTitoliSelect();
                
                // Se simulazioniController esiste, ricarica le simulazioni
                if (window.simulazioniController) {
                    console.log('Ricaricamento simulazioni dopo caricamento titoli...');
                    window.simulazioniController.loadSimulazioniFromServer();
                }
            })
            .catch(error => {
                console.error('Errore nel caricamento dei titoli:', error);
                // In caso di errore, carica i dati di esempio
                this.loadSampleData();
                
                // Aggiorna le viste con i dati di esempio
                this.updateTitoliTable();
                this.updateTitoliSelect();
                
                // Se simulazioniController esiste, ricarica le simulazioni
                if (window.simulazioniController) {
                    console.log('Ricaricamento simulazioni dopo caricamento dati di esempio...');
                    window.simulazioniController.loadSimulazioniFromServer();
                }
            });
    },
    
    /**
     * Carica dati di esempio per i titoli
     */
    loadSampleData: function() {
        // Titoli di esempio
        const oggi = new Date();
        const scadenza1 = new Date();
        scadenza1.setFullYear(oggi.getFullYear() + 5);
        const scadenza2 = new Date();
        scadenza2.setFullYear(oggi.getFullYear() + 3);
        const scadenza3 = new Date();
        scadenza3.setFullYear(oggi.getFullYear() + 7);
        
        window.titoli = [
            { 
                id: 1, 
                nome: 'BTP Italia 2028', 
                codiceIsin: 'IT0005467482', 
                dataScadenza: scadenza1.toISOString().split('T')[0], 
                tassoNominale: 2.50, 
                periodicitaCedole: 'SEMESTRALE', 
                periodicitaBollo: 'ANNUALE',
                tipoTitolo: 'BTP',
                prezzo: 98.75 
            },
            { 
                id: 2, 
                nome: 'BOT 2026', 
                codiceIsin: 'IT0005451361', 
                dataScadenza: scadenza2.toISOString().split('T')[0], 
                tassoNominale: 1.85, 
                periodicitaCedole: 'SEMESTRALE', 
                periodicitaBollo: 'ANNUALE',
                tipoTitolo: 'BOT',
                prezzo: 99.50 
            },
            { 
                id: 3, 
                nome: 'BTP 2030', 
                codiceIsin: 'IT0005413171', 
                dataScadenza: scadenza3.toISOString().split('T')[0], 
                tassoNominale: 3.10, 
                periodicitaCedole: 'SEMESTRALE', 
                periodicitaBollo: 'ANNUALE',
                tipoTitolo: 'BTP',
                prezzo: 97.80 
            }
        ];
    },
    
    /**
     * Aggiorna la tabella dei titoli
     */
    updateTitoliTable: function() {
        const tbody = document.getElementById('titoli-list');
        if (!tbody) {
            console.log('Elemento titoli-list non trovato nel DOM, potrebbe essere una pagina diversa');
            return;
        }
        
        tbody.innerHTML = '';
        
        window.titoli.forEach(titolo => {
            const row = document.createElement('tr');
            
            // Verifica se titolo.prezzo è definito prima di chiamare toFixed
            const prezzoFormattato = titolo.prezzo !== undefined && titolo.prezzo !== null 
                ? `${titolo.prezzo.toFixed(2)} €` 
                : 'N/D';
                
            // Verifica se titolo.tassoNominale è definito prima di formattarlo
            const tassoFormattato = titolo.tassoNominale !== undefined && titolo.tassoNominale !== null 
                ? `${Formatters.formatDecimal(titolo.tassoNominale)} %` 
                : 'N/D';
                
            // Formatta la data di scadenza
            const dataScadenzaFormattata = titolo.dataScadenza 
                ? Formatters.formatDate(titolo.dataScadenza) 
                : 'N/D';
            
            row.innerHTML = `
                <td>${titolo.codiceIsin || ''}</td>
                <td>${titolo.nome || ''}</td>
                <td>${dataScadenzaFormattata}</td>
                <td>${tassoFormattato}</td>
                <td>${prezzoFormattato}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="TitoliController.editTitolo(${titolo.id})">Modifica</button>
                    <button class="btn btn-sm btn-outline-danger" onclick="TitoliController.deleteTitolo(${titolo.id})">Elimina</button>
                </td>
            `;
            tbody.appendChild(row);
        });
        
        console.log(`Tabella titoli aggiornata con ${window.titoli.length} titoli`);
    },
    
    /**
     * Aggiorna il select dei titoli
     */
    updateTitoliSelect: function() {
        const select = document.getElementById('titolo-select');
        
        // Verifica se l'elemento select esiste
        if (!select) {
            console.log('Elemento titolo-select non trovato nel DOM, potrebbe essere una pagina diversa');
            return;
        }
        
        // Mantieni solo la prima opzione
        select.innerHTML = '<option value="">Seleziona un titolo</option>';
        
        window.titoli.forEach(titolo => {
            const option = document.createElement('option');
            option.value = titolo.id;
            
            // Verifica se titolo.prezzo è definito prima di chiamare toFixed
            const prezzoFormattato = titolo.prezzo !== undefined && titolo.prezzo !== null 
                ? `${titolo.prezzo.toFixed(2)} €` 
                : 'N/D';
            
            option.textContent = `${titolo.nome} (${titolo.codiceIsin}) - ${prezzoFormattato}`;
            select.appendChild(option);
        });
    },
    
    /**
     * Popola i select con i valori degli enum
     */
    populateEnumSelects: function() {
        // Popola il select delle periodicità cedole
        ApiService.getEnumValues('periodicita-cedole')
            .then(data => {
                const select = document.getElementById('periodicita-cedole');
                select.innerHTML = '<option value="">Seleziona</option>';
                
                Object.entries(data).forEach(([key, value]) => {
                    const option = document.createElement('option');
                    option.value = key;
                    option.textContent = value;
                    select.appendChild(option);
                });
            })
            .catch(error => console.error('Errore nel caricamento delle periodicità cedole:', error));
        
        // Popola il select delle periodicità bollo
        ApiService.getEnumValues('periodicita-bollo')
            .then(data => {
                const select = document.getElementById('periodicita-bollo');
                select.innerHTML = '<option value="">Seleziona</option>';
                
                Object.entries(data).forEach(([key, value]) => {
                    const option = document.createElement('option');
                    option.value = key;
                    option.textContent = value;
                    select.appendChild(option);
                });
            })
            .catch(error => console.error('Errore nel caricamento delle periodicità bollo:', error));
        
        // Popola il select dei tipi titolo
        ApiService.getEnumValues('tipo-titolo')
            .then(data => {
                const select = document.getElementById('tipo-titolo');
                select.innerHTML = '<option value="">Seleziona</option>';
                
                Object.entries(data).forEach(([key, value]) => {
                    const option = document.createElement('option');
                    option.value = key;
                    option.textContent = value;
                    select.appendChild(option);
                });
            })
            .catch(error => console.error('Errore nel caricamento dei tipi titolo:', error));
    },
    
    /**
     * Mostra il modal per aggiungere/modificare un titolo
     * @param {number} titoloId - L'ID del titolo da modificare (opzionale)
     */
    showTitoloModal: function(titoloId = null) {
        // Reset del form
        document.getElementById('titolo-form').reset();
        document.getElementById('titolo-id').value = '';
        document.getElementById('titolo-modal-label').textContent = 'Aggiungi Titolo';
        
        // Inizializza il datepicker con validazione per data futura
        const dataScadenzaInput = document.getElementById('data-scadenza');
        DomUtils.initDatepicker(dataScadenzaInput, {}, true);
        
        // Se è una modifica, popola il form con i dati del titolo
        if (titoloId) {
            const titolo = window.titoli.find(t => t.id === titoloId);
            if (titolo) {
                document.getElementById('titolo-id').value = titolo.id;
                document.getElementById('nome-titolo').value = titolo.nome;
                document.getElementById('codice-isin').value = titolo.codiceIsin || '';
                
                // Imposta la data di scadenza con validazione
                if (titolo.dataScadenza) {
                    const dataScadenza = new Date(titolo.dataScadenza);
                    DomUtils.setDatepickerDate(dataScadenzaInput, dataScadenza, true);
                }
                
                document.getElementById('tasso-nominale').value = titolo.tassoNominale ? Formatters.formatDecimal(titolo.tassoNominale) : '';
                document.getElementById('periodicita-cedole').value = titolo.periodicitaCedole || '';
                document.getElementById('periodicita-bollo').value = titolo.periodicitaBollo || '';
                document.getElementById('tipo-titolo').value = titolo.tipoTitolo || '';
                document.getElementById('prezzo-titolo').value = titolo.prezzo ? Formatters.formatDecimal(titolo.prezzo) : '';
                document.getElementById('titolo-modal-label').textContent = 'Modifica Titolo';
            }
        }
        
        // Mostra il modal
        const modal = new bootstrap.Modal(document.getElementById('titolo-modal'));
        modal.show();
    },
    
    /**
     * Salva un titolo (nuovo o esistente)
     */
    saveTitolo: function() {
        const titoloId = document.getElementById('titolo-id').value;
        const nome = document.getElementById('nome-titolo').value;
        const codiceIsin = document.getElementById('codice-isin').value;
        const dataScadenza = document.getElementById('data-scadenza').getAttribute('data-iso-date');
        const tassoNominaleStr = document.getElementById('tasso-nominale').value;
        const periodicitaCedole = document.getElementById('periodicita-cedole').value;
        const periodicitaBollo = document.getElementById('periodicita-bollo').value;
        const tipoTitolo = document.getElementById('tipo-titolo').value;
        const prezzoText = document.getElementById('prezzo-titolo').value;
        
        // Verifica quali campi sono vuoti e mostra un messaggio specifico
        let campiMancanti = [];
        if (!nome) campiMancanti.push('Nome titolo');
        if (!codiceIsin) campiMancanti.push('Codice ISIN');
        if (!dataScadenza) campiMancanti.push('Data scadenza');
        if (!tassoNominaleStr) campiMancanti.push('Tasso nominale');
        if (!periodicitaCedole) campiMancanti.push('Periodicità cedole');
        if (!periodicitaBollo) campiMancanti.push('Periodicità bollo');
        if (!prezzoText) campiMancanti.push('Prezzo attuale');
        
        if (campiMancanti.length > 0) {
            DomUtils.showAlert('Compila tutti i campi correttamente. Campi mancanti: ' + campiMancanti.join(', '), 'warning');
            return;
        }
        
        // Valida la data di scadenza
        const dataScadenzaValidation = DomUtils.validateExpirationDate(dataScadenza);
        if (!dataScadenzaValidation.isValid) {
            DomUtils.showAlert('Errore nella data di scadenza: ' + dataScadenzaValidation.message, 'warning');
            return;
        }
        
        const tassoNominale = Validators.parseNumericValue(tassoNominaleStr);
        const prezzo = Validators.parseNumericValue(prezzoText);
        
        // Crea l'oggetto titolo
        const titolo = {
            id: titoloId ? parseInt(titoloId) : null,
            nome: nome,
            codiceIsin: codiceIsin,
            dataScadenza: dataScadenza,
            tassoNominale: tassoNominale,
            periodicitaCedole: periodicitaCedole,
            periodicitaBollo: periodicitaBollo,
            tipoTitolo: tipoTitolo,
            prezzo: prezzo
        };
        
        // Mostra un indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        Titolo.save(titolo, prezzo)
            .then(titoloSalvato => {
                // Aggiorna il titolo nella lista locale o aggiungilo se è nuovo
                const index = window.titoli.findIndex(t => t.id === titoloSalvato.id);
                if (index !== -1) {
                    window.titoli[index] = titoloSalvato;
                } else {
                    window.titoli.push(titoloSalvato);
                }
                
                // Aggiorna le viste
                this.updateTitoliTable();
                this.updateTitoliSelect();
                
                // Chiudi il modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('titolo-modal'));
                modal.hide();
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
                
                // Mostra un messaggio di successo
                DomUtils.showAlert('Titolo salvato con successo!', 'success');
            })
            .catch(error => {
                console.error('Errore:', error);
                DomUtils.showAlert('Si è verificato un errore nel salvataggio del titolo.', 'danger');
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
            });
    },
    
    /**
     * Modifica un titolo
     * @param {number} titoloId - L'ID del titolo da modificare
     */
    editTitolo: function(titoloId) {
        // Trova il titolo nella lista
        const titolo = window.titoli.find(t => t.id === titoloId);
        if (!titolo) {
            alert('Titolo non trovato');
            return;
        }
        
        // Mostra il modal per la modifica
        this.showTitoloModal(titoloId);
    },
    
    /**
     * Elimina un titolo
     * @param {number} titoloId - L'ID del titolo da eliminare
     */
    deleteTitolo: function(titoloId) {
        // Chiedi conferma
        if (!confirm('Sei sicuro di voler eliminare questo titolo?')) {
            return;
        }
        
        // Mostra un indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        Titolo.delete(titoloId)
            .then(() => {
                // Rimuovi il titolo dalla lista locale
                const index = window.titoli.findIndex(t => t.id === titoloId);
                if (index !== -1) {
                    window.titoli.splice(index, 1);
                }
                
                // Aggiorna le viste
                this.updateTitoliTable();
                this.updateTitoliSelect();
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
                
                // Mostra un messaggio di successo
                DomUtils.showAlert('Titolo eliminato con successo!', 'success');
            })
            .catch(error => {
                console.error('Errore:', error);
                DomUtils.showAlert('Si è verificato un errore nell\'eliminazione del titolo.', 'danger');
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
            });
    },
    
    /**
     * Cerca un titolo per codice ISIN
     */
    cercaTitoloByIsin: function() {
        const isin = document.getElementById('cerca-isin').value;
        
        if (!isin) {
            alert('Inserisci un codice ISIN valido');
            return;
        }
        
        Titolo.loadByIsin(isin)
            .then(titoloTrovato => {
                console.log("Titolo trovato:", titoloTrovato);
                
                // Aggiorna il titolo nella lista locale o aggiungilo se è nuovo
                const index = window.titoli.findIndex(t => t.id === titoloTrovato.id);
                if (index !== -1) {
                    window.titoli[index] = titoloTrovato;
                } else {
                    window.titoli.push(titoloTrovato);
                }
                
                // Aggiorna le viste
                this.updateTitoliTable();
                this.updateTitoliSelect();
                
                // Mostra un messaggio di successo
                alert('Titolo trovato e aggiunto alla lista!');
            })
            .catch(error => {
                console.error('Errore:', error);
                alert('Titolo non trovato. Verifica il codice ISIN e riprova.');
            });
    },
    
    // Variabili per la paginazione
    currentPage: 0,
    pageSize: 10,
    totalPages: 1,
    totalElements: 0,
    currentTipoTitolo: '',
    
    /**
     * Mostra la lista dei titoli filtrata per tipo, recuperandoli da Borsa Italiana in un modal
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     */
    showListaTitoli: function(tipo) {
        console.log(`Recuperando titoli di tipo ${tipo} da Borsa Italiana...`);
        
        // Prepara il modal
        const modal = new bootstrap.Modal(document.getElementById('lista-titoli-modal'));
        const modalTitle = document.getElementById('lista-titoli-modal-label');
        const tipoSpan = document.getElementById('lista-titoli-tipo');
        const countSpan = document.getElementById('lista-titoli-count');
        const loadingSpinner = document.getElementById('lista-titoli-loading');
        const errorAlert = document.getElementById('lista-titoli-error');
        const tbody = document.getElementById('lista-titoli-body');
        
        // Imposta il titolo del modal
        modalTitle.textContent = `Lista Titoli ${tipo}`;
        tipoSpan.textContent = `Titoli ${tipo}`;
        countSpan.textContent = '';
        
        // Mostra il modal e l'indicatore di caricamento
        modal.show();
        loadingSpinner.classList.remove('d-none');
        errorAlert.classList.add('d-none');
        tbody.innerHTML = '';
        
        // Imposta il tipo di titolo corrente
        this.currentTipoTitolo = tipo;
        
        // Resetta la pagina corrente
        this.currentPage = 0;
        
        // Carica la prima pagina
        this.loadTitoliPage();
    },
    
    /**
     * Carica una pagina di titoli
     */
    loadTitoliPage: function() {
        const loadingSpinner = document.getElementById('lista-titoli-loading');
        const errorAlert = document.getElementById('lista-titoli-error');
        const tbody = document.getElementById('lista-titoli-body');
        const countSpan = document.getElementById('lista-titoli-count');
        
        // Mostra l'indicatore di caricamento
        loadingSpinner.classList.remove('d-none');
        errorAlert.classList.add('d-none');
        tbody.innerHTML = '';
        
        Titolo.loadTitoliPaginati(this.currentTipoTitolo, this.currentPage, this.pageSize)
            .then(paginatedResponse => {
                console.log(`Titoli ${this.currentTipoTitolo} recuperati da Borsa Italiana:`, paginatedResponse);
                
                // Nascondi l'indicatore di caricamento
                loadingSpinner.classList.add('d-none');
                
                // Aggiorna le variabili di paginazione
                this.totalPages = paginatedResponse.totalPages;
                this.totalElements = paginatedResponse.totalElements;
                
                // Aggiorna il conteggio dei titoli
                countSpan.textContent = `(${this.totalElements} titoli trovati)`;
                
                // Aggiorna i controlli di paginazione
                this.updatePaginationControls();
                
                const titoliFromServer = paginatedResponse.content;
                
                if (!titoliFromServer || titoliFromServer.length === 0) {
                    console.log(`Nessun titolo ${this.currentTipoTitolo} trovato in Borsa Italiana`);
                    
                    // Aggiungi una riga che indica che non ci sono titoli
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td colspan="6" class="text-center">Nessun titolo ${this.currentTipoTitolo} trovato in Borsa Italiana</td>
                    `;
                    tbody.appendChild(row);
                } else {
                    // Popola la tabella con i titoli recuperati
                    titoliFromServer.forEach(titoloDTO => {
                        // Verifica se il titolo esiste già nella lista locale
                        const existingIndex = window.titoli.findIndex(t => t.codiceIsin === titoloDTO.codiceIsin);
                        const isInList = existingIndex !== -1;
                        
                        // Crea la riga per la tabella
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>
                                <div class="form-check">
                                    <input class="form-check-input titolo-checkbox" type="checkbox" 
                                           value="${titoloDTO.codiceIsin}" 
                                           data-tipo="${this.currentTipoTitolo}"
                                           id="checkbox-${titoloDTO.codiceIsin}"
                                           ${isInList ? 'disabled' : ''}
                                           onchange="TitoliController.toggleTitoloSelection('${titoloDTO.codiceIsin}', '${this.currentTipoTitolo}')">
                                </div>
                            </td>
                            <td>${titoloDTO.codiceIsin || ''}</td>
                            <td>${titoloDTO.nome || ''}</td>
                            <td>${titoloDTO.dataScadenza ? Formatters.formatDate(titoloDTO.dataScadenza) : ''}</td>
                            <td>${titoloDTO.tassoNominale ? Formatters.formatDecimal(titoloDTO.tassoNominale) + '%' : ''}</td>
                            <td>${titoloDTO.corso ? Formatters.formatDecimal(titoloDTO.corso) + ' €' : ''}</td>
                            <td>
                                <button class="btn btn-sm ${isInList ? 'btn-success' : 'btn-primary'}" 
                                        onclick="TitoliController.aggiungiTitoloAllaLista('${titoloDTO.codiceIsin}', '${this.currentTipoTitolo}')"
                                        ${isInList ? 'disabled' : ''}>
                                    ${isInList ? 'Già in lista' : 'Aggiungi alla lista'}
                                </button>
                            </td>
                        `;
                        tbody.appendChild(row);
                    });
                    
                    console.log(`Tabella modal aggiornata con ${titoliFromServer.length} titoli di tipo ${this.currentTipoTitolo}`);
                }
            })
            .catch(error => {
                console.error(`Errore nel recupero dei titoli ${this.currentTipoTitolo}:`, error);
                
                // Nascondi l'indicatore di caricamento e mostra l'errore
                loadingSpinner.classList.add('d-none');
                errorAlert.classList.remove('d-none');
                errorAlert.textContent = `Si è verificato un errore durante il caricamento dei titoli ${this.currentTipoTitolo}: ${error.message}`;
            });
    },
    
    /**
     * Aggiorna i controlli di paginazione
     */
    updatePaginationControls: function() {
        DomUtils.updatePaginationControls(this.currentPage, this.totalPages);
    },
    
    /**
     * Aggiunge un titolo alla lista locale e lo salva nel database
     * @param {string} isin - Il codice ISIN del titolo
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     */
    aggiungiTitoloAllaLista: function(isin, tipo) {
        console.log(`Aggiungendo titolo con ISIN ${isin} alla lista...`);
        
        // Mostra un indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        Titolo.loadTitoloDettaglio(tipo, isin)
            .then(titoloObj => {
                console.log(`Dettagli titolo ${isin} recuperati:`, titoloObj);
                
                // Assicurati che il tipo di titolo sia impostato correttamente
                titoloObj.tipoTitolo = tipo;
                
                // Verifica se il titolo esiste già nella lista locale
                const existingIndex = window.titoli.findIndex(t => t.codiceIsin === titoloObj.codiceIsin);
                
                if (existingIndex !== -1) {
                    // Se il titolo esiste già, aggiorna solo la lista locale
                    window.titoli[existingIndex] = titoloObj;
                    console.log(`Titolo ${isin} aggiornato nella lista locale`);
                    
                    // Aggiorna le viste
                    this.updateTitoliTable();
                    this.updateTitoliSelect();
                    
                    // Nascondi l'indicatore di caricamento
                    DomUtils.toggleLoading(false);
                    
                    // Aggiorna il pulsante nella tabella del modal
                    this.updateButtonsInModal(isin, tipo);
                    
                    // Mostra un messaggio di successo
                    alert(`Titolo ${titoloObj.nome} (${isin}) aggiornato con successo!`);
                } else {
                    // Se il titolo non esiste, salvalo nel database
                    Titolo.save(titoloObj)
                        .then(titoloSalvato => {
                            console.log(`Titolo ${isin} salvato nel database:`, titoloSalvato);
                            
                            // Aggiungi il titolo salvato alla lista locale
                            window.titoli.push(titoloSalvato);
                            console.log(`Titolo ${isin} aggiunto alla lista locale`);
                            
                            // Aggiorna le viste
                            this.updateTitoliTable();
                            this.updateTitoliSelect();
                            
                            // Ricalcola i rendimenti di tutti i titoli
                            console.log('Ricalcolo dei rendimenti di tutti i titoli dopo l\'aggiunta di un nuovo titolo');
                            ApiService.calcolaRendimentiTuttiTitoli()
                                .then(data => {
                                    console.log('Rendimenti ricalcolati con successo:', data);
                                    
                                    // Nascondi l'indicatore di caricamento
                                    DomUtils.toggleLoading(false);
                                    
                                    // Aggiorna il pulsante nella tabella del modal
                                    this.updateButtonsInModal(isin, tipo);
                                    
                                    // Mostra un messaggio di successo
                                    alert(`Titolo ${titoloSalvato.nome} (${isin}) aggiunto alla lista e rendimenti ricalcolati con successo!`);
                                    
                                    // Se simulazioniController esiste, ricarica le simulazioni
                                    if (window.simulazioniController) {
                                        window.simulazioniController.loadSimulazioniFromServer();
                                    }
                                })
                                .catch(error => {
                                    console.error('Errore nel ricalcolo dei rendimenti:', error);
                                    
                                    // Nascondi l'indicatore di caricamento
                                    DomUtils.toggleLoading(false);
                                    
                                    // Aggiorna il pulsante nella tabella del modal
                                    this.updateButtonsInModal(isin, tipo);
                                    
                                    // Mostra un messaggio di successo per l'aggiunta del titolo
                                    alert(`Titolo ${titoloSalvato.nome} (${isin}) aggiunto alla lista, ma si è verificato un errore nel ricalcolo dei rendimenti.`);
                                });
                        })
                        .catch(error => {
                            console.error(`Errore nel salvataggio del titolo ${isin} nel database:`, error);
                            
                            // Nascondi l'indicatore di caricamento
                            DomUtils.toggleLoading(false);
                            
                            // Mostra un messaggio di errore
                            alert(`Si è verificato un errore nel salvataggio del titolo nel database: ${error.message}`);
                        });
                }
            })
            .catch(error => {
                console.error(`Errore nell'aggiunta del titolo ${isin} alla lista:`, error);
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
                
                // Mostra un messaggio di errore
                alert(`Si è verificato un errore nell'aggiunta del titolo alla lista: ${error.message}`);
            });
    },
    
    /**
     * Aggiorna i pulsanti nella tabella del modal
     * @param {string} isin - Il codice ISIN del titolo
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     */
    updateButtonsInModal: function(isin, tipo) {
        const buttons = document.querySelectorAll(`button[onclick="TitoliController.aggiungiTitoloAllaLista('${isin}', '${tipo}')"]`);
        buttons.forEach(button => {
            button.classList.remove('btn-primary');
            button.classList.add('btn-success');
            button.textContent = 'Già in lista';
            button.disabled = true;
        });
        
        // Disabilita anche il checkbox corrispondente
        const checkbox = document.getElementById(`checkbox-${isin}`);
        if (checkbox) {
            checkbox.disabled = true;
            checkbox.checked = false;
        }
        
        // Rimuovi il titolo dalla lista dei selezionati se presente
        this.titoliSelezionati = this.titoliSelezionati.filter(t => t.codiceIsin !== isin);
        
        // Aggiorna il contatore e lo stato del pulsante "Aggiungi selezionati"
        this.updateSelectionCounter();
    },
    
    /**
     * Gestisce la selezione/deselezione di un titolo
     * @param {string} isin - Il codice ISIN del titolo
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     */
    toggleTitoloSelection: function(isin, tipo) {
        const checkbox = document.getElementById(`checkbox-${isin}`);
        
        if (checkbox.checked) {
            // Aggiungi il titolo alla lista dei selezionati
            this.titoliSelezionati.push({ codiceIsin: isin, tipoTitolo: tipo });
        } else {
            // Rimuovi il titolo dalla lista dei selezionati
            this.titoliSelezionati = this.titoliSelezionati.filter(t => t.codiceIsin !== isin);
        }
        
        // Aggiorna il contatore e lo stato del pulsante "Aggiungi selezionati"
        this.updateSelectionCounter();
    },
    
    /**
     * Gestisce la selezione/deselezione di tutti i titoli
     */
    toggleSelectAll: function() {
        const selectAllCheckbox = document.getElementById('select-all-titoli');
        const checkboxes = document.querySelectorAll('.titolo-checkbox:not([disabled])');
        
        checkboxes.forEach(checkbox => {
            checkbox.checked = selectAllCheckbox.checked;
            
            const isin = checkbox.value;
            const tipo = checkbox.getAttribute('data-tipo');
            
            if (selectAllCheckbox.checked) {
                // Aggiungi il titolo alla lista dei selezionati se non è già presente
                if (!this.titoliSelezionati.some(t => t.codiceIsin === isin)) {
                    this.titoliSelezionati.push({ codiceIsin: isin, tipoTitolo: tipo });
                }
            } else {
                // Rimuovi il titolo dalla lista dei selezionati
                this.titoliSelezionati = this.titoliSelezionati.filter(t => t.codiceIsin !== isin);
            }
        });
        
        // Aggiorna il contatore e lo stato del pulsante "Aggiungi selezionati"
        this.updateSelectionCounter();
    },
    
    /**
     * Aggiorna il contatore dei titoli selezionati e lo stato del pulsante "Aggiungi selezionati"
     */
    updateSelectionCounter: function() {
        const countElement = document.getElementById('titoli-selezionati-count');
        const countElementTop = document.getElementById('titoli-selezionati-count-top');
        const aggiungiSelezionatiBtn = document.getElementById('aggiungi-selezionati-btn');
        const aggiungiSelezionatiBtnTop = document.getElementById('aggiungi-selezionati-btn-top');
        
        // Aggiorna il contatore in entrambi i pulsanti
        if (countElement) {
            countElement.textContent = this.titoliSelezionati.length;
        }
        
        if (countElementTop) {
            countElementTop.textContent = this.titoliSelezionati.length;
        }
        
        // Abilita/disabilita entrambi i pulsanti
        if (aggiungiSelezionatiBtn) {
            aggiungiSelezionatiBtn.disabled = this.titoliSelezionati.length === 0;
        }
        
        if (aggiungiSelezionatiBtnTop) {
            aggiungiSelezionatiBtnTop.disabled = this.titoliSelezionati.length === 0;
        }
    },
    
    /**
     * Aggiunge i titoli selezionati alla lista
     */
    aggiungiTitoliSelezionati: function() {
        if (this.titoliSelezionati.length === 0) {
            alert('Nessun titolo selezionato');
            return;
        }
        
        console.log(`Aggiungendo ${this.titoliSelezionati.length} titoli selezionati alla lista...`);
        
        // Mostra un indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        // Importa i titoli selezionati
        Titolo.importaTitoliMultipli(this.titoliSelezionati)
            .then(response => {
                console.log('Risposta importazione multipla:', response);
                
                if (response.titoli && response.titoli.length > 0) {
                    // Aggiungi i titoli importati alla lista locale
                    response.titoli.forEach(titolo => {
                        const existingIndex = window.titoli.findIndex(t => t.codiceIsin === titolo.codiceIsin);
                        if (existingIndex !== -1) {
                            window.titoli[existingIndex] = titolo;
                        } else {
                            window.titoli.push(titolo);
                        }
                        
                        // Aggiorna i pulsanti e i checkbox nella tabella del modal
                        this.updateButtonsInModal(titolo.codiceIsin, titolo.tipoTitolo);
                    });
                    
                    // Aggiorna le viste
                    this.updateTitoliTable();
                    this.updateTitoliSelect();
                    
                    // Ricalcola i rendimenti di tutti i titoli
                    ApiService.calcolaRendimentiTuttiTitoli()
                        .then(data => {
                            console.log('Rendimenti ricalcolati con successo:', data);
                            
                            // Nascondi l'indicatore di caricamento
                            DomUtils.toggleLoading(false);
                            
                            // Mostra un messaggio di successo
                            let message = `${response.titoli.length} titoli aggiunti con successo!`;
                            if (response.errori && response.errori.length > 0) {
                                message += ` (${response.errori.length} errori)`;
                            }
                            DomUtils.showAlert(message, 'success');
                            
                            // Se simulazioniController esiste, ricarica le simulazioni
                            if (window.simulazioniController) {
                                window.simulazioniController.loadSimulazioniFromServer();
                            }
                        })
                        .catch(error => {
                            console.error('Errore nel ricalcolo dei rendimenti:', error);
                            
                            // Nascondi l'indicatore di caricamento
                            DomUtils.toggleLoading(false);
                            
                            // Mostra un messaggio di successo per l'aggiunta dei titoli
                            let message = `${response.titoli.length} titoli aggiunti con successo, ma si è verificato un errore nel ricalcolo dei rendimenti.`;
                            if (response.errori && response.errori.length > 0) {
                                message += ` (${response.errori.length} errori)`;
                            }
                            DomUtils.showAlert(message, 'warning');
                        });
                } else {
                    // Nascondi l'indicatore di caricamento
                    DomUtils.toggleLoading(false);
                    
                    // Mostra un messaggio di errore
                    let message = 'Nessun titolo aggiunto.';
                    if (response.errori && response.errori.length > 0) {
                        message += ` (${response.errori.length} errori)`;
                    }
                    DomUtils.showAlert(message, 'warning');
                }
                
                // Resetta la lista dei titoli selezionati
                this.titoliSelezionati = [];
                this.updateSelectionCounter();
            })
            .catch(error => {
                console.error('Errore nell\'importazione multipla dei titoli:', error);
                
                // Nascondi l'indicatore di caricamento
                DomUtils.toggleLoading(false);
                
                // Mostra un messaggio di errore
                DomUtils.showAlert('Si è verificato un errore nell\'importazione multipla dei titoli.', 'danger');
            });
    }
};

// Inizializza il controller quando il DOM è pronto e le librerie sono caricate
document.addEventListener('DOMContentLoaded', function() {
    // Funzione per verificare se jQuery e datepicker sono disponibili
    function checkJQueryAndDatepicker() {
        return window.jQuery && $.fn && $.fn.datepicker;
    }
    
    // Funzione per inizializzare il controller
    function initController() {
        if (checkJQueryAndDatepicker()) {
            // Inizializza il controller
            TitoliController.init();
            console.log('Controller titoli inizializzato con successo');
        } else {
            console.warn('jQuery o datepicker non disponibili. Inizializzazione controller rimandata.');
            // Riprova dopo un breve ritardo
            setTimeout(function() {
                if (checkJQueryAndDatepicker()) {
                    TitoliController.init();
                    console.log('Controller titoli inizializzato con successo (ritardato)');
                } else {
                    console.error('Impossibile inizializzare il controller: jQuery o datepicker non disponibili.');
                }
            }, 500);
        }
    }
    
    // Avvia l'inizializzazione
    initController();
});
