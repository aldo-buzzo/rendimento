package com.example.rendimento.utility;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Classe di utilità per la creazione di risposte di errore standardizzate.
 * Questa classe fornisce metodi per creare risposte di errore con un formato coerente
 * da utilizzare nei controller REST.
 */
public class ErrorResponseUtils {
    
    /**
     * Crea una risposta di errore con il codice di stato e il messaggio specificati.
     * 
     * @param status il codice di stato HTTP
     * @param errorType il tipo di errore (es. "Titolo non trovato", "Tipo titolo non supportato")
     * @param errorMessage il messaggio di errore dettagliato
     * @return un ResponseEntity contenente un oggetto Map con i dettagli dell'errore
     */
    public static ResponseEntity<Map<String, String>> createErrorResponse(
            HttpStatus status, String errorType, String errorMessage) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorType);
        errorResponse.put("message", errorMessage);
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Crea una risposta di errore 400 Bad Request.
     * 
     * @param errorType il tipo di errore
     * @param errorMessage il messaggio di errore dettagliato
     * @return un ResponseEntity con stato 400 Bad Request
     */
    public static ResponseEntity<Map<String, String>> createBadRequestResponse(
            String errorType, String errorMessage) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorType, errorMessage);
    }
    
    /**
     * Crea una risposta di errore 404 Not Found.
     * 
     * @param errorType il tipo di errore
     * @param errorMessage il messaggio di errore dettagliato
     * @return un ResponseEntity con stato 404 Not Found
     */
    public static ResponseEntity<Map<String, String>> createNotFoundResponse(
            String errorType, String errorMessage) {
        return createErrorResponse(HttpStatus.NOT_FOUND, errorType, errorMessage);
    }
    
    /**
     * Crea una risposta di errore 500 Internal Server Error.
     * 
     * @param errorMessage il messaggio di errore dettagliato
     * @return un ResponseEntity con stato 500 Internal Server Error
     */
    public static ResponseEntity<Map<String, String>> createInternalServerErrorResponse(
            String errorMessage) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno", errorMessage);
    }
    
    /**
     * Crea una risposta di errore per un'eccezione generica.
     * Questo metodo determina automaticamente il tipo di risposta in base al tipo di eccezione.
     * 
     * @param e l'eccezione da gestire
     * @return un ResponseEntity con lo stato appropriato
     */
    public static ResponseEntity<Map<String, String>> createErrorResponseFromException(Exception e) {
        if (e instanceof jakarta.persistence.EntityNotFoundException) {
            return createNotFoundResponse("Entità non trovata", e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            return createBadRequestResponse("Parametro non valido", e.getMessage());
        } else {
            return createInternalServerErrorResponse("Si è verificato un errore durante l'elaborazione della richiesta: " + e.getMessage());
        }
    }
}