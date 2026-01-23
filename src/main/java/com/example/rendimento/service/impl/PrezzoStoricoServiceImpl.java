package com.example.rendimento.service.impl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.rendimento.enums.TipoTitolo;
import com.example.rendimento.model.PrezzoStorico;
import com.example.rendimento.model.Titolo;
import com.example.rendimento.service.PrezzoStoricoService;

public class PrezzoStoricoServiceImpl implements PrezzoStoricoService {

    private static final Logger log = LoggerFactory.getLogger(PrezzoStoricoServiceImpl.class);
    private static final DateTimeFormatter FORMATTER_DOT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter FORMATTER_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<PrezzoStorico> estraiPrezziUltimi3Mesi(Titolo titolo, DayOfWeek giornoSettimana) throws Exception {
        List<PrezzoStorico> prezziStorici = new ArrayList<>();

        // Costruisci l'URL della pagina dei dati storici su Investing
        String nomeTitolo = titolo.getNome().toLowerCase().replace(" ", "-").replace("%", "");
        String url = "https://it.investing.com/rates-bonds/" + nomeTitolo + "-historical-data";
        log.info("Tentativo di accesso all'URL: {}", url);

        try {
            // Fetch della pagina con gestione errori migliorata
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0")
                    .timeout(10000)
                    .get();

            // Approccio 1: Cerca direttamente le celle con le classi specifiche fornite dall'utente
            Elements dateCells = doc.select("td[class*='datatable_cell']:contains(/)"); // Celle data contengono slash
            Elements priceCells = doc.select("td[class*='datatable_cell'][class*='align-end']"); // Celle prezzo sono align-end
            
            boolean usedDirectCellsApproach = false;
            List<LocalDate> dates = new ArrayList<>();
            List<String> prices = new ArrayList<>();
            
            if (!dateCells.isEmpty() && !priceCells.isEmpty() && dateCells.size() == priceCells.size()) {
                log.info("Utilizzando l'approccio diretto con selettori di celle specifici");
                usedDirectCellsApproach = true;
                
                for (int i = 0; i < dateCells.size(); i++) {
                    String dataStr = dateCells.get(i).text();
                    String prezzoStr = priceCells.get(i).text()
                                     .replace(".", "")
                                     .replace(",", ".")
                                     .replace("%", "");
                    
                    try {
                        LocalDate data;
                        try {
                            // Prima prova con il formato con punti
                            data = LocalDate.parse(dataStr, FORMATTER_DOT);
                            log.debug("Data parsata con formato dd.MM.yyyy: {}", dataStr);
                        } catch (Exception e1) {
                            // Se fallisce, prova con il formato con slash
                            try {
                                data = LocalDate.parse(dataStr, FORMATTER_SLASH);
                                log.debug("Data parsata con formato dd/MM/yyyy: {}", dataStr);
                            } catch (Exception e2) {
                                // Se entrambi falliscono, lancia un'eccezione
                                log.error("Impossibile parsare la data con nessun formato: {}", dataStr);
                                throw new IllegalArgumentException("Formato data non supportato: " + dataStr);
                            }
                        }
                        dates.add(data);
                        prices.add(prezzoStr);
                    } catch (Exception e) {
                        log.error("Errore nel parsing della data: {}", dataStr, e);
                    }
                }
            }
            
            // Se l'approccio diretto non ha funzionato, proviamo con l'approccio della tabella
            if (!usedDirectCellsApproach) {
                log.info("L'approccio diretto non ha funzionato, tentativo con l'approccio della tabella");
                
                // Seleziona la tabella dei dati storici - prova diversi selettori possibili
                Element table = null;
                
                // Array di possibili selettori per la tabella dei prezzi storici
                String[] possibleSelectors = {
                    "table.common-table",
                    "table.genTbl.closedTbl.historicalTbl",
                    "table.genTbl.closedTbl",
                    "table[data-test='historical-data-table']",
                    "table.historicalTbl",
                    "table[class*='datatable']"
                };
                
                // Prova ciascun selettore fino a trovare una tabella
                for (String selector : possibleSelectors) {
                    table = doc.selectFirst(selector);
                    if (table != null) {
                        log.info("Tabella trovata con selettore: {}", selector);
                        break;
                    }
                }
                
                // Se ancora non abbiamo trovato la tabella, cerchiamo qualsiasi tabella che contenga dati storici
                if (table == null) {
                    log.info("Tentativo di trovare qualsiasi tabella con dati storici...");
                    Elements allTables = doc.select("table");
                    for (Element t : allTables) {
                        // Cerca tabelle che contengono intestazioni tipiche dei dati storici
                        Elements headers = t.select("th");
                        boolean isHistoricalTable = false;
                        for (Element header : headers) {
                            String headerText = header.text().toLowerCase();
                            if (headerText.contains("data") || headerText.contains("ultimo") || 
                                headerText.contains("prezzo") || headerText.contains("chiusura")) {
                                isHistoricalTable = true;
                                break;
                            }
                        }
                        
                        if (isHistoricalTable) {
                            table = t;
                            log.info("Trovata tabella con intestazioni di dati storici");
                            break;
                        }
                    }
                }
                
                // Se ancora non abbiamo trovato la tabella, lanciamo un'eccezione
                if (table == null) {
                    log.error("Nessuna tabella di dati storici trovata per URL: {}", url);
                    throw new IllegalStateException("Tabella prezzi storici non trovata su Investing per " + titolo.getNome());
                }
    
                // Trova l'indice della colonna "Ultimo"
                int ultimoColIndex = -1;
                Elements headers = table.select("thead th");
                for (int i = 0; i < headers.size(); i++) {
                    if (headers.get(i).text().contains("Ultimo")) {
                        ultimoColIndex = i;
                        break;
                    }
                }
    
                // Se non troviamo la colonna "Ultimo", usiamo la seconda colonna come fallback
                if (ultimoColIndex == -1) {
                    ultimoColIndex = 1;
                    log.info("Colonna 'Ultimo' non trovata, usando colonna di indice 1 come fallback");
                } else {
                    log.info("Colonna 'Ultimo' trovata all'indice: {}", ultimoColIndex);
                }
    
                // Riga per riga
                Elements rows = table.select("tbody tr");
                
                for (Element row : rows) {
                    Elements cols = row.select("td");
                    if (cols.size() <= ultimoColIndex) continue;
    
                    String dataStr = cols.get(0).text();
                    String prezzoStr = cols.get(ultimoColIndex).text()
                                     .replace(".", "")
                                     .replace(",", ".")
                                     .replace("%", "");
                    
                    try {
                        LocalDate data;
                        try {
                            // Prima prova con il formato con punti
                            data = LocalDate.parse(dataStr, FORMATTER_DOT);
                            log.debug("Data parsata con formato dd.MM.yyyy: {}", dataStr);
                        } catch (Exception e1) {
                            // Se fallisce, prova con il formato con slash
                            try {
                                data = LocalDate.parse(dataStr, FORMATTER_SLASH);
                                log.debug("Data parsata con formato dd/MM/yyyy: {}", dataStr);
                            } catch (Exception e2) {
                                // Se entrambi falliscono, lancia un'eccezione
                                log.error("Impossibile parsare la data con nessun formato: {}", dataStr);
                                throw new IllegalArgumentException("Formato data non supportato: " + dataStr);
                            }
                        }
                        dates.add(data);
                        prices.add(prezzoStr);
                    } catch (Exception e) {
                        log.error("Errore nel parsing della data: {}", dataStr, e);
                    }
                }
            }
            
            // Elabora i dati raccolti
            LocalDate oggi = LocalDate.now();
            LocalDate treMesiFa = oggi.minusMonths(3);
            
            for (int i = 0; i < dates.size(); i++) {
                LocalDate data = dates.get(i);
                String prezzoStr = prices.get(i);
                
                if (data.isBefore(treMesiFa)) continue; // oltre 3 mesi, salta
                
                // Filtra per giorno della settimana
                if (data.getDayOfWeek() != giornoSettimana) continue;

                try {
                    BigDecimal prezzo = new BigDecimal(prezzoStr);

                    PrezzoStorico ps = new PrezzoStorico();
                    ps.setTitolo(titolo);
                    ps.setData(data);
                    ps.setPrezzo(prezzo);

                    prezziStorici.add(ps);
                    log.debug("Aggiunto prezzo storico: {} = {}", data, prezzo);
                } catch (Exception e) {
                    log.error("Errore nel parsing del prezzo: {}", prezzoStr, e);
                    // Continua con la prossima riga invece di interrompere tutto il processo
                }
            }

        } catch (Exception e) {
            log.error("Errore durante l'accesso all'URL: {}", url, e);
            throw e;
        }

        return prezziStorici;
    }
    
    @Override
    public List<Map<String, Object>> estraiPrezziUltimi3MesiMap(Titolo titolo, DayOfWeek giornoSettimana) throws Exception {
        List<Map<String, Object>> risultato = new ArrayList<>();
        
        // Riutilizziamo il metodo esistente per evitare duplicazione di codice
        List<PrezzoStorico> prezziStorici = estraiPrezziUltimi3Mesi(titolo, giornoSettimana);
        
        // Convertiamo la lista di PrezzoStorico in una lista di mappe
        for (PrezzoStorico ps : prezziStorici) {
            Map<String, Object> mappa = new HashMap<>();
            mappa.put("data", ps.getData());
            mappa.put("prezzo", ps.getPrezzo());
            risultato.add(mappa);
        }
        
        log.info("Convertiti {} prezzi storici in mappe per il titolo {}", risultato.size(), titolo.getCodiceIsin());
        return risultato;
    }
}
