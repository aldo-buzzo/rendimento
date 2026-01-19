package com.example.rendimento.service;

import com.example.rendimento.dto.RisultatoRendimentoAdvancedDTO;
import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.enums.ModalitaCalcoloBollo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Interfaccia per il servizio che gestisce le operazioni sulle simulazioni.
 */
public interface SimulazioneService {
    
    /**
     * Ricalcola tutti i valori di una simulazione esistente.
     * Utile per il dettaglio simulazione e altre visualizzazioni.
     * 
     * @param simulazione la simulazione di cui ricalcolare i valori
     * @return il risultato del calcolo del rendimento
     */
    RisultatoSimulazioneDTO ricalcolaValoriSimulazione(SimulazioneDTO simulazione);
    
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
     * Calcola il rendimento e salva la simulazione in un'unica operazione.
     * 
     * @param idTitolo ID del titolo
     * @param prezzoAcquisto prezzo di acquisto inserito dall'utente
     * @param importo importo dell'investimento
     * @param dataAcquisto data di acquisto
     * @param modalitaBollo modalità di calcolo del bollo (ANNUALE o MENSILE)
     * @param commissioniAcquisto commissioni di acquisto (in formato decimale, es. 0.0009 per 0.09%)
     * @return il DTO della simulazione salvata con ID aggiornato
     * @throws jakarta.persistence.EntityNotFoundException se il titolo non esiste
     * @throws IllegalArgumentException se i parametri non sono validi
     */
    SimulazioneDTO calcolaESalvaSimulazione(Integer idTitolo, BigDecimal prezzoAcquisto, 
                                          BigDecimal importo, LocalDate dataAcquisto,
                                          ModalitaCalcoloBollo modalitaBollo, BigDecimal commissioniAcquisto);
    
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
    
    /**
     * Recupera l'ultima simulazione per ogni titolo.
     * 
     * @return lista delle ultime simulazioni per ogni titolo
     */
    List<SimulazioneDTO> getLatestSimulazioneForEachTitolo();
    
    /**
     * Trova tutte le simulazioni associate a un titolo specifico tramite il suo ID.
     * 
     * @param idTitolo l'ID del titolo per cui cercare le simulazioni
     * @return lista di simulazioni associate al titolo
     */
    List<SimulazioneDTO> findByTitoloId(Integer idTitolo);
    
    /**
     * Trova la simulazione più recente per un titolo specifico tramite il suo ID.
     * 
     * @param idTitolo l'ID del titolo per cui cercare la simulazione più recente
     * @return la simulazione più recente per il titolo specificato
     * @throws jakarta.persistence.EntityNotFoundException se non esiste alcuna simulazione per il titolo
     */
    SimulazioneDTO getLatestSimulazioneByTitoloId(Integer idTitolo);
    
    /**
     * Trova tutte le simulazioni per un titolo specifico e una data di acquisto specifica.
     * Questo metodo è utile per verificare se esiste già una simulazione per lo stesso titolo nella stessa giornata.
     * 
     * @param idTitolo l'ID del titolo
     * @param dataAcquisto la data di acquisto
     * @return lista di simulazioni per il titolo e la data specificati
     */
    List<SimulazioneDTO> findByTitoloIdAndDataAcquisto(Integer idTitolo, LocalDate dataAcquisto);
    
    /**
     * Recupera tutte le simulazioni associate ai titoli di un utente specifico.
     * 
     * @param utenteId l'ID dell'utente
     * @param latest se true, recupera solo le simulazioni più recenti per ogni titolo
     * @return lista di simulazioni associate ai titoli dell'utente
     */
    List<SimulazioneDTO> getSimulazioniByUtenteId(Integer utenteId, boolean latest);
    
    /**
     * Calcola il rendimento avanzato di un titolo di Stato italiano (BTP/BOT) acquistato sul MOT e detenuto fino a scadenza.
     * Questo metodo implementa un modello lineare annualizzato che calcola quattro diversi rendimenti.
     * 
     * @param nominale il valore nominale del titolo
     * @param prezzoAcquistoPercentuale il prezzo di acquisto in percentuale (es. 99.71)
     * @param cedolaAnnua la cedola annua (es. 0.0185, zero per BOT)
     * @param anniDurata la durata in anni (es. 4.5)
     * @param commissionRate il tasso di commissione (es. 0.0009)
     * @param prezzoRiferimentoBollo il prezzo di riferimento per il calcolo del bollo (es. prezzo di acquisto)
     * @return DTO contenente i risultati del calcolo avanzato
     */
    RisultatoRendimentoAdvancedDTO calcolaRendimentoAdvanced(
        BigDecimal nominale,
        BigDecimal prezzoAcquistoPercentuale,
        BigDecimal cedolaAnnua,
        BigDecimal anniDurata,
        BigDecimal commissionRate,
        BigDecimal prezzoRiferimentoBollo);
}
