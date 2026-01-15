package com.example.rendimento.service;

import java.math.BigDecimal;
import java.util.List;
import com.example.rendimento.dto.TitoloDTO;
import com.example.rendimento.enums.TipoTitolo;

/**
 * Interfaccia unificata per i servizi di Borsa Italiana che combina le funzionalità
 * di CorsoService e CorsoTitoloService.
 */
public interface BorsaItalianaService {
    /**
     * Restituisce il corso ufficiale del titolo identificato da ISIN.
     *
     * @param isin codice ISIN del titolo
     * @return corso ufficiale
     */
    BigDecimal getCorsoByIsin(String isin);
    
    /**
     * Restituisce il corso ufficiale di un BOT dato l'ISIN.
     *
     * @param isin codice ISIN del titolo
     * @return corso ufficiale
     */
    BigDecimal getCorsoBotByIsin(String isin);
    
    /**
     * Recupera tutte le informazioni di un titolo dato il suo ISIN.
     *
     * @param isin codice ISIN del titolo
     * @return oggetto TitoloDTO contenente tutte le informazioni del titolo
     */
    TitoloDTO getTitoloByIsin(String isin);
    
    /**
     * Recupera tutte le informazioni di un BOT dato il suo ISIN.
     *
     * @param isin codice ISIN del titolo
     * @return oggetto TitoloDTO contenente tutte le informazioni del titolo
     */
    TitoloDTO getTitoloBotByIsin(String isin);
    
    /**
     * Recupera la lista dei titoli da Borsa Italiana.
     * 
     * @return lista dei titoli recuperati da Borsa Italiana
     */
    List<TitoloDTO> getListaTitoli();
    
    /**
     * Restituisce il tipo di titolo gestito da questa implementazione.
     * 
     * @return il tipo di titolo (BTP, BOT, ecc.)
     */
    TipoTitolo getTipoTitolo();
}
