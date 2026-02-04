package com.example.rendimento.service;

import java.time.LocalDate;
import java.util.List;

import com.example.rendimento.dto.TrendAggregatoDTO;
import com.example.rendimento.enums.PeriodoScadenza;

public interface TrendQueryService {

    List<TrendAggregatoDTO> getTrendAggregati(
            LocalDate dataInizio,
            LocalDate dataFine
    );
    
    /**
     * Ottiene i trend aggregati filtrati per un periodo di scadenza specifico
     * 
     * @param periodo Periodo di scadenza per il filtro
     * @param dataInizio Data di inizio opzionale per filtrare i dati (null per usare tutti i dati disponibili)
     * @return Lista di DTO con i trend aggregati filtrati
     */
    List<TrendAggregatoDTO> getTrendAggregatiPerPeriodo(
            PeriodoScadenza periodo, 
            LocalDate dataInizio
    );
}
