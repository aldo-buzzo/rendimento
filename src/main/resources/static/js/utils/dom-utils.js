/**
 * Modulo per la manipolazione del DOM
 * Contiene funzioni di utilità per interagire con il DOM
 */

// Namespace per il modulo DomUtils
window.DomUtils = {
    /**
     * Mostra o nasconde l'indicatore di caricamento
     * @param {boolean} show - true per mostrare, false per nascondere
     */
    toggleLoading: function(show) {
        if (show) {
            document.body.classList.add('loading');
        } else {
            document.body.classList.remove('loading');
        }
    },

    /**
     * Mostra un messaggio di alert
     * @param {string} message - Il messaggio da mostrare
     * @param {string} type - Il tipo di alert (success, error, warning, info)
     */
    showAlert: function(message, type = 'info') {
        alert(message);
        // In futuro, questa funzione potrebbe essere migliorata per utilizzare
        // un componente di alert più sofisticato (es. Bootstrap Toast o Alert)
    },

    /**
     * Inizializza un datepicker con le opzioni italiane
     * @param {HTMLElement|string} element - L'elemento o il selettore dell'elemento
     * @param {Object} options - Opzioni aggiuntive per il datepicker
     */
    initDatepicker: function(element, options = {}) {
        // Se element è una stringa, lo consideriamo un selettore
        const el = typeof element === 'string' ? document.querySelector(element) : element;
        
        if (!el) {
            console.error('Elemento non trovato per il datepicker');
            return;
        }
        
        // Verifica che jQuery e il datepicker siano disponibili
        if (window.jQuery && $.fn && $.fn.datepicker) {
            try {
                // Opzioni di default per il datepicker italiano
                const defaultOptions = {
                    format: 'dd-mm-yyyy',
                    language: 'it',
                    autoclose: true,
                    todayHighlight: true
                };
                
                // Unisci le opzioni di default con quelle fornite
                const mergedOptions = { ...defaultOptions, ...options };
                
                // Inizializza il datepicker
                $(el).datepicker(mergedOptions);
                
                // Aggiungi un event listener per aggiornare l'attributo data-iso-date quando la data cambia
                $(el).on('changeDate', function(e) {
                    const selectedDate = e.date;
                    if (selectedDate) {
                        const isoDate = selectedDate.toISOString().split('T')[0];
                        el.setAttribute('data-iso-date', isoDate);
                    }
                });
            } catch (error) {
                console.warn('Errore nell\'inizializzazione del datepicker:', error);
            }
        } else {
            console.warn('Plugin datepicker non disponibile, inizializzazione standard');
            // Imposta l'input come tipo date per avere un fallback nativo
            el.type = 'date';
            
            // Aggiungi un event listener per aggiornare l'attributo data-iso-date quando la data cambia
            el.addEventListener('change', function() {
                if (el.value) {
                    el.setAttribute('data-iso-date', el.value);
                }
            });
        }
    },

    /**
     * Imposta la data corrente su un datepicker
     * @param {HTMLElement|string} element - L'elemento o il selettore dell'elemento
     * @param {Date} date - La data da impostare (default: data corrente)
     */
    setDatepickerDate: function(element, date = new Date()) {
        // Se element è una stringa, lo consideriamo un selettore
        const el = typeof element === 'string' ? document.querySelector(element) : element;
        
        if (!el) {
            console.error('Elemento non trovato per il datepicker');
            return;
        }
        
        // Verifica che jQuery e il datepicker siano disponibili
        if (window.jQuery && $.fn && $.fn.datepicker) {
            try {
                // Imposta la data
                $(el).datepicker('setDate', date);
            } catch (error) {
                console.warn('Errore nell\'impostazione del datepicker:', error);
                // Imposta la data in formato standard come fallback
                this.setDateFallback(el, date);
            }
        } else {
            console.warn('Plugin datepicker non disponibile, impostazione data standard');
            // Imposta la data in formato standard come fallback
            this.setDateFallback(el, date);
        }
        
        // Memorizza il formato ISO per l'invio al server
        const isoDate = date.toISOString().split('T')[0];
        el.setAttribute('data-iso-date', isoDate);
    },
    
    /**
     * Imposta la data in un campo input in formato standard (fallback)
     * @param {HTMLElement} element - L'elemento input
     * @param {Date} date - La data da impostare
     */
    setDateFallback: function(element, date) {
        if (!element || !date) return;
        
        // Formatta la data in formato dd-mm-yyyy
        const day = date.getDate().toString().padStart(2, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const year = date.getFullYear();
        element.value = `${day}-${month}-${year}`;
    },

    /**
     * Aggiorna i controlli di paginazione
     * @param {number} currentPage - La pagina corrente (0-based)
     * @param {number} totalPages - Il numero totale di pagine
     */
    updatePaginationControls: function(currentPage, totalPages) {
        const currentPageSpan = document.getElementById('current-page');
        const totalPagesSpan = document.getElementById('total-pages');
        const prevPageBtn = document.getElementById('prev-page-btn');
        const nextPageBtn = document.getElementById('next-page-btn');
        
        if (!currentPageSpan || !totalPagesSpan || !prevPageBtn || !nextPageBtn) {
            console.error('Elementi di paginazione non trovati');
            return;
        }
        
        // Aggiorna i numeri di pagina
        currentPageSpan.textContent = (currentPage + 1).toString();
        totalPagesSpan.textContent = totalPages.toString();
        
        // Abilita/disabilita i pulsanti di navigazione
        prevPageBtn.disabled = currentPage === 0;
        nextPageBtn.disabled = currentPage >= totalPages - 1;
    }
};

// Configurazione globale per il datepicker
document.addEventListener('DOMContentLoaded', function() {
    // Imposta le opzioni globali per il datepicker
    if ($.fn.datepicker) {
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
    }
});
