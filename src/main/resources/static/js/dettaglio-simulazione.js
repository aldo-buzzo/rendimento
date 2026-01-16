// Script per la gestione della pagina di dettaglio simulazione

// Alias per le funzioni di utilità più usate
const parseNumericValue = Validators.parseNumericValue;
const formatDecimal = Formatters.formatDecimal;
const formatDate = Formatters.formatDate;

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

// Funzione per ottenere i parametri dall'URL
function getUrlParams() {
    const params = new URLSearchParams(window.location.search);
    return {
        titoloId: params.get('titoloId')
    };
}

// Funzione per calcolare i giorni alla scadenza
function calcolaGiorniAllaScadenza(dataAcquisto, dataScadenza) {
    const acquisto = new Date(dataAcquisto);
    const scadenza = new Date(dataScadenza);
    
    // Calcola la differenza in millisecondi e converti in giorni
    const diffTime = scadenza - acquisto;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    return diffDays > 0 ? diffDays : 0;
}

// Funzione per caricare i dettagli della simulazione
function caricaDettagliSimulazione(titoloId) {
    if (!titoloId) {
        alert('ID titolo non specificato');
        window.location.href = 'index.html';
        return;
    }
    
    // Mostra un indicatore di caricamento
    DomUtils.toggleLoading(true);
    
    // Crea una simulazione di fallback
    const simulazioneFallback = {
        id: null,
        titoloId: titoloId,
        prezzoAcquisto: 100.00,
        dataAcquisto: new Date().toISOString().split('T')[0],
        importoNominale: 10000,
        commissioniAcquisto: 0.25,
        rendimentoLordo: 0,
        rendimentoTassato: 0,
        rendimentoNettoCedole: 0,
        impostaBollo: 0.20,
        rendimentoNettoBollo: 0,
        plusMinusValenza: 0
    };
    
    // Utilizziamo il modello Simulazione per caricare i dati
    Simulazione.load(true)
        .then(simulazioni => {
            // Filtra le simulazioni per il titolo specificato
            const simulazioneDelTitolo = simulazioni.find(s => s.titoloId == titoloId);
            
            if (!simulazioneDelTitolo) {
                console.warn(`Nessuna simulazione trovata per il titolo ID ${titoloId}, utilizzando simulazione di fallback`);
                return simulazioneFallback;
            }
            
            console.log("Simulazione caricata:", simulazioneDelTitolo);
            return simulazioneDelTitolo;
        })
        .catch(error => {
            console.warn('Errore nel caricamento delle simulazioni:', error);
            console.warn(`Utilizzando simulazione di fallback per il titolo ID ${titoloId}`);
            return simulazioneFallback;
        })
        .then(simulazione => {
            // Carica il titolo associato
            // Titolo.loadById ora gestisce internamente gli errori e restituisce sempre un titolo (reale o fallback)
            return Titolo.loadById(titoloId)
                .then(titolo => {
                    console.log("Titolo caricato:", titolo);
                    return { simulazione, titolo };
                });
        })
        .then(data => {
            // Popola i campi con i dati ricevuti
            popolaCampi(data.simulazione, data.titolo);
            
            try {
                // Carica anche tutte le simulazioni dello stesso titolo
                caricaStoricoSimulazioni(data.simulazione.titoloId);
            } catch (error) {
                console.warn('Errore nel caricamento dello storico simulazioni:', error);
                document.getElementById('no-simulazioni').classList.remove('d-none');
            }
            
            // Nascondi l'indicatore di caricamento
            DomUtils.toggleLoading(false);
        })
        .catch(error => {
            // Questo catch è solo per sicurezza, non dovremmo mai arrivare qui
            // perché tutti gli errori sono già gestiti nei catch precedenti
            console.error('Errore imprevisto nel caricamento dei dettagli della simulazione:', error);
            
            // Mostra un messaggio di errore all'utente
            alert('Si è verificato un errore nel caricamento dei dettagli della simulazione. Verranno utilizzati dati di esempio.');
            
            // Crea un titolo di fallback
            const titoloFallback = {
                id: titoloId,
                nome: `Titolo #${titoloId}`,
                codiceIsin: 'N/A',
                dataScadenza: new Date().toISOString().split('T')[0],
                tassoNominale: 0,
                periodicitaCedole: 'SEMESTRALE',
                periodicitaBollo: 'ANNUALE',
                tipoTitolo: 'BTP',
                prezzo: 100.00
            };
            
            // Popola i campi con i dati di fallback
            popolaCampi(simulazioneFallback, titoloFallback);
            
            // Mostra il messaggio che non ci sono simulazioni
            document.getElementById('no-simulazioni').classList.remove('d-none');
            
            // Nascondi l'indicatore di caricamento
            DomUtils.toggleLoading(false);
        });
}

