package com.example.rendimento.exception;

/**
 * Eccezione lanciata quando si verifica un conflitto di modifica a causa del controllo ottimistico della concorrenza.
 * Questo accade quando due utenti tentano di modificare la stessa entità contemporaneamente.
 */
public class ConflittoModificaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message il messaggio di errore
     */
    public ConflittoModificaException(String message) {
        super(message);
    }

    /**
     * Costruttore con messaggio di errore e causa.
     *
     * @param message il messaggio di errore
     * @param cause la causa dell'eccezione
     */
    public ConflittoModificaException(String message, Throwable cause) {
        super(message, cause);
    }
}
