package com.sistema.nforaprazo.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardDataDto {
    private BigDecimal exposicaoTotal;
    private BigDecimal multasPagas;
    private BigDecimal multasEvitadas;
    private List<AutoInfracaoMensalDto> evolucaoMensal;
    private List<ResponsabilidadeDto> responsabilidade;
    private List<ReincidenteDto> reincidentes;
}
