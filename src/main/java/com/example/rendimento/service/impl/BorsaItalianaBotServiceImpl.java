package com.example.rendimento.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.enums.PeriodicitaBollo;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.service.BorsaItalianaService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementazione del servizio per ottenere il corso e la lista dei BOT.
 */
@Service
public class BorsaItalianaBotServiceImpl implements BorsaItalianaService {

    private static final Logger logger = LoggerFactory.getLogger(BorsaItalianaBotServiceImpl.class);
    private static final String BASE_URL_BOT = "https://www.borsaitaliana.it/borsa/obbligazioni/mot/bot/scheda/";
    private static final String URL_BORSA_ITALIANA_BOT = "https://www.borsaitaliana.it/borsa/obbligazioni/mot/bot/lista.html";
    
    // Formati di data aggiuntivi per il parsing
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yy"),
        DateTimeFormatter.ofPattern("dd/mm/yy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yy"),
        DateTimeFormatter.ofPattern("dd-mm-yy")
    };

    @Override
    public BigDecimal getCorsoByIsin(String isin) {
        // Nuovo formato URL: [ISIN]-MOTX.html?lang=it
        return scrapeCorso(BASE_URL_BOT + isin + "-MOTX.html?lang=it");
    }
    
    @Override
    public BigDecimal getCorsoBotByIsin(String isin) {
        return getCorsoByIsin(isin);
    }
    
    @Override
    public TitoloDTO getTitoloByIsin(String isin) {
        throw new UnsupportedOperationException("Metodo non supportato per i BOT");
    }
    
    @Override
    public TitoloDTO getTitoloBotByIsin(String isin) {
        String url = BASE_URL_BOT + isin + "-MOTX.html?lang=it";
        TitoloDTO titolo = scrapeTitolo(url, TipoTitolo.BOT, isin);
        
        // Aggiungiamo il corso attuale
        BigDecimal corso = getCorsoByIsin(isin);
        titolo.setCorso(corso);
        
        return titolo;
    }
    
    @Override
    public List<TitoloDTO> getListaTitoli() {
        List<TitoloDTO> titoli = new ArrayList<>();
        
        try {
            // Recupera i titoli da tutte le pagine (da 1 a 5)
            for (int page = 1; page <= 5; page++) {
                String url = URL_BORSA_ITALIANA_BOT;
                if (page > 1) {
                    url += "?&page=" + page;
                }
                
                logger.info("Recupero titoli dalla pagina {}: {}", page, url);
                
                // Connessione alla pagina di Borsa Italiana
                Document doc = Jsoup.connect(url).get();
                
                // Selezione della tabella dei titoli
                Elements rows = doc.select("table.m-table tbody tr");
                
                // Se non ci sono righe, probabilmente abbiamo raggiunto la fine delle pagine
                if (rows.isEmpty()) {
                    logger.info("Nessun titolo trovato nella pagina {}, interrompo la ricerca", page);
                    break;
                }
                
                for (Element row : rows) {
                    try {
                        // Estrazione dei dati dalle colonne
                        Elements columns = row.select("td");
                        
                        if (columns.size() < 5) {
                            continue; // Salta righe non valide
                        }
                        
                        // Estrai il codice ISIN dal tag <a> all'interno della prima colonna
                        Element firstCell = columns.get(0);
                        Element link = firstCell.selectFirst("a");
                        String rawIsin = link != null ? link.text().trim() : firstCell.text().trim();
                        
                        // Estrai solo il codice ISIN (12 caratteri alfanumerici)
                        String codiceIsin = rawIsin;
                        if (rawIsin.contains(" ")) {
                            // Se contiene spazi, prendi solo la prima parte (il codice ISIN)
                            codiceIsin = rawIsin.split(" ")[0].trim();
                        }
                        
                        // Log per debug
                        logger.debug("Codice ISIN estratto: {} (da: {})", codiceIsin, rawIsin);
                        String nome = columns.get(1).text().trim();
                        String prezzoStr = columns.get(2).text().trim().replace(".", "").replace(",", ".");
                        // I BOT non hanno un tasso nominale esplicito, è implicito nel prezzo
                        String scadenzaStr = columns.get(4).text().trim();
                        
                        // Salta i titoli con prezzo N/A
                        if (prezzoStr.equals("N/A") || prezzoStr.isEmpty()) {
                            continue;
                        }
                        
                        // Parsing dei dati
                        LocalDate dataScadenza = null;
                        boolean dataParsed = false;
                        
                        // Prova tutti i formati di data disponibili
                        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                            try {
                                dataScadenza = LocalDate.parse(scadenzaStr, formatter);
                                dataParsed = true;
                                break;
                            } catch (DateTimeParseException e) {
                                // Continua con il prossimo formato
                            }
                        }
                        
                        if (!dataParsed) {
                            logger.warn("Impossibile parsare la data di scadenza: {}", scadenzaStr);
                            continue; // Salta titoli con data non valida
                        }
                        
                        double prezzo = 0.0;
                        try {
                            prezzo = Double.parseDouble(prezzoStr);
                        } catch (NumberFormatException e) {
                            logger.warn("Impossibile parsare il prezzo: {}", prezzoStr);
                        }
                        
                        // I BOT non hanno un tasso nominale esplicito
                        
                        // Creazione del DTO
                        TitoloDTO titolo = new TitoloDTO();
                        titolo.setCodiceIsin(codiceIsin);
                        titolo.setNome(nome);
                        titolo.setDataScadenza(dataScadenza);
                        titolo.setTassoNominale(null); // I BOT non hanno un tasso nominale
                        titolo.setPeriodicitaCedole(null); // I BOT sono zero coupon, non hanno cedole
                        titolo.setPeriodicitaBollo(PeriodicitaBollo.ANNUALE.name());
                        titolo.setTipoTitolo(TipoTitolo.BOT);
                        titolo.setCorso(BigDecimal.valueOf(prezzo));
                        
                        titoli.add(titolo);
                    } catch (Exception e) {
                        logger.error("Errore durante il parsing di una riga della tabella BOT", e);
                    }
                }
                
                // Aggiungi i titoli alla lista principale
                logger.info("Trovati {} titoli nella pagina {}", rows.size(), page);
            }
            
            // Ordinamento per data di scadenza
            titoli.sort(Comparator.comparing(TitoloDTO::getDataScadenza));
            
            logger.info("Totale titoli recuperati da tutte le pagine: {}", titoli.size());
            
        } catch (IOException e) {
            logger.error("Errore durante il recupero della lista dei BOT da Borsa Italiana", e);
        }
        
        return titoli;
    }
    
    @Override
    public TipoTitolo getTipoTitolo() {
        return TipoTitolo.BOT;
    }
    
    private TitoloDTO scrapeTitolo(String url, TipoTitolo tipoTitolo, String isin) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();
                    
            TitoloDTO titolo = new TitoloDTO();
            titolo.setCodiceIsin(isin);
            titolo.setTipoTitolo(tipoTitolo);
            
            // Estrazione nome
            Element titleElement = doc.selectFirst("h1.t-text");
            if (titleElement != null) {
                titolo.setNome(titleElement.text());
            } else {
                // Fallback: prova a trovare il titolo in un altro elemento
                titleElement = doc.selectFirst("div.t-title");
                if (titleElement != null) {
                    titolo.setNome(titleElement.text());
                } else {
                    titolo.setNome("BOT " + isin);
                }
            }
            
            // Estrazione altre informazioni dalla tabella
            Elements rows = doc.select("table.m-table tr");
            for (Element row : rows) {
                Elements cells = row.select("th, td");
                if (cells.size() >= 2) {
                    String label = cells.get(0).text().trim();
                    String value = cells.get(1).text().trim();
                    
                    if (label.contains("Data Scadenza") || label.contains("Scadenza")) {
                        boolean dataParsed = false;
                        
                        // Prova tutti i formati di data disponibili
                        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                            try {
                                titolo.setDataScadenza(LocalDate.parse(value, formatter));
                                dataParsed = true;
                                break;
                            } catch (DateTimeParseException e) {
                                // Continua con il prossimo formato
                            }
                        }
                        
                        if (!dataParsed) {
                            // Non impostiamo un valore di default, lasciamo che sia null
                            logger.warn("Errore nel parsing della data di scadenza: {}", value);
                        }
                    // I BOT non hanno un tasso nominale esplicito
                    }
                }
            }
            
            // Non impostiamo valori di default, lasciamo che siano null se non trovati
            
            // I BOT non hanno cedole, quindi impostiamo un valore null
            titolo.setPeriodicitaCedole(null);
            
            // Impostiamo solo la periodicità del bollo che è un campo obbligatorio per il funzionamento
            // ma non è presente nella pagina
            titolo.setPeriodicitaBollo("ANNUALE");
            
            return titolo;
        } catch (IOException e) {
            throw new RuntimeException("Errore scraping titolo da: " + url, e);
        }
    }

    private BigDecimal scrapeCorso(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();

            Elements rows = doc.select("table tr");

            for (Element row : rows) {
                String rowText = row.text();

                if (rowText.contains("Prezzo ufficiale")
                    || rowText.contains("Prezzo Ultimo Contratto")
                    || rowText.contains("Corso ufficiale")) {

                    String valore = row.select("td")
                            .last()
                            .text()
                            .replace(",", ".");
                    return new BigDecimal(valore);
                }
            }
            throw new IllegalStateException("Corso non trovato nella pagina: " + url);
        } catch (IOException e) {
            throw new RuntimeException("Errore scraping corso da: " + url, e);
        }
    }
}
