package com.alianca.nforaprazo.dto;

import com.alianca.nforaprazo.model.enums.Responsavel;
import java.math.BigDecimal;

public interface ResponsabilidadeProjection {
    Responsavel getResponsavel();
    Long getQuantidade();
    BigDecimal getValorTotal();
}
