package com.example.rendimento.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.rendimento.dto.TrendAggregatoDTO;
import com.example.rendimento.enums.PeriodoScadenza;
import com.example.rendimento.service.TrendQueryService;

@RestController
@RequestMapping("/api/trend")
public class TrendController {

    private final TrendQueryService trendService;

    public TrendController(TrendQueryService trendService) {
        this.trendService = trendService;
    }

    /**
     * Restituisce i trend aggregati dei tassi di interesse.
     * Parametri facoltativi: dataInizio, dataFine, keyword
     */
    @GetMapping("/aggregati")
    public ResponseEntity<List<TrendAggregatoDTO>> getTrendAggregati(
            @RequestParam(name = "dataInizio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInizio,

            @RequestParam(name = "dataFine", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFine,

            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        // Se le date non sono specificate, si possono usare dei default (es. ultimi 30 giorni)
        if (dataInizio == null) {
            dataInizio = LocalDate.now().minusDays(30);
        }
        if (dataFine == null) {
            dataFine = LocalDate.now();
        }

        // Chiama il servizio per ottenere i trend aggregati
        List<TrendAggregatoDTO> trendList = trendService.getTrendAggregati(dataInizio, dataFine);

        // Se è presente una keyword, filtriamo lato controller (esempio: ISIN o etichetta bucket)
        if (keyword != null && !keyword.isBlank()) {
            trendList = trendList.stream()
                    .filter(t -> t.getBucket().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
        }

        return ResponseEntity.ok(trendList);
    }
    
    /**
     * Restituisce i trend aggregati dei tassi di interesse filtrati per periodo di scadenza.
     * Parametro facoltativo: dataInizio
     * 
     * @param periodoStr Il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     * @param dataInizio La data di inizio opzionale per filtrare i dati
     * @return Lista di trend aggregati filtrati per periodo di scadenza
     */
    @GetMapping("/periodo/{periodo}")
    public ResponseEntity<List<TrendAggregatoDTO>> getTrendAggregatiPerPeriodo(
            @PathVariable("periodo") String periodoStr,
            
            @RequestParam(name = "dataInizio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInizio
    ) {
        // Converti la stringa in enum
        PeriodoScadenza periodo = PeriodoScadenza.fromString(periodoStr);
        
        // Chiama il servizio
        List<TrendAggregatoDTO> trendList = trendService.getTrendAggregatiPerPeriodo(periodo, dataInizio);
        
        return ResponseEntity.ok(trendList);
    }
    
    /**
     * Restituisce gli andamenti dei tassi di interesse nel tempo per un periodo specifico.
     * Questo endpoint è ottimizzato per la visualizzazione grafica degli andamenti dei tassi.
     * 
     * @param periodoStr Il periodo di scadenza (trimestrali, semestrali, annuali, triennali, tutti)
     * @param dataInizio La data di inizio opzionale per filtrare i dati
     * @param dataFine La data di fine opzionale per filtrare i dati
     * @return Dati formattati per la visualizzazione grafica degli andamenti dei tassi
     */
    @GetMapping("/andamenti/{periodo}")
    public ResponseEntity<Map<String, Object>> getAndamentiTassi(
            @PathVariable("periodo") String periodoStr,
            
            @RequestParam(name = "dataInizio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInizioParam,
            
            @RequestParam(name = "dataFine", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFineParam
    ) {
        // Se le date non sono specificate, si possono usare dei default
        LocalDate dataInizio = dataInizioParam != null ? dataInizioParam : LocalDate.now().minusDays(90); // Ultimi 3 mesi per default
        LocalDate dataFine = dataFineParam != null ? dataFineParam : LocalDate.now();
        
        // Converti la stringa in enum
        PeriodoScadenza periodo = PeriodoScadenza.fromString(periodoStr);
        
        // Chiama il servizio
        List<TrendAggregatoDTO> trendList = trendService.getTrendAggregatiPerPeriodo(periodo, dataInizio);
        
        // Filtra per data fine
        trendList = trendList.stream()
                .filter(trend -> !trend.getDataSnapshot().isAfter(dataFine))
                .collect(Collectors.toList());
        
        // Organizza i dati per la visualizzazione grafica
        Map<String, Object> response = new HashMap<>();
        
        // Estrai le date uniche e ordinate
        List<LocalDate> dates = trendList.stream()
                .map(TrendAggregatoDTO::getDataSnapshot)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        // Converti le date in stringhe per il frontend
        List<String> labels = dates.stream()
                .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .collect(Collectors.toList());
        
        // Prepara i dati per il grafico
        List<Double> rendimentiMedi = new ArrayList<>();
        List<Double> rendimentiMinimi = new ArrayList<>();
        List<Double> rendimentiMassimi = new ArrayList<>();
        
        // Per ogni data, calcola i rendimenti aggregati
        for (LocalDate date : dates) {
            // Filtra i trend per la data corrente
            List<TrendAggregatoDTO> trendsPerData = trendList.stream()
                    .filter(trend -> trend.getDataSnapshot().equals(date))
                    .collect(Collectors.toList());
            
            // Calcola i rendimenti aggregati per questa data
            double rendimentoMinimo = trendsPerData.stream()
                    .mapToDouble(TrendAggregatoDTO::getRendimentoMinimo)
                    .min()
                    .orElse(0.0);
                    
            double rendimentoMassimo = trendsPerData.stream()
                    .mapToDouble(TrendAggregatoDTO::getRendimentoMassimo)
                    .max()
                    .orElse(0.0);
                    
            // Calcola il rendimento medio ponderato
            double sommaRendimentiPesati = trendsPerData.stream()
                    .mapToDouble(t -> t.getRendimentoMedio() * t.getNumeroTitoli())
                    .sum();
                    
            int totaleTitoli = trendsPerData.stream()
                    .mapToInt(TrendAggregatoDTO::getNumeroTitoli)
                    .sum();
                    
            double rendimentoMedio = totaleTitoli > 0 ? sommaRendimentiPesati / totaleTitoli : 0.0;
            
            // Aggiungi i rendimenti alle liste
            rendimentiMinimi.add(rendimentoMinimo);
            rendimentiMedi.add(rendimentoMedio);
            rendimentiMassimi.add(rendimentoMassimo);
        }
        
        // Costruisci la risposta
        response.put("labels", labels);
        response.put("rendimentiMinimi", rendimentiMinimi);
        response.put("rendimentiMedi", rendimentiMedi);
        response.put("rendimentiMassimi", rendimentiMassimi);
        response.put("periodo", periodoStr);
        
        return ResponseEntity.ok(response);
    }
    
}
