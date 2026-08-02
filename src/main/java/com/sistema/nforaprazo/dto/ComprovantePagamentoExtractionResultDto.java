package com.sistema.nforaprazo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ComprovantePagamentoExtractionResultDto(BigDecimal valorPago, LocalDate dataPagamento) {
}
