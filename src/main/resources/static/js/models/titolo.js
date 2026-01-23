/**
 * Modulo per la gestione dei titoli
 * Contiene funzioni per interagire con i dati dei titoli
 */

// Namespace per il modulo Titolo
window.Titolo = {
    /**
     * Carica tutti i titoli dal server
     * @returns {Promise} - Promise che risolve con l'array dei titoli convertiti per il frontend
     */
    load: function() {
        return ApiService.getTitoli()
            .then(data => {
                // Converti i DTO in oggetti per il frontend
                return data.map(dto => this.convertFromDTO(dto));
            });
    },
    
    /**
     * Carica un titolo specifico dal server
     * @param {number} id - L'ID del titolo da caricare
     * @returns {Promise} - Promise che risolve con il titolo convertito per il frontend
     */
    loadById: function(id) {
        return ApiService.getTitolo(id)
            .then(dto => {
                // Converti il DTO in oggetto per il frontend
                return this.convertFromDTO(dto);
            });
    },
    
    /**
     * Carica un titolo specifico dal server tramite ISIN
     * @param {string} isin - Il codice ISIN del titolo da caricare
     * @returns {Promise} - Promise che risolve con il titolo convertito per il frontend
     */
    loadByIsin: function(isin) {
        // Prima carica tutti i titoli e poi filtra per ISIN
        return this.load()
            .then(titoli => {
                const titolo = titoli.find(t => t.codiceIsin === isin);
                if (!titolo) {
                    throw new Error('Titolo non trovato con ISIN: ' + isin);
                }
                return titolo;
            });
    },
    
    /**
     * Salva un titolo
     * @param {Object} titolo - Il titolo da salvare
     * @returns {Promise} - Promise che risolve con il titolo salvato e convertito per il frontend
     */
    save: function(titolo) {
        // Converti l'oggetto frontend in DTO
        const titoloDTO = this.convertToDTO(titolo);
        
        return ApiService.saveTitolo(titoloDTO)
            .then(dto => {
                // Converti il DTO restituito in oggetto frontend
                return this.convertFromDTO(dto);
            });
    },
    
    /**
     * Elimina un titolo
     * @param {number} id - L'ID del titolo da eliminare
     * @returns {Promise} - Promise che risolve con l'esito dell'eliminazione
     */
    delete: function(id) {
        return ApiService.deleteTitolo(id);
    },
    
    /**
     * Converte un DTO in un oggetto per il frontend
     * @param {Object} dto - Il DTO da convertire
     * @returns {Object} - L'oggetto convertito per il frontend
     */
    convertFromDTO: function(dto) {
        return {
            id: dto.idTitolo,
            nome: dto.nome,
            codiceIsin: dto.codiceIsin,  // Corretto da isin a codiceIsin
            tipoTitolo: dto.tipoTitolo,  // Corretto da tipo a tipoTitolo
            dataEmissione: dto.dataEmissione,
            dataScadenza: dto.dataScadenza,
            tassoNominale: dto.tassoNominale,  // Aggiunto campo tassoNominale
            cedolaLorda: dto.cedolaLorda * 100, // Converti da decimale a percentuale
            prezzoEmissione: dto.prezzoEmissione,
            prezzoRimborso: dto.prezzoRimborso,
            periodicita: dto.periodicita,
            periodicitaCedole: dto.periodicitaCedole,  // Aggiunto campo periodicitaCedole
            modalitaCalcoloBollo: dto.modalitaCalcoloBollo,
            periodicitaBollo: dto.periodicitaBollo,
            prezzo: dto.corso  // Aggiunto campo prezzo mappato da corso
        };
    },
    
    /**
     * Converte un oggetto frontend in DTO
     * @param {Object} titolo - L'oggetto frontend da convertire
     * @returns {Object} - Il DTO convertito
     */
    convertToDTO: function(titolo) {
        return {
            idTitolo: titolo.id,
            nome: titolo.nome,
            codiceIsin: titolo.codiceIsin,  // Corretto da isin a codiceIsin
            tipoTitolo: titolo.tipoTitolo,  // Corretto da tipo a tipoTitolo
            dataEmissione: titolo.dataEmissione,
            dataScadenza: titolo.dataScadenza,
            tassoNominale: titolo.tassoNominale,  // Aggiunto campo tassoNominale
            cedolaLorda: titolo.cedolaLorda ? titolo.cedolaLorda / 100 : null, // Converti da percentuale a decimale, gestisci null
            prezzoEmissione: titolo.prezzoEmissione,
            prezzoRimborso: titolo.prezzoRimborso,
            periodicita: titolo.periodicita,
            periodicitaCedole: titolo.periodicitaCedole,  // Aggiunto campo periodicitaCedole
            modalitaCalcoloBollo: titolo.modalitaCalcoloBollo,
            periodicitaBollo: titolo.periodicitaBollo,
            corso: titolo.prezzo  // Aggiunto campo corso mappato da prezzo
        };
    },
    
    /**
     * Carica i titoli paginati da Borsa Italiana
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {number} pagina - Il numero di pagina (0-based)
     * @param {number} dimensione - Il numero di elementi per pagina
     * @returns {Promise} - Promise che risolve con l'oggetto paginato contenente i titoli
     */
    loadTitoliPaginati: function(tipo, pagina, dimensione) {
        console.log(`Caricamento titoli paginati di tipo ${tipo}, pagina ${pagina}, dimensione ${dimensione}`);
        
        return ApiService.getTitoliPaginati(tipo, pagina, dimensione)
            .then(response => {
                console.log(`Titoli paginati ricevuti:`, response);
                return response;
            })
            .catch(error => {
                console.error(`Errore nel caricamento dei titoli paginati di tipo ${tipo}:`, error);
                throw error;
            });
    },
    
    /**
     * Ottiene il prezzo corrente di un titolo da Borsa Italiana
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {string} isin - Il codice ISIN del titolo
     * @returns {Promise} - Promise che risolve con il prezzo corrente del titolo
     */
    getPrezzoCorrente: function(tipo, isin) {
        console.log(`Recupero prezzo corrente per titolo ${tipo} con ISIN ${isin}`);
        
        return ApiService.getPrezzoTitolo(tipo, isin)
            .then(response => {
                console.log(`Prezzo corrente ricevuto:`, response);
                
                // Il controller restituisce direttamente il valore BigDecimal, non un oggetto JSON
                if (response !== null && response !== undefined) {
                    return response;
                } else {
                    throw new Error('Prezzo non disponibile nella risposta');
                }
            })
            .catch(error => {
                console.error(`Errore nel recupero del prezzo corrente per il titolo ${isin}:`, error);
                throw error;
            });
    },
    
    /**
     * Carica i dettagli di un titolo specifico da Borsa Italiana
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {string} isin - Il codice ISIN del titolo
     * @returns {Promise} - Promise che risolve con i dettagli del titolo
     */
    loadTitoloDettaglio: function(tipo, isin) {
        console.log(`Caricamento dettagli per titolo ${tipo} con ISIN ${isin}`);
        
        return ApiService.getTitoloDettaglio(tipo, isin)
            .then(dto => {
                console.log(`Dettagli titolo ricevuti:`, dto);
                
                // Converti il DTO in oggetto per il frontend
                return this.convertFromDTO(dto);
            })
            .catch(error => {
                console.error(`Errore nel caricamento dei dettagli per il titolo ${isin}:`, error);
                throw error;
            });
    },
    
    /**
     * Importa più titoli contemporaneamente da Borsa Italiana
     * @param {Array} titoliImport - Array di oggetti {codiceIsin, tipoTitolo}
     * @returns {Promise} - Promise che risolve con la lista dei titoli importati
     */
    importaTitoliMultipli: function(titoliImport) {
        console.log(`Importazione multipla di ${titoliImport.length} titoli`);
        
        return ApiService.post(`${ApiService.baseUrl}/titolo/importa-multipli`, titoliImport)
            .then(response => {
                console.log(`Risposta importazione multipla:`, response);
                
                // Se ci sono titoli importati, convertili in oggetti per il frontend
                if (response && response.titoli) {
                    const titoliConvertiti = response.titoli.map(dto => this.convertFromDTO(dto));
                    return {
                        titoli: titoliConvertiti,
                        totale: response.totale,
                        errori: response.errori
                    };
                }
                
                return response;
            })
            .catch(error => {
                console.error(`Errore nell'importazione multipla dei titoli:`, error);
                throw error;
            });
    }
};
