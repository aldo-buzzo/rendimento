package com.example.rendimento.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.repository.UtenteRepository;

/**
 * Test per verificare il filtraggio dei titoli per utente.
 */
@SpringBootTest
@ActiveProfiles("test") // Utilizza il profilo di test con il database H2
@Transactional
public class TitoloServiceFilterTest {

    @Autowired
    private TitoloService titoloService;

    @Autowired
    private TitoloRepository titoloRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    private Utente utente1;
    private Utente utente2;

    @BeforeEach
    public void setup() {
        // Pulisce il repository prima di ogni test
        titoloRepository.deleteAll();
        
        // Crea due utenti di test
        utente1 = new Utente();
        utente1.setUsername("user1");
        utente1.setPassword("password1");
        utente1.setEmail("user1@example.com");
        utente1.setNome("Nome1");
        utente1.setCognome("Cognome1");
        utente1.setDataRegistrazione(LocalDateTime.now());
        utente1 = utenteRepository.save(utente1);
        
        utente2 = new Utente();
        utente2.setUsername("user2");
        utente2.setPassword("password2");
        utente2.setEmail("user2@example.com");
        utente2.setNome("Nome2");
        utente2.setCognome("Cognome2");
        utente2.setDataRegistrazione(LocalDateTime.now());
        utente2 = utenteRepository.save(utente2);
        
        // Crea titoli per l'utente 1
        Titolo titolo1 = new Titolo();
        titolo1.setNome("Titolo 1 - Utente 1");
        titolo1.setCodiceIsin("IT0001234561");
        titolo1.setDataScadenza(LocalDate.now().plusYears(5));
        titolo1.setTipoTitolo(com.example.rendimento.enums.TipoTitolo.BTP);
        titolo1.setPeriodicitaCedole("SEMESTRALE");
        titolo1.setPeriodicitaBollo("ANNUALE");
        titolo1.setTassoNominale(new java.math.BigDecimal("2.50"));
        titolo1.setUtente(utente1);
        titoloRepository.save(titolo1);
        
        Titolo titolo2 = new Titolo();
        titolo2.setNome("Titolo 2 - Utente 1");
        titolo2.setCodiceIsin("IT0001234562");
        titolo2.setDataScadenza(LocalDate.now().plusYears(3));
        titolo2.setTipoTitolo(com.example.rendimento.enums.TipoTitolo.BOT);
        titolo2.setPeriodicitaCedole("SEMESTRALE");
        titolo2.setPeriodicitaBollo("ANNUALE");
        titolo2.setTassoNominale(new java.math.BigDecimal("1.75"));
        titolo2.setUtente(utente1);
        titoloRepository.save(titolo2);
        
        // Crea titoli per l'utente 2
        Titolo titolo3 = new Titolo();
        titolo3.setNome("Titolo 3 - Utente 2");
        titolo3.setCodiceIsin("IT0001234563");
        titolo3.setDataScadenza(LocalDate.now().plusYears(4));
        titolo3.setTipoTitolo(com.example.rendimento.enums.TipoTitolo.BTP);
        titolo3.setPeriodicitaCedole("SEMESTRALE");
        titolo3.setPeriodicitaBollo("ANNUALE");
        titolo3.setTassoNominale(new java.math.BigDecimal("3.25"));
        titolo3.setUtente(utente2);
        titoloRepository.save(titolo3);
    }

    @Test
    public void testGetAllTitoli() {
        // Verifica che getAllTitoli restituisca tutti i titoli (3)
        List<TitoloDTO> allTitoli = titoloService.getAllTitoli();
        assertEquals(3, allTitoli.size(), "getAllTitoli dovrebbe restituire tutti i titoli");
    }

    @Test
    public void testGetTitoliByUtenteId() {
        // Verifica che getTitoliByUtenteId restituisca solo i titoli dell'utente 1 (2)
        List<TitoloDTO> titoliUtente1 = titoloService.getTitoliByUtenteId(utente1.getIdUtente());
        assertEquals(2, titoliUtente1.size(), "getTitoliByUtenteId dovrebbe restituire solo i titoli dell'utente 1");
        
        // Verifica che i titoli restituiti appartengano effettivamente all'utente 1
        for (TitoloDTO titolo : titoliUtente1) {
            assertEquals(utente1.getIdUtente(), titolo.getUtenteId(), 
                    "Il titolo dovrebbe appartenere all'utente 1");
        }
        
        // Verifica che getTitoliByUtenteId restituisca solo i titoli dell'utente 2 (1)
        List<TitoloDTO> titoliUtente2 = titoloService.getTitoliByUtenteId(utente2.getIdUtente());
        assertEquals(1, titoliUtente2.size(), "getTitoliByUtenteId dovrebbe restituire solo i titoli dell'utente 2");
        
        // Verifica che i titoli restituiti appartengano effettivamente all'utente 2
        for (TitoloDTO titolo : titoliUtente2) {
            assertEquals(utente2.getIdUtente(), titolo.getUtenteId(), 
                    "Il titolo dovrebbe appartenere all'utente 2");
        }
    }
}
