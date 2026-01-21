package com.example.rendimento.repository;

import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.model.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per il repository TitoloRepository utilizzando un database H2 in memoria.
 */
@SpringBootTest
@ActiveProfiles("test") // Utilizza il profilo di test con il database H2
@Transactional
public class TitoloRepositoryTest {

    @Autowired
    private TitoloRepository titoloRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    private Utente utente;

    @BeforeEach
    public void setup() {
        // Crea un utente di test
        utente = new Utente(
                "testuser",
                "password123",
                "test@example.com",
                "Test",
                "User",
                LocalDateTime.now()
        );
        utenteRepository.save(utente);
    }

    @Test
    public void testSalvaTitolo() {
        // Crea un titolo di test
        Titolo titolo = new Titolo(
                "BTP Italia 2030",
                "IT0005123456",
                LocalDate.of(2030, 12, 31),
                new BigDecimal("3.50"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );

        // Salva il titolo
        Titolo titoloSalvato = titoloRepository.save(titolo);

        // Verifica che il titolo sia stato salvato correttamente
        assertNotNull(titoloSalvato.getIdTitolo());
        assertEquals("BTP Italia 2030", titoloSalvato.getNome());
        assertEquals("IT0005123456", titoloSalvato.getCodiceIsin());
        assertEquals(utente.getIdUtente(), titoloSalvato.getUtente().getIdUtente());
    }

    @Test
    public void testTrovaTitoloPerCodiceIsin() {
        // Crea e salva un titolo di test
        Titolo titolo = new Titolo(
                "BOT 2025",
                "IT0005789012",
                LocalDate.of(2025, 6, 30),
                new BigDecimal("2.75"),
                "ANNUALE",
                "ANNUALE",
                TipoTitolo.BOT,
                utente
        );
        titoloRepository.save(titolo);

        // Cerca il titolo per codice ISIN
        Titolo titoloTrovato = titoloRepository.findByCodiceIsin("IT0005789012");

        // Verifica che il titolo sia stato trovato
        assertNotNull(titoloTrovato);
        assertEquals("BOT 2025", titoloTrovato.getNome());
        assertEquals(TipoTitolo.BOT, titoloTrovato.getTipoTitolo());
    }

    @Test
    public void testEsisteTitoloPerCodiceIsin() {
        // Crea e salva un titolo di test
        Titolo titolo = new Titolo(
                "BTP 2028",
                "IT0005345678",
                LocalDate.of(2028, 3, 15),
                new BigDecimal("4.00"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );
        titoloRepository.save(titolo);

        // Verifica che il titolo esista per codice ISIN
        boolean esiste = titoloRepository.existsByCodiceIsin("IT0005345678");
        boolean nonEsiste = titoloRepository.existsByCodiceIsin("CODICEINESISTENTE");

        // Verifica i risultati
        assertTrue(esiste);
        assertFalse(nonEsiste);
    }

    @Test
    public void testTrovaTitoliPerNome() {
        // Crea e salva alcuni titoli di test
        Titolo titolo1 = new Titolo(
                "BTP Italia 2026",
                "IT0005111111",
                LocalDate.of(2026, 5, 20),
                new BigDecimal("3.25"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );
        
        Titolo titolo2 = new Titolo(
                "BTP Futura 2028",
                "IT0005222222",
                LocalDate.of(2028, 8, 10),
                new BigDecimal("3.75"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );
        
        Titolo titolo3 = new Titolo(
                "BOT 2024",
                "IT0005333333",
                LocalDate.of(2024, 12, 15),
                new BigDecimal("2.50"),
                "ANNUALE",
                "ANNUALE",
                TipoTitolo.BOT,
                utente
        );
        
        titoloRepository.save(titolo1);
        titoloRepository.save(titolo2);
        titoloRepository.save(titolo3);

        // Cerca titoli per nome (parziale)
        List<Titolo> titoliTrovati = titoloRepository.findByNomeContainingIgnoreCase("BTP");

        // Verifica i risultati
        assertEquals(2, titoliTrovati.size());
        assertTrue(titoliTrovati.stream().anyMatch(t -> t.getNome().equals("BTP Italia 2026")));
        assertTrue(titoliTrovati.stream().anyMatch(t -> t.getNome().equals("BTP Futura 2028")));
    }

    @Test
    public void testTrovaTitoliConScadenzaFutura() {
        // Data di riferimento
        LocalDate dataRiferimento = LocalDate.now();
        
        // Crea e salva titoli con date di scadenza diverse
        Titolo titoloScaduto = new Titolo(
                "BTP Scaduto",
                "IT0005444444",
                dataRiferimento.minusYears(1), // Scaduto un anno fa
                new BigDecimal("2.00"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );
        
        Titolo titoloFuturo1 = new Titolo(
                "BTP Futuro 1",
                "IT0005555555",
                dataRiferimento.plusYears(1), // Scade tra un anno
                new BigDecimal("3.50"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );
        
        Titolo titoloFuturo2 = new Titolo(
                "BTP Futuro 2",
                "IT0005666666",
                dataRiferimento.plusYears(2), // Scade tra due anni
                new BigDecimal("4.00"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );
        
        titoloRepository.save(titoloScaduto);
        titoloRepository.save(titoloFuturo1);
        titoloRepository.save(titoloFuturo2);

        // Cerca titoli con scadenza futura
        List<Titolo> titoliScadenzaFutura = titoloRepository.findByDataScadenzaAfter(dataRiferimento);

        // Verifica i risultati
        assertEquals(2, titoliScadenzaFutura.size());
        assertTrue(titoliScadenzaFutura.stream().anyMatch(t -> t.getNome().equals("BTP Futuro 1")));
        assertTrue(titoliScadenzaFutura.stream().anyMatch(t -> t.getNome().equals("BTP Futuro 2")));
        assertFalse(titoliScadenzaFutura.stream().anyMatch(t -> t.getNome().equals("BTP Scaduto")));
    }
    
    @Test
    public void testTrovaTitoliPerUtenteId() {
        // Crea un secondo utente di test
        Utente utente2 = new Utente(
                "testuser2",
                "password456",
                "test2@example.com",
                "Test2",
                "User2",
                LocalDateTime.now()
        );
        utenteRepository.save(utente2);
        
        // Crea e salva titoli per entrambi gli utenti
        Titolo titoloUtente1 = new Titolo(
                "BTP Utente 1",
                "IT0005777777",
                LocalDate.of(2027, 5, 15),
                new BigDecimal("3.25"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );
        
        Titolo titoloUtente2 = new Titolo(
                "BTP Utente 2",
                "IT0005888888",
                LocalDate.of(2028, 7, 20),
                new BigDecimal("3.75"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente2
        );
        
        titoloRepository.save(titoloUtente1);
        titoloRepository.save(titoloUtente2);

        // Cerca titoli per ID utente
        List<Titolo> titoliUtente1 = titoloRepository.findByUtente_IdUtente(utente.getIdUtente());
        List<Titolo> titoliUtente2 = titoloRepository.findByUtente_IdUtente(utente2.getIdUtente());

        // Verifica i risultati
        assertEquals(1, titoliUtente1.size());
        assertEquals(1, titoliUtente2.size());
        assertEquals("BTP Utente 1", titoliUtente1.get(0).getNome());
        assertEquals("BTP Utente 2", titoliUtente2.get(0).getNome());
    }
}
