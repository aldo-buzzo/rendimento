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
    fetch('/api/frontend/simulazioni/latest')
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
            <td class="${rendimentoClass}">${simulazione.rendimentoNettoBollo.toFixed(2)}%</td>
            <td>${valoreFinaleTeorico.toFixed(2)} €</td>
        `;
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

// Salva un titolo (nuovo)
function saveTitolo() {
    const nome = document.getElementById('nome-titolo').value;
    const codiceIsin = document.getElementById('codice-isin').value;
    const dataScadenza = document.getElementById('data-scadenza').getAttribute('data-iso-date');
    const tassoNominaleStr = document.getElementById('tasso-nominale').value;
    const periodicitaCedole = document.getElementById('periodicita-cedole').value;
    const periodicitaBollo = document.getElementById('periodicita-bollo').value;
    const tipoTitolo = document.getElementById('tipo-titolo').value;
    
    // Gestione del prezzo come testo liberamente digitabile
    const prezzoText = document.getElementById('prezzo-titolo').value;
    
    // Commentiamo la validazione rigorosa del formato del prezzo
    /*if (!isValidNumber(prezzoText)) {
        alert('Il prezzo deve essere un valore numerico valido (usa la virgola come separatore decimale).');
        return;
    }*/
    
    const prezzo = parseNumericValue(prezzoText);
    
    // Verifica quali campi sono vuoti e mostra un messaggio specifico
    let campiMancanti = [];
    if (!nome) campiMancanti.push('Nome titolo');
    if (!codiceIsin) campiMancanti.push('Codice ISIN');
    if (!dataScadenza) campiMancanti.push('Data scadenza');
    if (!tassoNominaleStr) campiMancanti.push('Tasso nominale');
    if (!periodicitaCedole) campiMancanti.push('Periodicità cedole');
    if (!periodicitaBollo) campiMancanti.push('Periodicità bollo');
    
    if (campiMancanti.length > 0) {
        alert('Compila tutti i campi correttamente. Campi mancanti: ' + campiMancanti.join(', '));
        return;
    }
    
    // Converti i valori nei tipi corretti - commentiamo la validazione rigorosa
    /*if (!isValidNumber(tassoNominaleStr)) {
        alert('Il tasso nominale deve essere un valore numerico valido (usa la virgola come separatore decimale).');
        return;
    }*/
    
    const tassoNominale = parseNumericValue(tassoNominaleStr);
    
    // Crea l'oggetto DTO da inviare al server
    const titoloDTO = {
        nome: nome,
        codiceIsin: codiceIsin,
        dataScadenza: dataScadenza,
        tassoNominale: tassoNominale,
        periodicitaCedole: periodicitaCedole,
        periodicitaBollo: periodicitaBollo,
        tipoTitolo: tipoTitolo
    };
    
    // Mostra un indicatore di caricamento
    const saveButton = document.getElementById('save-titolo-btn');
    const originalText = saveButton.textContent;
    saveButton.disabled = true;
    saveButton.textContent = 'Salvataggio in corso...';
    
    // Chiamata API per salvare il titolo
    fetch('/api/frontend/titolo', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(titoloDTO)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Errore durante il salvataggio del titolo');
        }
        return response.json();
    })
    .then(data => {
        // Verifica se abbiamo ricevuto un messaggio (caso di aggiornamento)
        if (data.message && data.titolo) {
            // È un aggiornamento di un titolo esistente
            const titoloAggiornato = data.titolo;
            
            // Trova l'indice del titolo esistente
            const index = titoli.findIndex(t => t.codiceIsin === titoloAggiornato.codiceIsin);
            
            if (index !== -1) {
                // Aggiorna il titolo esistente
                titoli[index] = {
                    id: titoloAggiornato.idTitolo,
                    nome: titoloAggiornato.nome,
                    codiceIsin: titoloAggiornato.codiceIsin,
                    dataScadenza: titoloAggiornato.dataScadenza,
                    tassoNominale: titoloAggiornato.tassoNominale,
                    periodicitaCedole: titoloAggiornato.periodicitaCedole,
                    periodicitaBollo: titoloAggiornato.periodicitaBollo,
                    tipoTitolo: titoloAggiornato.tipoTitolo,
                    prezzo: prezzo // Manteniamo il prezzo inserito dall'utente
                };
            } else {
                // Nel caso improbabile in cui non troviamo il titolo, lo aggiungiamo
                titoli.push({
                    id: titoloAggiornato.idTitolo,
                    nome: titoloAggiornato.nome,
                    codiceIsin: titoloAggiornato.codiceIsin,
                    dataScadenza: titoloAggiornato.dataScadenza,
                    tassoNominale: titoloAggiornato.tassoNominale,
                    periodicitaCedole: titoloAggiornato.periodicitaCedole,
                    periodicitaBollo: titoloAggiornato.periodicitaBollo,
                    tipoTitolo: titoloAggiornato.tipoTitolo,
                    prezzo: prezzo // Manteniamo il prezzo inserito dall'utente
                });
            }
            
            // Mostra un messaggio di successo per l'aggiornamento
            alert(data.message);
        } else if (data && data.idTitolo) { // Verifica che data sia un oggetto valido con idTitolo
            // È un nuovo titolo
            const savedTitolo = data;
            
            // Aggiungi il nuovo titolo alla lista locale
            titoli.push({
                id: savedTitolo.idTitolo,
                nome: savedTitolo.nome || '',
                codiceIsin: savedTitolo.codiceIsin || '',
                dataScadenza: savedTitolo.dataScadenza || null,
                tassoNominale: savedTitolo.tassoNominale || 0,
                periodicitaCedole: savedTitolo.periodicitaCedole || '',
                periodicitaBollo: savedTitolo.periodicitaBollo || '',
                tipoTitolo: savedTitolo.tipoTitolo || '',
                prezzo: prezzo // Manteniamo il prezzo inserito dall'utente
            });
            
            // Mostra un messaggio di successo per il nuovo titolo
            alert('Titolo salvato con successo!');
        } else {
            // Caso imprevisto: risposta non valida dal server
            console.error('Risposta non valida dal server:', data);
            alert('Errore durante il salvataggio del titolo: risposta non valida dal server');
            return; // Esci dalla funzione senza aggiornare le viste
        }
        
        // Aggiorna le viste
        updateTitoliTable();
        updateTitoliSelect();
        
        // Chiudi il modal
        bootstrap.Modal.getInstance(document.getElementById('titolo-modal')).hide();
    })
    .catch(error => {
        console.error('Errore:', error);
        alert(error.message || 'Si è verificato un errore durante il salvataggio del titolo.');
    })
    .finally(() => {
        // Ripristina il pulsante
        saveButton.disabled = false;
        saveButton.textContent = originalText;
    });
}

// Modifica un titolo
function editTitolo(titoloId) {
    showTitoloModal(titoloId);
}

// Elimina un titolo
function deleteTitolo(titoloId) {
    if (confirm('Sei sicuro di voler eliminare questo titolo?')) {
        // Rimuovi il titolo
        titoli = titoli.filter(t => t.id !== titoloId);
        
        // Rimuovi anche le simulazioni associate
        simulazioni = simulazioni.filter(s => s.titoloId !== titoloId);
        
        // Aggiorna le viste
        updateTitoliTable();
        updateTitoliSelect();
        updateSimulazioniTable();
    }
}

// Calcola il rendimento di un titolo
function createSimulazione() {
    const titoloId = parseInt(document.getElementById('titolo-select').value);
    const prezzoAcquistoText = document.getElementById('prezzo-acquisto').value;
    
    // Commentiamo la validazione rigorosa del formato del prezzo
    /*if (!isValidNumber(prezzoAcquistoText)) {
        alert('Il prezzo di acquisto deve essere un valore numerico valido (usa la virgola come separatore decimale).');
        return;
    }*/
    
    const prezzoAcquisto = parseNumericValue(prezzoAcquistoText);
    let importoText = document.getElementById('importo-nominale').value;
    
    // Commentiamo la validazione rigorosa del formato dell'importo
    /*if (importoText && !isValidNumber(importoText)) {
        alert('L\'importo nominale deve essere un valore numerico valido (usa la virgola come separatore decimale).');
        return;
    }*/
    
    let importo = importoText ? parseNumericValue(importoText) : 1000; // Valore di default se non specificato
    const tassoInteresseText = document.getElementById('tasso-interesse').value;
    
    if (tassoInteresseText && !isValidNumber(tassoInteresseText)) {
        alert('Il tasso d\'interesse deve essere un valore numerico valido (usa la virgola come separatore decimale).');
        return;
    }
    
    const tassoInteresse = tassoInteresseText ? parseNumericValue(tassoInteresseText) : 0;
    const modalitaBollo = document.querySelector('input[name="modalita-bollo"]:checked').value;
    
    // Se l'importo non è stato inserito, usa il valore di default di 1000 euro
    if (isNaN(importo)) {
        importo = 1000;
    }
    
    // Verifica quali campi specifici sono mancanti
    let campiMancanti = [];
    if (!titoloId) campiMancanti.push('Titolo');
    if (isNaN(prezzoAcquisto)) campiMancanti.push('Prezzo di acquisto');
    
    if (campiMancanti.length > 0) {
        alert('Compila tutti i campi correttamente. Campi mancanti: ' + campiMancanti.join(', '));
        return;
    }
    
    // Mostra un indicatore di caricamento
    const submitButton = document.querySelector('#simulazione-form button[type="submit"]');
    const originalText = submitButton.textContent;
    submitButton.disabled = true;
    submitButton.textContent = 'Calcolo in corso...';
    
    // Chiamata API per calcolare il rendimento
    fetch(`/api/simulazioni/calcola-rendimento?idTitolo=${titoloId}&prezzoAcquisto=${prezzoAcquisto}&importo=${importo}&modalitaBollo=${modalitaBollo}&tassoInteresse=${tassoInteresse}`, {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Errore nel calcolo del rendimento');
        }
        return response.json();
    })
    .then(data => {
        // Memorizza il risultato per il salvataggio
        ultimoRisultatoCalcolo = data;
        
        // Se l'importo non era stato inserito, mostra il valore di default nel campo
        if (isNaN(parseFloat(document.getElementById('importo-nominale').value))) {
            document.getElementById('importo-nominale').value = importo;
        }
        
        // Aggiornamento dei campi di risultato
        document.getElementById('plusvalenza-netta').value = formatCurrency(data.plusvalenzaNetta);
        document.getElementById('interessi-netti').value = formatCurrency(data.interessiNetti);
        document.getElementById('commissioni').value = formatCurrency(data.commissioni);
        document.getElementById('guadagno-totale').value = formatCurrency(data.guadagnoTotale);
        document.getElementById('guadagno-netto-commissioni').value = formatCurrency(data.guadagnoNettoCommissioni);
        document.getElementById('imposta-bollo').value = formatCurrency(data.impostaBollo);
        document.getElementById('guadagno-netto-bollo').value = formatCurrency(data.guadagnoNettoBollo);
        document.getElementById('tasso').value = formatPercentage(data.tasso);
        document.getElementById('tasso-netto-commissioni').value = formatPercentage(data.tassoNettoCommissioni);
        document.getElementById('tasso-netto-bollo').value = formatPercentage(data.tassoNettoBollo);
        document.getElementById('importo-scadenza').value = formatCurrency(data.importoScadenza);
        
        // Abilita il pulsante di salvataggio
        document.getElementById('salva-simulazione-btn').disabled = false;
    })
    .catch(error => {
        console.error('Errore:', error);
        alert('Si è verificato un errore durante il calcolo del rendimento');
    })
    .finally(() => {
        // Ripristina il pulsante
        submitButton.disabled = false;
        submitButton.textContent = originalText;
    });
}

// Salva una simulazione
function salvaSimulazione() {
    if (!document.getElementById('titolo-select').value) {
        alert('Devi selezionare un titolo');
        return;
    }
    
    const titoloId = parseInt(document.getElementById('titolo-select').value);
    const prezzoAcquistoText = document.getElementById('prezzo-acquisto').value;
    
    // Commentiamo la validazione rigorosa del formato del prezzo
    /*if (!isValidNumber(prezzoAcquistoText)) {
        alert('Il prezzo di acquisto deve essere un valore numerico valido (usa la virgola come separatore decimale).');
        return;
    }*/
    
    const prezzoAcquisto = parseNumericValue(prezzoAcquistoText);
    
    // Usa il valore ISO della data per l'invio al server
    const dataAcquisto = document.getElementById('data-acquisto').getAttribute('data-iso-date');
    
    const importoText = document.getElementById('importo-nominale').value;
    
    // Commentiamo la validazione rigorosa del formato dell'importo
    /*if (!isValidNumber(importoText)) {
        alert('L\'importo nominale deve essere un valore numerico valido (usa la virgola come separatore decimale).');
        return;
    }*/
    
    const importo = parseNumericValue(importoText);
    
    const tassoInteresseText = document.getElementById('tasso-interesse').value;
    
    if (tassoInteresseText && !isValidNumber(tassoInteresseText)) {
        alert('Il tasso d\'interesse deve essere un valore numerico valido (usa la virgola come separatore decimale).');
        return;
    }
    
    const tassoInteresse = tassoInteresseText ? parseNumericValue(tassoInteresseText) : 0;
    
    const commissioniAcquistoText = document.getElementById('commissioni-acquisto').value;
    
    if (!isValidNumber(commissioniAcquistoText)) {
        alert('Le commissioni di acquisto devono essere un valore numerico valido (usa la virgola come separatore decimale).');
        return;
    }
    
    const commissioniAcquisto = parseNumericValue(commissioniAcquistoText); // Mantieni come percentuale
    const modalitaBollo = document.querySelector('input[name="modalita-bollo"]:checked').value;
    
    // Verifica quali campi specifici sono mancanti
    let campiMancanti = [];
    if (!titoloId) campiMancanti.push('Titolo');
    if (isNaN(prezzoAcquisto)) campiMancanti.push('Prezzo di acquisto');
    if (!dataAcquisto) campiMancanti.push('Data di acquisto');
    if (isNaN(importo)) campiMancanti.push('Importo nominale');
    if (isNaN(commissioniAcquisto)) campiMancanti.push('Commissioni di acquisto');
    
    if (campiMancanti.length > 0) {
        alert('Compila tutti i campi correttamente. Campi mancanti: ' + campiMancanti.join(', '));
        return;
    }
    
    // Mostra un indicatore di caricamento
    const saveButton = document.getElementById('salva-simulazione-btn');
    const originalText = saveButton.textContent;
    saveButton.disabled = true;
    saveButton.textContent = 'Salvataggio in corso...';
    
    // Chiamata API per calcolare e salvare la simulazione in un'unica operazione
    fetch(`/api/simulazioni/calcola-e-salva?idTitolo=${titoloId}&prezzoAcquisto=${prezzoAcquisto}&importo=${importo}&dataAcquisto=${dataAcquisto}&modalitaBollo=${modalitaBollo}&commissioniAcquisto=${commissioniAcquisto}&tassoInteresse=${tassoInteresse}`, {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Errore durante il salvataggio della simulazione');
        }
        return response.json();
    })
    .then(savedSimulazione => {
        // Aggiorna anche il risultato del calcolo per mostrare i valori corretti
        fetch(`/api/simulazioni/calcola-rendimento?idTitolo=${titoloId}&prezzoAcquisto=${prezzoAcquisto}&importo=${importo}&modalitaBollo=${modalitaBollo}&tassoInteresse=${tassoInteresse}`, {
            method: 'POST'
        })
        .then(response => response.json())
        .then(data => {
            // Aggiorna il risultato del calcolo
            ultimoRisultatoCalcolo = data;
            
            // Aggiorna i campi di risultato
            document.getElementById('plusvalenza-netta').value = formatCurrency(data.plusvalenzaNetta);
            document.getElementById('interessi-netti').value = formatCurrency(data.interessiNetti);
            document.getElementById('commissioni').value = formatCurrency(data.commissioni);
            document.getElementById('guadagno-totale').value = formatCurrency(data.guadagnoTotale);
            document.getElementById('guadagno-netto-commissioni').value = formatCurrency(data.guadagnoNettoCommissioni);
            document.getElementById('imposta-bollo').value = formatCurrency(data.impostaBollo);
            document.getElementById('guadagno-netto-bollo').value = formatCurrency(data.guadagnoNettoBollo);
            document.getElementById('tasso').value = formatPercentage(data.tasso);
            document.getElementById('tasso-netto-commissioni').value = formatPercentage(data.tassoNettoCommissioni);
            document.getElementById('tasso-netto-bollo').value = formatPercentage(data.tassoNettoBollo);
            document.getElementById('importo-scadenza').value = formatCurrency(data.importoScadenza);
        });
        
        // Aggiungi la nuova simulazione alla lista locale
        simulazioni.push({
            id: savedSimulazione.idSimulazione,
            titoloId: savedSimulazione.idTitolo,
            prezzoAcquisto: savedSimulazione.prezzoAcquisto,
            dataAcquisto: savedSimulazione.dataAcquisto,
            importoNominale: importo,
            commissioniAcquisto: savedSimulazione.commissioniAcquisto,
            rendimentoLordo: savedSimulazione.rendimentoLordo,
            rendimentoTassato: savedSimulazione.rendimentoTassato,
            rendimentoNettoCedole: savedSimulazione.rendimentoNettoCedole,
            impostaBollo: savedSimulazione.impostaBollo,
            rendimentoNettoBollo: savedSimulazione.rendimentoNettoBollo,
            plusMinusValenza: savedSimulazione.plusMinusValenza
        });
        
        // Aggiorna la vista
        updateSimulazioniTable();
        
        // Mostra un messaggio di successo
        alert('Simulazione salvata con successo!');
        
        // Disabilita il pulsante di salvataggio per evitare salvataggi duplicati
        document.getElementById('salva-simulazione-btn').disabled = true;
    })
    .catch(error => {
        console.error('Errore:', error);
        alert('Si è verificato un errore durante il salvataggio della simulazione');
    })
    .finally(() => {
        // Ripristina il pulsante
        saveButton.disabled = false;
        saveButton.textContent = originalText;
    });
}

// Formatta una data in formato italiano (gg-mm-aaaa)
function formatDate(dateString) {
    const date = new Date(dateString);
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    return `${day}-${month}-${year}`;
}

// Formatta un valore monetario
function formatCurrency(value) {
    return new Intl.NumberFormat('it-IT', { 
        style: 'currency', 
        currency: 'EUR',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(value);
}

// Formatta un valore percentuale
function formatPercentage(value) {
    // Assicurati che il valore sia un numero
    const numValue = parseFloat(value);
    
    // Usa Intl.NumberFormat per formattare il valore come percentuale con esattamente 2 decimali
    return new Intl.NumberFormat('it-IT', { 
        style: 'percent', 
        minimumFractionDigits: 2, 
        maximumFractionDigits: 2 
    }).format(numValue / 100);
}

// Cerca un titolo tramite ISIN e tipo
function cercaTitoloByIsin() {
    const isin = document.getElementById('codice-isin-search').value.trim();
    const tipo = document.getElementById('tipo-titolo-search').value.trim();
    
    if (!isin || !tipo) {
        alert('Inserisci sia il codice ISIN che il tipo di titolo');
        return;
    }
    
    // Mostra un indicatore di caricamento
    const cercaButton = document.getElementById('cerca-titolo-btn');
    const originalText = cercaButton.textContent;
    cercaButton.disabled = true;
    cercaButton.textContent = 'Ricerca...';
    
    // Chiamata API per recuperare le informazioni del titolo
    fetch(`/api/borsa-italiana/${tipo.toLowerCase()}/${isin}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Titolo non trovato');
            }
            return response.json();
        })
        .then(titolo => {
            // Popola i campi del form con i dati ricevuti
            document.getElementById('nome-titolo').value = titolo.nome || `${tipo} ${isin}`;
            document.getElementById('codice-isin').value = titolo.codiceIsin || isin;
            document.getElementById('tipo-titolo').value = tipo;
            
            if (titolo.dataScadenza) {
                // Formatta la data di scadenza nel formato italiano (gg-mm-aaaa)
                const dataScadenzaInput = document.getElementById('data-scadenza');
                const formattedDate = formatDate(titolo.dataScadenza);
                
                // Imposta il valore visualizzato nel formato italiano
                dataScadenzaInput.value = formattedDate;
                
                // Memorizza la data ISO come attributo personalizzato per l'invio al server
                dataScadenzaInput.setAttribute('data-iso-date', titolo.dataScadenza);
                
                // Aggiorna il datepicker per riflettere la nuova data
                $(dataScadenzaInput).datepicker('update');
            }
            
        if (titolo.tassoNominale) {
            document.getElementById('tasso-nominale').value = formatDecimal(titolo.tassoNominale);
        }
        
        if (titolo.periodicitaCedole) {
            document.getElementById('periodicita-cedole').value = titolo.periodicitaCedole;
        }
        
        // Imposta sempre il campo periodicità bollo nascosto ad ANNUALE o al valore ricevuto
        document.getElementById('periodicita-bollo').value = titolo.periodicitaBollo || 'ANNUALE';
        
        if (titolo.corso) {
            document.getElementById('prezzo-titolo').value = formatDecimal(titolo.corso);
        }
            
            // Aggiungi un pulsante per importare direttamente il titolo
            const importaBtn = document.createElement('button');
            importaBtn.className = 'btn btn-success mt-3';
            importaBtn.textContent = 'Importa Titolo';
            importaBtn.onclick = function() {
                // Mostra un indicatore di caricamento
                importaBtn.disabled = true;
                importaBtn.textContent = 'Importazione...';
                
                // Chiamata API per importare il titolo da Borsa Italiana
                fetch(`/api/frontend/titolo/importa?codiceIsin=${isin}&tipoTitolo=${tipo}`, {
                    method: 'POST'
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Errore durante l\'importazione del titolo');
                    }
                    return response.json();
                })
                .then(data => {
                    // Verifica se abbiamo ricevuto un messaggio (caso di aggiornamento)
                    if (data.message && data.titolo) {
                        // È un aggiornamento di un titolo esistente
                        alert(data.message);
                    } else if (data && data.idTitolo) {
                        // È un nuovo titolo
                        alert('Titolo importato con successo!');
                    } else {
                        // Caso imprevisto: risposta non valida dal server
                        console.error('Risposta non valida dal server:', data);
                        alert('Errore durante l\'importazione del titolo: risposta non valida dal server');
                        return;
                    }
                    
                    // Chiudi il modal
                    bootstrap.Modal.getInstance(document.getElementById('titolo-modal')).hide();
                    
                    // Ricarica i titoli dal server
                    loadTitoliFromServer();
                })
                .catch(error => {
                    console.error('Errore:', error);
                    alert(error.message || 'Si è verificato un errore durante l\'importazione del titolo.');
                    
                    // Ripristina il pulsante
                    importaBtn.disabled = false;
                    importaBtn.textContent = 'Importa Titolo';
                });
            };
            
            // Aggiungi il pulsante al form
            const formFooter = document.querySelector('#titolo-modal .modal-footer');
            // Rimuovi il pulsante se già presente
            const existingBtn = formFooter.querySelector('.btn-success');
            if (existingBtn) {
                formFooter.removeChild(existingBtn);
            }
            formFooter.insertBefore(importaBtn, formFooter.querySelector('.btn-primary'));
            
            // Mostra un messaggio di successo
            alert('Titolo trovato! I campi sono stati compilati automaticamente.');
        })
        .catch(error => {
            console.error('Errore:', error);
            alert('Titolo non trovato o errore nella ricerca. Verifica ISIN e tipo titolo.');
        })
        .finally(() => {
            // Ripristina il pulsante
            cercaButton.disabled = false;
            cercaButton.textContent = originalText;
        });
}

