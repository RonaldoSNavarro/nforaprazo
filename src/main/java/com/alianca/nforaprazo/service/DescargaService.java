package com.alianca.nforaprazo.service;

import com.alianca.nforaprazo.model.Cte;
import com.alianca.nforaprazo.model.PagamentoSefaz;
import com.alianca.nforaprazo.model.Usuario;
import com.alianca.nforaprazo.model.enums.StatusCte;
import com.alianca.nforaprazo.repository.CteRepository;
import com.alianca.nforaprazo.repository.PagamentoSefazRepository;
import com.alianca.nforaprazo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DescargaService {

    private final CteRepository cteRepository;
    private final PagamentoSefazRepository pagamentoSefazRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;

    @Transactional
    public PagamentoSefaz registrarPagamento(
            UUID cteId, BigDecimal valorMulta, LocalDate dataPagamento,
            MultipartFile darPdf, MultipartFile comprovantePdf,
            MultipartFile autoInfracaoPdf, MultipartFile capaPdf,
            String emailUsuario) {
        
        log.info("Iniciando registro de pagamento para CT-e: {}", cteId);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário inválido"));

        Cte cte = cteRepository.findById(cteId)
                .orElseThrow(() -> new IllegalArgumentException("CT-e não encontrado"));

        // Validação de segurança e concorrência (regra do CTO)
        if (cte.getStatus() != StatusCte.PENDENTE_DESCARGA) {
            throw new IllegalStateException("Apenas CT-es com status PENDENTE_DESCARGA podem receber pagamento.");
        }

        // 1. Salvar os arquivos isoladamente
        String darPath = storageService.store(darPdf);
        String comprovantePath = storageService.store(comprovantePdf);
        String autoInfracaoPath = storageService.store(autoInfracaoPdf);
        String capaPath = (capaPdf != null && !capaPdf.isEmpty()) ? storageService.store(capaPdf) : null;

        // 2. Criar a entidade de pagamento vinculada
        PagamentoSefaz pagamento = PagamentoSefaz.builder()
                .cte(cte)
                .valorMulta(valorMulta)
                .dataPagamento(dataPagamento)
                .pathDarPdf(darPath)
                .pathComprovantePdf(comprovantePath)
                .pathAutoInfracaoPdf(autoInfracaoPath)
                .pathCapaPdf(capaPath)
                .usuarioRegistro(usuario)
                .build();

        // 3. Atualizar o CT-e (O @Version garantirá que não foi alterado desde a leitura)
        cte.setStatus(StatusCte.DESEMBARACADO);
        cteRepository.save(cte);

        // 4. Salvar o pagamento
        PagamentoSefaz salvo = pagamentoSefazRepository.save(pagamento);
        log.info("Pagamento registrado com sucesso para o CT-e {}", cte.getChaveAcesso());
        
        return salvo;
    }
}
