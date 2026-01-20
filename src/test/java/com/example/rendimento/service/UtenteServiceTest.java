package com.example.rendimento.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.rendimento.dto.UtenteDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.UtenteRepository;

/**
 * Test per il servizio UtenteService utilizzando un database H2 in memoria.
 * Verifica le funzionalità di registrazione, login e gestione degli utenti.
 */
@SpringBootTest
@ActiveProfiles("test") // Utilizza il profilo di test con il database H2
@Transactional
public class UtenteServiceTest {

    @Autowired
    private UtenteService utenteService;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UtenteDTO createTestUtenteDTO() {
        UtenteDTO utenteDTO = new UtenteDTO();
        utenteDTO.setUsername("testuser");
        utenteDTO.setPassword("password123");
        utenteDTO.setEmail("test@example.com");
        utenteDTO.setNome("Test");
        utenteDTO.setCognome("User");
        return utenteDTO;
    }

    @BeforeEach
    public void setup() {
        // Pulisce il repository prima di ogni test
        utenteRepository.deleteAll();
    }

    @Test
    public void testRegistrazioneUtente() {
        // Crea un utente di test
        UtenteDTO utenteDTO = createTestUtenteDTO();

        // Registra l'utente
        UtenteResponseDTO utenteRegistrato = utenteService.registraUtente(utenteDTO);

        // Verifica che l'utente sia stato registrato correttamente
        assertNotNull(utenteRegistrato);
        assertNotNull(utenteRegistrato.getIdUtente());
        assertEquals("testuser", utenteRegistrato.getUsername());
        assertEquals("test@example.com", utenteRegistrato.getEmail());
        assertEquals("Test", utenteRegistrato.getNome());
        assertEquals("User", utenteRegistrato.getCognome());
        assertNotNull(utenteRegistrato.getDataRegistrazione());
        assertFalse(utenteRegistrato.getIsSystemUser());

        // Verifica che l'utente sia stato salvato nel database
        Optional<Utente> utenteOptional = utenteRepository.findByUsername("testuser");
        assertTrue(utenteOptional.isPresent());
        
        // Verifica che la password sia stata codificata
        Utente utente = utenteOptional.get();
        assertTrue(passwordEncoder.matches("password123", utente.getPassword()));
    }

    @Test
    public void testRegistrazioneUtenteConUsernameEsistente() {
        // Crea e registra un primo utente
        UtenteDTO utenteDTO1 = createTestUtenteDTO();
        utenteService.registraUtente(utenteDTO1);

        // Crea un secondo utente con lo stesso username
        UtenteDTO utenteDTO2 = new UtenteDTO();
        utenteDTO2.setUsername("testuser"); // Stesso username
        utenteDTO2.setPassword("password456");
        utenteDTO2.setEmail("test2@example.com"); // Email diversa
        utenteDTO2.setNome("Test2");
        utenteDTO2.setCognome("User2");

        // Verifica che venga lanciata un'eccezione
        Exception exception = assertThrows(RuntimeException.class, () -> {
            utenteService.registraUtente(utenteDTO2);
        });

        // Verifica il messaggio di errore
        assertTrue(exception.getMessage().contains("Username già in uso"));
    }

    @Test
    public void testRegistrazioneUtenteConEmailEsistente() {
        // Crea e registra un primo utente
        UtenteDTO utenteDTO1 = createTestUtenteDTO();
        utenteService.registraUtente(utenteDTO1);

        // Crea un secondo utente con la stessa email
        UtenteDTO utenteDTO2 = new UtenteDTO();
        utenteDTO2.setUsername("testuser2"); // Username diverso
        utenteDTO2.setPassword("password456");
        utenteDTO2.setEmail("test@example.com"); // Stessa email
        utenteDTO2.setNome("Test2");
        utenteDTO2.setCognome("User2");

        // Verifica che venga lanciata un'eccezione
        Exception exception = assertThrows(RuntimeException.class, () -> {
            utenteService.registraUtente(utenteDTO2);
        });

        // Verifica il messaggio di errore
        assertTrue(exception.getMessage().contains("Email già in uso"));
    }

    @Test
    public void testTrovaUtentePerUsername() {
        // Crea e registra un utente
        UtenteDTO utenteDTO = createTestUtenteDTO();
        UtenteResponseDTO utenteRegistrato = utenteService.registraUtente(utenteDTO);

        // Trova l'utente per username
        Optional<UtenteResponseDTO> utenteOptional = utenteService.findByUsername("testuser");

        // Verifica che l'utente sia stato trovato
        assertTrue(utenteOptional.isPresent());
        assertEquals(utenteRegistrato.getIdUtente(), utenteOptional.get().getIdUtente());
        assertEquals(utenteRegistrato.getUsername(), utenteOptional.get().getUsername());
        assertEquals(utenteRegistrato.getEmail(), utenteOptional.get().getEmail());
    }