// Funzione per caricare tutte le simulazioni dello stesso titolo
function caricaStoricoSimulazioni(titoloId) {
    if (!titoloId) return;
    
    // Utilizziamo il modello Simulazione per caricare i dati
    Simulazione.load(false)
        .then(simulazioni => {
            // Filtra le simulazioni per il titolo specificato
            const simulazioniDelTitolo = simulazioni.filter(s => s.titoloId == titoloId);
            
            console.log("Simulazioni caricate:", simulazioniDelTitolo);
            
            // Popola la tabella con le simulazioni
            popolaTabellaSimulazioni(simulazioniDelTitolo);
        })
        .catch(error => {
            console.error('Errore nel caricamento delle simulazioni:', error);
            document.getElementById('no-simulazioni').classList.remove('d-none');
        });
}

// Funzione per popolare la tabella delle simulazioni
function popolaTabellaSimulazioni(simulazioni) {
    const tbody = document.getElementById('simulazioni-list');
    tbody.innerHTML = ''; // Pulisci la tabella
    
    if (!simulazioni || simulazioni.length === 0) {
        document.getElementById('no-simulazioni').classList.remove('d-none');
        return;
    }
    
    document.getElementById('no-simulazioni').classList.add('d-none');
    
    // Ordina le simulazioni per data di acquisto decrescente
    simulazioni.sort((a, b) => new Date(b.dataAcquisto) - new Date(a.dataAcquisto));
    
    // Popola la tabella con le simulazioni
    simulazioni.forEach(simulazione => {
        const row = document.createElement('tr');
        
        // Data Acquisto
        const dataCell = document.createElement('td');
        dataCell.textContent = formatDate(simulazione.dataAcquisto);
        row.appendChild(dataCell);
        
        // Prezzo Acquisto
        const prezzoCell = document.createElement('td');
        prezzoCell.textContent = formatDecimal(simulazione.prezzoAcquisto);
        row.appendChild(prezzoCell);
        
        // Importo
        const importoCell = document.createElement('td');
        importoCell.textContent = formatDecimal(simulazione.importoNominale || 10000);
        row.appendChild(importoCell);
        
        // Rendimento Lordo
        const rendimentoLordoCell = document.createElement('td');
        rendimentoLordoCell.textContent = formatDecimal(simulazione.rendimentoLordo) + '%';
        row.appendChild(rendimentoLordoCell);
        
        // Rendimento Netto
        const rendimentoNettoCell = document.createElement('td');
        rendimentoNettoCell.textContent = formatDecimal(simulazione.rendimentoNettoCedole) + '%';
        row.appendChild(rendimentoNettoCell);
        
        // Rendimento Netto Bollo
        const rendimentoNettoBolloCell = document.createElement('td');
        rendimentoNettoBolloCell.textContent = formatDecimal(simulazione.rendimentoNettoBollo) + '%';
        row.appendChild(rendimentoNettoBolloCell);
        
        // Colonna vuota per mantenere l'allineamento con l'intestazione
        const azioniCell = document.createElement('td');
        azioniCell.textContent = '-';
        row.appendChild(azioniCell);
        
        tbody.appendChild(row);
    });
}

// Funzione per ricalcolare i valori della simulazione
function ricalcolaValori(titoloId) {
    if (!titoloId) {
        console.error("ID titolo non disponibile per il ricalcolo");
        return;
    }
    
    // Ottieni i dati necessari per il ricalcolo
    const prezzoAcquisto = parseNumericValue(document.getElementById('prezzo-acquisto').value);
    const importo = parseNumericValue(document.getElementById('importo-nominale').value);
    const modalitaBollo = document.getElementById('modalita-bollo').value || 'ANNUALE';
    
    // Mostra un indicatore di caricamento
    DomUtils.toggleLoading(true);
    
    // Utilizziamo il modello Simulazione per ricalcolare i valori
    Simulazione.calcolaRendimento(titoloId, prezzoAcquisto, importo, modalitaBollo)
        .then(risultato => {
            console.log("Valori ricalcolati:", risultato);
            
            // Aggiorna i campi con i valori ricalcolati
            aggiornaRisultatiCalcolo(risultato);
            
            // Nascondi l'indicatore di caricamento
            DomUtils.toggleLoading(false);
            
            // Mostra un messaggio di successo
            alert('Valori ricalcolati con successo!');
        })
        .catch(error => {
            console.error('Errore:', error);
            alert('Si è verificato un errore nel ricalcolo dei valori');
            
            // Nascondi l'indicatore di caricamento
            DomUtils.toggleLoading(false);
        });
}

