/**
 * Script per inizializzare i controller dell'applicazione
 * Questo script deve essere caricato dopo la definizione dei controller
 * ma prima del loro utilizzo
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Inizializzazione dei controller...');
    
    // Inizializza il controller delle simulazioni
    if (typeof SimulazioniController !== 'undefined') {
        console.log('Istanziazione di SimulazioniController...');
        window.simulazioniController = new SimulazioniController();
        console.log('SimulazioniController istanziato con successo');
    } else {
        console.error('SimulazioniController non è definito');
    }
});