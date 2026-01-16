// Funzioni per l'applicazione Rendimento Titoli

// Utilizziamo le funzioni dai moduli di utility
// Validators.isValidNumber, Validators.parseNumericValue
// Formatters.formatDecimal, Formatters.formatDate
// DomUtils.toggleLoading, DomUtils.showAlert, DomUtils.initDatepicker, DomUtils.setDatepickerDate, DomUtils.updatePaginationControls

// Alias per le funzioni di utilità più usate
const isValidNumber = Validators.isValidNumber;
const parseNumericValue = Validators.parseNumericValue;
const formatDecimal = Formatters.formatDecimal;
const formatDate = Formatters.formatDate;

document.addEventListener('DOMContentLoaded', function() {
    // Imposta le opzioni globali per il datepicker
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
    
    // Inizializzazione
    initApp();
});

// Inizializzazione dell'applicazione
function initApp() {
    // Carica i metadati dell'applicazione
    loadAppMetadata();
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
