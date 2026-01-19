/**
 * Modulo per la gestione delle simulazioni
 * Contiene funzioni per interagire con i dati delle simulazioni
 */

// Namespace per il modulo Simulazione
window.Simulazione = {
    /**
     * Carica tutte le simulazioni dal server
     * @param {boolean} latest - Se true, recupera solo le ultime simulazioni
     * @returns {Promise} - Promise che risolve con l'array delle simulazioni convertite per il frontend
     */
    load: function(latest = true) {
        return ApiService.getSimulazioni(latest)
            .then(data => {
                // Converti i DTO in oggetti per il frontend
                return data.map(dto => this.convertFromDTO(dto));
            });
    },
    
    /**
     * Salva una simulazione
     * @param {Object} simulazione - La simulazione da salvare
     * @returns {Promise} - Promise che risolve con la simulazione salvata e convertita per il frontend
     */
    save: function(simulazione) {
        // Converti l'oggetto frontend in DTO
        const simulazioneDTO = this.convertToDTO(simulazione);
        
        return ApiService.saveSimulazione(simulazioneDTO)
            .then(dto => {
                // Converti il DTO restituito in oggetto frontend
                return this.convertFromDTO(dto);
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
        return ApiService.calcolaRendimento(idTitolo, prezzoAcquisto, importo, modalitaBollo);
    },
    
    /**
     * Calcola i rendimenti di tutti i titoli
     * @returns {Promise} - Promise che risolve con l'esito del calcolo
     */
    calcolaRendimentiTuttiTitoli: function() {
        return ApiService.calcolaRendimentiTuttiTitoli();
    },
    
    /**
     * Crea un oggetto simulazione da salvare
     * @param {number} titoloId - L'ID del titolo
     * @param {number} prezzoAcquisto - Il prezzo di acquisto
     * @param {string} dataAcquisto - La data di acquisto in formato ISO
     * @param {number} commissioniAcquisto - Le commissioni di acquisto (in percentuale)
     * @param {Object} risultatoCalcolo - Il risultato del calcolo del rendimento
     * @returns {Object} - L'oggetto simulazione pronto per essere salvato
     */
    creaSimulazione: function(titoloId, prezzoAcquisto, dataAcquisto, commissioniAcquisto, risultatoCalcolo) {
        return {
            titoloId: parseInt(titoloId),
            prezzoAcquisto: prezzoAcquisto,
            dataAcquisto: dataAcquisto,
            importoNominale: risultatoCalcolo.importo || 10000, // Valore di default se non presente
            commissioniAcquisto: commissioniAcquisto / 100, // Converti da percentuale a decimale
            rendimentoLordo: risultatoCalcolo.tasso,
            rendimentoTassato: risultatoCalcolo.tasso * 0.875, // Tassazione del 12.5%
            rendimentoNettoCedole: risultatoCalcolo.tassoNettoCommissioni,
            impostaBollo: risultatoCalcolo.impostaBollo,
            rendimentoNettoBollo: risultatoCalcolo.tassoNettoBollo,
            plusMinusValenza: risultatoCalcolo.plusvalenzaNetta,
            // Nuovi campi per il calcolo avanzato dei rendimenti
            nominale: risultatoCalcolo.nominale,
            prezzoRiferimentoBollo: risultatoCalcolo.prezzoRiferimentoBollo,
            capitaleInvestito: risultatoCalcolo.capitaleInvestito,
            capitaleConCommissioni: risultatoCalcolo.capitaleConCommissioni,
            cedoleNetteAnnue: risultatoCalcolo.cedoleNetteAnnue,
            guadagnoNettoSenzaCosti: risultatoCalcolo.guadagnoNettoSenzaCosti,
            rendimentoSenzaCosti: risultatoCalcolo.rendimentoSenzaCosti,
            rendimentoConCommissioni: risultatoCalcolo.rendimentoConCommissioni,
            rendimentoConBolloMensile: risultatoCalcolo.rendimentoConCommissioniEBolloMensile,
            bolloTotaleMensile: risultatoCalcolo.bolloTotaleMensile
        };
    },
    
    /**
     * Converte un DTO in un oggetto per il frontend
     * @param {Object} dto - Il DTO da convertire
     * @returns {Object} - L'oggetto convertito per il frontend
     */
    convertFromDTO: function(dto) {
        return {
            id: dto.idSimulazione,
            titoloId: dto.idTitolo,
            prezzoAcquisto: dto.prezzoAcquisto,
            dataAcquisto: dto.dataAcquisto,
            importoNominale: dto.importoNominale || 10000, // Valore di default se non presente
            commissioniAcquisto: dto.commissioniAcquisto * 100, // Converti da decimale a percentuale
            rendimentoLordo: dto.rendimentoLordo * 100, // Converti da decimale a percentuale
            rendimentoTassato: dto.rendimentoTassato * 100, // Converti da decimale a percentuale
            rendimentoNettoCedole: dto.rendimentoNettoCedole * 100, // Converti da decimale a percentuale
            impostaBollo: dto.impostaBollo,
            rendimentoNettoBollo: dto.rendimentoNettoBollo, // Il valore è già una percentuale nel backend
            plusMinusValenza: dto.plusMinusValenza,
            // Nuovi campi per il calcolo avanzato dei rendimenti
            nominale: dto.nominale,
            prezzoRiferimentoBollo: dto.prezzoRiferimentoBollo,
            capitaleInvestito: dto.capitaleInvestito,
            capitaleConCommissioni: dto.capitaleConCommissioni,
            cedoleNetteAnnue: dto.cedoleNetteAnnue,
            guadagnoNettoSenzaCosti: dto.guadagnoNettoSenzaCosti,
            rendimentoSenzaCosti: dto.rendimentoSenzaCosti * 100, // Converti da decimale a percentuale
            rendimentoConCommissioni: dto.rendimentoConCommissioni * 100, // Converti da decimale a percentuale
            rendimentoConBolloMensile: dto.rendimentoConBolloMensile * 100, // Converti da decimale a percentuale
            bolloTotaleMensile: dto.bolloTotaleMensile
        };
    },
    
    /**
     * Converte un oggetto frontend in DTO
     * @param {Object} simulazione - L'oggetto frontend da convertire
     * @returns {Object} - Il DTO convertito
     */
    convertToDTO: function(simulazione) {
        return {
            idSimulazione: simulazione.id,
            idTitolo: simulazione.titoloId,
            prezzoAcquisto: simulazione.prezzoAcquisto,
            dataAcquisto: simulazione.dataAcquisto,
            importoNominale: simulazione.importoNominale,
            commissioniAcquisto: simulazione.commissioniAcquisto / 100, // Converti da percentuale a decimale
            rendimentoLordo: simulazione.rendimentoLordo / 100, // Converti da percentuale a decimale
            rendimentoTassato: simulazione.rendimentoTassato / 100, // Converti da percentuale a decimale
            rendimentoNettoCedole: simulazione.rendimentoNettoCedole / 100, // Converti da percentuale a decimale
            impostaBollo: simulazione.impostaBollo,
            rendimentoNettoBollo: simulazione.rendimentoNettoBollo / 100, // Converti da percentuale a decimale
            plusMinusValenza: simulazione.plusMinusValenza,
            // Nuovi campi per il calcolo avanzato dei rendimenti
            nominale: simulazione.nominale,
            prezzoRiferimentoBollo: simulazione.prezzoRiferimentoBollo,
            capitaleInvestito: simulazione.capitaleInvestito,
            capitaleConCommissioni: simulazione.capitaleConCommissioni,
            cedoleNetteAnnue: simulazione.cedoleNetteAnnue,
            guadagnoNettoSenzaCosti: simulazione.guadagnoNettoSenzaCosti,
            rendimentoSenzaCosti: simulazione.rendimentoSenzaCosti / 100, // Converti da percentuale a decimale
            rendimentoConCommissioni: simulazione.rendimentoConCommissioni / 100, // Converti da percentuale a decimale
            rendimentoConBolloMensile: simulazione.rendimentoConBolloMensile / 100, // Converti da percentuale a decimale
            bolloTotaleMensile: simulazione.bolloTotaleMensile
        };
    }
};
