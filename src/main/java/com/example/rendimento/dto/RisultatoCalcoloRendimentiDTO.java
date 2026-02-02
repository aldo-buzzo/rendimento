package com.example.rendimento.dto;

import java.util.List;

/**
 * DTO per il risultato del calcolo dei rendimenti di tutti i titoli.
 * Contiene informazioni sul numero di titoli letti, aggiornati con successo e falliti.
 */
public class RisultatoCalcoloRendimentiDTO {
    
    private int titoliLetti;
    private int titoliAggiornati;
    private int titoliFalliti;
    private String messaggio;
    private List<SimulazioneDTO> simulazioniAggiornate;
    
    public RisultatoCalcoloRendimentiDTO() {
        // Costruttore vuoto per Jackson
    }
    
    public RisultatoCalcoloRendimentiDTO(int titoliLetti, int titoliAggiornati, List<SimulazioneDTO> simulazioniAggiornate) {
        this.titoliLetti = titoliLetti;
        this.titoliAggiornati = titoliAggiornati;
        this.titoliFalliti = titoliLetti - titoliAggiornati;
        this.simulazioniAggiornate = simulazioniAggiornate;
        this.messaggio = String.format("Elaborati %d titoli: %d aggiornati con successo, %d falliti", 
                                      titoliLetti, titoliAggiornati, this.titoliFalliti);
    }
    
    public int getTitoliLetti() {
        return titoliLetti;
    }
    
    public void setTitoliLetti(int titoliLetti) {
        this.titoliLetti = titoliLetti;
    }
    
    public int getTitoliAggiornati() {
        return titoliAggiornati;
    }
    
    public void setTitoliAggiornati(int titoliAggiornati) {
        this.titoliAggiornati = titoliAggiornati;
    }
    
    public int getTitoliFalliti() {
        return titoliFalliti;
    }
    
    public void setTitoliFalliti(int titoliFalliti) {
        this.titoliFalliti = titoliFalliti;
    }
    
    public String getMessaggio() {
        return messaggio;
    }
    
    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }
    
    public List<SimulazioneDTO> getSimulazioniAggiornate() {
        return simulazioniAggiornate;
    }
    
    public void setSimulazioniAggiornate(List<SimulazioneDTO> simulazioniAggiornate) {
        this.simulazioniAggiornate = simulazioniAggiornate;
    }
}