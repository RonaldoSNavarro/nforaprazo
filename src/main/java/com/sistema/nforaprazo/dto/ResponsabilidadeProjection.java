package com.sistema.nforaprazo.dto;

import com.sistema.nforaprazo.model.enums.Responsavel;
import java.math.BigDecimal;

public interface ResponsabilidadeProjection {
    Responsavel getResponsavel();
    Long getQuantidade();
    BigDecimal getValorTotal();
}
