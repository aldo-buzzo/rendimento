package com.example.rendimento.dto;

/**
 * DTO per contenere i risultati dell'elaborazione della simulazione.
 */
public class ElaborazioneRisultatoDTO {
    private final SimulazioneDTO simulazione;
    private final RisultatoRendimentoAdvancedDTO risultatoDettagliato;
    
    public ElaborazioneRisultatoDTO(SimulazioneDTO simulazione, RisultatoRendimentoAdvancedDTO risultatoDettagliato) {
        this.simulazione = simulazione;
        this.risultatoDettagliato = risultatoDettagliato;
    }
    
    public SimulazioneDTO getSimulazione() {
        return simulazione;
    }
    
    public RisultatoRendimentoAdvancedDTO getRisultatoDettagliato() {
        return risultatoDettagliato;
    }
}