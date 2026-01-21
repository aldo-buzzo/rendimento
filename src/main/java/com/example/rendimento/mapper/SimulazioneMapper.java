package com.example.rendimento.mapper;

import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.model.Simulazione;
import com.example.rendimento.repository.TitoloRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mapper per la conversione tra entità Simulazione e DTO SimulazioneDTO.
 */
@Component
public class SimulazioneMapper {

    private final TitoloRepository titoloRepository;

    /**
     * Costruttore con parametri per l'iniezione delle dipendenze.
     *
     * @param titoloRepository repository per le operazioni CRUD sui titoli
     */
    @Autowired
    public SimulazioneMapper(TitoloRepository titoloRepository) {
        this.titoloRepository = titoloRepository;
    }

    /**
     * Converte un'entità Simulazione in un DTO SimulazioneDTO.
     *
     * @param simulazione l'entità da convertire
     * @return il DTO corrispondente
     */
    public SimulazioneDTO toDTO(Simulazione simulazione) {
        if (simulazione == null) {
            return null;
        }

        SimulazioneDTO dto = new SimulazioneDTO();
        dto.setIdSimulazione(simulazione.getIdSimulazione());
        dto.setIdTitolo(simulazione.getTitolo() != null ? simulazione.getTitolo().getIdTitolo() : null);
        dto.setDataAcquisto(simulazione.getDataAcquisto());
        dto.setPrezzoAcquisto(simulazione.getPrezzoAcquisto());
        dto.setRendimentoLordo(simulazione.getRendimentoLordo());
        dto.setRendimentoTassato(simulazione.getRendimentoTassato());
        dto.setCommissioniAcquisto(simulazione.getCommissioniAcquisto());
        dto.setRendimentoNettoCedole(simulazione.getRendimentoNettoCedole());
        dto.setImpostaBollo(simulazione.getImpostaBollo());
        dto.setRendimentoNettoBollo(simulazione.getRendimentoNettoBollo());
        dto.setPlusMinusValenza(simulazione.getPlusMinusValenza());
        dto.setVersion(simulazione.getVersion());
        
        // Mapping dei nuovi campi
        dto.setNominale(simulazione.getNominale());
        dto.setPrezzoRiferimentoBollo(simulazione.getPrezzoRiferimentoBollo());
        dto.setCapitaleInvestito(simulazione.getCapitaleInvestito());
        dto.setCapitaleConCommissioni(simulazione.getCapitaleConCommissioni());
        dto.setCedoleNetteAnnue(simulazione.getCedoleNetteAnnue());
        dto.setGuadagnoNettoSenzaCosti(simulazione.getGuadagnoNettoSenzaCosti());
        dto.setRendimentoSenzaCosti(simulazione.getRendimentoSenzaCosti());
        dto.setRendimentoConCommissioni(simulazione.getRendimentoConCommissioni());
        dto.setRendimentoConBolloMensile(simulazione.getRendimentoConBolloMensile());
        dto.setBolloTotaleMensile(simulazione.getBolloTotaleMensile());
        dto.setRendimentoConBolloAnnuale(simulazione.getRendimentoConBolloAnnuale());
        dto.setBolloTotaleAnnuale(simulazione.getBolloTotaleAnnuale());
        
        // Aggiungi il titolo al DTO
        if (simulazione.getTitolo() != null) {
            TitoloDTO titoloDTO = new TitoloDTO();
            titoloDTO.setIdTitolo(simulazione.getTitolo().getIdTitolo());
            titoloDTO.setNome(simulazione.getTitolo().getNome());
            titoloDTO.setCodiceIsin(simulazione.getTitolo().getCodiceIsin());
            titoloDTO.setDataScadenza(simulazione.getTitolo().getDataScadenza());
            titoloDTO.setTassoNominale(simulazione.getTitolo().getTassoNominale());
            titoloDTO.setPeriodicitaCedole(simulazione.getTitolo().getPeriodicitaCedole().toString());
            titoloDTO.setPeriodicitaBollo(simulazione.getTitolo().getPeriodicitaBollo().toString());
            titoloDTO.setTipoTitolo(simulazione.getTitolo().getTipoTitolo());
            titoloDTO.setUtenteId(simulazione.getTitolo().getUtente() != null ? simulazione.getTitolo().getUtente().getIdUtente() : null);
            
            dto.setTitolo(titoloDTO);
        }

        return dto;
    }

    /**
     * Converte un DTO SimulazioneDTO in un'entità Simulazione.
     *
     * @param dto il DTO da convertire
     * @return l'entità corrispondente
     * @throws EntityNotFoundException se il titolo associato non esiste
     */
    public Simulazione toEntity(SimulazioneDTO dto) {
        if (dto == null) {
            return null;
        }

        Simulazione simulazione = new Simulazione();
        simulazione.setIdSimulazione(dto.getIdSimulazione());
        
        if (dto.getIdTitolo() != null) {
            simulazione.setTitolo(titoloRepository.findById(dto.getIdTitolo())
                .orElseThrow(() -> new EntityNotFoundException("Titolo non trovato con ID: " + dto.getIdTitolo())));
        }
        
        simulazione.setDataAcquisto(dto.getDataAcquisto());
        simulazione.setPrezzoAcquisto(dto.getPrezzoAcquisto());
        simulazione.setRendimentoLordo(dto.getRendimentoLordo());
        simulazione.setRendimentoTassato(dto.getRendimentoTassato());
        simulazione.setCommissioniAcquisto(dto.getCommissioniAcquisto());
        simulazione.setRendimentoNettoCedole(dto.getRendimentoNettoCedole());
        simulazione.setImpostaBollo(dto.getImpostaBollo());
        simulazione.setRendimentoNettoBollo(dto.getRendimentoNettoBollo());
        simulazione.setPlusMinusValenza(dto.getPlusMinusValenza());
        simulazione.setVersion(dto.getVersion());
        
        // Mapping dei nuovi campi
        simulazione.setNominale(dto.getNominale());
        simulazione.setPrezzoRiferimentoBollo(dto.getPrezzoRiferimentoBollo());
        simulazione.setCapitaleInvestito(dto.getCapitaleInvestito());
        simulazione.setCapitaleConCommissioni(dto.getCapitaleConCommissioni());
        simulazione.setCedoleNetteAnnue(dto.getCedoleNetteAnnue());
        simulazione.setGuadagnoNettoSenzaCosti(dto.getGuadagnoNettoSenzaCosti());
        simulazione.setRendimentoSenzaCosti(dto.getRendimentoSenzaCosti());
        simulazione.setRendimentoConCommissioni(dto.getRendimentoConCommissioni());
        simulazione.setRendimentoConBolloMensile(dto.getRendimentoConBolloMensile());
        simulazione.setBolloTotaleMensile(dto.getBolloTotaleMensile());
        simulazione.setRendimentoConBolloAnnuale(dto.getRendimentoConBolloAnnuale());
        simulazione.setBolloTotaleAnnuale(dto.getBolloTotaleAnnuale());

        return simulazione;
    }
}
