/**
 * Servizio per le chiamate API al backend
 * Contiene funzioni per interagire con i vari endpoint REST
 */

// Namespace per il servizio API
window.ApiService = {
    /**
     * URL base per le chiamate API
     */
    baseUrl: '/api',
    
    /**
     * Esegue una richiesta GET
     * @param {string} url - L'URL della richiesta
     * @returns {Promise} - Promise che risolve con i dati della risposta
     */
    get: function(url) {
        return fetch(url)
            .then(response => {
                if (!response.ok) {
                    // Se l'errore è 401 (Unauthorized) o 403 (Forbidden), reindirizza alla pagina di login
                    if (response.status === 401 || response.status === 403) {
                        console.log('Sessione scaduta o utente non autorizzato. Reindirizzamento alla pagina di login...');
                        window.location.href = '/login';
                        throw new Error('Reindirizzamento alla pagina di login');
                    }
                    throw new Error('Errore nella richiesta: ' + response.status);
                }
                return response.json();
            });
    },
    
    /**
     * Esegue una richiesta POST
     * @param {string} url - L'URL della richiesta
     * @param {Object} data - I dati da inviare nel corpo della richiesta
     * @returns {Promise} - Promise che risolve con i dati della risposta
     */
    post: function(url, data) {
        return fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            if (!response.ok) {
                // Se l'errore è 401 (Unauthorized) o 403 (Forbidden), reindirizza alla pagina di login
                if (response.status === 401 || response.status === 403) {
                    console.log('Sessione scaduta o utente non autorizzato. Reindirizzamento alla pagina di login...');
                    window.location.href = '/login';
                    throw new Error('Reindirizzamento alla pagina di login');
                }
                throw new Error('Errore nella richiesta: ' + response.status);
            }
            return response.json();
        });
    },
    
    /**
     * Esegue una richiesta PUT
     * @param {string} url - L'URL della richiesta
     * @param {Object} data - I dati da inviare nel corpo della richiesta
     * @returns {Promise} - Promise che risolve con i dati della risposta
     */
    put: function(url, data) {
        return fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            if (!response.ok) {
                // Se l'errore è 401 (Unauthorized) o 403 (Forbidden), reindirizza alla pagina di login
                if (response.status === 401 || response.status === 403) {
                    console.log('Sessione scaduta o utente non autorizzato. Reindirizzamento alla pagina di login...');
                    window.location.href = '/login';
                    throw new Error('Reindirizzamento alla pagina di login');
                }
                throw new Error('Errore nella richiesta: ' + response.status);
            }
            return response.json();
        });
    },
    
    /**
     * Esegue una richiesta DELETE
     * @param {string} url - L'URL della richiesta
     * @returns {Promise} - Promise che risolve con i dati della risposta
     */
    delete: function(url) {
        return fetch(url, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                // Se l'errore è 401 (Unauthorized) o 403 (Forbidden), reindirizza alla pagina di login
                if (response.status === 401 || response.status === 403) {
                    console.log('Sessione scaduta o utente non autorizzato. Reindirizzamento alla pagina di login...');
                    window.location.href = '/login';
                    throw new Error('Reindirizzamento alla pagina di login');
                }
                throw new Error('Errore nella richiesta: ' + response.status);
            }
            return response.json();
        });
    },
    
    /**
     * Recupera tutti i titoli dal server
     * @returns {Promise} - Promise che risolve con l'array dei titoli
     */
    getTitoli: function() {
        return this.get(`${this.baseUrl}/frontend/titolo`);
    },
    
    /**
     * Recupera un titolo specifico dal server
     * @param {number} id - L'ID del titolo da recuperare
     * @returns {Promise} - Promise che risolve con il titolo richiesto
     */
    getTitolo: function(id) {
        return this.get(`${this.baseUrl}/frontend/titolo/${id}`);
    },
    
    /**
     * Salva un titolo sul server
     * @param {Object} titolo - Il titolo da salvare
     * @returns {Promise} - Promise che risolve con il titolo salvato
     */
    saveTitolo: function(titolo) {
        return this.post(`${this.baseUrl}/titolo`, titolo);
    },
    
    /**
     * Elimina un titolo dal server
     * @param {number} id - L'ID del titolo da eliminare
     * @returns {Promise} - Promise che risolve con l'esito dell'eliminazione
     */
    deleteTitolo: function(id) {
        return this.delete(`${this.baseUrl}/frontend/titolo/${id}`);
    },
    
    /**
     * Recupera tutte le simulazioni dal server
     * @param {boolean} latest - Se true, recupera solo le ultime simulazioni
     * @returns {Promise} - Promise che risolve con l'array delle simulazioni
     */
    getSimulazioni: function(latest = true) {
        if (latest) {
            return this.get(`${this.baseUrl}/frontend/simulazioni/latest`);
        } else {
            return this.get(`${this.baseUrl}/simulazioni`);
        }
    },
    
    /**
     * Recupera una simulazione specifica dal server
     * @param {number} id - L'ID della simulazione da recuperare
     * @returns {Promise} - Promise che risolve con la simulazione richiesta
     */
    getSimulazione: function(id) {
        return this.get(`${this.baseUrl}/simulazioni/${id}`);
    },
    
    /**
     * Salva una simulazione sul server
     * @param {Object} simulazione - La simulazione da salvare
     * @returns {Promise} - Promise che risolve con la simulazione salvata
     */
    saveSimulazione: function(simulazione) {
        if (simulazione.idSimulazione) {
            return this.put(`${this.baseUrl}/simulazioni/${simulazione.idSimulazione}`, simulazione);
        } else {
            return this.post(`${this.baseUrl}/simulazioni`, simulazione);
        }
    },
    
    /**
     * Elimina una simulazione dal server
     * @param {number} id - L'ID della simulazione da eliminare
     * @returns {Promise} - Promise che risolve con l'esito dell'eliminazione
     */
    deleteSimulazione: function(id) {
        return this.delete(`${this.baseUrl}/simulazioni/${id}`);
    },
    
    /**
     * Calcola il rendimento di un titolo
     * @param {number} idTitolo - L'ID del titolo
     * @param {number} prezzoAcquisto - Il prezzo di acquisto
     * @param {number} importo - L'importo nominale
     * @returns {Promise} - Promise che risolve con il risultato del calcolo
     */
    calcolaRendimento: function(idTitolo, prezzoAcquisto, importo) {
        // L'endpoint è definito come @PostMapping ma si aspetta i parametri come @RequestParam
        // Quindi dobbiamo inviare una richiesta POST con i parametri nella query string
        const url = `${this.baseUrl}/simulazioni/calcola-rendimento?idTitolo=${idTitolo}&prezzoAcquisto=${prezzoAcquisto}&importo=${importo}`;
        
        return fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                // Se l'errore è 401 (Unauthorized) o 403 (Forbidden), reindirizza alla pagina di login
                if (response.status === 401 || response.status === 403) {
                    console.log('Sessione scaduta o utente non autorizzato. Reindirizzamento alla pagina di login...');
                    window.location.href = '/login';
                    throw new Error('Reindirizzamento alla pagina di login');
                }
                throw new Error('Errore nella richiesta: ' + response.status);
            }
            return response.json();
        });
    },
    
    /**
     * Calcola i rendimenti di tutti i titoli
     * @returns {Promise} - Promise che risolve con l'esito del calcolo
     */
    calcolaRendimentiTuttiTitoli: function() {
        return this.post(`${this.baseUrl}/simulazioni/calcola-rendimenti-tutti-titoli`, {});
    },
    
    /**
     * Recupera i metadati dell'applicazione
     * @returns {Promise} - Promise che risolve con i metadati
     */
    getMetadata: function() {
        return this.get(`${this.baseUrl}/frontend/app-info`);
    },
    
    /**
     * Recupera i titoli paginati da Borsa Italiana
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {number} pagina - Il numero di pagina (0-based)
     * @param {number} dimensione - Il numero di elementi per pagina
     * @returns {Promise} - Promise che risolve con l'oggetto paginato contenente i titoli
     */
    getTitoliPaginati: function(tipo, pagina, dimensione) {
        return this.get(`${this.baseUrl}/borsa-italiana/lista-paginata/${tipo}?page=${pagina}&size=${dimensione}`);
    },
    
    /**
     * Recupera il prezzo corrente di un titolo da Borsa Italiana
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {string} isin - Il codice ISIN del titolo
     * @returns {Promise} - Promise che risolve con il prezzo corrente del titolo
     */
    getPrezzoTitolo: function(tipo, isin) {
        return this.get(`${this.baseUrl}/borsa-italiana/corso/${tipo}/${isin}`);
    },
    
    /**
     * Recupera i dettagli di un titolo specifico da Borsa Italiana
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {string} isin - Il codice ISIN del titolo
     * @returns {Promise} - Promise che risolve con i dettagli del titolo
     */
    getTitoloDettaglio: function(tipo, isin) {
        return this.get(`${this.baseUrl}/borsa-italiana/${tipo}/${isin}`);
    },
    
    /**
     * Recupera i valori degli enum dal server
     * @param {string} enumName - Il nome dell'enum da recuperare
     * @returns {Promise} - Promise che risolve con i valori dell'enum
     */
    getEnumValues: function(enumName) {
        return this.get(`${this.baseUrl}/frontend/enum/${enumName}`);
    }
};
