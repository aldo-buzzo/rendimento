package com.example.rendimento.utility;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.example.rendimento.RendimentoApplication;
import com.example.rendimento.dto.ElaborazioneRisultatoDTO;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.model.PrezzoStorico;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.service.PrezzoStoricoService;
import com.example.rendimento.service.SimulazioneService;
import com.example.rendimento.service.TrendService;
import com.example.rendimento.service.impl.PrezzoStoricoServiceImpl;

public class TestPrezziStorici {
    private static final Logger log = LoggerFactory.getLogger(TestPrezziStorici.class);
    private static final String JDBC_URL = "jdbc:postgresql://ep-wandering-band-agn2ylxj-pooler.c-2.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require&channelBinding=require";
    private static final String JDBC_USER = "neondb_owner";
    private static final String JDBC_PASSWORD = "npg_xAE6a0OKNWDB";

    public static void main(String[] args) throws Exception {
        // Avvia il contesto Spring per ottenere i bean configurati
        ConfigurableApplicationContext springContext = SpringApplication.run(RendimentoApplication.class, args);
        
        // Ottieni i servizi necessari dal contesto Spring
        SimulazioneService simulazioneService = springContext.getBean(SimulazioneService.class);
        TrendService trendService = springContext.getBean(TrendService.class);
        
        log.info("Contesto Spring avviato, servizi ottenuti");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT id_titolo, nome, codice_isin, data_scadenza, tasso_nominale, periodicita_cedole, periodicita_bollo, tipo_titolo FROM titolo")) {

            PrezzoStoricoService servizio = new PrezzoStoricoServiceImpl();

            while (rs.next()) {
                Titolo t = new Titolo();
                t.setIdTitolo(rs.getInt("id_titolo"));
                t.setNome(rs.getString("nome"));
                t.setCodiceIsin(rs.getString("codice_isin"));
                t.setDataScadenza(rs.getDate("data_scadenza").toLocalDate());
                t.setTassoNominale(rs.getBigDecimal("tasso_nominale"));
                t.setPeriodicitaCedole(rs.getString("periodicita_cedole"));
                t.setPeriodicitaBollo(rs.getString("periodicita_bollo"));
                t.setTipoTitolo(TipoTitolo.valueOf(rs.getString("tipo_titolo")));

                // Salta al titolo successivo se il tipo non è BTP
                if (t.getTipoTitolo() != TipoTitolo.BTP) {
                    System.out.println("Titolo saltato (non BTP): " + t.getNome() + " (" + t.getCodiceIsin() + ")");
                    continue;
                }

                try {
                    // Richiamo il metodo che estrae i prezzi storici come oggetti PrezzoStorico
                    List<PrezzoStorico> prezzi = servizio.estraiPrezziUltimi3Mesi(t, DayOfWeek.MONDAY);
                    System.out.println("Titolo: " + t.getNome() + " (" + t.getCodiceIsin() + ")");
                    System.out.println("Prezzi come oggetti PrezzoStorico:");
                    prezzi.forEach(p -> System.out.println("  Prezzo: " + p));

                    // Richiamo il metodo che estrae i prezzi storici come mappe
                    List<Map<String, Object>> prezziMap = servizio.estraiPrezziUltimi3MesiMap(t, DayOfWeek.MONDAY);
                    System.out.println("\nPrezzi come mappe data/prezzo:");
                    for (Map<String, Object> mappa : prezziMap) {
                        LocalDate data = (LocalDate) mappa.get("data");
                        BigDecimal prezzo = (BigDecimal) mappa.get("prezzo");

                        // Utilizziamo il servizio ottenuto dal contesto Spring
                        try {
                            // Elabora la simulazione e ottieni il risultato dettagliato
                            ElaborazioneRisultatoDTO risultatoElaborazione = simulazioneService.elaboraSimulazionePerTitolo(t, prezzo, data);
                            if (risultatoElaborazione == null || risultatoElaborazione.getSimulazione() == null) {
                                log.warn("Elaborazione fallita per il titolo ID: {}, ISIN: {}", t.getIdTitolo(), t.getCodiceIsin());
                                continue; // Salta questo titolo se l'elaborazione ha fallito
                            }
                            
                            // Salva il trend per il titolo utilizzando il rendimento senza costi
                            trendService.salvaOAggiornaTrendPerTitolo(
                                t,
                                prezzo,
                                risultatoElaborazione.getRisultatoDettagliato().getRendimentoSenzaCosti(),
                                data
                            );
                            log.info("Trend salvato per il titolo ID: {}, ISIN: {}", 
                                    t.getIdTitolo(), t.getCodiceIsin());
                            
                            System.out.println("  Data: " + data + ", Prezzo: " + prezzo + 
                                               ", Rendimento: " + risultatoElaborazione.getRisultatoDettagliato().getRendimentoSenzaCosti());
                        } catch (Exception e) {
                            log.error("Errore nell'elaborazione della simulazione per il titolo ID: {}, ISIN: {}, Errore: {}", 
                                    t.getIdTitolo(), t.getCodiceIsin(), e.getMessage());
                        }

                    }
                } catch (Exception e) {
                    System.err.println("Errore nel titolo " + t.getCodiceIsin() + ": " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Chiudi il contesto Spring
            if (springContext != null) {
                springContext.close();
                log.info("Contesto Spring chiuso");
            }
        }
    }
}
