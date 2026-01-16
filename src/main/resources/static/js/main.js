// Funzioni per l'applicazione Rendimento Titoli

// Funzione di validazione per i campi numerici - versione semplificata
// NOTA: Controllo sul formato commentato per evitare problemi con numeri decimali
function isValidNumber(value) {
    // Accetta qualsiasi valore che possa essere convertito in numero
    // Non fa controlli specifici sul formato (punto o virgola)
    if (value === null || value === undefined || value === '') return false;
    
    try {
        // Normalizza il valore sostituendo le virgole con punti
        let normalizedValue = value.toString().trim();
        normalizedValue = normalizedValue.replace(/,/g, '.');
        
        // Verifica solo che sia un numero, senza controlli rigorosi sul formato
        return !isNaN(parseFloat(normalizedValue));
    } catch (e) {
        console.error("Errore nella validazione del numero:", e);
        return false;
    }
}

// Funzione per convertire una stringa in numero
function parseNumericValue(value) {
    if (!value) return 0;
    
    try {
        // Normalizza il valore sostituendo le virgole con punti
        let normalizedValue = value.toString().trim();
        
        // Sostituisci tutte le virgole con punti
        normalizedValue = normalizedValue.replace(/,/g, '.');
        
        // Converte in numero
        return parseFloat(normalizedValue);
    } catch (e) {
        console.error("Errore nella conversione del numero:", e);
        return 0;
    }
}

// Funzione per formattare un numero decimale con la virgola come separatore
function formatDecimal(value) {
    if (value === null || value === undefined) return '';
    
    // Converti il valore in numero e poi formatta con 5 decimali massimo
    const num = parseFloat(value);
    if (isNaN(num)) return '';
    
    // Usa Intl.NumberFormat per formattare il numero secondo le convenzioni italiane
    return new Intl.NumberFormat('it-IT', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 5
    }).format(num);
}

// Formatta una data in formato italiano (gg-mm-aaaa)
function formatDate(dateString) {
    if (!dateString) return '';
    
    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return '';
        
        const day = date.getDate().toString().padStart(2, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const year = date.getFullYear();
        return `${day}-${month}-${year}`;
    } catch (e) {
        console.error("Errore nella formattazione della data:", e);
        return '';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // Imposta le opzioni globali per il datepicker
    $.fn.datepicker.defaults.format = 'dd-mm-yyyy';
    $.fn.datepicker.defaults.language = 'it';
    $.fn.datepicker.defaults.autoclose = true;
    $.fn.datepicker.defaults.todayHighlight = true;
    
    // Forza il formato italiano per le date
    $.fn.datepicker.dates['it'] = {
        days: ["Domenica", "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato"],
        daysShort: ["Dom", "Lun", "Mar", "Mer", "Gio", "Ven", "Sab"],
        daysMin: ["Do", "Lu", "Ma", "Me", "Gi", "Ve", "Sa"],
        months: ["Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"],
        monthsShort: ["Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"],
        today: "Oggi",
        clear: "Cancella",
        format: "dd-mm-yyyy",
        titleFormat: "MM yyyy",
        weekStart: 1
    };
    
    // Inizializzazione
    initApp();
    
    // Event listeners
    document.getElementById('add-titolo-btn').addEventListener('click', showTitoloModal);
    document.getElementById('save-titolo-btn').addEventListener('click', saveTitolo);
    document.getElementById('cerca-titolo-btn').addEventListener('click', cercaTitoloByIsin);
    document.getElementById('prezzo-corrente-btn').addEventListener('click', getPrezzoCorrente);
    document.getElementById('lista-btp-btn').addEventListener('click', function() { showListaTitoli('BTP'); });
    document.getElementById('lista-bot-btn').addEventListener('click', function() { showListaTitoli('BOT'); });
    document.getElementById('calcola-rendimenti-btn').addEventListener('click', calcolaRendimentiTuttiTitoli);
    document.getElementById('simulazione-form').addEventListener('submit', function(e) {
        e.preventDefault();
        createSimulazione();
    });
    
    // Event listeners per i pulsanti di paginazione
    document.getElementById('prev-page-btn').addEventListener('click', function() {
        if (currentPage > 0) {
            currentPage--;
            loadTitoliPage();
        }
    });
    
    document.getElementById('next-page-btn').addEventListener('click', function() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadTitoliPage();
        }
    });
    
    // Event listener per il calcolo dei giorni alla scadenza
    document.getElementById('titolo-select').addEventListener('change', updateGiorniAllaScadenza);
    document.getElementById('data-acquisto').addEventListener('change', updateGiorniAllaScadenza);
    
    // Event listener per il pulsante di salvataggio della simulazione
    document.getElementById('salva-simulazione-btn').addEventListener('click', salvaSimulazione);
    
    // Inizializza il datepicker per il campo data-acquisto
    const dataAcquistoInput = document.getElementById('data-acquisto');
    if (dataAcquistoInput) {
        $(dataAcquistoInput).datepicker();
        
        // Imposta la data di oggi come valore iniziale
        const today = new Date();
        const formattedToday = formatDate(today.toISOString().split('T')[0]);
        $(dataAcquistoInput).datepicker('setDate', today);
        
        // Memorizza il formato ISO per l'invio al server
        dataAcquistoInput.setAttribute('data-iso-date', today.toISOString().split('T')[0]);
        
        // Aggiungi un event listener per aggiornare l'attributo data-iso-date quando la data cambia
        $(dataAcquistoInput).on('changeDate', function(e) {
            const selectedDate = e.date;
            const isoDate = selectedDate.toISOString().split('T')[0];
            dataAcquistoInput.setAttribute('data-iso-date', isoDate);
            updateGiorniAllaScadenza();
        });
    }
    
    // Inizializza il datepicker per il campo data-scadenza
    const dataScadenzaInput = document.getElementById('data-scadenza');
    if (dataScadenzaInput) {
        $(dataScadenzaInput).datepicker();
        
        // Aggiungi un event listener per aggiornare l'attributo data-iso-date quando la data cambia
        $(dataScadenzaInput).on('changeDate', function(e) {
            const selectedDate = e.date;
            const isoDate = selectedDate.toISOString().split('T')[0];
            dataScadenzaInput.setAttribute('data-iso-date', isoDate);
        });
    }
});

