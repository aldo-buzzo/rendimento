package com.example.rendimento.mapper;

import com.example.rendimento.dto.ProfiloCalcoloDTO;
import com.example.rendimento.model.ProfiloCalcolo;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mapper per convertire tra l'entità ProfiloCalcolo e il relativo DTO.
 */
@Component
public class ProfiloCalcoloMapper {

    @Autowired
    private UtenteRepository utenteRepository;

    /**
     * Converte un'entità ProfiloCalcolo in un DTO.
     *
     * @param entity l'entità da convertire
     * @return il DTO corrispondente
     */
    public ProfiloCalcoloDTO toDto(ProfiloCalcolo entity) {
        if (entity == null) {
            return null;
        }

        ProfiloCalcoloDTO dto = new ProfiloCalcoloDTO();
        dto.setIdProfilo(entity.getIdProfilo());
        dto.setIdUtente(entity.getUtente() != null ? entity.getUtente().getIdUtente() : null);
        dto.setNome(entity.getNome());
        dto.setPeriodicitaBollo(entity.getPeriodicitaBollo());
        dto.setPercentualeBollo(entity.getPercentualeBollo());
        dto.setCommissioneBtp(entity.getCommissioneBtp());
        dto.setCommissioneBot120gg(entity.getCommissioneBot120gg());
        dto.setCommissioneBot240gg(entity.getCommissioneBot240gg());
        dto.setCommissioneBotOltre(entity.getCommissioneBotOltre());
        dto.setCommissioneCct(entity.getCommissioneCct());
        dto.setCommissioneCtz(entity.getCommissioneCtz());
        dto.setIsDefault(entity.getIsDefault());
        dto.setPlusvalenzaEsente(entity.getPlusvalenzaEsente());

        return dto;
    }

    /**
     * Converte un DTO ProfiloCalcolo in un'entità.
     *
     * @param dto il DTO da convertire
     * @return l'entità corrispondente
     * @throws IllegalArgumentException se l'utente specificato nel DTO non esiste
     */
    public ProfiloCalcolo toEntity(ProfiloCalcoloDTO dto) {
        if (dto == null) {
            return null;
        }

        ProfiloCalcolo entity = new ProfiloCalcolo();
        entity.setIdProfilo(dto.getIdProfilo());
        
        // Recupera l'utente dal repository
        if (dto.getIdUtente() != null) {
            Optional<Utente> utenteOpt = utenteRepository.findById(dto.getIdUtente());
            if (utenteOpt.isPresent()) {
                entity.setUtente(utenteOpt.get());
            } else {
                throw new IllegalArgumentException("Utente non trovato con ID: " + dto.getIdUtente());
            }
        }
        
        entity.setNome(dto.getNome());
        entity.setPeriodicitaBollo(dto.getPeriodicitaBollo());
        entity.setPercentualeBollo(dto.getPercentualeBollo());
        entity.setCommissioneBtp(dto.getCommissioneBtp());
        entity.setCommissioneBot120gg(dto.getCommissioneBot120gg());
        entity.setCommissioneBot240gg(dto.getCommissioneBot240gg());
        entity.setCommissioneBotOltre(dto.getCommissioneBotOltre());
        entity.setCommissioneCct(dto.getCommissioneCct());
        entity.setCommissioneCtz(dto.getCommissioneCtz());
        entity.setIsDefault(dto.getIsDefault());
        entity.setPlusvalenzaEsente(dto.getPlusvalenzaEsente());

        return entity;
    }

    /**
     * Aggiorna un'entità esistente con i dati di un DTO.
     *
     * @param entity l'entità da aggiornare
     * @param dto il DTO con i nuovi dati
     * @return l'entità aggiornata
     */
    public ProfiloCalcolo updateEntityFromDto(ProfiloCalcolo entity, ProfiloCalcoloDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        // Non aggiorniamo l'ID o l'utente, poiché sono identificatori
        entity.setNome(dto.getNome());
        entity.setPeriodicitaBollo(dto.getPeriodicitaBollo());
        entity.setPercentualeBollo(dto.getPercentualeBollo());
        entity.setCommissioneBtp(dto.getCommissioneBtp());
        entity.setCommissioneBot120gg(dto.getCommissioneBot120gg());
        entity.setCommissioneBot240gg(dto.getCommissioneBot240gg());
        entity.setCommissioneBotOltre(dto.getCommissioneBotOltre());
        entity.setCommissioneCct(dto.getCommissioneCct());
        entity.setCommissioneCtz(dto.getCommissioneCtz());
        entity.setIsDefault(dto.getIsDefault());
        entity.setPlusvalenzaEsente(dto.getPlusvalenzaEsente());

        return entity;
    }

    /**
     * Converte una lista di entità in una lista di DTO.
     *
     * @param entities la lista di entità da convertire
     * @return la lista di DTO corrispondenti
     */
    public List<ProfiloCalcoloDTO> toDtoList(List<ProfiloCalcolo> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Converte una lista di DTO in una lista di entità.
     *
     * @param dtos la lista di DTO da convertire
     * @return la lista di entità corrispondenti
     */
    public List<ProfiloCalcolo> toEntityList(List<ProfiloCalcoloDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}