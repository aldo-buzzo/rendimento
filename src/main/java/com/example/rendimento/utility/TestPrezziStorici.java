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

import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.model.PrezzoStorico;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.service.PrezzoStoricoService;
import com.example.rendimento.service.impl.PrezzoStoricoServiceImpl;

public class TestPrezziStorici {
    private static final String JDBC_URL = "jdbc:postgresql://ep-wandering-band-agn2ylxj-pooler.c-2.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require&channelBinding=require";
    private static final String JDBC_USER = "neondb_owner";
    private static final String JDBC_PASSWORD = "npg_xAE6a0OKNWDB";

    public static void main(String[] args) throws Exception {

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
                        System.out.println("  Data: " + data + ", Prezzo: " + prezzo);
                    }
                } catch (Exception e) {
                    System.err.println("Errore nel titolo " + t.getCodiceIsin() + ": " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
