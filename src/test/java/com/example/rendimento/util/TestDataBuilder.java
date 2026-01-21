package com.example.rendimento.util;

import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.model.Utente;
import com.example.rendimento.repository.TitoloRepository;
import com.example.rendimento.repository.UtenteRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe di utilità per creare dati di test.
 * Questa classe facilita la creazione di oggetti di test per i vari test unitari e di integrazione.
 */
public class TestDataBuilder {

    /**
     * Crea un utente di test.
     *
     * @param username Nome utente
     * @param password Password
     * @param email Email
     * @param nome Nome
     * @param cognome Cognome
     * @return Utente creato
     */
    public static Utente createUtente(String username, String password, String email, String nome, String cognome) {
        return new Utente(
                username,
                password,
                email,
                nome,
                cognome,
                LocalDateTime.now()
        );
    }

    /**
     * Crea un utente di test con valori predefiniti.
     *
     * @param suffix Suffisso da aggiungere ai valori predefiniti
     * @return Utente creato
     */
    public static Utente createDefaultUtente(String suffix) {
        return createUtente(
                "testuser" + suffix,
                "password" + suffix,
                "test" + suffix + "@example.com",
                "Test" + suffix,
                "User" + suffix
        );
    }

    /**
     * Crea un titolo di test.
     *
     * @param nome Nome del titolo
     * @param codiceIsin Codice ISIN
     * @param dataScadenza Data di scadenza
     * @param tassoNominale Tasso nominale
     * @param periodicitaCedole Periodicità delle cedole
     * @param periodicitaBollo Periodicità del bollo
     * @param tipoTitolo Tipo di titolo
     * @param utente Utente proprietario
     * @return Titolo creato
     */
    public static Titolo createTitolo(String nome, String codiceIsin, LocalDate dataScadenza,
                                     BigDecimal tassoNominale, String periodicitaCedole,
                                     String periodicitaBollo, TipoTitolo tipoTitolo, Utente utente) {
        return new Titolo(
                nome,
                codiceIsin,
                dataScadenza,
                tassoNominale,
                periodicitaCedole,
                periodicitaBollo,
                tipoTitolo,
                utente
        );
    }

    /**
     * Crea un titolo di test con valori predefiniti.
     *
     * @param suffix Suffisso da aggiungere ai valori predefiniti
     * @param utente Utente proprietario
     * @return Titolo creato
     */
    public static Titolo createDefaultTitolo(String suffix, Utente utente) {
        return createTitolo(
                "BTP Test " + suffix,
                "IT000" + suffix,
                LocalDate.now().plusYears(1),
                new BigDecimal("3.50"),
                "SEMESTRALE",
                "ANNUALE",
                TipoTitolo.BTP,
                utente
        );
    }

    /**
     * Crea un insieme di dati di test e li salva nel database.
     *
     * @param utenteRepository Repository per gli utenti
     * @param titoloRepository Repository per i titoli
     * @param numUtenti Numero di utenti da creare
     * @param titoliPerUtente Numero di titoli da creare per ogni utente
     * @return Lista degli utenti creati
     */
    public static List<Utente> createTestData(UtenteRepository utenteRepository, TitoloRepository titoloRepository,
                                             int numUtenti, int titoliPerUtente) {
        List<Utente> utenti = new ArrayList<>();
        
        for (int i = 1; i <= numUtenti; i++) {
            Utente utente = createDefaultUtente(String.valueOf(i));
            utenteRepository.save(utente);
            utenti.add(utente);
            
            for (int j = 1; j <= titoliPerUtente; j++) {
                Titolo titolo = createDefaultTitolo(i + "-" + j, utente);
                titoloRepository.save(titolo);
            }
        }
        
        return utenti;
    }
}
