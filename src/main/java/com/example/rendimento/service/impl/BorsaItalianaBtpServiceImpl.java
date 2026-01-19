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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.enums.PeriodicitaBollo;
import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.service.BorsaItalianaService;

/**
 * Implementazione del servizio per ottenere il corso e la lista dei BTP.
 */
@Service
public class BorsaItalianaBtpServiceImpl implements BorsaItalianaService {

    private static final Logger logger = LoggerFactory.getLogger(BorsaItalianaBtpServiceImpl.class);
    private static final String BASE_URL_BTP = "https://www.borsaitaliana.it/borsa/obbligazioni/mot/btp/scheda/";
    private static final String URL_BORSA_ITALIANA_BTP = "https://www.borsaitaliana.it/borsa/obbligazioni/mot/btp/lista.html";
   
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
        return scrapeCorso(BASE_URL_BTP + isin + "-MOTX.html?lang=it");
    }
    
    @Override
    public BigDecimal getCorsoBotByIsin(String isin) {
        throw new UnsupportedOperationException("Metodo non supportato per i BTP");
    }
    
    @Override
    public TitoloDTO getTitoloByIsin(String isin) {
        String url = BASE_URL_BTP + isin + "-MOTX.html?lang=it";
        TitoloDTO titolo = scrapeTitolo(url, TipoTitolo.BTP, isin);
        
        // Aggiungiamo il corso attuale
        BigDecimal corso = getCorsoByIsin(isin);
        titolo.setCorso(corso);
        
        return titolo;
    }
    
    @Override
    public TitoloDTO getTitoloBotByIsin(String isin) {
        throw new UnsupportedOperationException("Metodo non supportato per i BTP");
    }
    
    @Override
    public List<TitoloDTO> getListaTitoli() {
        List<TitoloDTO> titoli = new ArrayList<>();
        
        try {
            // Recupera i titoli da tutte le pagine (da 1 a 10)
            for (int page = 1; page <= 10; page++) {
                String url = URL_BORSA_ITALIANA_BTP;
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
                    String tassoStr = columns.get(3).text().trim().replace("%", "").replace(",", ".");
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
                    
                    double tasso = 0.0;
                    try {
                        tasso = Double.parseDouble(tassoStr);
                    } catch (NumberFormatException e) {
                        logger.warn("Impossibile parsare il tasso: {}", tassoStr);
                    }
                    
                    double prezzo = 0.0;
                    try {
                        prezzo = Double.parseDouble(prezzoStr);
                    } catch (NumberFormatException e) {
                        logger.warn("Impossibile parsare il prezzo: {}", prezzoStr);
                    }
                    
                    // Creazione del DTO
                    TitoloDTO titolo = new TitoloDTO();
                    titolo.setCodiceIsin(codiceIsin);
                    titolo.setNome(nome);
                    titolo.setDataScadenza(dataScadenza);
                    titolo.setTassoNominale(BigDecimal.valueOf(tasso));
                    titolo.setPeriodicitaCedole("SEMESTRALE"); // La maggior parte dei BTP ha cedole semestrali
                    titolo.setPeriodicitaBollo(PeriodicitaBollo.ANNUALE.name());
                    titolo.setTipoTitolo(TipoTitolo.BTP);
                    titolo.setCorso(BigDecimal.valueOf(prezzo));
                    
                    titoli.add(titolo);
                } catch (Exception e) {
                    logger.error("Errore durante il parsing di una riga della tabella BTP", e);
                }
            }
            
                // Aggiungi i titoli alla lista principale
                logger.info("Trovati {} titoli nella pagina {}", rows.size(), page);
            }
            
            // Ordinamento per data di scadenza
            titoli.sort(Comparator.comparing(TitoloDTO::getDataScadenza));
            
            logger.info("Totale titoli recuperati da tutte le pagine: {}", titoli.size());
            
        } catch (IOException e) {
            logger.error("Errore durante il recupero della lista dei BTP da Borsa Italiana", e);
        }
        
        return titoli;
    }
    
    @Override
    public TipoTitolo getTipoTitolo() {
        return TipoTitolo.BTP;
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
                    titolo.setNome("BTP " + isin);
                }
            }
            
            // Variabili per memorizzare i valori trovati
            BigDecimal tassoPeriodale = null;
            
            // Estrazione altre informazioni dalla tabella
            Elements rows = doc.select("table.m-table tr");
            
            // Prima passata: estrai la periodicità delle cedole
            for (Element row : rows) {
                Elements cells = row.select("th, td");
                if (cells.size() >= 2) {
                    String label = cells.get(0).text().trim();
                    String value = cells.get(1).text().trim();
                    
                    if (label.contains("Periodicità cedola")) {
                        // Estrai la periodicità delle cedole
                        String periodicitaCedole = value.toUpperCase();
                        if (periodicitaCedole.contains("SEMESTRALE")) {
                            titolo.setPeriodicitaCedole("SEMESTRALE");
                        } else if (periodicitaCedole.contains("TRIMESTRALE")) {
                            titolo.setPeriodicitaCedole("TRIMESTRALE");
                        } else if (periodicitaCedole.contains("MENSILE")) {
                            titolo.setPeriodicitaCedole("MENSILE");
                        } else {
                            titolo.setPeriodicitaCedole("ANNUALE"); // Default
                        }
                        break; // Abbiamo trovato la periodicità, possiamo uscire dal ciclo
                    }
                }
            }
            
            // Seconda passata: estrai le altre informazioni
            for (Element row : rows) {
                Elements cells = row.select("th, td");
                if (cells.size() >= 2) {
                    String label = cells.get(0).text().trim();
                    String value = cells.get(1).text().trim();
                    
                    if (label.contains("Scadenza")) {
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
                    } else if (label.contains("Tasso Cedola Periodale")) {
                        try {
                            // Parsing del tasso nominale
                            String tassoStr = value.replace("%", "").replace(",", ".").trim();
                            tassoPeriodale = new BigDecimal(tassoStr);
                        } catch (NumberFormatException e) {
                            // Non impostiamo un valore di default, lasciamo che sia null
                            logger.warn("Errore nel parsing del tasso: {}", e.getMessage());
                        }
                    }
                }
            }
            
            // Calcolo del tasso annuale in base alla periodicità
            if (tassoPeriodale != null) {
                if ("SEMESTRALE".equals(titolo.getPeriodicitaCedole())) {
                    titolo.setTassoNominale(tassoPeriodale.multiply(new BigDecimal(2)));
                } else if ("TRIMESTRALE".equals(titolo.getPeriodicitaCedole())) {
                    titolo.setTassoNominale(tassoPeriodale.multiply(new BigDecimal(4)));
                } else if ("MENSILE".equals(titolo.getPeriodicitaCedole())) {
                    titolo.setTassoNominale(tassoPeriodale.multiply(new BigDecimal(12)));
                } else {
                    titolo.setTassoNominale(tassoPeriodale); // ANNUALE
                }
                
                // Verifica se il nome del titolo contiene il tasso nominale
                // e usa quello come riferimento se possibile
                if (titolo.getNome() != null && titolo.getNome().contains("%")) {
                    try {
                        // Estrai il tasso dal nome (es. "BTP 3,5% 01/11/2026")
                        String nomeTitolo = titolo.getNome();
                        int indexPercent = nomeTitolo.indexOf('%');
                        if (indexPercent > 0) {
                            // Cerca all'indietro fino a trovare uno spazio o l'inizio della stringa
                            int startIndex = indexPercent - 1;
                            while (startIndex >= 0 && 
                                  (Character.isDigit(nomeTitolo.charAt(startIndex)) || 
                                   nomeTitolo.charAt(startIndex) == ',' || 
                                   nomeTitolo.charAt(startIndex) == '.')) {
                                startIndex--;
                            }
                            
                            if (startIndex < indexPercent - 1) {
                                String tassoNomeStr = nomeTitolo.substring(startIndex + 1, indexPercent)
                                                              .replace(",", ".")
                                                              .trim();
                                BigDecimal tassoNome = new BigDecimal(tassoNomeStr);
                                
                                // Confronta il tasso calcolato con quello nel nome
                                BigDecimal tassoDifferenza = tassoNome.subtract(titolo.getTassoNominale()).abs();
                                if (tassoDifferenza.compareTo(new BigDecimal("0.1")) > 0) {
                                    logger.warn("Attenzione: Tasso calcolato ({}) differisce significativamente dal tasso nel nome ({})", 
                                               titolo.getTassoNominale(), tassoNome);
                                }
                                
                                // Usa il tasso dal nome come riferimento principale
                                titolo.setTassoNominale(tassoNome);
                            }
                        }
                    } catch (Exception e) {
                        // In caso di errore nell'estrazione dal nome, mantieni il valore già impostato
                        logger.warn("Errore nell'estrazione del tasso dal nome: {}", e.getMessage());
                    }
                }
            }
            
            // Non impostiamo valori di default, lasciamo che siano null se non trovati
            
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
