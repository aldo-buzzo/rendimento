package com.example.rendimento.service.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.rendimento.dto.TrendAggregatoDTO;
import com.example.rendimento.enums.PeriodoScadenza;
import com.example.rendimento.enums.TrendBucket;
import com.example.rendimento.repository.TrendRepository;
import com.example.rendimento.repository.projection.TrendAggregatoProjection;
import com.example.rendimento.service.TrendQueryService;

@Service
@Transactional(readOnly = true)
public class TrendQueryServiceImpl implements TrendQueryService {

    private final TrendRepository trendRepository;

    public TrendQueryServiceImpl(TrendRepository trendRepository) {
        this.trendRepository = trendRepository;
    }

    @Override
    public List<TrendAggregatoDTO> getTrendAggregati(LocalDate dataInizio, LocalDate dataFine) {
        return trendRepository.findTrendAggregati(dataInizio, dataFine)
                .stream()
                .map(this::toDto)
                .toList();
    }
    
    @Override
    public List<TrendAggregatoDTO> getTrendAggregatiPerPeriodo(PeriodoScadenza periodo, LocalDate dataInizio) {
        // Ottieni il bucket corrispondente al periodo
        TrendBucket bucket = periodo.getTrendBucket();
        
        // Prepara la query
        List<TrendAggregatoProjection> allTrends;
        
        if (dataInizio != null) {
            // Se è specificata una data di inizio, filtra da quella data in poi
            allTrends = trendRepository.findTrendAggregatiFromDate(dataInizio);
        } else {
            // Altrimenti, recupera tutti i dati disponibili
            allTrends = trendRepository.findAllTrendAggregati();
        }
        
        // Se il periodo è TUTTI, restituisci tutti i trend senza filtrare per bucket
        if (bucket == null) {
            return allTrends.stream()
                    .map(this::toDto)
                    .toList();
        }
        
        // Altrimenti, filtra i trend per il bucket specifico
        return allTrends.stream()
                .filter(trend -> bucket.matches(trend.getAnniAllaScadenza()))
                .map(this::toDto)
                .toList();
    }

    private TrendAggregatoDTO toDto(TrendAggregatoProjection p) {
        TrendBucket bucket = Arrays.stream(TrendBucket.values())
                .filter(b -> b.matches(p.getAnniAllaScadenza()))
                .findFirst()
                .orElse(TrendBucket.OLTRE_SESSANTA_MESI);

        TrendAggregatoDTO dto = new TrendAggregatoDTO();
        dto.setDataSnapshot(p.getDataSnapshot());
        dto.setBucket(bucket.getLabel());

        dto.setRendimentoMedio(
                p.getRendimentoMedio() != null ? p.getRendimentoMedio().doubleValue() : 0.0);
        dto.setRendimentoMinimo(
                p.getRendimentoMinimo() != null ? p.getRendimentoMinimo().doubleValue() : 0.0);
        dto.setRendimentoMassimo(
                p.getRendimentoMassimo() != null ? p.getRendimentoMassimo().doubleValue() : 0.0);

        dto.setNumeroTitoli(
                p.getNumeroTitoli() != null ? p.getNumeroTitoli().intValue() : 0);

        return dto;
    }

}
