package com.example.rendimento.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe di utilità per testare la connessione al database PostgreSQL
 */
public class DatabaseConnectionTest {

    // Costanti di configurazione del database (da valorizzare manualmente)
    private static final String DB_URL = "jdbc:postgresql://ep-wandering-band-agn2ylxj-pooler.c-2.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require&channelBinding=require";
    private static final String DB_USER = "neondb_owner";
    private static final String DB_PASSWORD = "npg_xAE6a0OKNWDB";
    
    /**
     * Testa la connessione al database PostgreSQL
     * @return true se la connessione è stabilita con successo, false altrimenti
     */
    public static boolean testConnection() {
        try {
            // Carica il driver JDBC di PostgreSQL
            Class.forName("org.postgresql.Driver");
            
            // Tenta di stabilire una connessione
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            // Se la connessione è stabilita con successo
            if (connection != null && !connection.isClosed()) {
                System.out.println("Connessione al database stabilita con successo!");
                System.out.println("URL: " + DB_URL);
                System.out.println("Utente: " + DB_USER);
                
                // Chiude la connessione
                connection.close();
                return true;
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL non trovato: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Errore di connessione al database: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Metodo main per eseguire il test direttamente
     */
    public static void main(String[] args) {
        boolean connected = testConnection();
        System.out.println("Risultato del test di connessione: " + (connected ? "SUCCESSO" : "FALLIMENTO"));
    }
}