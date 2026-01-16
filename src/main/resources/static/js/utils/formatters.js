/**
 * Modulo per la formattazione di valori
 * Contiene funzioni per formattare numeri, date e altri valori
 */

// Namespace per il modulo formatters
window.Formatters = {
    /**
     * Formatta un valore percentuale
     * @param {number|string} value - Il valore da formattare (già moltiplicato per 100)
     * @returns {string} Il valore formattato come percentuale
     */
    formatPercentage: function(value) {
        if (value === null || value === undefined) return '';
        
        // Assicurati che il valore sia un numero
        const numValue = parseFloat(value);
        if (isNaN(numValue)) return '';
        
        // Usa Intl.NumberFormat per formattare il valore come percentuale con esattamente 2 decimali
        return new Intl.NumberFormat('it-IT', { 
            style: 'percent', 
            minimumFractionDigits: 2, 
            maximumFractionDigits: 2 
        }).format(numValue / 100);
    },
    /**
     * Formatta un numero decimale con la virgola come separatore
     * @param {number|string} value - Il valore da formattare
     * @returns {string} Il valore formattato
     */
    formatDecimal: function(value) {
        if (value === null || value === undefined) return '';
        
        // Converti il valore in numero e poi formatta con 5 decimali massimo
        const num = parseFloat(value);
        if (isNaN(num)) return '';
        
        // Usa Intl.NumberFormat per formattare il numero secondo le convenzioni italiane
        return new Intl.NumberFormat('it-IT', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 5
        }).format(num);
    },

    /**
     * Formatta una data in formato italiano (gg-mm-aaaa)
     * @param {string} dateString - La data in formato ISO
     * @returns {string} La data formattata
     */
    formatDate: function(dateString) {
        if (!dateString) return '';
        
        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return '';
            
            const day = date.getDate().toString().padStart(2, '0');
            const month = (date.getMonth() + 1).toString().padStart(2, '0');
            const year = date.getFullYear();
            return `${day}-${month}-${year}`;
        } catch (e) {
            console.error("Errore nella formattazione della data:", e);
            return '';
        }
    }
};
