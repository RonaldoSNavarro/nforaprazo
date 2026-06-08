package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.annotation.Auditable;
import com.sistema.nforaprazo.dto.InvestigacaoRequest;
import com.sistema.nforaprazo.model.AutoInfracao;
import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.model.enums.Responsavel;
import com.sistema.nforaprazo.model.enums.StatusCte;
import com.sistema.nforaprazo.repository.AutoInfracaoRepository;
import com.sistema.nforaprazo.repository.CteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocsFiscalService {

    private final CteRepository cteRepository;
    private final AutoInfracaoRepository autoInfracaoRepository;
    private final StorageService storageService;
    private final EmailService emailService;

    @Transactional
    @Auditable(acao = "Inicio de Investigacao")
    public Cte iniciarInvestigacao(UUID cteId, String emailUsuario) {
        Cte cte = cteRepository.findById(cteId)
                .orElseThrow(() -> new IllegalArgumentException("CT-e não encontrado"));

        if (cte.getStatus() != StatusCte.AUTO_RECEBIDO) {
            throw new IllegalStateException("O CT-e deve estar no status AUTO_RECEBIDO para iniciar a investigação. Status atual: " + cte.getStatus());
        }

        cte.setStatus(StatusCte.EM_INVESTIGACAO);
        log.info("CT-e {} colocado em INVESTIGACAO por {}", cteId, emailUsuario);
        return cteRepository.save(cte);
    }

    @Transactional
    @Auditable(acao = "Conclusao de Investigacao")
    public AutoInfracao concluirInvestigacao(InvestigacaoRequest request, String emailUsuario) {
        Cte cte = cteRepository.findById(request.getCteId())
                .orElseThrow(() -> new IllegalArgumentException("CT-e não encontrado"));

        if (cte.getStatus() != StatusCte.EM_INVESTIGACAO) {
            throw new IllegalStateException("O CT-e deve estar no status EM_INVESTIGACAO para concluir a investigação. Status atual: " + cte.getStatus());
        }

        AutoInfracao auto = cte.getAutoInfracao();
        if (auto == null) {
            auto = AutoInfracao.builder().cte(cte).build();
        }

        if (request.getResponsavel() == Responsavel.PENDENTE) {
            throw new IllegalArgumentException("Responsável da multa deve ser explicitamente definido como SISTEMA ou CLIENTE.");
        }

        if (request.getMotivoErro() == null || request.getMotivoErro().trim().length() < 10) {
            throw new IllegalArgumentException("O motivo do erro deve ter pelo menos 10 caracteres.");
        }

        if (request.getNumeroTicket() == null || request.getNumeroTicket().trim().isEmpty()) {
            throw new IllegalArgumentException("O número do ticket é obrigatório.");
        }

        auto.setResponsavel(request.getResponsavel());
        auto.setMotivoErro(request.getMotivoErro());
        auto.setNumeroTicket(request.getNumeroTicket());

        if (request.getArquivoTicket() != null && !request.getArquivoTicket().isEmpty()) {
            String pathTicket = storageService.store(request.getArquivoTicket());
            auto.setArquivoTicket(pathTicket);
        }

        cte.setStatus(StatusCte.AGUARDANDO_PAGAMENTO);
        cteRepository.save(cte);
        AutoInfracao autoSalvo = autoInfracaoRepository.save(auto);

        log.info("Investigação concluída para CT-e {} por {}. Responsável: {}. Enviando e-mail para Descarga.", 
                request.getCteId(), emailUsuario, request.getResponsavel());
        
        emailService.enviarAlertaPagamentoDescarga(cte);

        return autoSalvo;
    }
}