// Variabili globali per memorizzare i dati
let titoli = [];
let simulazioni = [];
let ultimoRisultatoCalcolo = null;
let simulazioniCaricate = false;

// Variabili per la paginazione
let currentPage = 0;
let pageSize = 10;
let totalPages = 1;
let totalElements = 0;
let currentTipoTitolo = '';

// Inizializzazione dell'applicazione
function initApp() {
    // Carica i titoli dal server invece dei dati di esempio
    loadTitoliFromServer();
    
    // Carica le simulazioni dal server
    loadSimulazioniFromServer();
    
    // Popola i select con i valori degli enum
    populateEnumSelects();
    
    // Aggiorna le viste (le viste dei titoli e delle simulazioni verranno aggiornate dopo il caricamento)
    
    // Imposta le date di default
    setDefaultDates();
    
    // Carica i metadati dell'applicazione
    loadAppMetadata();
}

// Carica i titoli dal server
function loadTitoliFromServer() {
    fetch('/api/frontend/titolo')
        .then(response => {
            if (!response.ok) {
                throw new Error('Errore nel caricamento dei titoli');
            }
            return response.json();
        })
        .then(data => {
            // Converti i DTO in oggetti per il frontend
            titoli = data.map(dto => ({
                id: dto.idTitolo,
                nome: dto.nome,
                codiceIsin: dto.codiceIsin,
                dataScadenza: dto.dataScadenza,
                tassoNominale: dto.tassoNominale,
                periodicitaCedole: dto.periodicitaCedole,
                periodicitaBollo: dto.periodicitaBollo,
                tipoTitolo: dto.tipoTitolo,
                prezzo: 100.00 // Prezzo di default, potrebbe essere aggiunto al DTO in futuro
            }));
            
            // Aggiorna le viste
            updateTitoliTable();
            updateTitoliSelect();
        })
        .catch(error => {
            console.error('Errore nel caricamento dei titoli:', error);
            // In caso di errore, carica i dati di esempio
            loadSampleData();
            
            // Aggiorna le viste con i dati di esempio
            updateTitoliTable();
            updateTitoliSelect();
        });
}

// Carica le simulazioni dal server
function loadSimulazioniFromServer() {
    fetch('/api/simulazioni?latest=true')
        .then(response => {
            if (!response.ok) {
                throw new Error('Errore nel caricamento delle simulazioni');
            }
            return response.json();
        })
        .then(data => {
            // Converti i DTO in oggetti per il frontend
            simulazioni = data.map(dto => ({
                id: dto.idSimulazione,
                titoloId: dto.idTitolo,
                prezzoAcquisto: dto.prezzoAcquisto,
                dataAcquisto: dto.dataAcquisto,
                importoNominale: 10000, // Valore di default, non presente nel DTO
                commissioniAcquisto: dto.commissioniAcquisto,
                rendimentoLordo: dto.rendimentoLordo * 100, // Converti da decimale a percentuale
                rendimentoTassato: dto.rendimentoTassato * 100,
                rendimentoNettoCedole: dto.rendimentoNettoCedole * 100,
                impostaBollo: dto.impostaBollo,
                rendimentoNettoBollo: dto.rendimentoNettoBollo,
                plusMinusValenza: dto.plusMinusValenza
            }));
            
            simulazioniCaricate = true;
            
            // Aggiorna la vista delle simulazioni
            updateSimulazioniTable();
        })
        .catch(error => {
            console.error('Errore nel caricamento delle simulazioni:', error);
            // In caso di errore, se non ci sono già simulazioni caricate, carica i dati di esempio
            if (!simulazioniCaricate && simulazioni.length === 0) {
                loadSampleData();
                updateSimulazioniTable();
            }
        });
}

