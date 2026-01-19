/**
 * Script per collegare i pulsanti nella sezione "Rendimenti" alle stesse funzionalità
 * dei pulsanti nella sezione "Lista Titoli"
 */

document.addEventListener('DOMContentLoaded', function() {
    // Verifica che i pulsanti esistano
    const addTitoloBtn = document.getElementById('add-titolo-btn-rendimenti');
    const listaBtpBtn = document.getElementById('lista-btp-btn-rendimenti');
    const listaBotBtn = document.getElementById('lista-bot-btn-rendimenti');
    const calcolaRendimentiBtn = document.getElementById('calcola-rendimenti-btn-rendimenti');
    
    // Aggiungi event listener per il pulsante "Aggiungi Titolo"
    if (addTitoloBtn) {
        addTitoloBtn.addEventListener('click', function() {
            // Chiama la stessa funzione del pulsante originale
            TitoliController.showTitoloModal();
        });
    }
    
    // Aggiungi event listener per il pulsante "Lista BTP"
    if (listaBtpBtn) {
        listaBtpBtn.addEventListener('click', function() {
            // Chiama la stessa funzione del pulsante originale
            TitoliController.showListaTitoli('BTP');
        });
    }
    
    // Aggiungi event listener per il pulsante "Lista BOT"
    if (listaBotBtn) {
        listaBotBtn.addEventListener('click', function() {
            // Chiama la stessa funzione del pulsante originale
            TitoliController.showListaTitoli('BOT');
        });
    }
    
    // Aggiungi event listener per il pulsante "Calcola Rendimenti"
    if (calcolaRendimentiBtn) {
        calcolaRendimentiBtn.addEventListener('click', function() {
            // Verifica che simulazioniController esista
            if (window.simulazioniController) {
                // Chiama la stessa funzione del pulsante originale
                window.simulazioniController.calcolaRendimentiTuttiTitoli();
            } else {
                console.error('simulazioniController non è definito');
                alert('Si è verificato un errore. Ricarica la pagina e riprova.');
            }
        });
    }
});
