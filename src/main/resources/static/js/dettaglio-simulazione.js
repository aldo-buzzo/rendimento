// Script per la gestione della pagina di dettaglio simulazione

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
    document.body.classList.add('loading');
    
    // Chiamata API per recuperare i dettagli della simulazione
    fetch(`/api/simulazioni/titolo/${titoloId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Errore nel recupero dei dettagli della simulazione');
            }
            return response.json();
        })
        .then(simulazione => {
            console.log("Simulazione caricata:", simulazione);
            
            // Ora il titolo è incluso direttamente nella risposta dell'API
            const titolo = simulazione.titolo || {
                nome: "Titolo",
                codiceIsin: "",
                dataScadenza: "",
                tassoNominale: 0
            };
            
            console.log("Titolo incluso nella simulazione:", titolo);
            
            // Combina i dati della simulazione e del titolo
            return { simulazione, titolo };
        })
        .then(data => {
            // Popola i campi con i dati ricevuti
            popolaCampi(data.simulazione, data.titolo);
            
            // Carica anche tutte le simulazioni dello stesso titolo
            caricaStoricoSimulazioni(data.simulazione.idTitolo);
            
            // Nascondi l'indicatore di caricamento
            document.body.classList.remove('loading');
        })
        .catch(error => {
            console.error('Errore:', error);
            alert('Si è verificato un errore nel caricamento dei dettagli della simulazione');
            
            // Nascondi l'indicatore di caricamento
            document.body.classList.remove('loading');
            
            // Reindirizza alla pagina principale
            window.location.href = 'index.html';
        });
}

// Funzione per caricare tutte le simulazioni dello stesso titolo
function caricaStoricoSimulazioni(titoloId) {
    if (!titoloId) return;
    
    // Chiamata API per recuperare tutte le simulazioni del titolo
    fetch(`/api/simulazioni/titolo/${titoloId}/all`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Errore nel recupero delle simulazioni');
            }
            return response.json();
        })
        .then(simulazioni => {
            console.log("Simulazioni caricate:", simulazioni);
            
            // Popola la tabella con le simulazioni
            popolaTabellaSimulazioni(simulazioni);
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
        importoCell.textContent = formatDecimal(10000); // Valore fisso di 10.000 euro
        row.appendChild(importoCell);
        
        // Rendimento Lordo
        const rendimentoLordoCell = document.createElement('td');
        rendimentoLordoCell.textContent = formatPercentage(simulazione.rendimentoLordo * 100);
        row.appendChild(rendimentoLordoCell);
        
        // Rendimento Netto
        const rendimentoNettoCell = document.createElement('td');
        rendimentoNettoCell.textContent = formatPercentage(simulazione.rendimentoNettoCedole * 100);
        row.appendChild(rendimentoNettoCell);
        
        // Rendimento Netto Bollo
        const rendimentoNettoBolloCell = document.createElement('td');
        rendimentoNettoBolloCell.textContent = formatPercentage(simulazione.rendimentoNettoBollo);
        row.appendChild(rendimentoNettoBolloCell);
        
        // Colonna vuota per mantenere l'allineamento con l'intestazione
        const azioniCell = document.createElement('td');
        azioniCell.textContent = '-';
        row.appendChild(azioniCell);
        
        tbody.appendChild(row);
    });
}

// Funzione per ricalcolare i valori della simulazione
function ricalcolaValori(simulazioneId) {
    if (!simulazioneId) {
        console.error("ID simulazione non disponibile per il ricalcolo");
        return;
    }
    
    // Mostra un indicatore di caricamento
    document.body.classList.add('loading');
    
    // Chiamata API per ricalcolare i valori della simulazione
    fetch(`/api/simulazioni/${simulazioneId}/ricalcola`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Errore nel ricalcolo dei valori della simulazione');
            }
            return response.json();
        })
        .then(risultato => {
            console.log("Valori ricalcolati:", risultato);
            
            // Aggiorna i campi con i valori ricalcolati
            aggiornaRisultatiCalcolo(risultato);
            
            // Nascondi l'indicatore di caricamento
            document.body.classList.remove('loading');
            
            // Mostra un messaggio di successo
            alert('Valori ricalcolati con successo!');
        })
        .catch(error => {
            console.error('Errore:', error);
            alert('Si è verificato un errore nel ricalcolo dei valori');
            
            // Nascondi l'indicatore di caricamento
            document.body.classList.remove('loading');
        });
}

// Funzione per aggiornare i campi con i risultati del calcolo
function aggiornaRisultatiCalcolo(risultato) {
    // Aggiorna i campi con i valori ricalcolati
    document.getElementById('plusvalenza-netta').value = formatCurrency(risultato.plusvalenzaNetta);
    document.getElementById('interessi-netti').value = formatCurrency(risultato.interessiNetti);
    document.getElementById('commissioni').value = formatCurrency(risultato.commissioni);
    document.getElementById('imposta-bollo').value = formatCurrency(risultato.impostaBollo);
    document.getElementById('guadagno-totale').value = formatCurrency(risultato.guadagnoTotale);
    document.getElementById('guadagno-netto-commissioni').value = formatCurrency(risultato.guadagnoNettoCommissioni);
    document.getElementById('guadagno-netto-bollo').value = formatCurrency(risultato.guadagnoNettoBollo);
    document.getElementById('tasso').value = formatPercentage(risultato.tasso);
    document.getElementById('tasso-netto-commissioni').value = formatPercentage(risultato.tassoNettoCommissioni);
    document.getElementById('tasso-netto-bollo').value = formatPercentage(risultato.tassoNettoBollo);
    document.getElementById('importo-scadenza').value = formatCurrency(risultato.importoScadenza);
}

// Funzione per popolare i campi con i dati della simulazione
function popolaCampi(simulazione, titolo) {
    try {
        console.log("Popolamento campi con simulazione:", simulazione, "e titolo:", titolo);
        
        // Dati del titolo
        document.getElementById('titolo-nome').value = `${titolo.nome || simulazione.nomeTitolo || ""} (${titolo.codiceIsin || simulazione.codiceIsin || ""})`;
        document.getElementById('tasso-interesse').value = formatDecimal(titolo.tassoNominale || simulazione.tassoNominale || 0);
        
        // Dati della simulazione
        document.getElementById('prezzo-acquisto').value = formatDecimal(simulazione.prezzoAcquisto || 0);
        document.getElementById('importo-nominale').value = formatDecimal(10000); // Valore fisso di 10.000 euro
        document.getElementById('data-acquisto').value = formatDate(simulazione.dataAcquisto || new Date().toISOString());
        document.getElementById('commissioni-acquisto').value = formatDecimal((simulazione.commissioniAcquisto || 0) * 100); // Converti da decimale a percentuale
        document.getElementById('modalita-bollo').value = simulazione.modalitaBollo || 'ANNUALE'; // Valore di default
        
        // Calcola i giorni alla scadenza
        const dataScadenza = titolo.dataScadenza || simulazione.dataScadenza;
        const giorniAllaScadenza = dataScadenza ? calcolaGiorniAllaScadenza(simulazione.dataAcquisto, dataScadenza) : 0;
        document.getElementById('giorni-alla-scadenza').value = giorniAllaScadenza;
        
        // Importo fisso per i calcoli
        const importo = 10000; // Valore fisso di 10.000 euro
        
        // Risultati del calcolo
        const plusvalenzaNetta = simulazione.plusMinusValenza || 0;
        document.getElementById('plusvalenza-netta').value = formatCurrency(plusvalenzaNetta);
        
        // Calcola gli interessi netti
        const tassoDecimale = (titolo.tassoNominale || simulazione.tassoNominale || 0) / 100;
        const interessiNetti = importo * 0.875 * tassoDecimale * giorniAllaScadenza / 360;
        document.getElementById('interessi-netti').value = formatCurrency(interessiNetti);
        
        // Calcola le commissioni
        const commissioni = importo * (simulazione.commissioniAcquisto || 0);
        document.getElementById('commissioni').value = formatCurrency(commissioni);
        
        // Imposta bollo
        const impostaBollo = simulazione.impostaBollo || 0;
        document.getElementById('imposta-bollo').value = formatCurrency(impostaBollo);
        
        // Calcola il guadagno totale (plusvalenza + interessi)
        const guadagnoTotale = plusvalenzaNetta + interessiNetti;
        document.getElementById('guadagno-totale').value = formatCurrency(guadagnoTotale);
        
        // Calcola il guadagno netto commissioni (guadagno totale - commissioni)
        const guadagnoNettoCommissioni = guadagnoTotale - commissioni;
        document.getElementById('guadagno-netto-commissioni').value = formatCurrency(guadagnoNettoCommissioni);
        
        // Calcola il guadagno netto bollo (guadagno netto commissioni - imposta bollo)
        const guadagnoNettoBollo = guadagnoNettoCommissioni - impostaBollo;
        document.getElementById('guadagno-netto-bollo').value = formatCurrency(guadagnoNettoBollo);
        
        // Calcola il tasso (rendimento lordo)
        const rendimentoLordo = simulazione.rendimentoLordo || 0;
        document.getElementById('tasso').value = formatPercentage(rendimentoLordo * 100);
        
        // Calcola il tasso netto commissioni
        const rendimentoNettoCedole = simulazione.rendimentoNettoCedole || 0;
        document.getElementById('tasso-netto-commissioni').value = formatPercentage(rendimentoNettoCedole * 100);
        
        // Calcola il tasso netto bollo
        // Formula corretta: (guadagnoTotale - commissioni - impostaBollo) / importo * 100
        
        // Calcola il tasso al netto del bollo come percentuale dell'importo
        const tassoNettoBollo = (guadagnoNettoBollo / importo) * 100;
        document.getElementById('tasso-netto-bollo').value = formatPercentage(tassoNettoBollo);
        
        // Calcola l'importo a scadenza (importo + guadagno netto bollo)
        const importoScadenza = importo + guadagnoNettoBollo;
        document.getElementById('importo-scadenza').value = formatCurrency(importoScadenza);
    } catch (error) {
        console.error("Errore durante il popolamento dei campi:", error);
        alert("Si è verificato un errore durante il caricamento dei dati. Controlla la console per i dettagli.");
    }
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
        
        // Recupera la simulazione più recente per il titolo
        fetch(`/api/simulazioni/titolo/${titoloId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Errore nel recupero della simulazione');
                }
                return response.json();
            })
            .then(simulazione => {
                // Ricalcola i valori della simulazione
                ricalcolaValori(simulazione.idSimulazione);
            })
            .catch(error => {
                console.error('Errore:', error);
                alert('Si è verificato un errore nel recupero della simulazione');
            });
    });
});
