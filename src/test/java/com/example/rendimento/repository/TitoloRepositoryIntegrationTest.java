package com.example.rendimento.repository;

import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.model.Utente;
import com.example.rendimento.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per il repository TitoloRepository utilizzando un database H2 in memoria.
 * Questo test dimostra come utilizzare la classe TestDataBuilder per creare dati di test.
 */
@SpringBootTest
@ActiveProfiles("test") // Utilizza il profilo di test con il database H2
@Transactional
public class TitoloRepositoryIntegrationTest {

    @Autowired
    private TitoloRepository titoloRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    private Utente utente1;
    private Utente utente2;

    @BeforeEach
    public void setup() {
        // Utilizza TestDataBuilder per creare utenti di test
        utente1 = TestDataBuilder.createDefaultUtente("1");
        utente2 = TestDataBuilder.createDefaultUtente("2");
        
        utenteRepository.save(utente1);
        utenteRepository.save(utente2);
        
        // Crea titoli con diverse date di scadenza per il primo utente
        Titolo titoloScaduto = TestDataBuilder.createTitolo(
                "BTP Scaduto",
                "IT0001",
                LocalDate.now().minusYears(1),
                new BigDecimal("2.00"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente1
        );
        
        Titolo titoloTrimestrale = TestDataBuilder.createTitolo(
                "BTP Trimestrale",
                "IT0002",
                LocalDate.now().plusMonths(3),
                new BigDecimal("2.50"),
                "TRIMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente1
        );
        
        Titolo titoloSemestrale = TestDataBuilder.createTitolo(
                "BTP Semestrale",
                "IT0003",
                LocalDate.now().plusMonths(6),
                new BigDecimal("3.00"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente1
        );
        
        Titolo titoloAnnuale = TestDataBuilder.createTitolo(
                "BTP Annuale",
                "IT0004",
                LocalDate.now().plusYears(1),
                new BigDecimal("3.50"),
                "ANNUALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente1
        );
        
        // Crea un titolo per il secondo utente
        Titolo titoloUtente2 = TestDataBuilder.createTitolo(
                "BTP Utente 2",
                "IT0005",
                LocalDate.now().plusYears(2),
                new BigDecimal("4.00"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente2
        );
        
        // Salva tutti i titoli
        titoloRepository.save(titoloScaduto);
        titoloRepository.save(titoloTrimestrale);
        titoloRepository.save(titoloSemestrale);
        titoloRepository.save(titoloAnnuale);
        titoloRepository.save(titoloUtente2);
    }

    @Test
    public void testFindByDataScadenzaAfter() {
        // Trova titoli con scadenza futura
        List<Titolo> titoliScadenzaFutura = titoloRepository.findByDataScadenzaAfter(LocalDate.now());
        
        // Verifica che ci siano 4 titoli con scadenza futura
        assertEquals(4, titoliScadenzaFutura.size());
        
        // Verifica che non ci siano titoli scaduti
        assertTrue(titoliScadenzaFutura.stream().noneMatch(t -> t.getNome().equals("BTP Scaduto")));
    }

    @Test
    public void testFindByNomeContainingIgnoreCase() {
        // Trova titoli per nome (ricerca parziale, case-insensitive)
        List<Titolo> titoliBTP = titoloRepository.findByNomeContainingIgnoreCase("BTP");
        
        // Verifica che ci siano 5 titoli con "BTP" nel nome
        assertEquals(5, titoliBTP.size());
        
        // Ricerca case-insensitive
        List<Titolo> titoliBtpLowercase = titoloRepository.findByNomeContainingIgnoreCase("btp");
        assertEquals(5, titoliBtpLowercase.size());
        
        // Ricerca parziale
        List<Titolo> titoliTrimestrali = titoloRepository.findByNomeContainingIgnoreCase("Trimestrale");
        assertEquals(1, titoliTrimestrali.size());
        assertEquals("BTP Trimestrale", titoliTrimestrali.get(0).getNome());
    }

    @Test
    public void testFindByUtente_IdUtente() {
        // Trova titoli per ID utente
        List<Titolo> titoliUtente1 = titoloRepository.findByUtente_IdUtente(utente1.getIdUtente());
        List<Titolo> titoliUtente2 = titoloRepository.findByUtente_IdUtente(utente2.getIdUtente());
        
        // Verifica che ci siano 4 titoli per il primo utente
        assertEquals(4, titoliUtente1.size());
        
        // Verifica che ci sia 1 titolo per il secondo utente
        assertEquals(1, titoliUtente2.size());
        assertEquals("BTP Utente 2", titoliUtente2.get(0).getNome());
    }

    @Test
    public void testFindByCodiceIsin() {
        // Trova titolo per codice ISIN
        Titolo titoloTrovato = titoloRepository.findByCodiceIsin("IT0003");
        
        // Verifica che il titolo sia stato trovato
        assertNotNull(titoloTrovato);
        assertEquals("BTP Semestrale", titoloTrovato.getNome());
    }

    @Test
    public void testExistsByCodiceIsin() {
        // Verifica che il titolo esista per codice ISIN
        boolean esiste = titoloRepository.existsByCodiceIsin("IT0004");
        boolean nonEsiste = titoloRepository.existsByCodiceIsin("CODICEINESISTENTE");
        
        // Verifica i risultati
        assertTrue(esiste);
        assertFalse(nonEsiste);
    }
    
    @Test
    public void testFindByDataScadenzaAfterAndUtente_IdUtente() {
        // Trova titoli con scadenza futura per un utente specifico
        List<Titolo> titoliUtente1Futuri = titoloRepository.findByDataScadenzaAfterAndUtente_IdUtente(
                LocalDate.now(), utente1.getIdUtente());
        
        // Verifica che ci siano 3 titoli con scadenza futura per l'utente 1
        assertEquals(3, titoliUtente1Futuri.size());
        
        // Verifica che non ci siano titoli scaduti
        assertTrue(titoliUtente1Futuri.stream().noneMatch(t -> t.getNome().equals("BTP Scaduto")));
    }
}
