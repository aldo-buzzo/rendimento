package com.example.rendimento.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PrezzoStorico {

    private Titolo titolo;        // riferimento al titolo
    private LocalDate data;       // data del prezzo
    private BigDecimal prezzo;    // prezzo di chiusura

    // getter e setter
    public Titolo getTitolo() {
        return titolo;
    }

    public void setTitolo(Titolo titolo) {
        this.titolo = titolo;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    @Override
    public String toString() {
        return "PrezzoStorico{" +
                "titolo=" + (titolo != null ? titolo.getCodiceIsin() : "null") +
                ", data=" + data +
                ", prezzo=" + prezzo +
                '}';
    }
}
