package com.example.rendimento.service;

import java.time.LocalDate;
import java.util.List;

import com.example.rendimento.dto.TrendAggregatoDTO;

public interface TrendQueryService {

    List<TrendAggregatoDTO> getTrendAggregati(
            LocalDate dataInizio,
            LocalDate dataFine
    );
}
