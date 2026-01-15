package com.example.rendimento.constants;

/**
 * Classe che contiene le costanti per i messaggi dell'applicazione.
 */
public class AppMessages {
    // Messaggi di successo
    public static final String TITOLO_SALVATO = "Titolo salvato con successo";
    public static final String TITOLO_AGGIORNATO = "Titolo aggiornato con successo";
    public static final String TITOLO_ELIMINATO = "Titolo eliminato con successo";
    
    // Messaggi di errore
    public static final String TITOLO_GIA_PRESENTE = "Titolo già presente con questo codice ISIN";
    public static final String TITOLO_NON_TROVATO = "Titolo non trovato";
    public static final String ERRORE_SALVATAGGIO = "Errore durante il salvataggio del titolo";
    public static final String ERRORE_ELIMINAZIONE = "Errore durante l'eliminazione del titolo";
    public static final String CAMPI_OBBLIGATORI = "Compila tutti i campi obbligatori";
    public static final String PREZZO_NON_VALIDO = "Il prezzo deve essere un valore numerico valido";
    
    // Messaggi per la validazione
    public static final String NOME_OBBLIGATORIO = "Il nome è obbligatorio";
    public static final String CODICE_ISIN_OBBLIGATORIO = "Il codice ISIN è obbligatorio";
    public static final String DATA_SCADENZA_OBBLIGATORIA = "La data di scadenza è obbligatoria";
    public static final String TASSO_NOMINALE_OBBLIGATORIO = "Il tasso nominale è obbligatorio";
    public static final String PERIODICITA_CEDOLE_OBBLIGATORIA = "La periodicità delle cedole è obbligatoria";
    public static final String PERIODICITA_BOLLO_OBBLIGATORIA = "La periodicità del bollo è obbligatoria";
    
    private AppMessages() {
        // Costruttore privato per evitare l'istanziazione
    }
}