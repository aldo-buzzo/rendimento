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
        log.warn("Metodo aggiornaTrendGiornaliero() è deprecato. Utilizzare salvaOAggiornaTrendPerTitolo() direttamente.");
        // Implementazione vuota - metodo deprecato
    }
    
    @Override
    @Transactional
    public void aggiornaTrendGiornalieroPerUtente(Integer utenteId) {
        log.warn("Metodo aggiornaTrendGiornalieroPerUtente() è deprecato. Utilizzare salvaOAggiornaTrendPerTitolo() direttamente.");
        // Implementazione vuota - metodo deprecato
    }
    
    @Override
    @Transactional
    public TrendEntity salvaOAggiornaTrendPerTitolo(Titolo titolo, BigDecimal prezzo, BigDecimal rendimentoCalcolato, LocalDate dataSnapshot) {
        // Se dataSnapshot è null, usa la data corrente
        LocalDate dataEffettiva = dataSnapshot != null ? dataSnapshot : LocalDate.now();
        
        log.info("Salvataggio o aggiornamento trend per titolo ISIN: {} con prezzo: {} e rendimento: {} per data: {}", 
                titolo.getCodiceIsin(), prezzo, rendimentoCalcolato, dataEffettiva);
        
        // Cerca se esiste già un trend per questo ISIN e data
        java.util.Optional<TrendEntity> trendEsistente = trendRepository.findByIsinAndDataSnapshot(titolo.getCodiceIsin(), dataEffettiva);
        
        TrendEntity trend;
        if (trendEsistente.isPresent()) {
            // Aggiorna l'entità esistente
            trend = trendEsistente.get();
            // Aggiorna solo i campi che potrebbero essere cambiati
            trend.setPrezzo(prezzo);
            trend.setRendimentoAnnuo(rendimentoCalcolato);
            log.debug("Aggiornamento trend esistente per titolo {}: rendimento={}, data={}", 
                titolo.getCodiceIsin(), rendimentoCalcolato, dataEffettiva);
        } else {
            // Crea una nuova entità
            trend = new TrendEntity();
            trend.setIsin(titolo.getCodiceIsin());
            trend.setDataSnapshot(dataEffettiva);
            trend.setDataScadenza(titolo.getDataScadenza());
            
            // Calcola i giorni e anni alla scadenza
            long giorniAllaScadenza = ChronoUnit.DAYS.between(dataEffettiva, titolo.getDataScadenza());
            BigDecimal anniAllaScadenza = BigDecimal.valueOf(giorniAllaScadenza)
                .divide(BigDecimal.valueOf(365), 6, RoundingMode.HALF_UP);
                
            trend.setGiorniAllaScadenza(BigDecimal.valueOf(giorniAllaScadenza));
            trend.setAnniAllaScadenza(anniAllaScadenza);
            trend.setPrezzo(prezzo);
            trend.setRendimentoAnnuo(rendimentoCalcolato);
            log.debug("Creazione nuovo trend per titolo {}: rendimento={}, data={}", 
                titolo.getCodiceIsin(), rendimentoCalcolato, dataEffettiva);
        }

        // Salva o aggiorna l'entità
        TrendEntity savedTrend = trendRepository.save(trend);
        log.debug("Salvato/aggiornato trend per titolo {}: rendimento={}, data={}", 
            titolo.getCodiceIsin(), rendimentoCalcolato, dataEffettiva);
        
        return savedTrend;
    }
}
