package com.example.rendimento.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.rendimento.model.Titolo;
import com.example.rendimento.model.TrendEntity;

public interface TrendService {

    /**
     * Calcola i rendimenti giornalieri di tutti i titoli e salva
     * uno snapshot nella tabella trends.
     */
    void aggiornaTrendGiornaliero();
    
    /**
     * Calcola i rendimenti giornalieri dei titoli di un utente specifico e salva
     * uno snapshot nella tabella trends.
     * 
     * @param utenteId l'ID dell'utente proprietario dei titoli
     */
    void aggiornaTrendGiornalieroPerUtente(Integer utenteId);
    
    /**
     * Salva un nuovo trend o aggiorna un trend esistente per un titolo specifico.
     * Se esiste già un trend per lo stesso ISIN e data snapshot, lo aggiorna.
     * Altrimenti, crea un nuovo trend.
     * 
     * @param titolo il titolo per cui salvare o aggiornare il trend
     * @param prezzo il prezzo utilizzato per il calcolo
     * @param rendimentoCalcolato il rendimento già calcolato (con commissioni e bollo mensile)
     * @param dataSnapshot la data dello snapshot, se null viene utilizzata la data corrente
     * @return l'entità TrendEntity salvata o aggiornata
     */
    TrendEntity salvaOAggiornaTrendPerTitolo(Titolo titolo, BigDecimal prezzo, BigDecimal rendimentoCalcolato, LocalDate dataSnapshot);
}
