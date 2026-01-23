package com.example.rendimento.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.example.rendimento.model.PrezzoStorico;
import com.example.rendimento.model.Titolo;

public interface PrezzoStoricoService {

    /**
     * Estrae i prezzi storici per un titolo sugli ultimi 3 mesi filtrando per un giorno della settimana.
     *
     * @param titolo titolo da cui estrarre i prezzi
     * @param giornoSettimana giorno della settimana da considerare (es. MONDAY)
     * @return lista di PrezzoStorico
     * @throws IOException se non riesce a leggere la pagina
     */
    List<PrezzoStorico> estraiPrezziUltimi3Mesi(Titolo titolo, DayOfWeek giornoSettimana) throws Exception;
    
    /**
     * Estrae i prezzi storici per un titolo sugli ultimi 3 mesi filtrando per un giorno della settimana.
     * Restituisce una lista di mappe con data e prezzo.
     *
     * @param titolo titolo da cui estrarre i prezzi
     * @param giornoSettimana giorno della settimana da considerare (es. MONDAY)
     * @return lista di mappe con data e prezzo
     * @throws IOException se non riesce a leggere la pagina
     */
    List<Map<String, Object>> estraiPrezziUltimi3MesiMap(Titolo titolo, DayOfWeek giornoSettimana) throws Exception;
}
