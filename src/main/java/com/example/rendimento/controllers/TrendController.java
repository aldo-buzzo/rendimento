package com.example.rendimento.controllers;

import com.example.rendimento.dto.TrendAggregatoDTO;
import com.example.rendimento.service.TrendQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
}