// Mostra la lista dei titoli da Borsa Italiana con paginazione
function showListaTitoli(tipoTitolo) {
    // Aggiorna il titolo del modal
    document.getElementById('lista-titoli-modal-label').textContent = `Lista ${tipoTitolo}`;
    document.getElementById('lista-titoli-tipo').textContent = `Titoli ${tipoTitolo}`;
    
    // Resetta la paginazione
    currentPage = 0;
    currentTipoTitolo = tipoTitolo;
    
    // Mostra il modal
    const modal = new bootstrap.Modal(document.getElementById('lista-titoli-modal'));
    modal.show();
    
    // Carica la prima pagina
    loadTitoliPage(tipoTitolo, currentPage);
    
    // Aggiungi event listener ai pulsanti di navigazione
    document.getElementById('prev-page-btn').addEventListener('click', function() {
        if (currentPage > 0) {
            currentPage--;
            loadTitoliPage(currentTipoTitolo, currentPage);
        }
    });
    
    document.getElementById('next-page-btn').addEventListener('click', function() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadTitoliPage(currentTipoTitolo, currentPage);
        }
    });
}

// Carica una pagina di titoli
function loadTitoliPage(tipoTitolo, page) {
    // Mostra l'indicatore di caricamento
    document.getElementById('lista-titoli-loading').style.display = 'inline-block';
    document.getElementById('lista-titoli-error').classList.add('d-none');
    document.getElementById('lista-titoli-body').innerHTML = '';
    document.getElementById('lista-titoli-count').textContent = '';
    
    // Disabilita i pulsanti di navigazione durante il caricamento
    document.getElementById('prev-page-btn').disabled = true;
    document.getElementById('next-page-btn').disabled = true;
    
    // Chiamata API per recuperare la lista paginata dei titoli
    fetch(`/api/borsa-italiana/lista-paginata/${tipoTitolo}?page=${page}&size=${pageSize}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Errore nel recupero della lista dei titoli');
            }
            return response.json();
        })
        .then(data => {
            // Nascondi l'indicatore di caricamento
            document.getElementById('lista-titoli-loading').style.display = 'none';
            
            // Aggiorna le variabili di paginazione
            currentPage = data.pageNumber;
            totalPages = data.totalPages;
            totalElements = data.totalElements;
            
            // Aggiorna l'interfaccia di paginazione
            document.getElementById('current-page').textContent = currentPage + 1;
            document.getElementById('total-pages').textContent = totalPages;
            document.getElementById('prev-page-btn').disabled = data.first;
            document.getElementById('next-page-btn').disabled = data.last;
            
            // Aggiorna il contatore
            document.getElementById('lista-titoli-count').textContent = `(${totalElements} titoli)`;
            
            // Popola la tabella con i titoli
            const tbody = document.getElementById('lista-titoli-body');
            tbody.innerHTML = '';
            
            const titoli = data.content;
            
            if (titoli.length === 0) {
                const row = document.createElement('tr');
                row.innerHTML = `<td colspan="6" class="text-center">Nessun titolo trovato</td>`;
                tbody.appendChild(row);
                return;
            }
            
            titoli.forEach(titolo => {
                const row = document.createElement('tr');
                
                // Formatta la data di scadenza
                const dataScadenza = titolo.dataScadenza ? formatDate(titolo.dataScadenza) : 'N/A';
                
                // Formatta il tasso
                const tasso = titolo.tassoNominale ? `${formatDecimal(titolo.tassoNominale)}%` : 'N/A';
                
                // Formatta il prezzo
                const prezzo = titolo.corso ? `${formatDecimal(titolo.corso)} €` : 'N/A';
                
                row.innerHTML = `
                    <td>${titolo.codiceIsin || 'N/A'}</td>
                    <td>${titolo.nome || 'N/A'}</td>
                    <td>${dataScadenza}</td>
                    <td>${tasso}</td>
                    <td>${prezzo}</td>
                    <td>
                        <button class="btn btn-sm btn-primary aggiungi-titolo-btn" 
                                data-nome="${titolo.nome || ''}"
                                data-isin="${titolo.codiceIsin || ''}"
                                data-scadenza="${titolo.dataScadenza || ''}"
                                data-tasso="${titolo.tassoNominale || ''}"
                                data-periodicita="${titolo.periodicitaCedole || ''}"
                                data-tipo="${tipoTitolo}"
                                data-prezzo="${titolo.corso || ''}">
                            Aggiungi
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
                
                // Aggiungi event listener al pulsante "Aggiungi"
                const aggiungiBtn = row.querySelector('.aggiungi-titolo-btn');
                aggiungiBtn.addEventListener('click', function() {
                    aggiungiTitoloDaLista(this);
                });
            });
        })
        .catch(error => {
            console.error('Errore:', error);
            document.getElementById('lista-titoli-loading').style.display = 'none';
            document.getElementById('lista-titoli-error').classList.remove('d-none');
            
            // Resetta l'interfaccia di paginazione in caso di errore
            document.getElementById('current-page').textContent = '1';
            document.getElementById('total-pages').textContent = '1';
            document.getElementById('prev-page-btn').disabled = true;
            document.getElementById('next-page-btn').disabled = true;
        });
}

