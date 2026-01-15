package com.example.rendimento.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handler globale per le eccezioni dell'applicazione.
 * Gestisce le eccezioni e le converte in risposte HTTP appropriate.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestisce le eccezioni di tipo ConflittoModificaException.
     * Queste eccezioni si verificano quando c'è un conflitto di concorrenza ottimistica.
     *
     * @param ex l'eccezione catturata
     * @param request la richiesta web
     * @return una risposta con stato 409 Conflict e dettagli sull'errore
     */
    @ExceptionHandler(ConflittoModificaException.class)
    public ResponseEntity<Object> handleConflittoModificaException(
            ConflittoModificaException ex, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflitto di Modifica");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }
    
    /**
     * Gestisce le eccezioni di tipo EntityNotFoundException.
     * Queste eccezioni si verificano quando un'entità richiesta non viene trovata.
     *
     * @param ex l'eccezione catturata
     * @param request la richiesta web
     * @return una risposta con stato 404 Not Found e dettagli sull'errore
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Risorsa Non Trovata");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Gestisce le eccezioni di tipo IllegalArgumentException.
     * Queste eccezioni si verificano quando vengono forniti argomenti non validi.
     *
     * @param ex l'eccezione catturata
     * @param request la richiesta web
     * @return una risposta con stato 400 Bad Request e dettagli sull'errore
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Richiesta Non Valida");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Gestisce tutte le altre eccezioni non gestite specificamente.
     *
     * @param ex l'eccezione catturata
     * @param request la richiesta web
     * @return una risposta con stato 500 Internal Server Error e dettagli sull'errore
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(
            Exception ex, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Errore Interno del Server");
        body.put("message", "Si è verificato un errore imprevisto. Contattare l'amministratore.");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
