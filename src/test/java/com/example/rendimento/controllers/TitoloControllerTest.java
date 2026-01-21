package com.example.rendimento.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.repository.UtenteRepository;
import com.example.rendimento.service.TitoloService;
import com.example.rendimento.service.UtenteService;

/**
 * Test per il controller TitoloController utilizzando un database H2 in memoria.
 */
@SpringBootTest
@ActiveProfiles("test") // Utilizza il profilo di test con il database H2
@Transactional
public class TitoloControllerTest {

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;

    @Autowired
    private TitoloRepository titoloRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private TitoloService titoloService;

    @Autowired
    private UtenteService utenteService;

    private Utente utente1;
    private Utente utente2;
    private Titolo titolo1;
    private Titolo titolo2;

    @BeforeEach
    public void setup() {
        // Configura MockMvc manualmente
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        // Crea due utenti di test
        utente1 = new Utente(
                "testuser1",
                "password123",
                "test1@example.com",
                "Test1",
                "User1",
                LocalDateTime.now()
        );
        
        utente2 = new Utente(
                "testuser2",
                "password456",
                "test2@example.com",
                "Test2",
                "User2",
                LocalDateTime.now()
        );
        
        utenteRepository.save(utente1);
        utenteRepository.save(utente2);

        // Crea titoli per entrambi gli utenti
        titolo1 = new Titolo(
                "BTP Utente 1",
                "IT0005777777",
                LocalDate.of(2027, 5, 15),
                new BigDecimal("3.25"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente1
        );
        
        titolo2 = new Titolo(
                "BTP Utente 2",
                "IT0005888888",
                LocalDate.of(2028, 7, 20),
                new BigDecimal("3.75"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente2
        );
        
        titoloRepository.save(titolo1);
        titoloRepository.save(titolo2);
    }

    @Test
    @WithMockUser(username = "testuser1")
    public void testGetAllTitoli() throws Exception {
        // Esegui la richiesta GET /api/titolo
        mockMvc.perform(get("/api/titolo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("BTP Utente 1")))
                .andExpect(jsonPath("$[0].codiceIsin", is("IT0005777777")));
    }

    @Test
    @WithMockUser(username = "testuser1")
    public void testGetTitoloById() throws Exception {
        // Esegui la richiesta GET /api/titolo/{id}
        mockMvc.perform(get("/api/titolo/" + titolo1.getIdTitolo())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nome", is("BTP Utente 1")))
                .andExpect(jsonPath("$.codiceIsin", is("IT0005777777")));
    }

    @Test
    @WithMockUser(username = "testuser1")
    public void testGetTitoloByIdNonAutorizzato() throws Exception {
        // Esegui la richiesta GET /api/titolo/{id} per un titolo di un altro utente
        mockMvc.perform(get("/api/titolo/" + titolo2.getIdTitolo())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser1")
    public void testGetTitoloByIdNonEsistente() throws Exception {
        // Esegui la richiesta GET /api/titolo/{id} per un titolo che non esiste
        mockMvc.perform(get("/api/titolo/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser1")
    public void testGetTitoloByCodiceIsin() throws Exception {
        // Esegui la richiesta GET /api/titolo/isin/{codiceIsin}
        mockMvc.perform(get("/api/titolo/isin/IT0005777777")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nome", is("BTP Utente 1")))
                .andExpect(jsonPath("$.codiceIsin", is("IT0005777777")));
    }

    @Test
    @WithMockUser(username = "testuser1")
    public void testGetTitoloByCodiceIsinNonEsistente() throws Exception {
        // Esegui la richiesta GET /api/titolo/isin/{codiceIsin} per un titolo che non esiste
        mockMvc.perform(get("/api/titolo/isin/CODICEINESISTENTE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
