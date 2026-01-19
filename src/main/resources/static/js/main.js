// Funzioni per l'applicazione Rendimento Titoli

// Utilizziamo le funzioni dai moduli di utility
// Validators.isValidNumber, Validators.parseNumericValue
// Formatters.formatDecimal, Formatters.formatDate
// DomUtils.toggleLoading, DomUtils.showAlert, DomUtils.initDatepicker, DomUtils.setDatepickerDate, DomUtils.updatePaginationControls

// Utilizziamo i modelli per la gestione dei dati
// Titolo.load, Titolo.loadById, Titolo.loadByIsin, Titolo.save, Titolo.delete
// Simulazione.load, Simulazione.save, Simulazione.calcolaRendimento, Simulazione.calcolaRendimentiTuttiTitoli

// Utilizziamo il modulo Auth per la gestione dell'autenticazione
// Auth.isAuthenticated, Auth.login, Auth.register, Auth.logout, Auth.getCurrentUser, Auth.requireAuth, Auth.updateUI

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
    
    // Verifica se siamo su una pagina di autenticazione (login o registrazione)
    const currentPath = window.location.pathname;
    const isAuthPage = currentPath === '/login' || currentPath === '/login.html' || 
                       currentPath === '/registrazione' || currentPath === '/registrazione.html';
    
    if (isAuthPage) {
        // Se siamo su una pagina di autenticazione, non facciamo il controllo di autenticazione
        console.log('Pagina di autenticazione rilevata, inizializzazione specifica');
        // Eventuali inizializzazioni specifiche per le pagine di autenticazione
    } else {
        // Verifica autenticazione e inizializzazione per le altre pagine
        Auth.requireAuth().then(authenticated => {
            if (authenticated) {
                // Inizializzazione
                initApp();
            }
        });
    }
});

// Inizializzazione dell'applicazione
function initApp() {
    // Carica i metadati dell'applicazione
    loadAppMetadata();
    
    // Aggiorna l'interfaccia utente in base allo stato di autenticazione
    updateAuthUI();
}

// Aggiorna l'interfaccia utente in base allo stato di autenticazione
function updateAuthUI() {
    // Ottieni gli elementi dell'interfaccia utente
    const userInfoContainer = document.getElementById('user-info-container');
    const userInfo = document.getElementById('user-info');
    const logoutBtn = document.getElementById('logout-btn');
    
    // Se gli elementi non esistono, creali
    if (!userInfoContainer) {
        const header = document.querySelector('header') || document.body.firstElementChild;
        
        // Crea il container per le informazioni dell'utente
        const container = document.createElement('div');
        container.id = 'user-info-container';
        container.className = 'user-info-container';
        container.style.position = 'absolute';
        container.style.top = '10px';
        container.style.right = '10px';
        container.style.textAlign = 'right';
        
        // Crea l'elemento per le informazioni dell'utente
        const infoElement = document.createElement('div');
        infoElement.id = 'user-info';
        infoElement.className = 'user-info';
        infoElement.style.marginBottom = '5px';
        
        // Crea il pulsante di logout
        const logoutButton = document.createElement('button');
        logoutButton.id = 'logout-btn';
        logoutButton.className = 'btn btn-sm btn-outline-danger';
        logoutButton.textContent = 'Logout';
        logoutButton.addEventListener('click', function(event) {
            event.preventDefault();
            Auth.logout();
        });
        
        // Aggiungi gli elementi al container
        container.appendChild(infoElement);
        container.appendChild(logoutButton);
        
        // Aggiungi il container alla pagina
        header.appendChild(container);
    }
    
    // Aggiorna le informazioni dell'utente
    Auth.isAuthenticated().then(authenticated => {
        if (authenticated) {
            const user = Auth.getCurrentUser();
            if (user) {
                document.getElementById('user-info').textContent = `Benvenuto, ${user.username}`;
                document.getElementById('user-info').style.display = 'block';
                document.getElementById('logout-btn').style.display = 'block';
            }
        }
    });
}

// Carica i metadati dell'applicazione
function loadAppMetadata() {
    ApiService.getMetadata()
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
