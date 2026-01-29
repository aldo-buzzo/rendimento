package com.example.rendimento.service;

import com.example.rendimento.model.ProfiloCalcolo;
import com.example.rendimento.model.Utente;

import java.util.List;
import java.util.Optional;

/**
 * Interfaccia per il servizio che gestisce le operazioni relative all'entità ProfiloCalcolo.
 */
public interface ProfiloCalcoloService {
    
    /**
     * Recupera tutti i profili di calcolo.
     * 
     * @return una lista di tutti i profili di calcolo
     */
    List<ProfiloCalcolo> getAllProfiliCalcolo();
    
    /**
     * Recupera un profilo di calcolo per ID.
     * 
     * @param idProfilo l'ID del profilo di calcolo
     * @return il profilo di calcolo, se trovato
     */
    Optional<ProfiloCalcolo> getProfiloCalcoloById(Integer idProfilo);
    
    /**
     * Recupera tutti i profili di calcolo di un utente.
     * 
     * @param utente l'utente di cui recuperare i profili
     * @return una lista dei profili di calcolo dell'utente
     */
    List<ProfiloCalcolo> getProfiliCalcoloByUtente(Utente utente);
    
    /**
     * Recupera tutti i profili di calcolo di un utente per ID.
     * 
     * @param idUtente l'ID dell'utente
     * @return una lista dei profili di calcolo dell'utente
     */
    List<ProfiloCalcolo> getProfiliCalcoloByUtenteId(Integer idUtente);
    
    /**
     * Recupera il profilo di calcolo predefinito di un utente.
     * 
     * @param utente l'utente di cui recuperare il profilo predefinito
     * @return il profilo di calcolo predefinito dell'utente, se esiste
     */
    Optional<ProfiloCalcolo> getProfiloCalcoloPredefinito(Utente utente);
    
    /**
     * Recupera il profilo di calcolo predefinito di un utente per ID.
     * 
     * @param idUtente l'ID dell'utente
     * @return il profilo di calcolo predefinito dell'utente, se esiste
     */
    Optional<ProfiloCalcolo> getProfiloCalcoloPredefinito(Integer idUtente);
    
    /**
     * Salva un profilo di calcolo.
     * 
     * @param profiloCalcolo il profilo di calcolo da salvare
     * @return il profilo di calcolo salvato
     */
    ProfiloCalcolo saveProfiloCalcolo(ProfiloCalcolo profiloCalcolo);
    
    
    /**
     * Imposta un profilo di calcolo come predefinito per un utente.
     * 
     * @param idProfilo l'ID del profilo da impostare come predefinito
     * @param idUtente l'ID dell'utente
     * @return il profilo di calcolo aggiornato
     */
    ProfiloCalcolo setProfiloCalcoloPredefinito(Integer idProfilo, Integer idUtente);
    
    /**
     * Elimina un profilo di calcolo.
     * 
     * @param idProfilo l'ID del profilo da eliminare
     */
    void deleteProfiloCalcolo(Integer idProfilo);
    
    /**
     * Verifica se un utente ha almeno un profilo di calcolo.
     * 
     * @param idUtente l'ID dell'utente
     * @return true se l'utente ha almeno un profilo, false altrimenti
     */
    boolean hasProfiloCalcolo(Integer idUtente);
}