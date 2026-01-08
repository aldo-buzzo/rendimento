package com.example.rendimento.mapper;

import com.example.rendimento.dto.SimulazioneDTO;
import com.example.rendimento.model.Simulazione;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.repository.TitoloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe di utilità per la conversione tra l'entità Simulazione e il DTO SimulazioneDTO.
 */
@Component
public class SimulazioneMapper {

    private final TitoloMapper titoloMapper;
    private final TitoloRepository titoloRepository;

    /**
     * Costruttore con parametri.
     *
     * @param titoloMapper il mapper per la conversione tra Titolo e TitoloDTO
     * @param titoloRepository il repository per accedere ai dati dei titoli
     */
    @Autowired
    public SimulazioneMapper(TitoloMapper titoloMapper, TitoloRepository titoloRepository) {
        this.titoloMapper = titoloMapper;
        this.titoloRepository = titoloRepository;
    }

    /**
     * Converte un'entità Simulazione in un DTO SimulazioneDTO.
     *
     * @param entity l'entità da convertire
     * @return il DTO risultante, o null se l'entità è null
     */
    public SimulazioneDTO toDto(Simulazione entity) {
        if (entity == null) {
            return null;
        }
        
        SimulazioneDTO dto = new SimulazioneDTO(
                entity.getIdSimulazione(),
                entity.getTitolo() != null ? entity.getTitolo().getIdTitolo() : null,
                entity.getDataAcquisto(),
                entity.getPrezzoAcquisto(),
                entity.getRendimentoLordo(),
                entity.getRendimentoTassato(),
                entity.getCommissioniAcquisto(),
                entity.getRendimentoNettoCedole(),
                entity.getImpostaBollo(),
                entity.getRendimentoNettoBollo(),
                entity.getPlusMinusValenza()
        );
        
        // Se l'entità ha un titolo associato, convertiamo anche quello
        if (entity.getTitolo() != null) {
            dto.setTitolo(titoloMapper.toDto(entity.getTitolo()));
        }
        
        return dto;
    }

    /**
     * Converte un DTO SimulazioneDTO in un'entità Simulazione.
     *
     * @param dto il DTO da convertire
     * @return l'entità risultante, o null se il DTO è null
     */
    public Simulazione toEntity(SimulazioneDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Simulazione entity = new Simulazione();
        entity.setIdSimulazione(dto.getIdSimulazione());
        entity.setDataAcquisto(dto.getDataAcquisto());
        entity.setPrezzoAcquisto(dto.getPrezzoAcquisto());
        entity.setRendimentoLordo(dto.getRendimentoLordo());
        entity.setRendimentoTassato(dto.getRendimentoTassato());
        entity.setCommissioniAcquisto(dto.getCommissioniAcquisto());
        entity.setRendimentoNettoCedole(dto.getRendimentoNettoCedole());
        entity.setImpostaBollo(dto.getImpostaBollo());
        entity.setRendimentoNettoBollo(dto.getRendimentoNettoBollo());
        entity.setPlusMinusValenza(dto.getPlusMinusValenza());
        
        // Gestiamo la relazione con Titolo
        if (dto.getIdTitolo() != null) {
            Titolo titolo = titoloRepository.findById(dto.getIdTitolo()).orElse(null);
            entity.setTitolo(titolo);
        } else if (dto.getTitolo() != null && dto.getTitolo().getIdTitolo() != null) {
            Titolo titolo = titoloRepository.findById(dto.getTitolo().getIdTitolo()).orElse(null);
            entity.setTitolo(titolo);
        }
        
        return entity;
    }

    /**
     * Converte una lista di entità Simulazione in una lista di DTO SimulazioneDTO.
     *
     * @param entities la lista di entità da convertire
     * @return la lista di DTO risultante
     */
    public List<SimulazioneDTO> toDtoList(List<Simulazione> entities) {
        if (entities == null) {
            return List.of();
        }
        
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Aggiorna un'entità esistente con i dati di un DTO.
     *
     * @param entity l'entità da aggiornare
     * @param dto il DTO contenente i dati aggiornati
     * @return l'entità aggiornata
     */
    public Simulazione updateEntityFromDto(Simulazione entity, SimulazioneDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }
        
        // Non aggiorniamo l'ID perché è la chiave primaria
        entity.setDataAcquisto(dto.getDataAcquisto());
        entity.setPrezzoAcquisto(dto.getPrezzoAcquisto());
        entity.setRendimentoLordo(dto.getRendimentoLordo());
        entity.setRendimentoTassato(dto.getRendimentoTassato());
        entity.setCommissioniAcquisto(dto.getCommissioniAcquisto());
        entity.setRendimentoNettoCedole(dto.getRendimentoNettoCedole());
        entity.setImpostaBollo(dto.getImpostaBollo());
        entity.setRendimentoNettoBollo(dto.getRendimentoNettoBollo());
        entity.setPlusMinusValenza(dto.getPlusMinusValenza());
        
        // Aggiorniamo la relazione con Titolo se necessario
        if (dto.getIdTitolo() != null) {
            Titolo titolo = titoloRepository.findById(dto.getIdTitolo()).orElse(null);
            entity.setTitolo(titolo);
        } else if (dto.getTitolo() != null && dto.getTitolo().getIdTitolo() != null) {
            Titolo titolo = titoloRepository.findById(dto.getTitolo().getIdTitolo()).orElse(null);
            entity.setTitolo(titolo);
        }
        
        return entity;
    }
}