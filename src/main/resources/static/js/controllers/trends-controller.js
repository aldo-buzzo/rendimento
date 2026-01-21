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
        
        // Chiama l'API per recuperare i titoli filtrati
        ApiService.get(`${ApiService.baseUrl}/titolo/scadenza/${periodo}`)
            .then(titoli => {
                console.log(`Titoli caricati per periodo ${periodo}:`, titoli);
                
                // Filtra i titoli in base all'intorno di scadenza specificato
                const titoliInIntorno = filtraTitoliPerIntornoScadenza(titoli, periodo);
                
                // Aggiorna la tabella dei titoli
                updateTitoliTable(titoliInIntorno, sectionIndex);
                
                // Calcola e visualizza i rendimenti
                updateRendimentiStats(titoliInIntorno, sectionIndex);
                
                // Nascondi indicatore di caricamento
                DomUtils.toggleLoading(false);
            })
            .catch(error => {
                console.error(`Errore nel caricamento dei titoli per periodo ${periodo}:`, error);
                DomUtils.showAlert('Errore nel caricamento dei titoli', 'danger');
                DomUtils.toggleLoading(false);
            });
    }
    
    /**
     * Filtra i titoli in base all'intorno di scadenza specificato
     * 
     * @param {Array} titoli - Array di titoli
     * @param {string} periodo - Il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     * @returns {Array} - Array di titoli filtrati
     */
    function filtraTitoliPerIntornoScadenza(titoli, periodo) {
        if (periodo === 'tutti') {
            return titoli;
        }
        
        const oggi = new Date();
        let minMesi = 0;
        let maxMesi = 0;
        
        // Definisci l'intorno di scadenza in base al periodo
        switch (periodo) {
            case 'trimestrali':
                minMesi = 2;
                maxMesi = 3;
                break;
            case 'semestrali':
                minMesi = 5;
                maxMesi = 6;
                break;
            case 'annuali':
                minMesi = 11;
                maxMesi = 12;
                break;
            case 'triennali':
                minMesi = 30; // 2 anni e mezzo
                maxMesi = 36; // 3 anni
                break;
            default:
                return titoli;
        }
        
        // Calcola le date di scadenza minima e massima
        const dataMinima = new Date(oggi);
        dataMinima.setMonth(dataMinima.getMonth() + minMesi);
        
        const dataMassima = new Date(oggi);
        dataMassima.setMonth(dataMassima.getMonth() + maxMesi);
        
        // Filtra i titoli in base all'intorno di scadenza
        return titoli.filter(titolo => {
            const dataScadenza = new Date(titolo.dataScadenza);
            return dataScadenza >= dataMinima && dataScadenza <= dataMassima;
        });
    }
    
    /**
     * Aggiorna la tabella dei titoli
     * 
     * @param {Array} titoli - Array di titoli
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
            
            // Calcola i rendimenti del titolo
            const rendimentoTrimestrale = calcolaRendimentoTrimestrale(titolo);
            const rendimentoAnnuale = calcolaRendimentoAnnuale(titolo);
            
            row.innerHTML = `
                <td>${titolo.nome}</td>
                <td>${Formatters.formatDecimal(rendimentoTrimestrale)}%</td>
                <td>${Formatters.formatDecimal(rendimentoAnnuale)}%</td>
            `;
            
            titoliList.appendChild(row);
        });
    }
    
    /**
     * Calcola e visualizza i rendimenti minimi, medi e massimi
     * 
     * @param {Array} titoli - Array di titoli
     * @param {number} sectionIndex - L'indice della sezione (1, 2, 3)
     */
    function updateRendimentiStats(titoli, sectionIndex) {
        // Se non ci sono titoli, imposta i rendimenti a 0
        if (!titoli || titoli.length === 0) {
            document.getElementById(`rendimento-minimo-${sectionIndex}`).textContent = '0.00%';
            document.getElementById(`rendimento-medio-${sectionIndex}`).textContent = '0.00%';
            document.getElementById(`rendimento-massimo-${sectionIndex}`).textContent = '0.00%';
            return;
        }
        
        // Calcola i rendimenti annuali di tutti i titoli
        const rendimenti = titoli.map(titolo => calcolaRendimentoAnnuale(titolo));
        
        // Calcola il rendimento minimo
        const rendimentoMinimo = Math.min(...rendimenti);
        document.getElementById(`rendimento-minimo-${sectionIndex}`).textContent = Formatters.formatDecimal(rendimentoMinimo) + '%';
        
        // Calcola il rendimento medio
        const rendimentoMedio = rendimenti.reduce((a, b) => a + b, 0) / rendimenti.length;
        document.getElementById(`rendimento-medio-${sectionIndex}`).textContent = Formatters.formatDecimal(rendimentoMedio) + '%';
        
        // Calcola il rendimento massimo
        const rendimentoMassimo = Math.max(...rendimenti);
        document.getElementById(`rendimento-massimo-${sectionIndex}`).textContent = Formatters.formatDecimal(rendimentoMassimo) + '%';
    }
    
    /**
     * Calcola il rendimento trimestrale di un titolo (solo interessi e plusvalenze/minusvalenze, escluse commissioni e spese)
     * 
     * @param {Object} titolo - Il titolo
     * @returns {number} Il rendimento trimestrale
     */
    function calcolaRendimentoTrimestrale(titolo) {
        // Calcolo del rendimento trimestrale basato solo su interessi e plusvalenze/minusvalenze
        // In un'implementazione reale, questo calcolo sarebbe più complesso e terrebbe conto
        // di tutti i fattori che influenzano il rendimento
        
        // Rendimento base (tasso nominale)
        const tassoNominale = titolo.tassoNominale || 0;
        
        // Calcolo degli interessi trimestrali (25% degli interessi annuali)
        const interessiTrimestrali = tassoNominale * 0.25;
        
        // Calcolo della plusvalenza/minusvalenza (semplificato)
        // In un'implementazione reale, questo dipenderebbe dal prezzo di acquisto e dal prezzo attuale
        const plusvalenza = 0; // Semplificato per questo esempio
        
        // Rendimento trimestrale = interessi + plusvalenza (escluse commissioni e spese)
        return interessiTrimestrali + plusvalenza;
    }
    
    /**
     * Calcola il rendimento annuale di un titolo (solo interessi e plusvalenze/minusvalenze, escluse commissioni e spese)
     * 
     * @param {Object} titolo - Il titolo
     * @returns {number} Il rendimento annuale
     */
    function calcolaRendimentoAnnuale(titolo) {
        // Calcolo del rendimento annuale basato solo su interessi e plusvalenze/minusvalenze
        // In un'implementazione reale, questo calcolo sarebbe più complesso e terrebbe conto
        // di tutti i fattori che influenzano il rendimento
        
        // Rendimento base (tasso nominale)
        const tassoNominale = titolo.tassoNominale || 0;
        
        // Calcolo degli interessi annuali
        const interessiAnnuali = tassoNominale;
        
        // Calcolo della plusvalenza/minusvalenza (semplificato)
        // In un'implementazione reale, questo dipenderebbe dal prezzo di acquisto e dal prezzo attuale
        const plusvalenza = 0; // Semplificato per questo esempio
        
        // Rendimento annuale = interessi + plusvalenza (escluse commissioni e spese)
        return interessiAnnuali + plusvalenza;
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
