// Funzioni per l'applicazione Rendimento Titoli

document.addEventListener('DOMContentLoaded', function() {
    // Inizializzazione
    initApp();
    
    // Event listeners
    document.getElementById('add-titolo-btn').addEventListener('click', showTitoloModal);
    document.getElementById('save-titolo-btn').addEventListener('click', saveTitolo);
    document.getElementById('simulazione-form').addEventListener('submit', function(e) {
        e.preventDefault();
        createSimulazione();
    });
    
    // Event listener per il calcolo dei giorni alla scadenza
    document.getElementById('titolo-select').addEventListener('change', updateGiorniAllaScadenza);
    document.getElementById('data-acquisto').addEventListener('change', updateGiorniAllaScadenza);
    
    // Event listener per il pulsante di salvataggio della simulazione
    document.getElementById('salva-simulazione-btn').addEventListener('click', salvaSimulazione);
});

// Variabili globali per memorizzare i dati
let titoli = [];
let simulazioni = [];
let ultimoRisultatoCalcolo = null;

// Inizializzazione dell'applicazione
function initApp() {
    // Carica i titoli dal server invece dei dati di esempio
    loadTitoliFromServer();
    
    // Popola i select con i valori degli enum
    populateEnumSelects();
    
    // Aggiorna le viste (le viste dei titoli verranno aggiornate dopo il caricamento)
    updateSimulazioniTable();
    
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
            prezzo: 98.75 
        },
        { 
            id: 2, 
            nome: 'CCT EU 2026', 
            codiceIsin: 'IT0005451361', 
            dataScadenza: scadenza2.toISOString().split('T')[0], 
            tassoNominale: 1.85, 
            periodicitaCedole: 'SEMESTRALE', 
            periodicitaBollo: 'ANNUALE', 
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
            <td>${titolo.nome}</td>
            <td>${titolo.codiceIsin}</td>
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
            <td>${formatDate(simulazione.dataAcquisto)}</td>
            <td>${formatDate(titolo.dataScadenza)}</td>
            <td class="${rendimentoClass}">${simulazione.rendimentoNettoBollo.toFixed(2)}%</td>
            <td>${valoreFinaleTeorico.toFixed(2)} €</td>
        `;
        tbody.appendChild(row);
    });
}

// Imposta le date di default per il form di simulazione
function setDefaultDates() {
    const today = new Date();
    document.getElementById('data-acquisto').valueAsDate = today;
    
    // Imposta il valore di default per le commissioni (0,9/1000 = 0.09%)
    document.getElementById('commissioni-acquisto').value = "0.09";
}

// Calcola e aggiorna i giorni mancanti alla scadenza
function updateGiorniAllaScadenza() {
    const titoloId = document.getElementById('titolo-select').value;
    const dataAcquisto = document.getElementById('data-acquisto').value;
    
    if (!titoloId || !dataAcquisto) {
        document.getElementById('giorni-alla-scadenza').value = '';
        return;
    }
    
    const titolo = titoli.find(t => t.id == titoloId);
    if (!titolo || !titolo.dataScadenza) {
        document.getElementById('giorni-alla-scadenza').value = '';
        return;
    }
    
    // Calcola i giorni tra la data di acquisto e la data di scadenza
    const acquisto = new Date(dataAcquisto);
    const scadenza = new Date(titolo.dataScadenza);
    
    // Calcola la differenza in millisecondi e converti in giorni
    const diffTime = scadenza - acquisto;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    document.getElementById('giorni-alla-scadenza').value = diffDays > 0 ? diffDays : 0;
}

// Mostra il modal per aggiungere/modificare un titolo
function showTitoloModal(titoloId = null) {
    // Reset del form
    document.getElementById('titolo-form').reset();
    document.getElementById('titolo-id').value = '';
    document.getElementById('titolo-modal-label').textContent = 'Aggiungi Titolo';
    
    // Imposta una data di scadenza predefinita (5 anni da oggi)
    const defaultScadenza = new Date();
    defaultScadenza.setFullYear(defaultScadenza.getFullYear() + 5);
    document.getElementById('data-scadenza').valueAsDate = defaultScadenza;
    
    // Se è una modifica, popola il form con i dati del titolo
    if (titoloId) {
        const titolo = titoli.find(t => t.id === titoloId);
        if (titolo) {
            document.getElementById('titolo-id').value = titolo.id;
            document.getElementById('nome-titolo').value = titolo.nome;
            document.getElementById('codice-isin').value = titolo.codiceIsin || '';
            document.getElementById('data-scadenza').value = titolo.dataScadenza || '';
            document.getElementById('tasso-nominale').value = titolo.tassoNominale || '';
            document.getElementById('periodicita-cedole').value = titolo.periodicitaCedole || '';
            document.getElementById('periodicita-bollo').value = titolo.periodicitaBollo || '';
            document.getElementById('prezzo-titolo').value = titolo.prezzo;
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
    const dataScadenza = document.getElementById('data-scadenza').value;
    const tassoNominaleStr = document.getElementById('tasso-nominale').value;
    const periodicitaCedole = document.getElementById('periodicita-cedole').value;
    const periodicitaBollo = document.getElementById('periodicita-bollo').value;
    
    // Gestione del prezzo come testo liberamente digitabile
    const prezzoText = document.getElementById('prezzo-titolo').value.replace(',', '.');
    const prezzo = parseFloat(prezzoText);
    
    if (!nome || !codiceIsin || !dataScadenza || !tassoNominaleStr || 
        !periodicitaCedole || !periodicitaBollo || isNaN(prezzo)) {
        alert('Compila tutti i campi correttamente. Il prezzo deve essere un valore numerico valido.');
        return;
    }
    
    // Converti i valori nei tipi corretti
    const tassoNominale = parseFloat(tassoNominaleStr);
    
    // Crea l'oggetto DTO da inviare al server
    const titoloDTO = {
        nome: nome,
        codiceIsin: codiceIsin,
        dataScadenza: dataScadenza,
        tassoNominale: tassoNominale,
        periodicitaCedole: periodicitaCedole,
        periodicitaBollo: periodicitaBollo
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
        if (response.status === 409) {
            // Conflitto: ISIN già presente
            return response.json().then(data => {
                throw new Error(data.message || 'Titolo già presente');
            });
        }
        if (!response.ok) {
            throw new Error('Errore durante il salvataggio del titolo');
        }
        return response.json();
    })
    .then(savedTitolo => {
        // Aggiungi il nuovo titolo alla lista locale
        titoli.push({
            id: savedTitolo.idTitolo,
            nome: savedTitolo.nome,
            codiceIsin: savedTitolo.codiceIsin,
            dataScadenza: savedTitolo.dataScadenza,
            tassoNominale: savedTitolo.tassoNominale,
            periodicitaCedole: savedTitolo.periodicitaCedole,
            periodicitaBollo: savedTitolo.periodicitaBollo,
            prezzo: prezzo // Manteniamo il prezzo inserito dall'utente
        });
        
        // Aggiorna le viste
        updateTitoliTable();
        updateTitoliSelect();
        
        // Chiudi il modal
        bootstrap.Modal.getInstance(document.getElementById('titolo-modal')).hide();
        
        // Mostra un messaggio di successo
        alert('Titolo salvato con successo!');
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
    const prezzoAcquisto = parseFloat(document.getElementById('prezzo-acquisto').value);
    const importo = parseFloat(document.getElementById('importo-nominale').value);
    const modalitaBollo = document.querySelector('input[name="modalita-bollo"]:checked').value;
    
    if (!titoloId || isNaN(prezzoAcquisto) || isNaN(importo)) {
        alert('Compila tutti i campi correttamente');
        return;
    }
    
    // Mostra un indicatore di caricamento
    const submitButton = document.querySelector('#simulazione-form button[type="submit"]');
    const originalText = submitButton.textContent;
    submitButton.disabled = true;
    submitButton.textContent = 'Calcolo in corso...';
    
    // Chiamata API per calcolare il rendimento
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
        // Memorizza il risultato per il salvataggio
        ultimoRisultatoCalcolo = data;
        
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
    if (!ultimoRisultatoCalcolo) {
        alert('Devi prima calcolare il rendimento');
        return;
    }
    
    const titoloId = parseInt(document.getElementById('titolo-select').value);
    const prezzoAcquisto = parseFloat(document.getElementById('prezzo-acquisto').value);
    const dataAcquisto = document.getElementById('data-acquisto').value;
    const importo = parseFloat(document.getElementById('importo-nominale').value);
    const commissioniAcquisto = parseFloat(document.getElementById('commissioni-acquisto').value) / 100; // Converti da percentuale a decimale
    
    // Crea l'oggetto DTO da inviare al server
    const simulazioneDTO = {
        idTitolo: titoloId,
        dataAcquisto: dataAcquisto,
        prezzoAcquisto: prezzoAcquisto,
        rendimentoLordo: ultimoRisultatoCalcolo.tasso / 100, // Converti da percentuale a decimale
        rendimentoTassato: ultimoRisultatoCalcolo.tasso * 0.875 / 100, // Simulazione tassazione al 12.5%
        commissioniAcquisto: commissioniAcquisto,
        rendimentoNettoCedole: ultimoRisultatoCalcolo.tassoNettoCommissioni / 100,
        impostaBollo: ultimoRisultatoCalcolo.impostaBollo,
        rendimentoNettoBollo: ultimoRisultatoCalcolo.guadagnoNettoBollo / importo * 100,
        plusMinusValenza: ultimoRisultatoCalcolo.plusvalenzaNetta
    };
    
    // Mostra un indicatore di caricamento
    const saveButton = document.getElementById('salva-simulazione-btn');
    const originalText = saveButton.textContent;
    saveButton.disabled = true;
    saveButton.textContent = 'Salvataggio in corso...';
    
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
            throw new Error('Errore durante il salvataggio della simulazione');
        }
        return response.json();
    })
    .then(savedSimulazione => {
        // Aggiungi la nuova simulazione alla lista locale
        const titolo = titoli.find(t => t.id === titoloId);
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

// Formatta una data in formato italiano
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('it-IT');
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
    return new Intl.NumberFormat('it-IT', { 
        style: 'percent', 
        minimumFractionDigits: 2, 
        maximumFractionDigits: 2 
    }).format(value / 100);
}