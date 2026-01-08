package com.example.rendimento.mapper;

import com.example.rendimento.dto.AppMetadataDTO;
import com.example.rendimento.model.AppMetadata;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe di utilità per la conversione tra l'entità AppMetadata e il DTO AppMetadataDTO.
 */
@Component
public class AppMetadataMapper {

    /**
     * Converte un'entità AppMetadata in un DTO AppMetadataDTO.
     *
     * @param entity l'entità da convertire
     * @return il DTO risultante, o null se l'entità è null
     */
    public AppMetadataDTO toDto(AppMetadata entity) {
        if (entity == null) {
            return null;
        }
        
        return new AppMetadataDTO(
                entity.getId(),
                entity.getAppName(),
                entity.getAppVersion(),
                entity.getCreatedAt()
        );
    }

    /**
     * Converte un DTO AppMetadataDTO in un'entità AppMetadata.
     *
     * @param dto il DTO da convertire
     * @return l'entità risultante, o null se il DTO è null
     */
    public AppMetadata toEntity(AppMetadataDTO dto) {
        if (dto == null) {
            return null;
        }
        
        AppMetadata entity = new AppMetadata();
        entity.setId(dto.getId());
        entity.setAppName(dto.getAppName());
        entity.setAppVersion(dto.getAppVersion());
        entity.setCreatedAt(dto.getCreatedAt());
        
        return entity;
    }

    /**
     * Converte una lista di entità AppMetadata in una lista di DTO AppMetadataDTO.
     *
     * @param entities la lista di entità da convertire
     * @return la lista di DTO risultante
     */
    public List<AppMetadataDTO> toDtoList(List<AppMetadata> entities) {
        if (entities == null) {
            return List.of();
        }
        
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}