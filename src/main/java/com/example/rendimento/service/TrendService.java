package com.example.rendimento.service;

import java.math.BigDecimal;

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
     * Salva un trend per un titolo specifico utilizzando dati già calcolati.
     * Questo metodo è utile quando si hanno già i dati necessari in memoria
     * e si vuole evitare di ricalcolarli.
     * 
     * @param titolo il titolo per cui salvare il trend
     * @param prezzoAcquisto il prezzo di acquisto utilizzato per il calcolo
     * @param rendimentoCalcolato il rendimento già calcolato (con commissioni e bollo mensile)
     * @return l'entità TrendEntity salvata
     */
    TrendEntity salvaTrendPerTitolo(Titolo titolo, BigDecimal prezzoAcquisto, BigDecimal rendimentoCalcolato);
}
