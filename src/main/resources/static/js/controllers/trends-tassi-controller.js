/**
 * Controller per la gestione della pagina trends-tassi.html
 * Gestisce la visualizzazione dei rendimenti e degli andamenti dei tassi per diversi periodi
 */

// Namespace per il controller
window.TrendsTassiController = (function() {
    // Stili CSS per il popup dei valori finali
    const valoriFinaliStyles = `
        .valori-finali-row {
            background-color: #f8f9fa;
            border-bottom: 1px solid #dee2e6;
        }
        .valori-finali-inline {
            padding: 0.5rem 1rem;
            font-size: 0.85rem;
        }
        .valori-finali-label {
            margin-right: 1rem;
        }
        .valori-finali-values {
            flex-grow: 1;
            text-align: right;
        }
        .valor-tipo {
            font-weight: 600;
            color: #495057;
        }
        .valor-value {
            font-weight: 500;
            color: #0d6efd;
        }
        
        /* Stili per il popup in stile modale */
        .modal-style-popup {
            position: fixed;
            z-index: 1060;
            background-color: white;
            border-radius: 0.3rem;
            box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.5);
            width: 90%;
            max-width: 600px;
            opacity: 0;
            transition: opacity 0.3s ease;
        }
        .modal-style-popup.popup-visible {
            opacity: 1;
        }
        .popup-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem;
            border-bottom: 1px solid #dee2e6;
            background-color: #f8f9fa;
            border-top-left-radius: 0.3rem;
            border-top-right-radius: 0.3rem;
        }
        .popup-header h5 {
            margin: 0;
            font-size: 1.1rem;
            font-weight: 500;
        }
        .close-button {
            background: none;
            border: none;
            font-size: 1.5rem;
            font-weight: 700;
            line-height: 1;
            color: #000;
            text-shadow: 0 1px 0 #fff;
            opacity: 0.5;
            cursor: pointer;
            padding: 0;
            margin-left: 1rem;
        }
        .close-button:hover {
            opacity: 0.75;
        }
        .popup-body {
            padding: 1rem;
            max-height: 70vh;
            overflow-y: auto;
        }
        .popup-table {
            width: 100%;
            margin-bottom: 0;
            border-collapse: collapse;
        }
        .popup-table th, .popup-table td {
            padding: 0.5rem;
            text-align: center;
            border: 1px solid #dee2e6;
        }
        .popup-table th {
            background-color: #f8f9fa;
            font-weight: 600;
            font-size: 0.9rem;
        }
        .popup-table td {
            font-size: 0.9rem;
        }
        .scenario-column {
            text-align: left !important;
            font-weight: 600;
            min-width: 180px; /* Larghezza minima per evitare sovrapposizioni */
            width: 30%;
        }
    `;
    // Periodi disponibili
    const periodi = ['trimestrali', 'semestrali', 'annuali', 'triennali', 'tutti'];
    
    // Riferimenti agli elementi DOM per i rendimenti
    const dataRendimentiElement = document.getElementById('data-rendimenti');
    
    /**
     * Inizializza il controller
     */
    function init() {
        console.log('Inizializzazione TrendsTassiController');
        
        // Imposta la data corrente nel campo data-rendimenti
        setDataRendimenti();
        
        // Aggiungi gli script per Chart.js se non sono già presenti
        if (!document.getElementById('chartjs-script')) {
            const script = document.createElement('script');
            script.id = 'chartjs-script';
            script.src = 'https://cdn.jsdelivr.net/npm/chart.js';
            script.onload = function() {
                console.log('Chart.js caricato con successo');
                // Carica i dati per tutti i periodi
                caricaDatiPerTuttiIPeriodi();
            };
            document.head.appendChild(script);
        } else {
            // Chart.js è già caricato, carica i dati per tutti i periodi
            caricaDatiPerTuttiIPeriodi();
        }
    }
    
    /**
     * Carica i dati per tutti i periodi
     */
    function caricaDatiPerTuttiIPeriodi() {
        // Mostra indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        // Carica i dati per ogni periodo
        let promesse = [];
        
        periodi.forEach(periodo => {
            // Carica i rendimenti
            promesse.push(
                loadRendimentiByPeriodo(periodo)
                    .then(trendData => {
                        // Aggiungi event listener per il click sulla riga dei rendimenti
                        const rendimentiRow = document.getElementById(`rendimenti-${periodo}`);
                        if (rendimentiRow) {
                            rendimentiRow.style.cursor = 'pointer';
                            rendimentiRow.addEventListener('click', function() {
                                showTitoliPopup(periodo);
                            });
                        }
                        return trendData;
                    })
                    .catch(error => {
                        console.error(`Errore nel caricamento dei rendimenti per periodo ${periodo}:`, error);
                        return null;
                    })
            );
            
            // Carica gli andamenti
            promesse.push(
                loadAndamentiTassi(periodo)
                    .catch(error => {
                        console.error(`Errore nel caricamento degli andamenti per periodo ${periodo}:`, error);
                        return null;
                    })
            );
        });
        
        // Quando tutte le promesse sono risolte, nascondi l'indicatore di caricamento
        Promise.all(promesse)
            .then(() => {
                DomUtils.toggleLoading(false);
            })
            .catch(error => {
                console.error('Errore nel caricamento dei dati:', error);
                DomUtils.toggleLoading(false);
                DomUtils.showAlert('Errore nel caricamento dei dati', 'danger');
            });
    }
    
    /**
     * Imposta la data corrente nel campo data-rendimenti
     */
    function setDataRendimenti() {
        const dataOggi = new Date();
        const dataFormattata = Formatters.formatDate(dataOggi);
        
        // Imposta la data nel campo
        if (dataRendimentiElement) {
            dataRendimentiElement.textContent = dataFormattata;
        }
    }
    
    /**
     * Carica i rendimenti per un periodo specifico
     * 
     * @param {string} periodo - Il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     * @returns {Promise} - Promise che si risolve con i dati dei rendimenti
     */
    function loadRendimentiByPeriodo(periodo) {
        console.log(`Caricamento rendimenti per periodo: ${periodo}`);
        
        // Chiama l'endpoint per recuperare i dati di trend dei rendimenti per periodo
        // Usa lo stesso endpoint che viene utilizzato in trends.html
        return ApiService.get(`${ApiService.baseUrl}/simulazioni/trends/${periodo}`)
            .then(trendData => {
                console.log(`Dati di trend caricati per periodo ${periodo}:`, trendData);
                
                // Aggiorna i rendimenti statistici
                updateRendimentiStatsFromTrendData(trendData, periodo);
                
                return trendData;
            });
    }
    
    /**
     * Aggiorna i rendimenti statistici dai dati di trend
     * 
     * @param {Object} trendData - Dati di trend dei rendimenti
     * @param {string} periodo - Il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     */
    function updateRendimentiStatsFromTrendData(trendData, periodo) {
        // Usa lo stesso formato di dati che viene utilizzato in trends.html
        let rendimentoMinimo = trendData.rendimentoMinimo || 0;
        let rendimentoMedio = trendData.rendimentoMedio || 0;
        let rendimentoMassimo = trendData.rendimentoMassimo || 0;
        
        // Imposta i rendimenti minimi, medi e massimi
        // Moltiplica per 100 perché i valori sono decimali (es. 0.05 per 5%)
        const minimoElement = document.getElementById(`rendimento-minimo-${periodo}`);
        const medioElement = document.getElementById(`rendimento-medio-${periodo}`);
        const massimoElement = document.getElementById(`rendimento-massimo-${periodo}`);
        
        if (minimoElement) minimoElement.textContent = Formatters.formatDecimal3(rendimentoMinimo * 100) + '%';
        if (medioElement) medioElement.textContent = Formatters.formatDecimal3(rendimentoMedio * 100) + '%';
        if (massimoElement) massimoElement.textContent = Formatters.formatDecimal3(rendimentoMassimo * 100) + '%';
    }
    
    /**
     * Carica gli andamenti dei tassi per un periodo specifico
     * 
     * @param {string} periodo - Il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     * @returns {Promise} - Promise che si risolve con i dati degli andamenti
     */
    function loadAndamentiTassi(periodo) {
        console.log(`Caricamento andamenti tassi per periodo: ${periodo}`);
        
        // Chiama l'endpoint per gli andamenti dei tassi
        return ApiService.get(`${ApiService.baseUrl}/trend/andamenti/${periodo}`)
            .then(andamentiData => {
                console.log(`Dati di andamenti caricati per periodo ${periodo}:`, andamentiData);
                
                // Visualizza gli andamenti dei tassi
                visualizzaAndamentiTassi(andamentiData, periodo);
                
                return andamentiData;
            });
    }
    
    /**
     * Mostra un popup con i titoli presi in considerazione per calcolare i rendimenti
     * 
     * @param {string} periodo - Il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     */
    function showTitoliPopup(periodo) {
        console.log(`Mostra popup titoli per periodo: ${periodo}`);
        
        // Mostra indicatore di caricamento
        DomUtils.toggleLoading(true);
        
        // Chiama l'endpoint esistente per recuperare i titoli per il periodo specificato
        ApiService.get(`${ApiService.baseUrl}/simulazioni/trends/${periodo}`)
            .then(data => {
                console.log(`Titoli caricati per periodo ${periodo}:`, data);
                
                // Verifica se ci sono titoli da visualizzare
                if (!data.titoli || data.titoli.length === 0) {
                    DomUtils.showAlert(`Nessun titolo trovato per il periodo ${periodo}`, 'info');
                    DomUtils.toggleLoading(false);
                    return;
                }
                
                // Crea il popup
                createTitoliPopup(data.titoli, periodo);
                
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
     * Crea un popup con i titoli
     * 
     * @param {Array} titoli - Array di titoli
     * @param {string} periodo - Il periodo di scadenza
     */
    function createTitoliPopup(titoli, periodo) {
        // Rimuovi eventuali popup esistenti
        const existingPopup = document.getElementById('titoli-popup');
        if (existingPopup) {
            existingPopup.remove();
        }
        
        // Aggiungi gli stili CSS se non sono già presenti
        if (!document.getElementById('valori-finali-styles')) {
            const style = document.createElement('style');
            style.id = 'valori-finali-styles';
            style.textContent = valoriFinaliStyles;
            document.head.appendChild(style);
        }
        
        // Crea il popup
        const popup = document.createElement('div');
        popup.id = 'titoli-popup';
        popup.className = 'modal-style-popup';
        
        // Crea l'intestazione del popup
        let contentHtml = `
            <div class="popup-header">
                <h5>Titoli ${periodo}</h5>
                <button type="button" class="close-button" aria-label="Chiudi">×</button>
            </div>
            <div class="popup-body">
                <table class="table table-sm popup-table">
                    <thead>
                        <tr>
                            <th>Nome</th>
                            <th>Rend. Bl.Ms.</th>
                            <th>Rend. Bl.Ann.</th>
                        </tr>
                    </thead>
                    <tbody>
        `;
        
        // Aggiungi i titoli alla tabella
        titoli.forEach(titolo => {
            contentHtml += `
                <tr>
                    <td>${titolo.nome || 'N/D'}</td>
                    <td>${Formatters.formatDecimal3((titolo.rendimentoBolloMensile || 0) * 100)}%</td>
                    <td>${Formatters.formatDecimal3((titolo.rendimentoBolloAnnuale || 0) * 100)}%</td>
                </tr>
            `;
        });
        
        // Chiudi la tabella e il popup
        contentHtml += `
                    </tbody>
                </table>
            </div>
        `;
        
        // Aggiungi il contenuto al popup
        popup.innerHTML = contentHtml;
        
        // Posiziona il popup al centro della pagina
        popup.style.position = 'fixed';
        popup.style.top = '50%';
        popup.style.left = '50%';
        popup.style.transform = 'translate(-50%, -50%)';
        
        // Aggiungi il popup al DOM
        document.body.appendChild(popup);
        
        // Aggiungi event listener per il pulsante di chiusura
        const closeButton = popup.querySelector('.close-button');
        if (closeButton) {
            closeButton.addEventListener('click', (e) => {
                e.stopPropagation();
                popup.remove();
            });
        }
        
        // Previeni la propagazione del click all'interno del popup
        popup.addEventListener('click', (e) => {
            e.stopPropagation();
        });
        
        // Aggiungi una classe per l'animazione di fade-in
        setTimeout(() => {
            popup.classList.add('popup-visible');
        }, 10);
        
        // Aggiungi event listener per chiudere il popup quando si clicca altrove
        document.addEventListener('click', function closePopupOnClickOutside(e) {
            if (popup && !popup.contains(e.target)) {
                popup.remove();
                document.removeEventListener('click', closePopupOnClickOutside);
            }
        });
    }
    
    /**
     * Visualizza gli andamenti dei tassi in un grafico
     * 
     * @param {Object} andamentiData - Dati degli andamenti dei tassi
     * @param {string} periodo - Il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     */
    function visualizzaAndamentiTassi(andamentiData, periodo) {
        // Verifica se Chart.js è stato caricato
        if (typeof Chart === 'undefined') {
            console.error('Chart.js non è stato caricato');
            return;
        }
        
        // Verifica se ci sono dati da visualizzare
        if (!andamentiData.labels || andamentiData.labels.length === 0) {
            const chartContainer = document.getElementById(`chart-container-${periodo}`);
            if (chartContainer) {
                chartContainer.innerHTML = '<div class="alert alert-info">Nessun dato disponibile per il periodo selezionato</div>';
            }
            return;
        }
        
        // Prepara il canvas per il grafico
        const canvas = document.getElementById(`chart-${periodo}`);
        if (!canvas) {
            console.error(`Canvas chart-${periodo} non trovato`);
            return;
        }
        
        // Distruggi il grafico esistente se presente
        if (window[`chart${periodo}`]) {
            window[`chart${periodo}`].destroy();
        }
        
        // Crea il nuovo grafico (solo con rendimenti medi)
        window[`chart${periodo}`] = new Chart(canvas, {
            type: 'line',
            data: {
                labels: andamentiData.labels,
                datasets: [
                    {
                        label: 'Rendimento Medio',
                        data: andamentiData.rendimentiMedi.map(val => val * 100), // Converti in percentuale
                        borderColor: 'rgba(54, 162, 235, 1)',
                        backgroundColor: 'rgba(54, 162, 235, 0.2)',
                        borderWidth: 2,
                        tension: 0.1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: false // Il titolo è già presente nell'HTML
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.dataset.label + ': ' + Formatters.formatDecimal3(context.raw) + '%';
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        ticks: {
                            callback: function(value) {
                                return Formatters.formatDecimal3(value) + '%';
                            }
                        },
                        title: {
                            display: true,
                            text: 'Rendimento (%)'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'Data'
                        }
                    }
                }
            }
        });
    }
    
    // API pubblica
    return {
        init: init
    };
})();

// Inizializza il controller quando il DOM è pronto
document.addEventListener('DOMContentLoaded', function() {
    // Verifica se siamo nella pagina trends-tassi.html
    if (window.location.pathname.includes('trends-tassi.html')) {
        // Verifica autenticazione e inizializzazione
        Auth.requireAuth().then(authenticated => {
            if (authenticated) {
                TrendsTassiController.init();
            }
        });
    }
});