// Funzione per aggiornare i campi con i risultati del calcolo
function aggiornaRisultatiCalcolo(risultato) {
    // Aggiorna i campi con i valori ricalcolati
    document.getElementById('plusvalenza-netta').value = formatDecimal(risultato.plusvalenzaNetta);
    document.getElementById('interessi-netti').value = formatDecimal(risultato.interessiNetti);
    document.getElementById('commissioni').value = formatDecimal(risultato.commissioni);
    document.getElementById('imposta-bollo').value = formatDecimal(risultato.impostaBollo);
    document.getElementById('guadagno-totale').value = formatDecimal(risultato.guadagnoTotale);
    document.getElementById('guadagno-netto-commissioni').value = formatDecimal(risultato.guadagnoNettoCommissioni);
    document.getElementById('guadagno-netto-bollo').value = formatDecimal(risultato.guadagnoNettoBollo);
    document.getElementById('tasso').value = formatDecimal(risultato.tasso) + '%';
    document.getElementById('tasso-netto-commissioni').value = formatDecimal(risultato.tassoNettoCommissioni) + '%';
    document.getElementById('tasso-netto-bollo').value = formatDecimal(risultato.tassoNettoBollo) + '%';
    document.getElementById('importo-scadenza').value = formatDecimal(risultato.importoScadenza);
}

// Funzione per popolare i campi con i dati della simulazione
function popolaCampi(simulazione, titolo) {
    try {
        console.log("Popolamento campi con simulazione:", simulazione, "e titolo:", titolo);
        
        // Dati del titolo
        document.getElementById('titolo-nome').value = `${titolo.nome || ""} (${titolo.codiceIsin || ""})`;
        document.getElementById('tasso-interesse').value = formatDecimal(titolo.tassoNominale || 0);
        
        // Dati della simulazione
        document.getElementById('prezzo-acquisto').value = formatDecimal(simulazione.prezzoAcquisto || 0);
        document.getElementById('importo-nominale').value = formatDecimal(simulazione.importoNominale || 10000);
        document.getElementById('data-acquisto').value = formatDate(simulazione.dataAcquisto || new Date().toISOString());
        document.getElementById('commissioni-acquisto').value = formatDecimal(simulazione.commissioniAcquisto || 0);
        document.getElementById('modalita-bollo').value = simulazione.modalitaBollo || 'ANNUALE'; // Valore di default
        
        // Calcola i giorni alla scadenza
        const giorniAllaScadenza = calcolaGiorniAllaScadenza(simulazione.dataAcquisto, titolo.dataScadenza);
        document.getElementById('giorni-alla-scadenza').value = giorniAllaScadenza;
        
        // Risultati del calcolo
        document.getElementById('plusvalenza-netta').value = formatDecimal(simulazione.plusMinusValenza || 0);
        document.getElementById('interessi-netti').value = formatDecimal(simulazione.interessiNetti || 0);
        document.getElementById('commissioni').value = formatDecimal(simulazione.commissioni || 0);
        document.getElementById('imposta-bollo').value = formatDecimal(simulazione.impostaBollo || 0);
        document.getElementById('guadagno-totale').value = formatDecimal(simulazione.guadagnoTotale || 0);
        document.getElementById('guadagno-netto-commissioni').value = formatDecimal(simulazione.guadagnoNettoCommissioni || 0);
        document.getElementById('guadagno-netto-bollo').value = formatDecimal(simulazione.guadagnoNettoBollo || 0);
        document.getElementById('tasso').value = formatDecimal(simulazione.rendimentoLordo || 0) + '%';
        document.getElementById('tasso-netto-commissioni').value = formatDecimal(simulazione.rendimentoNettoCedole || 0) + '%';
        document.getElementById('tasso-netto-bollo').value = formatDecimal(simulazione.rendimentoNettoBollo || 0) + '%';
        document.getElementById('importo-scadenza').value = formatDecimal(simulazione.importoScadenza || 0);
    } catch (error) {
        console.error("Errore durante il popolamento dei campi:", error);
        alert("Si è verificato un errore durante il caricamento dei dati. Controlla la console per i dettagli.");
    }
}

// Carica i metadati dell'applicazione
function loadAppMetadata() {
    ApiService.getAppMetadata()
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

// Inizializzazione della pagina
document.addEventListener('DOMContentLoaded', function() {
    // Carica i metadati dell'applicazione
    loadAppMetadata();
    
    // Ottieni i parametri dall'URL
    const params = getUrlParams();
    
    // Carica i dettagli della simulazione
    caricaDettagliSimulazione(params.titoloId);
    
    // Aggiungi l'evento click al pulsante "Ricalcola"
    document.getElementById('btn-ricalcola').addEventListener('click', function() {
        // Ottieni l'ID della simulazione corrente
        const params = getUrlParams();
        const titoloId = params.titoloId;
        
        if (!titoloId) {
            alert('ID titolo non disponibile');
            return;
        }
        
        // Ricalcola i valori della simulazione direttamente
        ricalcolaValori(titoloId);
    });
});
