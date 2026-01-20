package com.example.rendimento.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;

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
     * Il metodo utilizza calcolaRendimentoAdvanced che calcola sia il bollo mensile che annuale
     * e utilizza il bollo mensile come default.
     * 
     * @param idTitolo ID del titolo
     * @param prezzoAcquisto prezzo di acquisto inserito dall'utente
     * @param importo importo dell'investimento
     * @return DTO contenente tutti i risultati del calcolo
     * @throws jakarta.persistence.EntityNotFoundException se il titolo non esiste
     * @throws IllegalArgumentException se i parametri non sono validi
     */
    RisultatoSimulazioneDTO calcolaRendimento(Integer idTitolo, BigDecimal prezzoAcquisto, 
                                             BigDecimal importo);
    
    /**
     * Salva una simulazione nel database.
     * 
     * @param simulazioneDTO il DTO contenente i dati della simulazione
     * @return il DTO della simulazione salvata con ID aggiornato
     */
    SimulazioneDTO salvaSimulazione(SimulazioneDTO simulazioneDTO);
    
    /**
     * Calcola il rendimento e salva la simulazione in un'unica operazione.
     * Il metodo utilizza calcolaRendimentoAdvanced che calcola sia il bollo mensile che annuale
     * e imposta un valore di default per le commissioni.
     * 
     * @param idTitolo ID del titolo
     * @param prezzoAcquisto prezzo di acquisto inserito dall'utente
     * @param importo importo dell'investimento
     * @param dataAcquisto data di acquisto
     * @return il DTO della simulazione salvata con ID aggiornato
     * @throws jakarta.persistence.EntityNotFoundException se il titolo non esiste
     * @throws IllegalArgumentException se i parametri non sono validi
     */
    SimulazioneDTO calcolaESalvaSimulazione(Integer idTitolo, BigDecimal prezzoAcquisto, 
                                          BigDecimal importo, LocalDate dataAcquisto);
    
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
     * Aggiorna una simulazione esistente con i risultati di un nuovo calcolo di rendimento.
     * Questo metodo utilizza internamente convertToSimulazioneDTO per garantire che tutti i campi
     * siano aggiornati correttamente.
     * 
     * @param simulazioneEsistente la simulazione esistente da aggiornare
     * @param risultato il risultato del nuovo calcolo di rendimento
     * @param importo l'importo dell'investimento
     * @return la simulazione aggiornata e salvata
     */
    SimulazioneDTO aggiornaSimulazione(SimulazioneDTO simulazioneEsistente, RisultatoSimulazioneDTO risultato, BigDecimal importo);
    
    /**
     * Recupera tutte le simulazioni associate ai titoli di un utente specifico,
     * ordinate per data di scadenza crescente.
     * 
     * @param utenteId l'ID dell'utente
     * @param latest se true, recupera solo le simulazioni più recenti per ogni titolo
     * @return lista di simulazioni associate ai titoli dell'utente, ordinate per data di scadenza crescente
     */
    List<SimulazioneDTO> getSimulazioniByUtenteIdOrderByScadenzaAsc(Integer utenteId, boolean latest);
}
