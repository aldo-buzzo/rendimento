package com.example.rendimento.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.rendimento.model.Titolo;
import com.example.rendimento.model.TrendEntity;
import com.example.rendimento.repository.TrendRepository;
import com.example.rendimento.service.TrendService;

@Service
public class TrendServiceImpl implements TrendService {

    private static final Logger log = LoggerFactory.getLogger(TrendServiceImpl.class);

    private final TrendRepository trendRepository;

    @Autowired
    public TrendServiceImpl(TrendRepository trendRepository) {
        this.trendRepository = trendRepository;
    }

    @Override
    @Transactional
    public void aggiornaTrendGiornaliero() {
        log.warn("Metodo aggiornaTrendGiornaliero() è deprecato. Utilizzare salvaTrendPerTitolo() direttamente.");
        // Implementazione vuota - metodo deprecato
    }
    
    @Override
    @Transactional
    public void aggiornaTrendGiornalieroPerUtente(Integer utenteId) {
        log.warn("Metodo aggiornaTrendGiornalieroPerUtente() è deprecato. Utilizzare salvaTrendPerTitolo() direttamente.");
        // Implementazione vuota - metodo deprecato
    }
    
    @Override
    @Transactional
    public TrendEntity salvaTrendPerTitolo(Titolo titolo, BigDecimal prezzoAcquisto, BigDecimal rendimentoCalcolato) {
        log.info("Salvataggio trend per titolo ISIN: {} con prezzo: {} e rendimento: {}", 
                titolo.getCodiceIsin(), prezzoAcquisto, rendimentoCalcolato);
        
        LocalDate oggi = LocalDate.now();
        
        // Calcola i giorni e anni alla scadenza
        long giorniAllaScadenza = ChronoUnit.DAYS.between(oggi, titolo.getDataScadenza());
        BigDecimal anniAllaScadenza = BigDecimal.valueOf(giorniAllaScadenza)
            .divide(BigDecimal.valueOf(365), 6, RoundingMode.HALF_UP);

        // Crea e popola l'entità TrendEntity con i campi disponibili
        TrendEntity trend = new TrendEntity();
        trend.setIsin(titolo.getCodiceIsin());
        trend.setDataSnapshot(oggi);
        trend.setDataScadenza(titolo.getDataScadenza());
        trend.setGiorniAllaScadenza(BigDecimal.valueOf(giorniAllaScadenza));
        trend.setAnniAllaScadenza(anniAllaScadenza);
        trend.setPrezzo(prezzoAcquisto);
        trend.setRendimentoAnnuo(rendimentoCalcolato);

        // Salva e restituisci l'entità
        TrendEntity savedTrend = trendRepository.save(trend);
        log.debug("Salvato trend per titolo {}: rendimento={}", 
            titolo.getCodiceIsin(), rendimentoCalcolato);
        
        return savedTrend;
    }
}