// Popola i select con i valori degli enum
function populateEnumSelects() {
    // Popola il select delle periodicità cedole
    fetch('/api/frontend/enum/periodicita-cedole')
        .then(response => response.json())
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
    fetch('/api/frontend/enum/periodicita-bollo')
        .then(response => response.json())
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
    fetch('/api/frontend/enum/tipo-titolo')
        .then(response => response.json())
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
}

// Carica i metadati dell'applicazione
function loadAppMetadata() {
    fetch('/api/frontend/app-info')
        .then(response => {
            if (!response.ok) {
                throw new Error('Errore nel caricamento dei metadati');
            }
            return response.json();
        })
        .then(data => {
            document.getElementById('app-name').textContent = data.appName || 'Rendimento Titoli';
            document.getElementById('app-version').textContent = data.appVersion || 'N/A';
        })
        .catch(error => {
            console.error('Errore:', error);
            document.getElementById('app-name').textContent = 'Rendimento Titoli';
            document.getElementById('app-version').textContent = 'N/A';
        });
}

// Carica dati di esempio
function loadSampleData() {
    // Titoli di esempio
    const oggi = new Date();
    const scadenza1 = new Date();
    scadenza1.setFullYear(oggi.getFullYear() + 5);
    const scadenza2 = new Date();
    scadenza2.setFullYear(oggi.getFullYear() + 3);
    const scadenza3 = new Date();
    scadenza3.setFullYear(oggi.getFullYear() + 7);
    
    titoli = [
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
    
    // Simulazioni di esempio
    const dataAcquisto1 = new Date();
    dataAcquisto1.setMonth(dataAcquisto1.getMonth() - 6);
    const dataAcquisto2 = new Date();
    dataAcquisto2.setMonth(dataAcquisto2.getMonth() - 3);
    
    simulazioni = [
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

// Aggiorna la tabella dei titoli
function updateTitoliTable() {
    const tbody = document.getElementById('titoli-list');
    tbody.innerHTML = '';
    
    titoli.forEach(titolo => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${titolo.codiceIsin}</td>
            <td>${titolo.nome}</td>
            <td>${titolo.prezzo.toFixed(2)} €</td>
            <td>
                <button class="btn btn-sm btn-outline-primary me-1" onclick="editTitolo(${titolo.id})">Modifica</button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteTitolo(${titolo.id})">Elimina</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Aggiorna il select dei titoli
function updateTitoliSelect() {
    const select = document.getElementById('titolo-select');
    
    // Mantieni solo la prima opzione
    select.innerHTML = '<option value="">Seleziona un titolo</option>';
    
    titoli.forEach(titolo => {
        const option = document.createElement('option');
        option.value = titolo.id;
        option.textContent = `${titolo.nome} (${titolo.codiceIsin}) - ${titolo.prezzo.toFixed(2)} €`;
        select.appendChild(option);
    });
}

// Aggiorna la tabella delle simulazioni
function updateSimulazioniTable() {
    const tbody = document.getElementById('simulazioni-list');
    tbody.innerHTML = '';
    
    simulazioni.forEach(simulazione => {
        const titolo = titoli.find(t => t.id == simulazione.titoloId);
        if (!titolo) return;
        
        const row = document.createElement('tr');
        
        // Aggiungi attributo data-titolo-id per il doppio click
        row.setAttribute('data-titolo-id', simulazione.titoloId);
        
        // Aggiungi classe per indicare che la riga è cliccabile
        row.classList.add('simulazione-row');
        
        // Determina la classe CSS in base al rendimento netto
        const rendimentoClass = simulazione.rendimentoNettoBollo >= 0 ? 'rendimento-positivo' : 'rendimento-negativo';
        
        // Calcola il valore finale
        const valoreFinaleTeorico = simulazione.importoNominale * (1 + (simulazione.rendimentoNettoBollo / 100));
        
        row.innerHTML = `
            <td>${titolo.nome} (${titolo.codiceIsin})</td>
            <td>${simulazione.importoNominale.toFixed(2)} €</td>
            <td>${simulazione.prezzoAcquisto.toFixed(2)} €</td>
            <td>${formatDate(simulazione.dataAcquisto)}</td>
            <td>${formatDate(titolo.dataScadenza)}</td>
            <td>${simulazione.commissioniAcquisto.toFixed(2)}%</td>
            <td>${simulazione.impostaBollo.toFixed(2)} €</td>
            <td>${simulazione.rendimentoLordo.toFixed(2)}%</td>
            <td>${simulazione.rendimentoNettoCedole.toFixed(2)}%</td>
            <td class="${rendimentoClass}">${simulazione.rendimentoNettoBollo.toFixed(2)}%</td>
            <td>${valoreFinaleTeorico.toFixed(2)} €</td>
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

// Imposta le date di default per il form di simulazione
function setDefaultDates() {
    const today = new Date();
    const dataAcquistoInput = document.getElementById('data-acquisto');
    
    // Imposta la data di oggi utilizzando il datepicker
    $(dataAcquistoInput).datepicker('setDate', today);
    
    // Memorizza il formato ISO per l'invio al server
    dataAcquistoInput.setAttribute('data-iso-date', today.toISOString().split('T')[0]);
    
    // Imposta il valore di default per le commissioni (0,9/1000 = 0.09%)
    document.getElementById('commissioni-acquisto').value = "0.09";
    
    // Imposta il valore di default per il tasso d'interesse a vuoto
    document.getElementById('tasso-interesse').value = "";
}

// Calcola e aggiorna i giorni mancanti alla scadenza e imposta il tasso d'interesse
function updateGiorniAllaScadenza() {
    const titoloId = document.getElementById('titolo-select').value;
    const dataAcquistoInput = document.getElementById('data-acquisto');
    
    if (!titoloId || !dataAcquistoInput.value) {
        document.getElementById('giorni-alla-scadenza').value = '';
        document.getElementById('tasso-interesse').value = '';
        return;
    }
    
    const titolo = titoli.find(t => t.id == titoloId);
    if (!titolo || !titolo.dataScadenza) {
        document.getElementById('giorni-alla-scadenza').value = '';
        document.getElementById('tasso-interesse').value = '';
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
    
    document.getElementById('giorni-alla-scadenza').value = diffDays > 0 ? diffDays : 0;
    
    // Imposta il tasso d'interesse con il valore del titolo selezionato
    if (titolo.tassoNominale) {
        document.getElementById('tasso-interesse').value = formatDecimal(titolo.tassoNominale);
    }
}

// Mostra il modal per aggiungere/modificare un titolo
function showTitoloModal(titoloId = null) {
    // Reset del form
    document.getElementById('titolo-form').reset();
    document.getElementById('titolo-id').value = '';
    document.getElementById('titolo-modal-label').textContent = 'Aggiungi Titolo';
    
    // Se è una modifica, popola il form con i dati del titolo
    if (titoloId) {
        const titolo = titoli.find(t => t.id === titoloId);
        if (titolo) {
            document.getElementById('titolo-id').value = titolo.id;
            document.getElementById('nome-titolo').value = titolo.nome;
            document.getElementById('codice-isin').value = titolo.codiceIsin || '';
            document.getElementById('data-scadenza').value = titolo.dataScadenza || '';
            document.getElementById('tasso-nominale').value = titolo.tassoNominale ? formatDecimal(titolo.tassoNominale) : '';
            document.getElementById('periodicita-cedole').value = titolo.periodicitaCedole || '';
            document.getElementById('periodicita-bollo').value = titolo.periodicitaBollo || '';
            document.getElementById('tipo-titolo').value = titolo.tipoTitolo || '';
            document.getElementById('prezzo-titolo').value = titolo.prezzo ? formatDecimal(titolo.prezzo) : '';
            document.getElementById('titolo-modal-label').textContent = 'Modifica Titolo';
        }
    }
    
    // Mostra il modal
    const modal = new bootstrap.Modal(document.getElementById('titolo-modal'));
    modal.show();
}

// Cerca un titolo per codice ISIN
function cercaTitoloByIsin() {
    const isin = document.getElementById('cerca-isin').value;
    
    if (!isin) {
        alert('Inserisci un codice ISIN valido');
        return;
    }
    
    // Mostra un indicatore di caricamento
    document.body.classList.add('loading');
    
    // Chiamata API per cercare il titolo per ISIN
    fetch(`/api/frontend/titolo/isin/${isin}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Titolo non trovato');
            }
            return response.json();
        })
        .then(titolo => {
            console.log("Titolo trovato:", titolo);
            
            // Aggiorna il titolo nella lista locale o aggiungilo se è nuovo
            const titoloTrovato = {
                id: titolo.idTitolo,
                nome: titolo.nome,
                codiceIsin: titolo.codiceIsin,
                dataScadenza: titolo.dataScadenza,
                tassoNominale: titolo.tassoNominale,
                periodicitaCedole: titolo.periodicitaCedole,
                periodicitaBollo: titolo.periodicitaBollo,
                tipoTitolo: titolo.tipoTitolo,
                prezzo: titolo.corso || 100.00
            };
            
            const index = titoli.findIndex(t => t.id === titoloTrovato.id);
            if (index !== -1) {
                titoli[index] = titoloTrovato;
            } else {
                titoli.push(titoloTrovato);
            }
            
            // Aggiorna le viste
            updateTitoliTable();
            updateTitoliSelect();
            
            // Nascondi l'indicatore di caricamento
            document.body.classList.remove('loading');
            
            // Mostra un messaggio di successo
            alert('Titolo trovato e aggiunto alla lista!');
        })
        .catch(error => {
            console.error('Errore:', error);
            alert('Titolo non trovato. Verifica il codice ISIN e riprova.');
            
            // Nascondi l'indicatore di caricamento
            document.body.classList.remove('loading');
        });
}

// Ottiene il prezzo corrente di un titolo
function getPrezzoCorrente() {
    const titoloId = document.getElementById('titolo-select').value;
    
    if (!titoloId) {
        alert('Seleziona un titolo');
        return;
    }
    
    const titolo = titoli.find(t => t.id == titoloId);
    if (!titolo) {
        alert('Titolo non trovato');
        return;
    }
    
    // Mostra un indicatore di caricamento
    document.body.classList.add('loading');
    
    // Ottieni il codice ISIN e il tipo di titolo
    const codiceIsin = titolo.codiceIsin;
    const tipoTitolo = titolo.tipoTitolo;
    
    if (!codiceIsin || !tipoTitolo) {
        alert('Informazioni sul titolo incomplete');
        document.body.classList.remove('loading');
        return;
    }
    
    // Chiamata API per ottenere il prezzo corrente
    fetch(`/api/borsa-italiana/corso/${tipoTitolo.toLowerCase()}/${codiceIsin}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Errore nel recupero del prezzo');
            }
            return response.json();
        })
        .then(prezzo => {
            console.log("Prezzo corrente:", prezzo);
            
            // Aggiorna il prezzo nel campo
            document.getElementById('prezzo-acquisto').value = formatDecimal(prezzo);
            
            // Nascondi l'indicatore di caricamento
            document.body.classList.remove('loading');
        })
        .catch(error => {
            console.error('Errore:', error);
            alert('Si è verificato un errore nel recupero del prezzo corrente.');
            
            // Nascondi l'indicatore di caricamento
            document.body.classList.remove('loading');
        });
}

// Mostra la lista dei titoli filtrata per tipo, recuperandoli da Borsa Italiana in un modal
function showListaTitoli(tipo) {
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
    currentTipoTitolo = tipo;
    
    // Resetta la pagina corrente
    currentPage = 0;
    
    // Carica la prima pagina
    loadTitoliPage();
}

// Carica una pagina di titoli
function loadTitoliPage() {
    const loadingSpinner = document.getElementById('lista-titoli-loading');
    const errorAlert = document.getElementById('lista-titoli-error');
    const tbody = document.getElementById('lista-titoli-body');
    const countSpan = document.getElementById('lista-titoli-count');
    
    // Mostra l'indicatore di caricamento
    loadingSpinner.classList.remove('d-none');
    errorAlert.classList.add('d-none');
    tbody.innerHTML = '';
    
    // Chiamata API per recuperare i titoli paginati da Borsa Italiana
    fetch(`/api/borsa-italiana/lista-paginata/${currentTipoTitolo}?page=${currentPage}&size=${pageSize}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Errore nel recupero dei titoli ${currentTipoTitolo}`);
            }
            return response.json();
        })
        .then(paginatedResponse => {
            console.log(`Titoli ${currentTipoTitolo} recuperati da Borsa Italiana:`, paginatedResponse);
            
            // Nascondi l'indicatore di caricamento
            loadingSpinner.classList.add('d-none');
            
            // Aggiorna le variabili di paginazione
            totalPages = paginatedResponse.totalPages;
            totalElements = paginatedResponse.totalElements;
            
            // Aggiorna il conteggio dei titoli
            countSpan.textContent = `(${totalElements} titoli trovati)`;
            
            // Aggiorna i controlli di paginazione
            updatePaginationControls();
            
            const titoliFromServer = paginatedResponse.content;
            
            if (!titoliFromServer || titoliFromServer.length === 0) {
                console.log(`Nessun titolo ${currentTipoTitolo} trovato in Borsa Italiana`);
                
                // Aggiungi una riga che indica che non ci sono titoli
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td colspan="6" class="text-center">Nessun titolo ${currentTipoTitolo} trovato in Borsa Italiana</td>
                `;
                tbody.appendChild(row);
            } else {
                // Popola la tabella con i titoli recuperati
                titoliFromServer.forEach(titoloDTO => {
                    // Verifica se il titolo esiste già nella lista locale
                    const existingIndex = titoli.findIndex(t => t.codiceIsin === titoloDTO.codiceIsin);
                    const isInList = existingIndex !== -1;
                    
                    // Crea la riga per la tabella
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${titoloDTO.codiceIsin || ''}</td>
                        <td>${titoloDTO.nome || ''}</td>
                        <td>${titoloDTO.dataScadenza ? formatDate(titoloDTO.dataScadenza) : ''}</td>
                        <td>${titoloDTO.tassoNominale ? formatDecimal(titoloDTO.tassoNominale) + '%' : ''}</td>
                        <td>${titoloDTO.corso ? formatDecimal(titoloDTO.corso) + ' €' : ''}</td>
                        <td>
                            <button class="btn btn-sm ${isInList ? 'btn-success' : 'btn-primary'}" 
                                    onclick="aggiungiTitoloAllaLista('${titoloDTO.codiceIsin}', '${currentTipoTitolo}')">
                                ${isInList ? 'Già in lista' : 'Aggiungi alla lista'}
                            </button>
                        </td>
                    `;
                    tbody.appendChild(row);
                });
                
                console.log(`Tabella modal aggiornata con ${titoliFromServer.length} titoli di tipo ${currentTipoTitolo}`);
            }
        })
        .catch(error => {
            console.error(`Errore nel recupero dei titoli ${currentTipoTitolo}:`, error);
            
            // Nascondi l'indicatore di caricamento e mostra l'errore
            loadingSpinner.classList.add('d-none');
            errorAlert.classList.remove('d-none');
            errorAlert.textContent = `Si è verificato un errore durante il caricamento dei titoli ${currentTipoTitolo}: ${error.message}`;
        });
}

// Aggiorna i controlli di paginazione
function updatePaginationControls() {
    const currentPageSpan = document.getElementById('current-page');
    const totalPagesSpan = document.getElementById('total-pages');
    const prevPageBtn = document.getElementById('prev-page-btn');
    const nextPageBtn = document.getElementById('next-page-btn');
    
    // Aggiorna i numeri di pagina
    currentPageSpan.textContent = (currentPage + 1).toString();
    totalPagesSpan.textContent = totalPages.toString();
    
    // Abilita/disabilita i pulsanti di navigazione
    prevPageBtn.disabled = currentPage === 0;
    nextPageBtn.disabled = currentPage >= totalPages - 1;
}

// Funzione per aggiungere un titolo alla lista locale
function aggiungiTitoloAllaLista(isin, tipo) {
    console.log(`Aggiungendo titolo con ISIN ${isin} alla lista...`);
    
    // Mostra un indicatore di caricamento
    document.body.classList.add('loading');
    
    // Chiamata API per ottenere i dettagli del titolo
    fetch(`/api/borsa-italiana/${tipo}/${isin}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Errore nel recupero dei dettagli del titolo ${isin}`);
            }
            return response.json();
        })
        .then(titoloDTO => {
            console.log(`Dettagli titolo ${isin} recuperati:`, titoloDTO);
            
            // Crea l'oggetto titolo per il frontend
            const titoloObj = {
                id: titoloDTO.idTitolo || null,
                nome: titoloDTO.nome,
                codiceIsin: titoloDTO.codiceIsin,
                dataScadenza: titoloDTO.dataScadenza,
                tassoNominale: titoloDTO.tassoNominale,
                periodicitaCedole: titoloDTO.periodicitaCedole || 'SEMESTRALE',
                periodicitaBollo: titoloDTO.periodicitaBollo || 'ANNUALE',
                tipoTitolo: tipo,
                prezzo: titoloDTO.corso || 100.00
            };
            
            // Verifica se il titolo esiste già nella lista locale
            const existingIndex = titoli.findIndex(t => t.codiceIsin === titoloDTO.codiceIsin);
            
            // Aggiorna o aggiungi il titolo alla lista locale
            if (existingIndex !== -1) {
                titoli[existingIndex] = titoloObj;
                console.log(`Titolo ${isin} aggiornato nella lista locale`);
            } else {
                titoli.push(titoloObj);
                console.log(`Titolo ${isin} aggiunto alla lista locale`);
            }
            
            // Aggiorna le viste
            updateTitoliTable();
            updateTitoliSelect();
            
            // Nascondi l'indicatore di caricamento
            document.body.classList.remove('loading');
            
            // Aggiorna il pulsante nella tabella del modal
            const buttons = document.querySelectorAll(`button[onclick="aggiungiTitoloAllaLista('${isin}', '${tipo}')"]`);
            buttons.forEach(button => {
                button.classList.remove('btn-primary');
                button.classList.add('btn-success');
                button.textContent = 'Già in lista';
            });
            
            // Mostra un messaggio di successo
            alert(`Titolo ${titoloDTO.nome} (${isin}) aggiunto alla lista con successo!`);
        })
        .catch(error => {
            console.error(`Errore nell'aggiunta del titolo ${isin} alla lista:`, error);
            
            // Nascondi l'indicatore di caricamento
            document.body.classList.remove('loading');
            
            // Mostra un messaggio di errore
            alert(`Si è verificato un errore nell'aggiunta del titolo alla lista: ${error.message}`);
        });
}

// Calcola i rendimenti di tutti i titoli
function calcolaRendimentiTuttiTitoli() {
    // Mostra un indicatore di caricamento
    document.body.classList.add('loading');
    
    // Chiamata API per calcolare i rendimenti di tutti i titoli
    fetch('/api/simulazioni/calcola-rendimenti-tutti-titoli', {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Errore nel calcolo dei rendimenti');
        }
        return response.json();
    })
    .then(data => {
        console.log("Rendimenti calcolati:", data);
        
        // Ricarica le simulazioni dal server
        loadSimulazioniFromServer();
        
        // Nascondi l'indicatore di caricamento
        document.body.classList.remove('loading');
        
        // Mostra un messaggio di successo
        alert('Rendimenti calcolati con successo!');
    })
    .catch(error => {
        console.error('Errore:', error);
        alert('Si è verificato un errore nel calcolo dei rendimenti.');
        
        // Nascondi l'indicatore di caricamento
        document.body.classList.remove('loading');
    });
}

// Crea una nuova simulazione
function createSimulazione() {
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
    const prezzoAcquisto = parseNumericValue(prezzoAcquistoText);
    const importo = parseNumericValue(importoText);
    const dataAcquistoISO = dataAcquistoInput.getAttribute('data-iso-date');
    const commissioniAcquisto = parseNumericValue(commissioniAcquistoText);
    
    // Verifica che i valori siano validi
    if (prezzoAcquisto <= 0 || importo <= 0 || commissioniAcquisto < 0) {
        alert('I valori devono essere positivi');
        return;
    }
    
    // Mostra un indicatore di caricamento
    document.body.classList.add('loading');
    
    // Chiamata API per calcolare il rendimento (senza salvare)
    fetch(`/api/simulazioni/calcola-rendimento?idTitolo=${titoloId}&prezzoAcquisto=${prezzoAcquisto}&importo=${importo}&modalitaBollo=${modalitaBollo}`, {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Errore nel calcolo del rendimento');
        }
        return response.json();
    })
    .then(data => {
        console.log("Rendimento calcolato:", data);
        
        // Memorizza l'ultimo risultato del calcolo
        ultimoRisultatoCalcolo = data;
        
        // Visualizza i risultati nei campi appropriati
        document.getElementById('plusvalenza-netta').value = formatDecimal(data.plusvalenzaNetta);
        document.getElementById('interessi-netti').value = formatDecimal(data.interessiNetti);
        document.getElementById('commissioni').value = formatDecimal(data.commissioni);
        document.getElementById('imposta-bollo').value = formatDecimal(data.impostaBollo);
        document.getElementById('guadagno-totale').value = formatDecimal(data.guadagnoTotale);
        document.getElementById('guadagno-netto-commissioni').value = formatDecimal(data.guadagnoNettoCommissioni);
        document.getElementById('tasso').value = formatDecimal(data.tasso) + '%';
        document.getElementById('tasso-netto-commissioni').value = formatDecimal(data.tassoNettoCommissioni) + '%';
        document.getElementById('guadagno-netto-bollo').value = formatDecimal(data.guadagnoNettoBollo);
        document.getElementById('tasso-netto-bollo').value = formatDecimal(data.tassoNettoBollo) + '%';
        document.getElementById('importo-scadenza').value = formatDecimal(data.importoScadenza);
        
        // Abilita il pulsante "Salva Simulazione"
        document.getElementById('salva-simulazione-btn').disabled = false;
        
        // Nascondi l'indicatore di caricamento
        document.body.classList.remove('loading');
        
        // Mostra un messaggio di successo
        alert('Rendimento calcolato con successo!');
    })
    .catch(error => {
        console.error('Errore:', error);
        alert('Si è verificato un errore nel calcolo del rendimento.');
        
        // Nascondi l'indicatore di caricamento
        document.body.classList.remove('loading');
    });
}

// Salva una simulazione
function salvaSimulazione() {
    // Verifica che ci sia un risultato di calcolo
    if (!ultimoRisultatoCalcolo) {
        alert('Calcola prima il rendimento');
        return;
    }
    
    const titoloId = document.getElementById('titolo-select').value;
    const prezzoAcquistoText = document.getElementById('prezzo-acquisto').value;
    const importoText = document.getElementById('importo-nominale').value;
    const dataAcquistoInput = document.getElementById('data-acquisto');
    const commissioniAcquistoText = document.getElementById('commissioni-acquisto').value;
    
    // Verifica che tutti i campi siano valorizzati
    if (!titoloId || !prezzoAcquistoText || !importoText || !dataAcquistoInput.value || !commissioniAcquistoText) {
        alert('Compila tutti i campi');
        return;
    }
    
    // Converti i valori
    const prezzoAcquisto = parseNumericValue(prezzoAcquistoText);
    const importo = parseNumericValue(importoText);
    const dataAcquistoISO = dataAcquistoInput.getAttribute('data-iso-date');
    const commissioniAcquisto = parseNumericValue(commissioniAcquistoText) / 100; // Converti da percentuale a decimale
    
    // Crea l'oggetto simulazione
    const simulazioneDTO = {
        idTitolo: parseInt(titoloId),
        prezzoAcquisto: prezzoAcquisto,
        dataAcquisto: dataAcquistoISO,
        commissioniAcquisto: commissioniAcquisto,
        rendimentoLordo: ultimoRisultatoCalcolo.tasso / 100, // Converti da percentuale a decimale
        rendimentoTassato: ultimoRisultatoCalcolo.tasso * 0.875 / 100, // Converti da percentuale a decimale
        rendimentoNettoCedole: ultimoRisultatoCalcolo.tassoNettoCommissioni / 100, // Converti da percentuale a decimale
        impostaBollo: ultimoRisultatoCalcolo.impostaBollo,
        rendimentoNettoBollo: ultimoRisultatoCalcolo.tassoNettoBollo / 100, // Converti da percentuale a decimale
        plusMinusValenza: ultimoRisultatoCalcolo.plusvalenzaNetta
    };
    
    // Mostra un indicatore di caricamento
    document.body.classList.add('loading');
    
    // Chiamata API per salvare la simulazione
    fetch('/api/simulazioni', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(simulazioneDTO)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Errore nel salvataggio della simulazione');
        }
        return response.json();
    })
    .then(data => {
        console.log("Simulazione salvata:", data);
        
        // Ricarica le simulazioni dal server
        loadSimulazioniFromServer();
        
        // Nascondi l'indicatore di caricamento
        document.body.classList.remove('loading');
        
        // Mostra un messaggio di successo
        alert('Simulazione salvata con successo!');
    })
    .catch(error => {
        console.error('Errore:', error);
        alert('Si è verificato un errore nel salvataggio della simulazione.');
        
        // Nascondi l'indicatore di caricamento
        document.body.classList.remove('loading');
    });
}

// Modifica un titolo
function editTitolo(titoloId) {
    // Trova il titolo nella lista
    const titolo = titoli.find(t => t.id === titoloId);
    if (!titolo) {
        alert('Titolo non trovato');
        return;
    }
    
    // Mostra il modal per la modifica
    showTitoloModal(titoloId);
}

// Elimina un titolo
function deleteTitolo(titoloId) {
    // Chiedi conferma
    if (!confirm('Sei sicuro di voler eliminare questo titolo?')) {
        return;
    }
    
    // Mostra un indicatore di caricamento
    document.body.classList.add('loading');
    
    // Chiamata API per eliminare il titolo
    fetch(`/api/frontend/titolo/${titoloId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Errore nell\'eliminazione del titolo');
        }
        
        // Rimuovi il titolo dalla lista locale
        const index = titoli.findIndex(t => t.id === titoloId);
        if (index !== -1) {
            titoli.splice(index, 1);
        }
        
        // Aggiorna le viste
        updateTitoliTable();
        updateTitoliSelect();
        
        // Nascondi l'indicatore di caricamento
        document.body.classList.remove('loading');
        
        // Mostra un messaggio di successo
        alert('Titolo eliminato con successo!');
    })
    .catch(error => {
        console.error('Errore:', error);
        alert('Si è verificato un errore nell\'eliminazione del titolo.');
        
        // Nascondi l'indicatore di caricamento
        document.body.classList.remove('loading');
    });
}

// Salva un titolo (nuovo o esistente)
function saveTitolo() {
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
        alert('Compila tutti i campi correttamente. Campi mancanti: ' + campiMancanti.join(', '));
        return;
    }
    
    const tassoNominale = parseNumericValue(tassoNominaleStr);
    const prezzo = parseNumericValue(prezzoText);
    
    // Crea l'oggetto DTO da inviare al server
    const titoloDTO = {
        idTitolo: titoloId ? parseInt(titoloId) : null,
        nome: nome,
        codiceIsin: codiceIsin,
        dataScadenza: dataScadenza,
        tassoNominale: tassoNominale,
        periodicitaCedole: periodicitaCedole,
        periodicitaBollo: periodicitaBollo,
        tipoTitolo: tipoTitolo
    };
    
    // Invia i dati al server
    fetch('/api/frontend/titolo', {
        method: titoloId ? 'PUT' : 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(titoloDTO)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Errore nel salvataggio del titolo');
        }
        return response.json();
    })
    .then(data => {
        // Aggiorna il titolo nella lista locale o aggiungilo se è nuovo
        const titoloSalvato = {
            id: data.idTitolo,
            nome: data.nome,
            codiceIsin: data.codiceIsin,
            dataScadenza: data.dataScadenza,
            tassoNominale: data.tassoNominale,
            periodicitaCedole: data.periodicitaCedole,
            periodicitaBollo: data.periodicitaBollo,
            tipoTitolo: data.tipoTitolo,
            prezzo: prezzo
        };
        
        const index = titoli.findIndex(t => t.id === titoloSalvato.id);
        if (index !== -1) {
            titoli[index] = titoloSalvato;
        } else {
            titoli.push(titoloSalvato);
        }
        
        // Aggiorna le viste
        updateTitoliTable();
        updateTitoliSelect();
        
        // Chiudi il modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('titolo-modal'));
        modal.hide();
        
        // Mostra un messaggio di successo
        alert('Titolo salvato con successo!');
    })
    .catch(error => {
        console.error('Errore:', error);
        alert('Si è verificato un errore nel salvataggio del titolo. Controlla la console per i dettagli.');
    });
}
