package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.dto.DashboardDataDto;
import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.model.enums.StatusCte;
import com.sistema.nforaprazo.repository.AutoInfracaoRepository;
import com.sistema.nforaprazo.repository.CteRepository;
import com.sistema.nforaprazo.repository.EncSemAutoRepository;
import com.sistema.nforaprazo.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final CteRepository cteRepository;
    private final PagamentoRepository pagamentoRepository;
    private final EncSemAutoRepository encSemAutoRepository;
    private final AutoInfracaoRepository autoInfracaoRepository;

    public DashboardDataDto obterDadosDashboard() {
        List<StatusCte> statusesExposicao = List.of(
                StatusCte.AGUARDANDO_DESEMBARACO,
                StatusCte.AUTO_RECEBIDO,
                StatusCte.EM_INVESTIGACAO,
                StatusCte.AGUARDANDO_PAGAMENTO
        );

        BigDecimal exposicao = cteRepository.sumValorPotencialMultaByStatusIn(statusesExposicao);
        BigDecimal pago = pagamentoRepository.sumTotalPago();
        BigDecimal evitado = encSemAutoRepository.sumTotalEvitado();

        var evolucao = autoInfracaoRepository.findEvolucaoMensal();
        var responsabilidade = autoInfracaoRepository.findResponsabilidadeStats();
        var topTomadores = autoInfracaoRepository.findTopTomadores(PageRequest.of(0, 5));

        return DashboardDataDto.builder()
                .exposicaoTotal(exposicao)
                .multasPagas(pago)
                .multasEvitadas(evitado)
                .evolucaoMensal(evolucao)
                .responsabilidade(responsabilidade)
                .reincidentes(topTomadores)
                .build();
    }

    public List<Cte> obterCtesParaRelatorio(String portoDestino, java.time.LocalDate dataInicio, java.time.LocalDate dataFim) {
        java.time.LocalDateTime startDateTime = dataInicio != null ? dataInicio.atStartOfDay() : java.time.LocalDateTime.of(1970, 1, 1, 0, 0);
        java.time.LocalDateTime endDateTime = dataFim != null ? dataFim.atTime(23, 59, 59) : java.time.LocalDateTime.of(2999, 12, 31, 23, 59, 59);
        return cteRepository.findCtesReport(portoDestino, startDateTime, endDateTime);
    }
}