// Aggiunge un titolo dalla lista di Borsa Italiana
function aggiungiTitoloDaLista(button) {
    // Recupera i dati dal pulsante
    const nome = button.getAttribute('data-nome');
    const codiceIsin = button.getAttribute('data-isin');
    const dataScadenza = button.getAttribute('data-scadenza');
    const tassoNominale = button.getAttribute('data-tasso');
    const periodicitaCedole = button.getAttribute('data-periodicita') || 'SEMESTRALE';
    const tipoTitolo = button.getAttribute('data-tipo');
    const prezzo = button.getAttribute('data-prezzo');
    
    // Mostra un indicatore di caricamento
    button.disabled = true;
    const originalText = button.textContent;
    button.textContent = 'Importazione...';
    
    // Chiamata API per importare il titolo da Borsa Italiana
    fetch(`/api/frontend/titolo/importa?codiceIsin=${codiceIsin}&tipoTitolo=${tipoTitolo}`, {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Errore durante il salvataggio del titolo');
        }
        return response.json();
    })
    .then(data => {
        // Verifica se abbiamo ricevuto un messaggio (caso di aggiornamento)
        if (data.message && data.titolo) {
            // È un aggiornamento di un titolo esistente
            const titoloAggiornato = data.titolo;
            
            // Trova l'indice del titolo esistente
            const index = titoli.findIndex(t => t.codiceIsin === titoloAggiornato.codiceIsin);
            
            if (index !== -1) {
                // Aggiorna il titolo esistente
                titoli[index] = {
                    id: titoloAggiornato.idTitolo,
                    nome: titoloAggiornato.nome,
                    codiceIsin: titoloAggiornato.codiceIsin,
                    dataScadenza: titoloAggiornato.dataScadenza,
                    tassoNominale: titoloAggiornato.tassoNominale,
                    periodicitaCedole: titoloAggiornato.periodicitaCedole,
                    periodicitaBollo: titoloAggiornato.periodicitaBollo,
                    tipoTitolo: titoloAggiornato.tipoTitolo,
                    prezzo: parseFloat(prezzo) || 0
                };
            } else {
                // Nel caso improbabile in cui non troviamo il titolo, lo aggiungiamo
                titoli.push({
                    id: titoloAggiornato.idTitolo,
                    nome: titoloAggiornato.nome,
                    codiceIsin: titoloAggiornato.codiceIsin,
                    dataScadenza: titoloAggiornato.dataScadenza,
                    tassoNominale: titoloAggiornato.tassoNominale,
                    periodicitaCedole: titoloAggiornato.periodicitaCedole,
                    periodicitaBollo: titoloAggiornato.periodicitaBollo,
                    tipoTitolo: titoloAggiornato.tipoTitolo,
                    prezzo: parseFloat(prezzo) || 0
                });
            }
            
            // Mostra un messaggio di successo per l'aggiornamento
            alert(data.message);
        } else if (data && data.idTitolo) {
            // È un nuovo titolo
            const savedTitolo = data;
            
            // Aggiungi il nuovo titolo alla lista locale
            titoli.push({
                id: savedTitolo.idTitolo,
                nome: savedTitolo.nome || '',
                codiceIsin: savedTitolo.codiceIsin || '',
                dataScadenza: savedTitolo.dataScadenza || null,
                tassoNominale: savedTitolo.tassoNominale || 0,
                periodicitaCedole: savedTitolo.periodicitaCedole || '',
                periodicitaBollo: savedTitolo.periodicitaBollo || '',
                tipoTitolo: savedTitolo.tipoTitolo || '',
                prezzo: parseFloat(prezzo) || 0
            });
            
            // Mostra un messaggio di successo per il nuovo titolo
            alert('Titolo aggiunto con successo!');
        } else {
            // Caso imprevisto: risposta non valida dal server
            console.error('Risposta non valida dal server:', data);
            alert('Errore durante il salvataggio del titolo: risposta non valida dal server');
            return;
        }
        
        // Aggiorna le viste
        updateTitoliTable();
        updateTitoliSelect();
        
        // Disabilita il pulsante per evitare aggiunte multiple
        button.disabled = true;
        button.textContent = 'Aggiunto';
        button.classList.remove('btn-primary');
        button.classList.add('btn-success');
    })
    .catch(error => {
        console.error('Errore:', error);
        alert(error.message || 'Si è verificato un errore durante il salvataggio del titolo.');
        
        // Ripristina il pulsante
        button.disabled = false;
        button.textContent = originalText;
    });
}

