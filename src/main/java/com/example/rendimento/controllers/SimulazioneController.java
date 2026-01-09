package com.example.rendimento.controllers;

import com.example.rendimento.dto.RisultatoSimulazioneDTO;
import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.enums.ModalitaCalcoloBollo;
import com.example.rendimento.service.SimulazioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller REST per la gestione delle simulazioni.
 */
@RestController
@RequestMapping("/api/simulazioni")
public class SimulazioneController {

    private final SimulazioneService simulazioneService;

    /**
     * Costruttore con parametri per l'iniezione delle dipendenze.
     *
     * @param simulazioneService servizio per la gestione delle simulazioni
     */
    @Autowired
    public SimulazioneController(SimulazioneService simulazioneService) {
        this.simulazioneService = simulazioneService;
    }

    /**
     * Endpoint per il calcolo del rendimento di un titolo.
     *
     * @param idTitolo ID del titolo
     * @param prezzoAcquisto prezzo di acquisto inserito dall'utente
     * @param importo importo dell'investimento
     * @param modalitaBollo modalità di calcolo del bollo (ANNUALE o MENSILE)
     * @return DTO contenente tutti i risultati del calcolo
     */
    @PostMapping("/calcola-rendimento")
    public ResponseEntity<RisultatoSimulazioneDTO> calcolaRendimento(
            @RequestParam Integer idTitolo,
            @RequestParam BigDecimal prezzoAcquisto,
            @RequestParam BigDecimal importo,
            @RequestParam ModalitaCalcoloBollo modalitaBollo) {
        
        RisultatoSimulazioneDTO risultato = simulazioneService.calcolaRendimento(
            idTitolo, prezzoAcquisto, importo, modalitaBollo);
        
        return ResponseEntity.ok(risultato);
    }

    /**
     * Endpoint per il salvataggio di una simulazione.
     *
     * @param simulazioneDTO il DTO contenente i dati della simulazione
     * @return il DTO della simulazione salvata con ID aggiornato
     */
    @PostMapping
    public ResponseEntity<SimulazioneDTO> salvaSimulazione(@RequestBody SimulazioneDTO simulazioneDTO) {
        SimulazioneDTO savedSimulazione = simulazioneService.salvaSimulazione(simulazioneDTO);
        return ResponseEntity.ok(savedSimulazione);
    }

    /**
     * Endpoint per il recupero di tutte le simulazioni.
     *
     * @return lista di tutte le simulazioni
     */
    @GetMapping
    public ResponseEntity<List<SimulazioneDTO>> getAllSimulazioni() {
        List<SimulazioneDTO> simulazioni = simulazioneService.getAllSimulazioni();
        return ResponseEntity.ok(simulazioni);
    }

    /**
     * Endpoint per il recupero di una simulazione per ID.
     *
     * @param id l'ID della simulazione da cercare
     * @return la simulazione trovata
     */
    @GetMapping("/{id}")
    public ResponseEntity<SimulazioneDTO> getSimulazioneById(@PathVariable Integer id) {
        SimulazioneDTO simulazione = simulazioneService.findById(id);
        return ResponseEntity.ok(simulazione);
    }

    /**
     * Endpoint per l'eliminazione di una simulazione per ID.
     *
     * @param id l'ID della simulazione da eliminare
     * @return risposta vuota con stato 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimulazione(@PathVariable Integer id) {
        simulazioneService.deleteSimulazione(id);
        return ResponseEntity.noContent().build();
    }
}