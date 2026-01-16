/**
 * Modulo per la gestione delle chiamate API
 * Contiene funzioni per interagire con le API del backend
 */

// Namespace per il modulo ApiService
window.ApiService = {
    /**
     * URL base per le API
     */
    BASE_URL: '',
    
    /**
     * URL per le API frontend
     */
    FRONTEND_API_URL: '/api/frontend',
    
    /**
     * URL per le API dei titoli
     */
    TITOLO_API_URL: '/api/titolo',
    
    /**
     * URL per le API delle simulazioni
     */
    SIMULAZIONI_API_URL: '/api/simulazioni',
    
    /**
     * URL per le API di Borsa Italiana
     */
    BORSA_ITALIANA_API_URL: '/api/borsa-italiana',
    
    /**
     * Gestisce gli errori delle chiamate API
     * @param {Response} response - La risposta HTTP
     * @returns {Promise} - Promise che risolve con i dati o rifiuta con un errore
     */
    handleResponse: function(response) {
        if (!response.ok) {
            throw new Error(`Errore API: ${response.status} ${response.statusText}`);
        }
        return response.json();
    },
    
    /**
     * Gestisce gli errori delle chiamate API
     * @param {Error} error - L'errore
     * @throws {Error} - Rilancia l'errore con un messaggio più descrittivo
     */
    handleError: function(error) {
        console.error('Errore API:', error);
        throw error;
    },
    
    /**
     * Mostra/nasconde l'indicatore di caricamento
     * @param {boolean} show - true per mostrare, false per nascondere
     */
    toggleLoading: function(show) {
        if (typeof DomUtils !== 'undefined' && DomUtils.toggleLoading) {
            DomUtils.toggleLoading(show);
        } else {
            if (show) {
                document.body.classList.add('loading');
            } else {
                document.body.classList.remove('loading');
            }
        }
    },
    
    // API per i titoli
    
    /**
     * Recupera tutti i titoli
     * @returns {Promise} - Promise che risolve con l'array dei titoli
     */
    getTitoli: function() {
        this.toggleLoading(true);
        
        return fetch(`${this.FRONTEND_API_URL}/titolo`)
            .then(this.handleResponse)
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    /**
     * Recupera un titolo per ID
     * @param {number} id - L'ID del titolo
     * @returns {Promise} - Promise che risolve con il titolo o con un oggetto vuoto in caso di errore
     */
    getTitoloById: function(id) {
        this.toggleLoading(true);
        
        // Crea un titolo di fallback con i dati minimi necessari
        const fallbackTitolo = {
            idTitolo: id,
            nome: `Titolo #${id}`,
            codiceIsin: 'N/A',
            dataScadenza: new Date().toISOString().split('T')[0], // Data corrente
            tassoNominale: 0,
            periodicitaCedole: 'SEMESTRALE',
            periodicitaBollo: 'ANNUALE',
            tipoTitolo: 'BTP',
            corso: 100.00
        };
        
        return fetch(`${this.TITOLO_API_URL}/${id}`)
            .then(response => {
                if (!response.ok) {
                    console.warn(`Errore nel recupero del titolo con ID ${id}: ${response.status} ${response.statusText}`);
                    console.warn(`Utilizzando titolo di fallback per ID ${id}`);
                    this.toggleLoading(false);
                    return fallbackTitolo;
                }
                return response.json();
            })
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                console.warn('Errore nel recupero del titolo:', error);
                console.warn(`Utilizzando titolo di fallback per ID ${id}`);
                return fallbackTitolo;
            });
    },
    
    /**
     * Recupera un titolo per codice ISIN
     * @param {string} isin - Il codice ISIN del titolo
     * @returns {Promise} - Promise che risolve con il titolo
     */
    getTitoloByIsin: function(isin) {
        this.toggleLoading(true);
        
        return fetch(`${this.FRONTEND_API_URL}/titolo/isin/${isin}`)
            .then(this.handleResponse)
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    /**
     * Salva un titolo (crea o aggiorna)
     * @param {Object} titolo - Il titolo da salvare
     * @returns {Promise} - Promise che risolve con il titolo salvato
     */
    saveTitolo: function(titolo) {
        this.toggleLoading(true);
        
        const method = titolo.idTitolo ? 'PUT' : 'POST';
        
        return fetch(`${this.FRONTEND_API_URL}/titolo`, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(titolo)
        })
            .then(this.handleResponse)
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    /**
     * Elimina un titolo
     * @param {number} id - L'ID del titolo da eliminare
     * @returns {Promise} - Promise che risolve con l'esito dell'eliminazione
     */
    deleteTitolo: function(id) {
        this.toggleLoading(true);
        
        return fetch(`${this.FRONTEND_API_URL}/titolo/${id}`, {
            method: 'DELETE'
        })
            .then(response => {
                this.toggleLoading(false);
                if (!response.ok) {
                    throw new Error(`Errore nell'eliminazione del titolo: ${response.status} ${response.statusText}`);
                }
                return true;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    // API per le simulazioni
    
    /**
     * Recupera tutte le simulazioni
     * @param {boolean} latest - Se true, recupera solo le ultime simulazioni
     * @returns {Promise} - Promise che risolve con l'array delle simulazioni
     */
    getSimulazioni: function(latest = true) {
        this.toggleLoading(true);
        
        return fetch(`${this.SIMULAZIONI_API_URL}?latest=${latest}`)
            .then(this.handleResponse)
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    /**
     * Salva una simulazione
     * @param {Object} simulazione - La simulazione da salvare
     * @returns {Promise} - Promise che risolve con la simulazione salvata
     */
    saveSimulazione: function(simulazione) {
        this.toggleLoading(true);
        
        return fetch(`${this.SIMULAZIONI_API_URL}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(simulazione)
        })
            .then(this.handleResponse)
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    /**
     * Calcola il rendimento di un titolo
     * @param {number} idTitolo - L'ID del titolo
     * @param {number} prezzoAcquisto - Il prezzo di acquisto
     * @param {number} importo - L'importo nominale
     * @param {string} modalitaBollo - La modalità di calcolo del bollo (ANNUALE o MENSILE)
     * @returns {Promise} - Promise che risolve con il risultato del calcolo
     */
    calcolaRendimento: function(idTitolo, prezzoAcquisto, importo, modalitaBollo = 'ANNUALE') {
        this.toggleLoading(true);
        
        return fetch(`${this.SIMULAZIONI_API_URL}/calcola-rendimento?idTitolo=${idTitolo}&prezzoAcquisto=${prezzoAcquisto}&importo=${importo}&modalitaBollo=${modalitaBollo}`, {
            method: 'POST'
        })
            .then(this.handleResponse)
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    /**
     * Calcola i rendimenti di tutti i titoli
     * @returns {Promise} - Promise che risolve con l'esito del calcolo
     */
    calcolaRendimentiTuttiTitoli: function() {
        this.toggleLoading(true);
        
        return fetch(`${this.SIMULAZIONI_API_URL}/calcola-rendimenti-tutti-titoli`, {
            method: 'POST'
        })
            .then(this.handleResponse)
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    // API per Borsa Italiana
    
    /**
     * Recupera i titoli paginati da Borsa Italiana
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {number} page - Il numero di pagina (0-based)
     * @param {number} size - La dimensione della pagina
     * @returns {Promise} - Promise che risolve con la risposta paginata
     */
    getTitoliPaginati: function(tipo, page = 0, size = 10) {
        this.toggleLoading(true);
        
        return fetch(`${this.BORSA_ITALIANA_API_URL}/lista-paginata/${tipo}?page=${page}&size=${size}`)
            .then(this.handleResponse)
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    /**
     * Recupera i dettagli di un titolo da Borsa Italiana
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {string} isin - Il codice ISIN del titolo
     * @returns {Promise} - Promise che risolve con i dettagli del titolo
     */
    getTitoloDettaglio: function(tipo, isin) {
        this.toggleLoading(true);
        
        return fetch(`${this.BORSA_ITALIANA_API_URL}/${tipo}/${isin}`)
            .then(this.handleResponse)
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    /**
     * Recupera il prezzo corrente di un titolo
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {string} isin - Il codice ISIN del titolo
     * @returns {Promise} - Promise che risolve con il prezzo corrente
     */
    getPrezzoCorrente: function(tipo, isin) {
        this.toggleLoading(true);
        
        return fetch(`${this.BORSA_ITALIANA_API_URL}/corso/${tipo.toLowerCase()}/${isin}`)
            .then(this.handleResponse)
            .then(data => {
                this.toggleLoading(false);
                return data;
            })
            .catch(error => {
                this.toggleLoading(false);
                this.handleError(error);
            });
    },
    
    // API per i metadati dell'applicazione
    
    /**
     * Recupera i metadati dell'applicazione
     * @returns {Promise} - Promise che risolve con i metadati
     */
    getAppMetadata: function() {
        return fetch(`${this.FRONTEND_API_URL}/app-info`)
            .then(this.handleResponse)
            .catch(this.handleError);
    },
    
    /**
     * Recupera i valori di un enum
     * @param {string} enumName - Il nome dell'enum
     * @returns {Promise} - Promise che risolve con i valori dell'enum
     */
    getEnumValues: function(enumName) {
        return fetch(`${this.FRONTEND_API_URL}/enum/${enumName}`)
            .then(this.handleResponse)
            .catch(this.handleError);
    }
};
