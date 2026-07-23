package com.sistema.nforaprazo.dto;

import com.sistema.nforaprazo.model.enums.Responsavel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponsabilidadeDto {
    private Responsavel responsavel;
    private Long quantidade;
    private BigDecimal valorTotal;
}
