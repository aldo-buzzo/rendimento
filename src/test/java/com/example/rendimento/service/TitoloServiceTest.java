package com.example.rendimento.service;

import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per il servizio TitoloService utilizzando un database H2 in memoria.
 * Questo esempio mostra come testare la logica di business con un database in memoria.
 */
@SpringBootTest
@ActiveProfiles("test") // Utilizza il profilo di test con il database H2
@Transactional
public class TitoloServiceTest {

    @Autowired
    private TitoloRepository titoloRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    // Servizio da testare (in un caso reale, questo sarebbe un servizio esistente)
    private TitoloService titoloService;

    private Utente utente;

    @BeforeEach
    public void setup() {
        // Inizializza il servizio con i repository
        titoloService = new TitoloService(titoloRepository);

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

    /**
     * Classe di servizio di esempio per i test.
     * In un'applicazione reale, questa classe sarebbe in un file separato.
     */
    public static class TitoloService {
        private final TitoloRepository titoloRepository;

        public TitoloService(TitoloRepository titoloRepository) {
            this.titoloRepository = titoloRepository;
        }

        /**
         * Crea un nuovo titolo se non esiste già un titolo con lo stesso codice ISIN.
         *
         * @param titolo il titolo da creare
         * @return il titolo creato
         * @throws IllegalArgumentException se esiste già un titolo con lo stesso codice ISIN
         */
        public Titolo creaTitolo(Titolo titolo) {
            if (titoloRepository.existsByCodiceIsin(titolo.getCodiceIsin())) {
                throw new IllegalArgumentException("Esiste già un titolo con il codice ISIN: " + titolo.getCodiceIsin());
            }
            return titoloRepository.save(titolo);
        }

        /**
         * Trova un titolo per ID.
         *
         * @param id l'ID del titolo da trovare
         * @return il titolo trovato, o Optional vuoto se non esiste
         */
        public Optional<Titolo> trovaTitoloPerId(Integer id) {
            return titoloRepository.findById(id);
        }

        /**
         * Aggiorna il tasso nominale di un titolo esistente.
         *
         * @param id l'ID del titolo da aggiornare
         * @param nuovoTasso il nuovo tasso nominale
         * @return il titolo aggiornato
         * @throws IllegalArgumentException se il titolo non esiste
         */
        public Titolo aggiornaTassoNominale(Integer id, BigDecimal nuovoTasso) {
            Titolo titolo = titoloRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Titolo non trovato con ID: " + id));
            
            titolo.setTassoNominale(nuovoTasso);
            return titoloRepository.save(titolo);
        }

        /**
         * Elimina un titolo per ID.
         *
         * @param id l'ID del titolo da eliminare
         * @throws IllegalArgumentException se il titolo non esiste
         */
        public void eliminaTitolo(Integer id) {
            if (!titoloRepository.existsById(id)) {
                throw new IllegalArgumentException("Titolo non trovato con ID: " + id);
            }
            titoloRepository.deleteById(id);
        }
    }

    @Test   
    public void testCreaTitolo() {
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

        // Crea il titolo tramite il servizio
        Titolo titoloCreato = titoloService.creaTitolo(titolo);

        // Verifica che il titolo sia stato creato correttamente
        assertNotNull(titoloCreato.getIdTitolo());
        assertEquals("BTP Italia 2030", titoloCreato.getNome());
        assertEquals("IT0005123456", titoloCreato.getCodiceIsin());
        assertEquals(new BigDecimal("3.50"), titoloCreato.getTassoNominale());
    }

    @Test
    public void testCreaTitoloConCodiceIsinDuplicato() {
        // Crea un primo titolo
        Titolo titolo1 = new Titolo(
                "BTP Italia 2030",
                "IT0005123456",
                LocalDate.of(2030, 12, 31),
                new BigDecimal("3.50"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );
        titoloService.creaTitolo(titolo1);

        // Crea un secondo titolo con lo stesso codice ISIN
        Titolo titolo2 = new Titolo(
                "BTP Italia 2030 Duplicato",
                "IT0005123456", // Stesso codice ISIN
                LocalDate.of(2030, 12, 31),
                new BigDecimal("3.75"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );

        // Verifica che venga lanciata un'eccezione
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            titoloService.creaTitolo(titolo2);
        });

        // Verifica il messaggio di errore
        assertTrue(exception.getMessage().contains("Esiste già un titolo con il codice ISIN"));
    }

    @Test
    public void testTrovaTitoloPerId() {
        // Crea un titolo di test
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
        Titolo titoloSalvato = titoloRepository.save(titolo);

        // Trova il titolo per ID
        Optional<Titolo> titoloTrovato = titoloService.trovaTitoloPerId(titoloSalvato.getIdTitolo());

        // Verifica che il titolo sia stato trovato
        assertTrue(titoloTrovato.isPresent());
        assertEquals("BOT 2025", titoloTrovato.get().getNome());
        assertEquals("IT0005789012", titoloTrovato.get().getCodiceIsin());
    }

    @Test
    public void testAggiornaTassoNominale() {
        // Crea un titolo di test
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
        Titolo titoloSalvato = titoloRepository.save(titolo);

        // Aggiorna il tasso nominale
        BigDecimal nuovoTasso = new BigDecimal("4.50");
        Titolo titoloAggiornato = titoloService.aggiornaTassoNominale(titoloSalvato.getIdTitolo(), nuovoTasso);

        // Verifica che il tasso sia stato aggiornato
        assertEquals(nuovoTasso, titoloAggiornato.getTassoNominale());

        // Verifica che il titolo sia stato aggiornato nel database
        Titolo titoloRicaricato = titoloRepository.findById(titoloSalvato.getIdTitolo()).orElse(null);
        assertNotNull(titoloRicaricato);
        assertEquals(nuovoTasso, titoloRicaricato.getTassoNominale());
    }

    @Test
    public void testEliminaTitolo() {
        // Crea un titolo di test
        Titolo titolo = new Titolo(
                "BTP da eliminare",
                "IT0005999999",
                LocalDate.of(2026, 1, 1),
                new BigDecimal("3.00"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );
        Titolo titoloSalvato = titoloRepository.save(titolo);
        Integer idTitolo = titoloSalvato.getIdTitolo();

        // Verifica che il titolo esista
        assertTrue(titoloRepository.existsById(idTitolo));

        // Elimina il titolo
        titoloService.eliminaTitolo(idTitolo);

        // Verifica che il titolo sia stato eliminato
        assertFalse(titoloRepository.existsById(idTitolo));
    }

    @Test
    public void testEliminaTitoloInesistente() {
        // Tenta di eliminare un titolo con un ID inesistente
        Integer idInesistente = 999999;

        // Verifica che venga lanciata un'eccezione
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            titoloService.eliminaTitolo(idInesistente);
        });

        // Verifica il messaggio di errore
        assertTrue(exception.getMessage().contains("Titolo non trovato con ID"));
    }
}