    @Test
    public void testTrovaUtentePerEmail() {
        // Crea e registra un utente
        UtenteDTO utenteDTO = createTestUtenteDTO();
        UtenteResponseDTO utenteRegistrato = utenteService.registraUtente(utenteDTO);

        // Trova l'utente per email
        Optional<UtenteResponseDTO> utenteOptional = utenteService.findByEmail("test@example.com");

        // Verifica che l'utente sia stato trovato
        assertTrue(utenteOptional.isPresent());
        assertEquals(utenteRegistrato.getIdUtente(), utenteOptional.get().getIdUtente());
        assertEquals(utenteRegistrato.getUsername(), utenteOptional.get().getUsername());
        assertEquals(utenteRegistrato.getEmail(), utenteOptional.get().getEmail());
    }

    @Test
    public void testCambioPassword() {
        // Crea e registra un utente
        UtenteDTO utenteDTO = createTestUtenteDTO();
        UtenteResponseDTO utenteRegistrato = utenteService.registraUtente(utenteDTO);

        // Cambia la password
        boolean risultato = utenteService.changePassword(
                utenteRegistrato.getIdUtente(),
                "password123", // Vecchia password
                "nuovaPassword456" // Nuova password
        );

        // Verifica che il cambio password sia avvenuto con successo
        assertTrue(risultato);

        // Verifica che la password sia stata aggiornata nel database
        Optional<Utente> utenteOptional = utenteRepository.findById(utenteRegistrato.getIdUtente());
        assertTrue(utenteOptional.isPresent());
        
        Utente utente = utenteOptional.get();
        assertTrue(passwordEncoder.matches("nuovaPassword456", utente.getPassword()));
        assertFalse(passwordEncoder.matches("password123", utente.getPassword()));
    }

    @Test
    public void testCambioPasswordConVecchiaPasswordErrata() {
        // Crea e registra un utente
        UtenteDTO utenteDTO = createTestUtenteDTO();
        UtenteResponseDTO utenteRegistrato = utenteService.registraUtente(utenteDTO);

        // Tenta di cambiare la password con una vecchia password errata
        boolean risultato = utenteService.changePassword(
                utenteRegistrato.getIdUtente(),
                "passwordErrata", // Vecchia password errata
                "nuovaPassword456" // Nuova password
        );

        // Verifica che il cambio password sia fallito
        assertFalse(risultato);

        // Verifica che la password non sia stata aggiornata nel database
        Optional<Utente> utenteOptional = utenteRepository.findById(utenteRegistrato.getIdUtente());
        assertTrue(utenteOptional.isPresent());
        
        Utente utente = utenteOptional.get();
        assertTrue(passwordEncoder.matches("password123", utente.getPassword()));
        assertFalse(passwordEncoder.matches("nuovaPassword456", utente.getPassword()));
    }

    @Test
    public void testUpdateUtente() {
        // Crea e registra un utente
        UtenteDTO utenteDTO = createTestUtenteDTO();
        UtenteResponseDTO utenteRegistrato = utenteService.registraUtente(utenteDTO);

        // Crea un DTO con i dati aggiornati
        UtenteDTO utenteAggiornato = new UtenteDTO();
        utenteAggiornato.setNome("NomeAggiornato");
        utenteAggiornato.setCognome("CognomeAggiornato");

        // Aggiorna l'utente
        UtenteResponseDTO risultato = utenteService.updateUtente(utenteRegistrato.getIdUtente(), utenteAggiornato);

        // Verifica che l'utente sia stato aggiornato correttamente
        assertNotNull(risultato);
        assertEquals("NomeAggiornato", risultato.getNome());
        assertEquals("CognomeAggiornato", risultato.getCognome());
        assertEquals("testuser", risultato.getUsername()); // Username non modificato
        assertEquals("test@example.com", risultato.getEmail()); // Email non modificata

        // Verifica che l'utente sia stato aggiornato nel database
        Optional<Utente> utenteOptional = utenteRepository.findById(utenteRegistrato.getIdUtente());
        assertTrue(utenteOptional.isPresent());
        
        Utente utente = utenteOptional.get();
        assertEquals("NomeAggiornato", utente.getNome());
        assertEquals("CognomeAggiornato", utente.getCognome());
    }

    @Test
    public void testEliminaUtente() {
        // Crea e registra un utente
        UtenteDTO utenteDTO = createTestUtenteDTO();
        UtenteResponseDTO utenteRegistrato = utenteService.registraUtente(utenteDTO);

        // Verifica che l'utente esista
        assertTrue(utenteRepository.existsById(utenteRegistrato.getIdUtente()));

        // Elimina l'utente
        utenteService.deleteUtente(utenteRegistrato.getIdUtente());

        // Verifica che l'utente sia stato eliminato
        assertFalse(utenteRepository.existsById(utenteRegistrato.getIdUtente()));
    }
}
