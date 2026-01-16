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
     * Carica un titolo per ID
     * @param {number} id - L'ID del titolo
     * @returns {Promise} - Promise che risolve con il titolo convertito per il frontend
     */
    loadById: function(id) {
        return ApiService.getTitoloById(id)
            .then(dto => {
                // ApiService.getTitoloById ora restituisce sempre un oggetto (reale o fallback)
                // quindi non dovremmo mai arrivare al catch
                return this.convertFromDTO(dto);
            })
            .catch(error => {
                // Questo catch è solo per sicurezza, nel caso in cui ci siano errori imprevisti
                console.warn(`Errore imprevisto nel caricamento del titolo con ID ${id}:`, error);
                
                // Crea un titolo di fallback con i dati minimi necessari
                const fallbackTitolo = {
                    id: id,
                    nome: `Titolo #${id}`,
                    codiceIsin: 'N/A',
                    dataScadenza: new Date().toISOString().split('T')[0], // Data corrente
                    tassoNominale: 0,
                    periodicitaCedole: 'SEMESTRALE',
                    periodicitaBollo: 'ANNUALE',
                    tipoTitolo: 'BTP',
                    prezzo: 100.00
                };
                
                console.warn(`Utilizzando titolo di fallback per ID ${id}:`, fallbackTitolo);
                return fallbackTitolo;
            });
    },
    
    /**
     * Carica un titolo per codice ISIN
     * @param {string} isin - Il codice ISIN del titolo
     * @returns {Promise} - Promise che risolve con il titolo convertito per il frontend
     */
    loadByIsin: function(isin) {
        return ApiService.getTitoloByIsin(isin)
            .then(dto => {
                return this.convertFromDTO(dto);
            });
    },
    
    /**
     * Salva un titolo (crea o aggiorna)
     * @param {Object} titolo - Il titolo da salvare
     * @param {number} prezzo - Il prezzo attuale del titolo (opzionale)
     * @returns {Promise} - Promise che risolve con il titolo salvato e convertito per il frontend
     */
    save: function(titolo, prezzo) {
        // Converti l'oggetto frontend in DTO
        const titoloDTO = this.convertToDTO(titolo);
        
        return ApiService.saveTitolo(titoloDTO)
            .then(dto => {
                // Converti il DTO restituito in oggetto frontend
                const titoloSalvato = this.convertFromDTO(dto);
                
                // Se è stato fornito un prezzo, aggiornalo
                if (prezzo !== undefined) {
                    titoloSalvato.prezzo = prezzo;
                }
                
                return titoloSalvato;
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
     * Ottiene il prezzo corrente di un titolo
     * @param {string} tipoTitolo - Il tipo di titolo (BTP o BOT)
     * @param {string} codiceIsin - Il codice ISIN del titolo
     * @returns {Promise} - Promise che risolve con il prezzo corrente
     */
    getPrezzoCorrente: function(tipoTitolo, codiceIsin) {
        return ApiService.getPrezzoCorrente(tipoTitolo, codiceIsin);
    },
    
    /**
     * Carica i titoli paginati da Borsa Italiana
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {number} page - Il numero di pagina (0-based)
     * @param {number} size - La dimensione della pagina
     * @returns {Promise} - Promise che risolve con la risposta paginata
     */
    loadTitoliPaginati: function(tipo, page = 0, size = 10) {
        return ApiService.getTitoliPaginati(tipo, page, size);
    },
    
    /**
     * Carica i dettagli di un titolo da Borsa Italiana
     * @param {string} tipo - Il tipo di titolo (BTP o BOT)
     * @param {string} isin - Il codice ISIN del titolo
     * @returns {Promise} - Promise che risolve con i dettagli del titolo convertiti per il frontend
     */
    loadTitoloDettaglio: function(tipo, isin) {
        return ApiService.getTitoloDettaglio(tipo, isin)
            .then(dto => {
                return this.convertFromDTO(dto);
            });
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
            codiceIsin: dto.codiceIsin,
            dataScadenza: dto.dataScadenza,
            tassoNominale: dto.tassoNominale,
            periodicitaCedole: dto.periodicitaCedole,
            periodicitaBollo: dto.periodicitaBollo,
            tipoTitolo: dto.tipoTitolo,
            prezzo: dto.corso || 100.00 // Prezzo di default, potrebbe essere aggiunto al DTO in futuro
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
            codiceIsin: titolo.codiceIsin,
            dataScadenza: titolo.dataScadenza,
            tassoNominale: titolo.tassoNominale,
            periodicitaCedole: titolo.periodicitaCedole,
            periodicitaBollo: titolo.periodicitaBollo,
            tipoTitolo: titolo.tipoTitolo
        };
    }
};
