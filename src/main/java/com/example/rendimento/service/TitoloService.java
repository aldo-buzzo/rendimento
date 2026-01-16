package com.example.rendimento.service;

import com.example.rendimento.dto.TitoloDTO;
import java.util.List;

/**
 * Interfaccia per il servizio che gestisce le operazioni sui titoli.
 */
public interface TitoloService {
    
    /**
     * Trova un titolo per ID.
     * 
     * @param id l'ID del titolo da cercare
     * @return il titolo trovato o null se non esiste
     */
    TitoloDTO findById(Integer id);
    
    /**
     * Importa un titolo da Borsa Italiana dato il codice ISIN e il tipo.
     * Recupera i dati del titolo e lo salva nel database.
     * 
     * @param codiceIsin il codice ISIN del titolo
     * @param tipoTitolo il tipo del titolo (BTP, BOT, ecc.)
     * @return il titolo importato e salvato
     */
    TitoloDTO importaTitoloDaBorsaItaliana(String codiceIsin, String tipoTitolo);
    
    /**
     * Recupera tutti i titoli.
     * 
     * @return lista di tutti i titoli
     */
    List<TitoloDTO> getAllTitoli();
    
    /**
     * Salva un nuovo titolo.
     * 
     * @param titoloDTO il DTO contenente i dati del titolo
     * @return il DTO del titolo salvato con ID aggiornato
     */
    TitoloDTO saveTitolo(TitoloDTO titoloDTO);
    
    /**
     * Trova un titolo per codice ISIN.
     * 
     * @param codiceIsin il codice ISIN del titolo da cercare
     * @return il titolo trovato o null se non esiste
     */
    TitoloDTO findByCodiceIsin(String codiceIsin);
    
    /**
     * Verifica se esiste un titolo con il codice ISIN specificato.
     * 
     * @param codiceIsin il codice ISIN da verificare
     * @return true se esiste, false altrimenti
     */
    boolean existsByCodiceIsin(String codiceIsin);
    
    /**
     * Elimina un titolo per ID.
     * 
     * @param id l'ID del titolo da eliminare
     */
    void deleteTitolo(Integer id);
}
