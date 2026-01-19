/**
 * Modulo per la gestione dell'autenticazione
 */
const Auth = (function() {
    // Variabili private
    let currentUser = null;
    
    /**
     * Verifica se l'utente è autenticato
     * @returns {Promise<boolean>} Promise che restituisce true se l'utente è autenticato, false altrimenti
     */
    function isAuthenticated() {
        return ApiService.get('/api/auth/utente-corrente')
            .then(user => {
                currentUser = user;
                return true;
            })
            .catch(() => {
                currentUser = null;
                return false;
            });
    }
    
    /**
     * Effettua il login
     * @param {string} username - Username dell'utente
     * @param {string} password - Password dell'utente
     * @returns {Promise} Promise che restituisce l'utente autenticato
     */
    function login(username, password) {
        // Nota: il login viene gestito direttamente dal form HTML
        // che invia i dati a Spring Security
        console.warn('La funzione login() è deprecata. Utilizzare il form di login standard.');
        
        // Verifica se siamo già sulla pagina di login
        const currentPath = window.location.pathname;
        if (currentPath !== '/login' && currentPath !== '/login.html') {
            // Reindirizza alla pagina di login solo se non siamo già su di essa
            window.location.href = '/login';
        }
        
        // Restituisce una promise che si risolve con null
        return Promise.resolve(null);
    }
    
    /**
     * Effettua la registrazione
     * @param {Object} utente - Dati dell'utente da registrare
     * @returns {Promise} Promise che restituisce l'utente registrato
     */
    function register(utente) {
        return ApiService.post('/api/auth/registrazione', utente);
    }
    
    /**
     * Effettua il logout
     * @returns {Promise} Promise che si risolve quando il logout è completato
     */
    function logout() {
        return fetch('/api/auth/logout', {
            method: 'POST'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Errore durante il logout');
            }
            currentUser = null;
            window.location.href = '/login?logout=true';
        });
    }
    
    /**
     * Ottiene l'utente corrente
     * @returns {Object|null} L'utente corrente o null se non autenticato
     */
    function getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Verifica se l'utente è autenticato e reindirizza alla pagina di login se non lo è
     * @returns {Promise} Promise che si risolve quando la verifica è completata
     */
    function requireAuth() {
        // Evita reindirizzamenti infiniti se siamo già sulla pagina di login o registrazione
        const currentPath = window.location.pathname;
        if (currentPath === '/login' || currentPath === '/login.html' || 
            currentPath === '/registrazione' || currentPath === '/registrazione.html') {
            return Promise.resolve(false);
        }
        
        return isAuthenticated()
            .then(authenticated => {
                if (!authenticated) {
                    window.location.href = '/login';
                    return false;
                }
                return true;
            });
    }
    
    /**
     * Aggiorna l'interfaccia utente in base allo stato di autenticazione
     */
    function updateUI() {
        const loginBtn = document.getElementById('login-btn');
        const logoutBtn = document.getElementById('logout-btn');
        const userInfo = document.getElementById('user-info');
        
        isAuthenticated()
            .then(authenticated => {
                if (authenticated && currentUser) {
                    if (loginBtn) loginBtn.style.display = 'none';
                    if (logoutBtn) logoutBtn.style.display = 'block';
                    if (userInfo) {
                        userInfo.style.display = 'block';
                        userInfo.textContent = `Benvenuto, ${currentUser.username}`;
                    }
                } else {
                    if (loginBtn) loginBtn.style.display = 'block';
                    if (logoutBtn) logoutBtn.style.display = 'none';
                    if (userInfo) userInfo.style.display = 'none';
                }
            });
    }
    
    // API pubblica
    return {
        isAuthenticated,
        login,
        register,
        logout,
        getCurrentUser,
        requireAuth,
        updateUI
    };
})();

// Quando il documento è pronto, aggiorna l'interfaccia utente
document.addEventListener('DOMContentLoaded', function() {
    // Aggiorna l'interfaccia utente in base allo stato di autenticazione
    Auth.updateUI();
    
    // Gestione del pulsante di logout
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(event) {
            event.preventDefault();
            Auth.logout();
        });
    }
});