// Calcola i rendimenti di tutti i titoli con scadenza futura
function calcolaRendimentiTuttiTitoli() {
    // Mostra un indicatore di caricamento
    const button = document.getElementById('calcola-rendimenti-btn');
    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = 'Calcolo in corso...';
    
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
        // Aggiorna la lista delle simulazioni con i risultati
        simulazioni = data.map(dto => ({
            id: dto.idSimulazione,
            titoloId: dto.idTitolo,
            prezzoAcquisto: dto.prezzoAcquisto,
            dataAcquisto: dto.dataAcquisto,
            importoNominale: 10000, // Valore fisso di 10.000 euro
            commissioniAcquisto: dto.commissioniAcquisto,
            rendimentoLordo: dto.rendimentoLordo * 100, // Converti da decimale a percentuale
            rendimentoTassato: dto.rendimentoTassato * 100,
            rendimentoNettoCedole: dto.rendimentoNettoCedole * 100,
            impostaBollo: dto.impostaBollo,
            rendimentoNettoBollo: dto.rendimentoNettoBollo,
            plusMinusValenza: dto.plusMinusValenza
        }));
        
        // Aggiorna la tabella delle simulazioni
        updateSimulazioniTable();
        
        // Mostra un messaggio di successo
        alert(`Rendimenti calcolati con successo per ${data.length} titoli!`);
    })
    .catch(error => {
        console.error('Errore:', error);
        alert('Si è verificato un errore durante il calcolo dei rendimenti');
    })
    .finally(() => {
        // Ripristina il pulsante
        button.disabled = false;
        button.textContent = originalText;
    });
}

