package com.example.rendimento.dto;

import java.time.LocalDate;

public class TrendAggregatoDTO {

    private LocalDate dataSnapshot;
    private String bucket;
    private double rendimentoMedio;
    private double rendimentoMinimo;
    private double rendimentoMassimo;
    private int numeroTitoli;

    // getter e setter
    public LocalDate getDataSnapshot() { return dataSnapshot; }
    public void setDataSnapshot(LocalDate dataSnapshot) { this.dataSnapshot = dataSnapshot; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public double getRendimentoMedio() { return rendimentoMedio; }
    public void setRendimentoMedio(double rendimentoMedio) { this.rendimentoMedio = rendimentoMedio; }

    public double getRendimentoMinimo() { return rendimentoMinimo; }
    public void setRendimentoMinimo(double rendimentoMinimo) { this.rendimentoMinimo = rendimentoMinimo; }

    public double getRendimentoMassimo() { return rendimentoMassimo; }
    public void setRendimentoMassimo(double rendimentoMassimo) { this.rendimentoMassimo = rendimentoMassimo; }

    public int getNumeroTitoli() { return numeroTitoli; }
    public void setNumeroTitoli(int numeroTitoli) { this.numeroTitoli = numeroTitoli; }
}
