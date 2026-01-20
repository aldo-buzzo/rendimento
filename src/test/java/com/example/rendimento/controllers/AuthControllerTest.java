package com.example.rendimento.controllers;

import com.example.rendimento.dto.UtenteDTO;
import com.example.rendimento.dto.UtenteResponseDTO;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.UtenteRepository;
import com.example.rendimento.service.UtenteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test per il controller AuthController utilizzando un database H2 in memoria.
 * Verifica le funzionalità di registrazione, login e gestione dell'autenticazione.
 */
@SpringBootTest
@ActiveProfiles("test") // Utilizza il profilo di test con il database H2
@Transactional
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;

    // Creiamo l'ObjectMapper manualmente invece di utilizzare l'autowiring
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UtenteService utenteService;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        // Configura MockMvc manualmente
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
                
        // Pulisce il repository prima di ogni test
        utenteRepository.deleteAll();
    }

    private UtenteDTO createTestUtenteDTO() {
        UtenteDTO utenteDTO = new UtenteDTO();
        utenteDTO.setUsername("testuser");
        utenteDTO.setPassword("password123");
        utenteDTO.setEmail("test@example.com");
        utenteDTO.setNome("Test");
        utenteDTO.setCognome("User");
        return utenteDTO;
    }

    @Test
    public void testRegistrazioneUtente() throws Exception {
        UtenteDTO utenteDTO = createTestUtenteDTO();

        // Esegue la richiesta POST per registrare un nuovo utente
        MvcResult result = mockMvc.perform(post("/api/auth/registrazione")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(utenteDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nome").value("Test"))
                .andExpect(jsonPath("$.cognome").value("User"))
                .andExpect(jsonPath("$.idUtente").exists())
                .andReturn();

        // Verifica che l'utente sia stato salvato nel database
        assertTrue(utenteRepository.existsByUsername("testuser"));
        
        // Verifica che la password sia stata codificata
        Utente utente = utenteRepository.findByUsername("testuser").orElseThrow();
        assertTrue(passwordEncoder.matches("password123", utente.getPassword()));
    }

    @Test
    public void testRegistrazioneUtenteConUsernameEsistente() throws Exception {
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

        // Esegue la richiesta POST per registrare il secondo utente
        mockMvc.perform(post("/api/auth/registrazione")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(utenteDTO2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errore").value("Username già in uso"));
    }

    @Test
    public void testRegistrazioneUtenteConEmailEsistente() throws Exception {
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

        // Esegue la richiesta POST per registrare il secondo utente
        mockMvc.perform(post("/api/auth/registrazione")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(utenteDTO2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errore").value("Email già in uso"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetUtenteCorrente() throws Exception {
        // Crea e registra un utente
        UtenteDTO utenteDTO = createTestUtenteDTO();
        UtenteResponseDTO utenteRegistrato = utenteService.registraUtente(utenteDTO);

        // Esegue la richiesta GET per ottenere l'utente corrente
        mockMvc.perform(get("/api/auth/utente-corrente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nome").value("Test"))
                .andExpect(jsonPath("$.cognome").value("User"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testCambioPassword() throws Exception {
        // Crea e registra un utente
        UtenteDTO utenteDTO = createTestUtenteDTO();
        UtenteResponseDTO utenteRegistrato = utenteService.registraUtente(utenteDTO);

        // Crea la mappa con le password
        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "password123");
        passwordMap.put("newPassword", "nuovaPassword456");

        // Esegue la richiesta POST per cambiare la password
        mockMvc.perform(post("/api/auth/cambia-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messaggio").value("Password cambiata con successo"));

        // Verifica che la password sia stata aggiornata nel database
        Utente utente = utenteRepository.findByUsername("testuser").orElseThrow();
        assertTrue(passwordEncoder.matches("nuovaPassword456", utente.getPassword()));
        assertFalse(passwordEncoder.matches("password123", utente.getPassword()));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testCambioPasswordConVecchiaPasswordErrata() throws Exception {
        // Crea e registra un utente
        UtenteDTO utenteDTO = createTestUtenteDTO();
        UtenteResponseDTO utenteRegistrato = utenteService.registraUtente(utenteDTO);

        // Crea la mappa con le password
        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "passwordErrata");
        passwordMap.put("newPassword", "nuovaPassword456");

        // Esegue la richiesta POST per cambiare la password
        mockMvc.perform(post("/api/auth/cambia-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordMap)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errore").value("La vecchia password non è corretta"));

        // Verifica che la password non sia stata aggiornata nel database
        Utente utente = utenteRepository.findByUsername("testuser").orElseThrow();
        assertTrue(passwordEncoder.matches("password123", utente.getPassword()));
        assertFalse(passwordEncoder.matches("nuovaPassword456", utente.getPassword()));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testLogout() throws Exception {
        // Esegue la richiesta POST per effettuare il logout
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().is3xxRedirection()); // Ci aspettiamo un reindirizzamento
    }

    @Test
    public void testShowLoginPage() throws Exception {
        // Esegue la richiesta GET per visualizzare la pagina di login
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    public void testShowRegistrationPage() throws Exception {
        // Esegue la richiesta GET per visualizzare la pagina di registrazione
        mockMvc.perform(get("/registrazione"))
                .andExpect(status().isOk());
    }
}
