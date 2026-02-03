/**
 * Controller per la gestione della pagina trends.html
 * Gestisce le select per filtrare i titoli per scadenza e visualizza i rendimenti
 */

// Namespace per il controller
window.TrendsController = (function() {
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
            
            // Aggiungi attributi data per il titolo
            row.setAttribute('data-titolo-id', titolo.idTitolo);
            // Non abbiamo un simulazioneId in TitoloRendimentoDTO
            row.setAttribute('data-simulazione-id', '');
            
            // Aggiungi classe per indicare che la riga è cliccabile
            row.classList.add('titolo-row');
            
            row.innerHTML = `
                <td>${titolo.nome}</td>
                <td>${Formatters.formatDecimal3(titolo.rendimentoBolloMensile * 100)}%</td>
                <td>${Formatters.formatDecimal3(titolo.rendimentoBolloAnnuale * 100)}%</td>
            `;
            
            // Aggiungi event listener per il doppio click
            row.addEventListener('dblclick', function() {
                const titoloId = this.getAttribute('data-titolo-id');
                const simulazioneId = this.getAttribute('data-simulazione-id');
                if (titoloId) {
                    showRendimentoInfo(titoloId, simulazioneId);
                }
            });
            
            titoliList.appendChild(row);
        });
    }
    
    /**
     * Mostra le informazioni dettagliate sul rendimento
     * @param {number} titoloId - ID del titolo
     * @param {number} simulazioneId - ID della simulazione
     */
    function showRendimentoInfo(titoloId, simulazioneId) {
        // Invece di reindirizzare, otteniamo i dati dettagliati e mostriamo il popup
        if (simulazioneId) {
            // Se abbiamo un ID simulazione, otteniamo i dati dettagliati direttamente
            fetchCalcoloDettagliato(simulazioneId);
        } else if (titoloId) {
            // Se abbiamo solo l'ID del titolo, otteniamo prima la simulazione più recente
            fetchLatestSimulazione(titoloId);
        }
    }
    
    /**
     * Ottiene i dati dettagliati di calcolo per una simulazione
     * @param {number} simulazioneId - ID della simulazione
     */
    function fetchCalcoloDettagliato(simulazioneId) {
        DomUtils.toggleLoading(true);
        
        ApiService.get(`${ApiService.baseUrl}/simulazioni/${simulazioneId}/calcolo-dettagliato`)
            .then(data => {
                DomUtils.toggleLoading(false);
                showPopupTable(data, simulazioneId);
            })
            .catch(error => {
                console.error('Errore nel recupero dei dati dettagliati:', error);
                DomUtils.toggleLoading(false);
                DomUtils.showAlert('Errore nel recupero dei dati dettagliati', 'danger');
            });
    }
    
    /**
     * Ottiene la simulazione più recente per un titolo
     * @param {number} titoloId - ID del titolo
     */
    function fetchLatestSimulazione(titoloId) {
        DomUtils.toggleLoading(true);
        
        ApiService.get(`${ApiService.baseUrl}/simulazioni/titolo/${titoloId}`)
            .then(simulazione => {
                DomUtils.toggleLoading(false);
                if (simulazione && simulazione.idSimulazione) {
                    fetchCalcoloDettagliato(simulazione.idSimulazione);
                } else {
                    DomUtils.showAlert('Nessuna simulazione trovata per questo titolo', 'warning');
                }
            })
            .catch(error => {
                console.error('Errore nel recupero della simulazione:', error);
                DomUtils.toggleLoading(false);
                DomUtils.showAlert('Errore nel recupero della simulazione', 'danger');
            });
    }
    
    /**
     * Mostra il popup con i dettagli dei valori finali
     * @param {Object} data - Dati dettagliati del calcolo
     * @param {number} simulazioneId - ID della simulazione
     */
    function showPopupTable(data, simulazioneId) {
        // Rimuovi eventuali popup esistenti
        const existingPopup = document.getElementById('valori-popup');
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
        
        // Recupera i valori necessari dai dati
        const valoreBolloAnnualePlusvalenzaNonEsente = data.valoreBolloAnnualePlusvalenzaNonEsente || 0;
        const valoreBolloMensilePlusvalenzaNonEsente = data.valoreBolloMensilePlusvalenzaNonEsente || 0;
        const valoreBolloAnnualePlusvalenzaEsente = data.valoreBolloAnnualePlusvalenzaEsente || 0;
        const valoreBolloMensilePlusvalenzaEsente = data.valoreBolloMensilePlusvalenzaEsente || 0;
        
        // Otteniamo i dati del titolo dalla simulazione
        ApiService.get(`${ApiService.baseUrl}/simulazioni/${simulazioneId}`)
            .then(simulazione => {
                let tipoTitolo = '';
                let titoloNome = '';
                let titoloIsin = '';
                
                if (simulazione && simulazione.titolo) {
                    tipoTitolo = simulazione.titolo.tipoTitolo || '';
                    titoloNome = simulazione.titolo.nome || '';
                    titoloIsin = simulazione.titolo.codiceIsin || '';
                }
                
                // Ora che abbiamo tutti i dati, creiamo il popup
                createPopup(tipoTitolo, titoloNome, titoloIsin);
            })
            .catch(error => {
                console.error('Errore nel recupero della simulazione:', error);
                // Se c'è un errore, usiamo i valori di default
                createPopup('', '', '');
            });
            
        /**
         * Crea il popup con i dati recuperati
         * @param {string} tipoTitolo - Tipo di titolo (BTP, BOT, ecc.)
         * @param {string} titoloNome - Nome del titolo
         * @param {string} titoloIsin - Codice ISIN del titolo
         */
        function createPopup(tipoTitolo, titoloNome, titoloIsin) {
            // Crea il popup
            const popup = document.createElement('div');
            popup.id = 'valori-popup';
            popup.className = 'modal-style-popup';
        
            // Crea il contenuto HTML con pulsante di chiusura
            let contentHtml = `
                <div class="popup-header" id="valori-popup-header">
                    <h5>Dettaglio Valori Finali - ${titoloNome} (${titoloIsin})</h5>
                    <button type="button" class="close-button" aria-label="Chiudi">×</button>
                </div>
                <div class="popup-body">
                    <table class="table table-sm popup-table">
                        <thead>
                            <tr>
                                <th class="scenario-column">Scenario</th>
                                <th>10.000€</th>
                                <th>30.000€</th>
                                <th>50.000€</th>
                                <th>100.000€</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td class="scenario-column">Bollo Annuo Plusv Tassata</td>
                                <td>${Formatters.formatDecimal(valoreBolloAnnualePlusvalenzaNonEsente)}€</td>
                                <td>${Formatters.formatDecimal(valoreBolloAnnualePlusvalenzaNonEsente * 3)}€</td>
                                <td>${Formatters.formatDecimal(valoreBolloAnnualePlusvalenzaNonEsente * 5)}€</td>
                                <td>${Formatters.formatDecimal(valoreBolloAnnualePlusvalenzaNonEsente * 10)}€</td>
                            </tr>
                            <tr>
                                <td class="scenario-column">Bollo Mese Plusv. Tassata</td>
                                <td>${Formatters.formatDecimal(valoreBolloMensilePlusvalenzaNonEsente)}€</td>
                                <td>${Formatters.formatDecimal(valoreBolloMensilePlusvalenzaNonEsente * 3)}€</td>
                                <td>${Formatters.formatDecimal(valoreBolloMensilePlusvalenzaNonEsente * 5)}€</td>
                                <td>${Formatters.formatDecimal(valoreBolloMensilePlusvalenzaNonEsente * 10)}€</td>
                            </tr>`;
                            
            if (tipoTitolo === 'BTP') {
                contentHtml += `
                    <tr>
                        <td class="scenario-column">Bollo Annuo Plusv Esente</td>
                        <td>${Formatters.formatDecimal(valoreBolloAnnualePlusvalenzaEsente)}€</td>
                        <td>${Formatters.formatDecimal(valoreBolloAnnualePlusvalenzaEsente * 3)}€</td>
                        <td>${Formatters.formatDecimal(valoreBolloAnnualePlusvalenzaEsente * 5)}€</td>
                        <td>${Formatters.formatDecimal(valoreBolloAnnualePlusvalenzaEsente * 10)}€</td>
                    </tr>
                    <tr>
                        <td class="scenario-column">Bollo Mese Plusv. Esente</td>
                        <td>${Formatters.formatDecimal(valoreBolloMensilePlusvalenzaEsente)}€</td>
                        <td>${Formatters.formatDecimal(valoreBolloMensilePlusvalenzaEsente * 3)}€</td>
                        <td>${Formatters.formatDecimal(valoreBolloMensilePlusvalenzaEsente * 5)}€</td>
                        <td>${Formatters.formatDecimal(valoreBolloMensilePlusvalenzaEsente * 10)}€</td>
                    </tr>`;
            } else {
                contentHtml += `
                    <tr>
                        <td class="scenario-column">Bollo Annuo Plusv Esente</td>
                        <td>N/A</td>
                        <td>N/A</td>
                        <td>N/A</td>
                        <td>N/A</td>
                    </tr>
                    <tr>
                        <td class="scenario-column">Bollo Mese Plusv. Esente</td>
                        <td>N/A</td>
                        <td>N/A</td>
                        <td>N/A</td>
                        <td>N/A</td>
                    </tr>`;
            }
            
            contentHtml += `
                        </tbody>
                    </table>
                </div>
            `;
            
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
