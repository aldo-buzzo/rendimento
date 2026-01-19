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
     * @param {boolean} validateFutureDate - Se true, valida che la data sia nel futuro
     */
    initDatepicker: function(element, options = {}, validateFutureDate = false) {
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
                    todayHighlight: true,
                    startDate: validateFutureDate ? new Date() : undefined // Se richiesto, imposta la data minima a oggi
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
                        
                        // Validazione aggiuntiva per data futura se richiesto
                        if (validateFutureDate) {
                            const today = new Date();
                            today.setHours(0, 0, 0, 0);
                            
                            if (selectedDate < today) {
                                console.warn('La data selezionata è nel passato');
                                el.classList.add('is-invalid');
                                // Aggiungi un messaggio di errore se non esiste già
                                if (!el.nextElementSibling || !el.nextElementSibling.classList.contains('invalid-feedback')) {
                                    const feedbackDiv = document.createElement('div');
                                    feedbackDiv.className = 'invalid-feedback';
                                    feedbackDiv.textContent = 'La data deve essere nel futuro';
                                    el.parentNode.insertBefore(feedbackDiv, el.nextSibling);
                                }
                            } else {
                                el.classList.remove('is-invalid');
                                // Rimuovi il messaggio di errore se esiste
                                if (el.nextElementSibling && el.nextElementSibling.classList.contains('invalid-feedback')) {
                                    el.nextElementSibling.remove();
                                }
                            }
                        }
                    }
                });
                
                // Aggiungi anche un event listener per l'input manuale
                el.addEventListener('change', function() {
                    if (el.value) {
                        // Converti la data dal formato dd-mm-yyyy a un oggetto Date
                        const parts = el.value.split('-');
                        if (parts.length === 3) {
                            const day = parseInt(parts[0], 10);
                            const month = parseInt(parts[1], 10) - 1; // I mesi in JavaScript sono 0-based
                            const year = parseInt(parts[2], 10);
                            
                            if (!isNaN(day) && !isNaN(month) && !isNaN(year)) {
                                const date = new Date(year, month, day);
                                const isoDate = date.toISOString().split('T')[0];
                                el.setAttribute('data-iso-date', isoDate);
                                
                                // Validazione aggiuntiva per data futura se richiesto
                                if (validateFutureDate) {
                                    const today = new Date();
                                    today.setHours(0, 0, 0, 0);
                                    
                                    if (date < today) {
                                        console.warn('La data inserita è nel passato');
                                        el.classList.add('is-invalid');
                                        // Aggiungi un messaggio di errore se non esiste già
                                        if (!el.nextElementSibling || !el.nextElementSibling.classList.contains('invalid-feedback')) {
                                            const feedbackDiv = document.createElement('div');
                                            feedbackDiv.className = 'invalid-feedback';
                                            feedbackDiv.textContent = 'La data deve essere nel futuro';
                                            el.parentNode.insertBefore(feedbackDiv, el.nextSibling);
                                        }
                                    } else {
                                        el.classList.remove('is-invalid');
                                        // Rimuovi il messaggio di errore se esiste
                                        if (el.nextElementSibling && el.nextElementSibling.classList.contains('invalid-feedback')) {
                                            el.nextElementSibling.remove();
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            } catch (error) {
                console.warn('Errore nell\'inizializzazione del datepicker:', error);
            }
        } else {
            console.warn('Plugin datepicker non disponibile, inizializzazione standard');
            // Imposta l'input come tipo date per avere un fallback nativo
            el.type = 'date';
            
            // Se richiesto, imposta la data minima a oggi
            if (validateFutureDate) {
                const today = new Date().toISOString().split('T')[0];
                el.min = today;
            }
            
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
     * @param {boolean} validateFutureDate - Se true, valida che la data sia nel futuro
     */
    setDatepickerDate: function(element, date = new Date(), validateFutureDate = false) {
        // Se element è una stringa, lo consideriamo un selettore
        const el = typeof element === 'string' ? document.querySelector(element) : element;
        
        if (!el) {
            console.error('Elemento non trovato per il datepicker');
            return;
        }
        
        // Se richiesto, valida che la data sia nel futuro
        if (validateFutureDate) {
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            
            if (date < today) {
                console.warn('La data impostata è nel passato, utilizzo la data corrente');
                date = new Date();
            }
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
        
        // Validazione aggiuntiva per data futura se richiesto
        if (validateFutureDate) {
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            
            if (date < today) {
                el.classList.add('is-invalid');
                // Aggiungi un messaggio di errore se non esiste già
                if (!el.nextElementSibling || !el.nextElementSibling.classList.contains('invalid-feedback')) {
                    const feedbackDiv = document.createElement('div');
                    feedbackDiv.className = 'invalid-feedback';
                    feedbackDiv.textContent = 'La data deve essere nel futuro';
                    el.parentNode.insertBefore(feedbackDiv, el.nextSibling);
                }
            } else {
                el.classList.remove('is-invalid');
                // Rimuovi il messaggio di errore se esiste
                if (el.nextElementSibling && el.nextElementSibling.classList.contains('invalid-feedback')) {
                    el.nextElementSibling.remove();
                }
            }
        }
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
    },
    
    /**
     * Valida una data di scadenza
     * @param {string} dateString - La data in formato ISO (YYYY-MM-DD)
     * @returns {Object} - Oggetto con proprietà isValid e message
     */
    validateExpirationDate: function(dateString) {
        if (!dateString) {
            return {
                isValid: false,
                message: 'La data di scadenza è obbligatoria'
            };
        }
        
        // Verifica che la data sia in formato valido
        const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
        if (!dateRegex.test(dateString)) {
            return {
                isValid: false,
                message: 'Formato data non valido. Utilizzare il formato YYYY-MM-DD'
            };
        }
        
        // Converti la stringa in un oggetto Date
        const date = new Date(dateString);
        
        // Verifica che la data sia valida
        if (isNaN(date.getTime())) {
            return {
                isValid: false,
                message: 'Data non valida'
            };
        }
        
        // Verifica che la data sia nel futuro
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        if (date < today) {
            return {
                isValid: false,
                message: 'La data di scadenza deve essere nel futuro'
            };
        }
        
        return {
            isValid: true,
            message: 'Data valida'
        };
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