// Ottiene il prezzo corrente del titolo selezionato
function getPrezzoCorrente() {
    const titoloId = document.getElementById('titolo-select').value;
    
    if (!titoloId) {
        alert('Seleziona prima un titolo');
        return;
    }
    
    // Trova il titolo selezionato
    const titolo = titoli.find(t => t.id == titoloId);
    if (!titolo) {
        alert('Titolo non trovato');
        return;
    }
    
    // Verifica che il titolo abbia un codice ISIN e un tipo
    if (!titolo.codiceIsin || !titolo.tipoTitolo) {
        alert('Il titolo selezionato non ha un codice ISIN o un tipo valido');
        return;
    }
    
    // Mostra un indicatore di caricamento
    const prezzoButton = document.getElementById('prezzo-corrente-btn');
    const originalText = prezzoButton.textContent;
    prezzoButton.disabled = true;
    prezzoButton.textContent = 'Ricerca...';
    
    // Chiamata API per recuperare il prezzo corrente del titolo
    fetch(`/api/borsa-italiana/${titolo.tipoTitolo.toLowerCase()}/${titolo.codiceIsin}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Prezzo non trovato');
            }
            return response.json();
        })
        .then(data => {
            // Valorizza il campo prezzo di acquisto con il prezzo corrente
            if (data.corso) {
                // Formatta il prezzo con la virgola come separatore decimale
                document.getElementById('prezzo-acquisto').value = formatDecimal(data.corso);
                
                // Aggiorna anche il tasso d'interesse se disponibile
                if (data.tassoNominale) {
                    // Formatta il tasso con la virgola come separatore decimale
                    document.getElementById('tasso-interesse').value = formatDecimal(data.tassoNominale);
                }
                
                // Mostra un messaggio di successo
                alert('Prezzo corrente aggiornato con successo!');
            } else {
                alert('Prezzo corrente non disponibile per questo titolo');
            }
        })
        .catch(error => {
            console.error('Errore:', error);
            alert('Errore nel recupero del prezzo corrente. Verifica che il titolo sia valido.');
        })
        .finally(() => {
            // Ripristina il pulsante
            prezzoButton.disabled = false;
            prezzoButton.textContent = originalText;
        });
}
