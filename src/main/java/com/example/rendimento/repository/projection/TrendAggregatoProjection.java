package com.example.rendimento.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TrendAggregatoProjection {
    LocalDate getDataSnapshot();
    BigDecimal getAnniAllaScadenza();
    BigDecimal getRendimentoMedio();
    BigDecimal getRendimentoMinimo();
    BigDecimal getRendimentoMassimo();
    Long getNumeroTitoli();
}

