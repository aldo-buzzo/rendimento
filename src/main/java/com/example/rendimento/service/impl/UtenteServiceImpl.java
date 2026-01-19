package com.example.rendimento.service.impl;

import com.example.rendimento.dto.UtenteDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.mapper.UtenteMapper;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.UtenteRepository;
import com.example.rendimento.service.UtenteService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementazione del servizio di gestione degli utenti.
 */
@Service
public class UtenteServiceImpl implements UtenteService {
    
    private final UtenteRepository utenteRepository;
    private final UtenteMapper utenteMapper;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Costruttore con parametri per l'iniezione delle dipendenze.
     *
     * @param utenteRepository repository per le operazioni CRUD sugli utenti
     * @param utenteMapper mapper per la conversione tra entità e DTO
     * @param passwordEncoder encoder per la codifica delle password
     */
    @Autowired
    public UtenteServiceImpl(UtenteRepository utenteRepository, UtenteMapper utenteMapper, @Lazy PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.utenteMapper = utenteMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional
    public UtenteResponseDTO registraUtente(UtenteDTO utenteDTO) {
        // Verifica se l'username è già in uso
        if (utenteRepository.existsByUsername(utenteDTO.getUsername())) {
            throw new RuntimeException("Username già in uso");
        }
        
        // Verifica se l'email è già in uso
        if (utenteRepository.existsByEmail(utenteDTO.getEmail())) {
            throw new RuntimeException("Email già in uso");
        }
        
        // Codifica la password
        utenteDTO.setPassword(passwordEncoder.encode(utenteDTO.getPassword()));
        
        // Imposta la data di registrazione
        utenteDTO.setDataRegistrazione(LocalDateTime.now());
        
        // Imposta isSystemUser a false per i nuovi utenti
        utenteDTO.setIsSystemUser(false);
        
        // Converte il DTO in entità e salva
        Utente utente = utenteMapper.toEntity(utenteDTO);
        Utente savedUtente = utenteRepository.save(utente);
        
        // Converte l'entità salvata in DTO di risposta
        return utenteMapper.toResponseDTO(savedUtente);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UtenteResponseDTO> findById(Integer id) {
        return utenteRepository.findById(id)
                .map(utenteMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UtenteResponseDTO> findByUsername(String username) {
        return utenteRepository.findByUsername(username)
                .map(utenteMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UtenteResponseDTO> findByEmail(String email) {
        return utenteRepository.findByEmail(email)
                .map(utenteMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UtenteResponseDTO> findSystemUser() {
        return utenteRepository.findByIsSystemUserTrue()
                .map(utenteMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return utenteRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return utenteRepository.existsByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UtenteResponseDTO> findAll() {
        return utenteRepository.findAll().stream()
                .map(utenteMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UtenteResponseDTO updateUtente(Integer id, UtenteDTO utenteDTO) {
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + id));
        
        // Verifica se il nuovo username è già in uso da un altro utente
        if (utenteDTO.getUsername() != null && !utenteDTO.getUsername().equals(utente.getUsername()) &&
                utenteRepository.existsByUsername(utenteDTO.getUsername())) {
            throw new RuntimeException("Username già in uso");
        }
        
        // Verifica se la nuova email è già in uso da un altro utente
        if (utenteDTO.getEmail() != null && !utenteDTO.getEmail().equals(utente.getEmail()) &&
                utenteRepository.existsByEmail(utenteDTO.getEmail())) {
            throw new RuntimeException("Email già in uso");
        }
        
        // Se è presente una nuova password, la codifica
        if (utenteDTO.getPassword() != null) {
            utenteDTO.setPassword(passwordEncoder.encode(utenteDTO.getPassword()));
        }
        
        // Aggiorna l'entità con i dati del DTO
        utente = utenteMapper.updateEntityFromDTO(utente, utenteDTO);
        
        // Salva l'entità aggiornata
        Utente updatedUtente = utenteRepository.save(utente);
        
        // Converte l'entità aggiornata in DTO di risposta
        return utenteMapper.toResponseDTO(updatedUtente);
    }
    
    @Override
    @Transactional
    public void deleteUtente(Integer id) {
        if (!utenteRepository.existsById(id)) {
            throw new EntityNotFoundException("Utente non trovato con ID: " + id);
        }
        
        utenteRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public boolean changePassword(Integer id, String oldPassword, String newPassword) {
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con ID: " + id));
        
        // Verifica che la vecchia password sia corretta
        if (!passwordEncoder.matches(oldPassword, utente.getPassword())) {
            return false;
        }
        
        // Codifica e imposta la nuova password
        utente.setPassword(passwordEncoder.encode(newPassword));
        
        // Salva l'entità aggiornata
        utenteRepository.save(utente);
        
        return true;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Utente getUtenteEntityByUsername(String username) {
        return utenteRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Utente non trovato con username: " + username));
    }
}
