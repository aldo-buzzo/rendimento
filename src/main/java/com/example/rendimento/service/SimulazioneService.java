package com.example.rendimento.service;

import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.enums.ModalitaCalcoloBollo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interfaccia per il servizio che gestisce le operazioni sulle simulazioni.
 */
public interface SimulazioneService {
    
    /**
     * Calcola il rendimento di un titolo in base ai parametri forniti.
     * 
     * @param idTitolo ID del titolo
     * @param prezzoAcquisto prezzo di acquisto inserito dall'utente
     * @param importo importo dell'investimento
     * @param modalitaBollo modalità di calcolo del bollo (ANNUALE o MENSILE)
     * @return DTO contenente tutti i risultati del calcolo
     * @throws jakarta.persistence.EntityNotFoundException se il titolo non esiste
     * @throws IllegalArgumentException se i parametri non sono validi
     */
    RisultatoSimulazioneDTO calcolaRendimento(Integer idTitolo, BigDecimal prezzoAcquisto, 
                                             BigDecimal importo, ModalitaCalcoloBollo modalitaBollo);
    
    /**
     * Salva una simulazione nel database.
     * 
     * @param simulazioneDTO il DTO contenente i dati della simulazione
     * @return il DTO della simulazione salvata con ID aggiornato
     */
    SimulazioneDTO salvaSimulazione(SimulazioneDTO simulazioneDTO);
    
    /**
     * Recupera tutte le simulazioni.
     * 
     * @return lista di tutte le simulazioni
     */
    List<SimulazioneDTO> getAllSimulazioni();
    
    /**
     * Trova una simulazione per ID.
     * 
     * @param id l'ID della simulazione da cercare
     * @return la simulazione trovata o null se non esiste
     */
    SimulazioneDTO findById(Integer id);
    
    /**
     * Elimina una simulazione per ID.
     * 
     * @param id l'ID della simulazione da eliminare
     */
    void deleteSimulazione(Integer id);
}