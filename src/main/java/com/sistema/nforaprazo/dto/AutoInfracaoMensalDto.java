package com.sistema.nforaprazo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoInfracaoMensalDto {
    private Integer ano;
    private Integer mes;
    private Long quantidade;
    private BigDecimal valorTotal;
}
