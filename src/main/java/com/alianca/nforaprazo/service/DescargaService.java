package com.alianca.nforaprazo.service;

import com.alianca.nforaprazo.annotation.Auditable;
import com.alianca.nforaprazo.dto.*;
import com.alianca.nforaprazo.model.*;
import com.alianca.nforaprazo.model.enums.Responsavel;
import com.alianca.nforaprazo.model.enums.StatusCte;
import com.alianca.nforaprazo.repository.AutoInfracaoRepository;
import com.alianca.nforaprazo.repository.CteRepository;
import com.alianca.nforaprazo.repository.EncSemAutoRepository;
import com.alianca.nforaprazo.repository.PagamentoRepository;
import com.alianca.nforaprazo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DescargaService {

    private final CteRepository cteRepository;
    private final AutoInfracaoRepository autoInfracaoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final EncSemAutoRepository encSemAutoRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;
    private final EmailService emailService;

    @Transactional
    @Auditable(acao = "Confirmacao de Desembaraco")
    public Cte confirmarDesembaraco(UUID cteId, String emailUsuario) {
        Cte cte = buscarCteEValidarStatus(cteId, StatusCte.AGUARDANDO_DESEMBARACO);
        cte.setStatus(StatusCte.DESEMBARACADO);
        log.info("CT-e {} marcado como DESEMBARACADO por {}", cteId, emailUsuario);
        return cteRepository.save(cte);
    }

    @Transactional
    @Auditable(acao = "Registro de Auto de Infracao")
    public AutoInfracao registrarAutoInfracao(AutoInfracaoRequest request, String emailUsuario) {
        Cte cte = buscarCteEValidarStatus(request.getCteId(), StatusCte.DESEMBARACADO);
        
        String pathPdf = storageService.store(request.getAutoInfracaoPdf());
        
        AutoInfracao auto = AutoInfracao.builder()
                .cte(cte)
                .arquivoPdfAuto(pathPdf)
                .responsavel(Responsavel.PENDENTE)
                .build();
                
        cte.setStatus(StatusCte.AUTO_RECEBIDO);
        cteRepository.save(cte);
        AutoInfracao autoSalvo = autoInfracaoRepository.save(auto);
        
        log.info("Auto de Infração registrado para CT-e {} por {}. Disparando e-mail para Docs Fiscal.", request.getCteId(), emailUsuario);
        emailService.enviarAlertaDocsFiscal(cte);
        
        return autoSalvo;
    }

    @Transactional
    @Auditable(acao = "Registro de Guia/DAR")
    public Pagamento registrarDar(DarRequest request, String emailUsuario) {
        Cte cte = buscarCteEValidarStatus(request.getCteId(), StatusCte.AGUARDANDO_PAGAMENTO);
        
        AutoInfracao auto = cte.getAutoInfracao();
        if (auto == null) {
            throw new IllegalStateException("CT-e não possui Auto de Infração vinculado.");
        }
        
        if (auto.getResponsavel() == Responsavel.PENDENTE) {
            throw new IllegalStateException("A investigação da equipe DOCS_FISCAL ainda não foi concluída.");
        }
        
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário inválido"));

        String pathDar = storageService.store(request.getDarPdf());
        
        Pagamento pagamento = auto.getPagamento();
        if (pagamento == null) {
            pagamento = Pagamento.builder()
                    .autoInfracao(auto)
                    .pathDarPdf(pathDar)
                    .usuarioRegistro(usuario)
                    .build();
        } else {
            pagamento.setPathDarPdf(pathDar);
        }
        
        log.info("DAR registrado para CT-e {} por {}", request.getCteId(), emailUsuario);
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    @Auditable(acao = "Registro de Comprovante de Pagamento")
    public Pagamento registrarComprovante(ComprovanteRequest request, String emailUsuario) {
        Cte cte = buscarCteEValidarStatus(request.getCteId(), StatusCte.AGUARDANDO_PAGAMENTO);
        
        AutoInfracao auto = cte.getAutoInfracao();
        if (auto == null) {
            throw new IllegalStateException("CT-e não possui Auto de Infração vinculado.");
        }
        
        if (auto.getResponsavel() == Responsavel.PENDENTE) {
            throw new IllegalStateException("A investigação da equipe DOCS_FISCAL ainda não foi concluída.");
        }
        
        Pagamento pagamento = auto.getPagamento();
        if (pagamento == null) {
            throw new IllegalStateException("Nenhum DAR foi registrado para este CT-e ainda.");
        }
        
        String pathComprovante = storageService.store(request.getComprovantePdf());
        String pathCapa = (request.getCapaPdf() != null && !request.getCapaPdf().isEmpty()) ? storageService.store(request.getCapaPdf()) : null;
        
        pagamento.setValorPago(request.getValorPago());
        pagamento.setDataPagamento(request.getDataPagamento());
        pagamento.setPathComprovantePdf(pathComprovante);
        if (pathCapa != null) pagamento.setPathCapaPdf(pathCapa);
        
        cte.setStatus(StatusCte.PAGO);
        cteRepository.save(cte);
        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);
        
        log.info("Comprovante de pagamento registrado para CT-e {} por {}. Disparando e-mail para Faturamento.", request.getCteId(), emailUsuario);
        emailService.enviarAlertaFaturamento(cte);
        
        return pagamentoSalvo;
    }

    @Transactional
    @Auditable(acao = "Encerramento Sem Auto de Infracao")
    public EncSemAuto encerrarSemAuto(EncSemAutoRequest request, String emailUsuario) {
        Cte cte = buscarCteEValidarStatus(request.getCteId(), StatusCte.DESEMBARACADO);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário inválido"));

        BigDecimal valorPotencialMulta = cte.getValorPotencialMulta();

        EncSemAuto enc = EncSemAuto.builder()
                .cte(cte)
                .valorPotencialMulta(valorPotencialMulta)
                .justificativa(request.getJustificativa())
                .usuario(usuario)
                .build();

        cte.setStatus(StatusCte.ENCERRADO_SEM_AUTO);
        cteRepository.save(cte);
        EncSemAuto encSalvo = encSemAutoRepository.save(enc);

        log.info("Processo do CT-e {} encerrado SEM auto por {}. Multa evitada: R$ {}.", 
                cte.getId(), emailUsuario, valorPotencialMulta);

        emailService.enviarAlertaEncerramentoSemAuto(cte);

        return encSalvo;
    }
    
    private Cte buscarCteEValidarStatus(UUID cteId, StatusCte statusEsperado) {
        Cte cte = cteRepository.findById(cteId)
                .orElseThrow(() -> new IllegalArgumentException("CT-e não encontrado"));
                
        if (cte.getStatus() != statusEsperado) {
            throw new IllegalStateException("Operação inválida. O CT-e deveria estar no status " + statusEsperado + " mas está em " + cte.getStatus());
        }
        return cte;
    }
}
