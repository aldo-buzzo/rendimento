package com.example.rendimento.mapper;

import com.example.rendimento.dto.UtenteDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.model.Utente;
import org.springframework.stereotype.Component;

/**
 * Mapper per la conversione tra entità Utente e DTO UtenteDTO/UtenteResponseDTO.
 */
@Component
public class UtenteMapper {

    /**
     * Converte un'entità Utente in un DTO UtenteDTO.
     *
     * @param utente l'entità da convertire
     * @return il DTO corrispondente
     */
    public UtenteDTO toDTO(Utente utente) {
        if (utente == null) {
            return null;
        }

        UtenteDTO dto = new UtenteDTO();
        dto.setIdUtente(utente.getIdUtente());
        dto.setUsername(utente.getUsername());
        // Non includiamo la password nel DTO per motivi di sicurezza
        dto.setEmail(utente.getEmail());
        dto.setNome(utente.getNome());
        dto.setCognome(utente.getCognome());
        dto.setDataRegistrazione(utente.getDataRegistrazione());
        dto.setIsSystemUser(utente.getIsSystemUser());

        return dto;
    }

    /**
     * Converte un'entità Utente in un DTO UtenteResponseDTO.
     * Questo DTO è specifico per le risposte e non include la password.
     *
     * @param utente l'entità da convertire
     * @return il DTO di risposta corrispondente
     */
    public UtenteResponseDTO toResponseDTO(Utente utente) {
        if (utente == null) {
            return null;
        }

        UtenteResponseDTO responseDTO = new UtenteResponseDTO();
        responseDTO.setIdUtente(utente.getIdUtente());
        responseDTO.setUsername(utente.getUsername());
        responseDTO.setEmail(utente.getEmail());
        responseDTO.setNome(utente.getNome());
        responseDTO.setCognome(utente.getCognome());
        responseDTO.setDataRegistrazione(utente.getDataRegistrazione());
        responseDTO.setIsSystemUser(utente.getIsSystemUser());

        return responseDTO;
    }

    /**
     * Converte un DTO UtenteDTO in un'entità Utente.
     *
     * @param dto il DTO da convertire
     * @return l'entità corrispondente
     */
    public Utente toEntity(UtenteDTO dto) {
        if (dto == null) {
            return null;
        }

        Utente utente = new Utente();
        utente.setIdUtente(dto.getIdUtente());
        utente.setUsername(dto.getUsername());
        utente.setPassword(dto.getPassword()); // La password dovrebbe essere già codificata
        utente.setEmail(dto.getEmail());
        utente.setNome(dto.getNome());
        utente.setCognome(dto.getCognome());
        utente.setDataRegistrazione(dto.getDataRegistrazione());
        utente.setIsSystemUser(dto.getIsSystemUser());

        return utente;
    }

    /**
     * Aggiorna un'entità Utente esistente con i dati di un DTO UtenteDTO.
     *
     * @param utente l'entità da aggiornare
     * @param dto il DTO con i nuovi dati
     * @return l'entità aggiornata
     */
    public Utente updateEntityFromDTO(Utente utente, UtenteDTO dto) {
        if (utente == null || dto == null) {
            return utente;
        }

        // Aggiorniamo solo i campi non null del DTO
        if (dto.getUsername() != null) {
            utente.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null) {
            utente.setPassword(dto.getPassword());
        }
        if (dto.getEmail() != null) {
            utente.setEmail(dto.getEmail());
        }
        if (dto.getNome() != null) {
            utente.setNome(dto.getNome());
        }
        if (dto.getCognome() != null) {
            utente.setCognome(dto.getCognome());
        }
        if (dto.getDataRegistrazione() != null) {
            utente.setDataRegistrazione(dto.getDataRegistrazione());
        }
        if (dto.getIsSystemUser() != null) {
            utente.setIsSystemUser(dto.getIsSystemUser());
        }

        return utente;
    }
}
