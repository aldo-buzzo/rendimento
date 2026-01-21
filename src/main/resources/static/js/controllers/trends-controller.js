/**
 * Controller per la gestione della pagina trends.html
 * Gestisce le select per filtrare i titoli per scadenza e visualizza i rendimenti
 */

// Namespace per il controller
window.TrendsController = (function() {
    // Riferimenti agli elementi DOM
    const periodoSelect1 = document.getElementById('periodo-select-1');
    const periodoSelect2 = document.getElementById('periodo-select-2');
    const periodoSelect3 = document.getElementById('periodo-select-3');
    
    // Riferimenti agli elementi per i rendimenti
    const rendimentoMinimo1 = document.getElementById('rendimento-minimo-1');
    const rendimentoMedio1 = document.getElementById('rendimento-medio-1');
    const rendimentoMassimo1 = document.getElementById('rendimento-massimo-1');
    
    const rendimentoMinimo2 = document.getElementById('rendimento-minimo-2');
    const rendimentoMedio2 = document.getElementById('rendimento-medio-2');
    const rendimentoMassimo2 = document.getElementById('rendimento-massimo-2');
    
    const rendimentoMinimo3 = document.getElementById('rendimento-minimo-3');
    const rendimentoMedio3 = document.getElementById('rendimento-medio-3');
    const rendimentoMassimo3 = document.getElementById('rendimento-massimo-3');
    
    // Riferimenti alle tabelle dei titoli
    const titoliList1 = document.getElementById('titoli-list-1');
    const titoliList2 = document.getElementById('titoli-list-2');
    const titoliList3 = document.getElementById('titoli-list-3');
    
    /**
     * Inizializza il controller
     */
    function init() {
        console.log('Inizializzazione TrendsController');
        
        // Imposta la data corrente nei campi data-rendimenti
        setDataRendimenti();
        
        // Aggiungi event listener alle select
        if (periodoSelect1) {
            periodoSelect1.addEventListener('change', function() {
                loadTitoliByPeriodo(periodoSelect1.value, 1);
            });
        }
        
        if (periodoSelect2) {
            periodoSelect2.addEventListener('change', function() {
                loadTitoliByPeriodo(periodoSelect2.value, 2);
            });
        }
        
        if (periodoSelect3) {
            periodoSelect3.addEventListener('change', function() {
                loadTitoliByPeriodo(periodoSelect3.value, 3);
            });
        }
        
        // Carica i dati iniziali
        loadTitoliByPeriodo('semestrali', 1);
        loadTitoliByPeriodo('annuali', 2);
        loadTitoliByPeriodo('tutti', 3);
    }
    
    /**
     * Imposta la data corrente nei campi data-rendimenti
     */
    function setDataRendimenti() {
        const dataOggi = new Date();
        const dataFormattata = Formatters.formatDate(dataOggi);
        
        // Imposta la data nei tre campi
        document.getElementById('data-rendimenti-1').textContent = dataFormattata;
        document.getElementById('data-rendimenti-2').textContent = dataFormattata;
        document.getElementById('data-rendimenti-3').textContent = dataFormattata;
    }
    
    /**
     * Carica i titoli filtrati per periodo
     * 
     * @param {string} periodo - Il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     * @param {number} sectionIndex - L'indice della sezione (1, 2, 3)
     */
    function loadTitoliByPeriodo(periodo, sectionIndex) {
        console.log(`Caricamento titoli per periodo: ${periodo}, sezione: ${sectionIndex}`);
        
        // Mostra indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        // Chiama l'API per recuperare i dati di trend dei rendimenti
        ApiService.get(`${ApiService.baseUrl}/simulazioni/trends/${periodo}`)
            .then(trendData => {
                console.log(`Dati di trend caricati per periodo ${periodo}:`, trendData);
                
                // Aggiorna la tabella dei titoli
                updateTitoliTable(trendData.titoli, sectionIndex);
                
                // Aggiorna i rendimenti statistici
                updateRendimentiStatsFromTrendData(trendData, sectionIndex);
                
                // Nascondi indicatore di caricamento
                DomUtils.toggleLoading(false);
            })
            .catch(error => {
                console.error(`Errore nel caricamento dei dati di trend per periodo ${periodo}:`, error);
                DomUtils.showAlert('Errore nel caricamento dei dati di trend', 'danger');
                DomUtils.toggleLoading(false);
            });
    }
    
    /**
     * Aggiorna i rendimenti statistici dai dati di trend
     * 
     * @param {Object} trendData - Dati di trend dei rendimenti
     * @param {number} sectionIndex - L'indice della sezione (1, 2, 3)
     */
    function updateRendimentiStatsFromTrendData(trendData, sectionIndex) {
        // Imposta i rendimenti minimi, medi e massimi
        // Moltiplica per 100 perché i valori sono decimali (es. 0.05 per 5%)
        document.getElementById(`rendimento-minimo-${sectionIndex}`).textContent = Formatters.formatDecimal3(trendData.rendimentoMinimo * 100) + '%';
        document.getElementById(`rendimento-medio-${sectionIndex}`).textContent = Formatters.formatDecimal3(trendData.rendimentoMedio * 100) + '%';
        document.getElementById(`rendimento-massimo-${sectionIndex}`).textContent = Formatters.formatDecimal3(trendData.rendimentoMassimo * 100) + '%';
    }
    
    /**
     * Aggiorna la tabella dei titoli
     * 
     * @param {Array} titoli - Array di titoli con rendimenti
     * @param {number} sectionIndex - L'indice della sezione (1, 2, 3)
     */
    function updateTitoliTable(titoli, sectionIndex) {
        const titoliList = document.getElementById(`titoli-list-${sectionIndex}`);
        
        if (!titoliList) {
            console.error(`Elemento titoliList-${sectionIndex} non trovato`);
            return;
        }
        
        // Svuota la tabella
        titoliList.innerHTML = '';
        
        // Se non ci sono titoli, mostra un messaggio
        if (!titoli || titoli.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = '<td colspan="4" class="text-center">Nessun titolo trovato</td>';
            titoliList.appendChild(row);
            return;
        }
        
        // Aggiungi i titoli alla tabella
        titoli.forEach(titolo => {
            const row = document.createElement('tr');
            
            row.innerHTML = `
                <td>${titolo.nome}</td>
                <td>${Formatters.formatDecimal3(titolo.rendimentoBolloMensile * 100)}%</td>
                <td>${Formatters.formatDecimal3(titolo.rendimentoBolloAnnuale * 100)}%</td>
            `;
            
            titoliList.appendChild(row);
        });
    }
    
    
    // API pubblica
    return {
        init: init
    };
})();

// Inizializza il controller quando il DOM è pronto
document.addEventListener('DOMContentLoaded', function() {
    // Verifica se siamo nella pagina trends.html
    if (window.location.pathname.includes('trends.html')) {
        // Verifica autenticazione e inizializzazione
        Auth.requireAuth().then(authenticated => {
            if (authenticated) {
                TrendsController.init();
            }
        });
    }
});
