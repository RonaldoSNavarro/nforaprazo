package com.sistema.nforaprazo.dto;

import java.math.BigDecimal;

public interface AutoInfracaoMensalProjection {
    Integer getAno();
    Integer getMes();
    Long getQuantidade();
    BigDecimal getValorTotal();
}
