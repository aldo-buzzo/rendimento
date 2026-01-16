/**
 * Modulo per la validazione di valori
 * Contiene funzioni per validare e convertire valori
 */

// Namespace per il modulo validators
window.Validators = {
    /**
     * Verifica se un valore è un numero valido
     * @param {any} value - Il valore da validare
     * @returns {boolean} true se il valore è un numero valido, false altrimenti
     */
    isValidNumber: function(value) {
        // Accetta qualsiasi valore che possa essere convertito in numero
        // Non fa controlli specifici sul formato (punto o virgola)
        if (value === null || value === undefined || value === '') return false;
        
        try {
            // Normalizza il valore sostituendo le virgole con punti
            let normalizedValue = value.toString().trim();
            normalizedValue = normalizedValue.replace(/,/g, '.');
            
            // Verifica solo che sia un numero, senza controlli rigorosi sul formato
            return !isNaN(parseFloat(normalizedValue));
        } catch (e) {
            console.error("Errore nella validazione del numero:", e);
            return false;
        }
    },

    /**
     * Converte una stringa in numero
     * @param {string|number} value - Il valore da convertire
     * @returns {number} Il valore convertito in numero
     */
    parseNumericValue: function(value) {
        if (!value) return 0;
        
        try {
            // Normalizza il valore sostituendo le virgole con punti
            let normalizedValue = value.toString().trim();
            
            // Sostituisci tutte le virgole con punti
            normalizedValue = normalizedValue.replace(/,/g, '.');
            
            // Converte in numero
            return parseFloat(normalizedValue);
        } catch (e) {
            console.error("Errore nella conversione del numero:", e);
            return 0;
        }
    }
};
