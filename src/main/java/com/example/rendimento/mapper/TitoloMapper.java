package com.example.rendimento.mapper;

import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.model.Titolo;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe di utilità per la conversione tra l'entità Titolo e il DTO TitoloDTO.
 */
@Component
public class TitoloMapper {

    /**
     * Converte un'entità Titolo in un DTO TitoloDTO.
     *
     * @param entity l'entità da convertire
     * @return il DTO risultante, o null se l'entità è null
     */
    public TitoloDTO toDto(Titolo entity) {
        if (entity == null) {
            return null;
        }
        
        return new TitoloDTO(
                entity.getIdTitolo(),
                entity.getNome(),
                entity.getCodiceIsin(),
                entity.getDataScadenza(),
                entity.getTassoNominale(),
                entity.getPeriodicitaCedole(),
                entity.getPeriodicitaBollo()
        );
    }

    /**
     * Converte un DTO TitoloDTO in un'entità Titolo.
     *
     * @param dto il DTO da convertire
     * @return l'entità risultante, o null se il DTO è null
     */
    public Titolo toEntity(TitoloDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Titolo entity = new Titolo();
        entity.setIdTitolo(dto.getIdTitolo());
        entity.setNome(dto.getNome());
        entity.setCodiceIsin(dto.getCodiceIsin());
        entity.setDataScadenza(dto.getDataScadenza());
        entity.setTassoNominale(dto.getTassoNominale());
        entity.setPeriodicitaCedole(dto.getPeriodicitaCedole());
        entity.setPeriodicitaBollo(dto.getPeriodicitaBollo());
        
        return entity;
    }

    /**
     * Converte una lista di entità Titolo in una lista di DTO TitoloDTO.
     *
     * @param entities la lista di entità da convertire
     * @return la lista di DTO risultante
     */
    public List<TitoloDTO> toDtoList(List<Titolo> entities) {
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
    public Titolo updateEntityFromDto(Titolo entity, TitoloDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }
        
        // Non aggiorniamo l'ID perché è la chiave primaria
        entity.setNome(dto.getNome());
        entity.setCodiceIsin(dto.getCodiceIsin());
        entity.setDataScadenza(dto.getDataScadenza());
        entity.setTassoNominale(dto.getTassoNominale());
        entity.setPeriodicitaCedole(dto.getPeriodicitaCedole());
        entity.setPeriodicitaBollo(dto.getPeriodicitaBollo());
        
        return entity;
    }
}