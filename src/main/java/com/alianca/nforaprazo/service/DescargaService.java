package com.alianca.nforaprazo.service;

import com.alianca.nforaprazo.model.Cte;
import com.alianca.nforaprazo.model.AutoInfracao;
import com.alianca.nforaprazo.model.Pagamento;
import com.alianca.nforaprazo.model.Usuario;
import com.alianca.nforaprazo.model.enums.StatusCte;
import com.alianca.nforaprazo.repository.AutoInfracaoRepository;
import com.alianca.nforaprazo.repository.CteRepository;
import com.alianca.nforaprazo.repository.PagamentoRepository;
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
    private final AutoInfracaoRepository autoInfracaoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;

    @Transactional
    public Cte confirmarDesembaraco(UUID cteId, String emailUsuario) {
        Cte cte = buscarCteEValidarStatus(cteId, StatusCte.AGUARDANDO_DESEMBARACO);
        cte.setStatus(StatusCte.DESEMBARACADO);
        log.info("CT-e {} marcado como DESEMBARACADO por {}", cteId, emailUsuario);
        return cteRepository.save(cte);
    }

    @Transactional
    public AutoInfracao registrarAutoInfracao(UUID cteId, MultipartFile autoPdf, String emailUsuario) {
        Cte cte = buscarCteEValidarStatus(cteId, StatusCte.DESEMBARACADO);
        
        String pathPdf = storageService.store(autoPdf);
        
        AutoInfracao auto = AutoInfracao.builder()
                .cte(cte)
                .arquivoPdfAuto(pathPdf)
                .build();
                
        cte.setStatus(StatusCte.AUTO_RECEBIDO);
        cteRepository.save(cte);
        
        log.info("Auto de Infração registrado para CT-e {} por {}", cteId, emailUsuario);
        return autoInfracaoRepository.save(auto);
    }

    @Transactional
    public Pagamento registrarDar(UUID cteId, MultipartFile darPdf, String emailUsuario) {
        Cte cte = buscarCteEValidarStatus(cteId, StatusCte.AUTO_RECEBIDO);
        
        // Pelo fluxo atual, o Auto deve existir
        AutoInfracao auto = cte.getAutoInfracao();
        if (auto == null) {
            throw new IllegalStateException("CT-e não possui Auto de Infração vinculado.");
        }
        
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário inválido"));

        String pathDar = storageService.store(darPdf);
        
        Pagamento pagamento = Pagamento.builder()
                .autoInfracao(auto)
                .pathDarPdf(pathDar)
                .usuarioRegistro(usuario)
                .build();
                
        cte.setStatus(StatusCte.AGUARDANDO_PAGAMENTO);
        cteRepository.save(cte);
        
        log.info("DAR registrado para CT-e {} por {}", cteId, emailUsuario);
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public Pagamento registrarComprovante(UUID cteId, BigDecimal valorPago, LocalDate dataPagamento, MultipartFile comprovantePdf, MultipartFile capaPdf, String emailUsuario) {
        Cte cte = buscarCteEValidarStatus(cteId, StatusCte.AGUARDANDO_PAGAMENTO);
        
        AutoInfracao auto = cte.getAutoInfracao();
        Pagamento pagamento = auto.getPagamento();
        if (pagamento == null) {
            throw new IllegalStateException("Nenhum DAR foi registrado para este CT-e ainda.");
        }
        
        String pathComprovante = storageService.store(comprovantePdf);
        String pathCapa = (capaPdf != null && !capaPdf.isEmpty()) ? storageService.store(capaPdf) : null;
        
        pagamento.setValorPago(valorPago);
        pagamento.setDataPagamento(dataPagamento);
        pagamento.setPathComprovantePdf(pathComprovante);
        if (pathCapa != null) pagamento.setPathCapaPdf(pathCapa);
        
        cte.setStatus(StatusCte.PAGO);
        cteRepository.save(cte);
        
        log.info("Comprovante de pagamento registrado para CT-e {} por {}", cteId, emailUsuario);
        return pagamentoRepository.save(pagamento);
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
