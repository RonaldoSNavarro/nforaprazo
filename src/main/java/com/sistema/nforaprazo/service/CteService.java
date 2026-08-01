package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.annotation.Auditable;
import com.sistema.nforaprazo.dto.CteUploadRequest;
import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.model.Usuario;
import com.sistema.nforaprazo.model.enums.StatusCte;
import com.sistema.nforaprazo.repository.CteRepository;
import com.sistema.nforaprazo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class CteService {

    private final StorageService storageService;
    private final PdfExtractionService pdfExtractionService;
    private final CteRepository cteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final com.sistema.nforaprazo.repository.PortoMonitoradoRepository portoMonitoradoRepository;
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Processa o upload de um CT-e com dados manuais do formulário.
     *
     * Fluxo:
     * 1. Salva o PDF no disco
     * 2. Tenta extrair chave de acesso via PDFBox
     * 3. Valida duplicidade de chave
     * 4. Persiste a entidade com status conforme RN05:
     *    - Porto monitorado (Manaus/PCM/Pecém) → AGUARDANDO_DESEMBARACO
     *    - Demais portos → REGISTRADO (sem alerta)
     */
    @Transactional
    @Auditable(acao = "Upload de CT-e")
    public Cte processarUploadCte(CteUploadRequest request, String emailUsuario) {
        log.info("Processando novo upload de CT-e enviado por {}", emailUsuario);
        
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Usuario invalido: " + emailUsuario));

        String fileName = null;
        String autorizacaoFileName = null;
        String nomeOriginalAutorizacao = null;

        try {
            // 1. Salvar arquivos no disco
            fileName = storageService.store(request.getArquivoCte());
            
            if (request.getArquivoAutorizacaoCusto() != null && !request.getArquivoAutorizacaoCusto().isEmpty()) {
                autorizacaoFileName = storageService.store(request.getArquivoAutorizacaoCusto());
                nomeOriginalAutorizacao = request.getArquivoAutorizacaoCusto().getOriginalFilename();
            }
            
            // 2. Extrair dados via PDFBox
            File savedPdf = Paths.get(uploadDir, fileName).toFile();
            String chaveExtraida = pdfExtractionService.extrairChaveAcesso(savedPdf);
            
            // 3. Checar se a chave já existe
            if (chaveExtraida != null && cteRepository.existsByChaveAcesso(chaveExtraida)) {
                log.warn("Chave de acesso duplicada detectada: {}", chaveExtraida);
                throw new IllegalArgumentException("Já existe um CT-e registrado com a chave de acesso: " + chaveExtraida);
            }

        // A tela de upload informa navio, viagem e direção em um único campo.
        String navio = request.getNavio();
        String viagem = request.getViagem();
        String direcao = request.getDirecao();
        if (request.getNavioViagemDirecao() != null && !request.getNavioViagemDirecao().isBlank()) {
            String[] partes = request.getNavioViagemDirecao().split("/", 3);
            if ((navio == null || navio.isBlank()) && partes.length > 0) {
                navio = partes[0].trim();
            }
            if ((viagem == null || viagem.isBlank()) && partes.length > 1) {
                viagem = partes[1].trim();
            }
            if ((direcao == null || direcao.isBlank()) && partes.length > 2) {
                direcao = partes[2].trim();
            }
        }

        // 4. Determinar status inicial PENDENTE
        Cte novoCte = Cte.builder()
            .numeroCte(request.getNumeroCte())
            .chaveAcesso(chaveExtraida)
            .tomadorNome(request.getTomadorNome())
            .tomadorCnpj(request.getTomadorCnpj())
            .navio(navio)
            .viagem(viagem)
            .direcao(direcao)
            .container(request.getContainer())
            .quantidadeNotas(request.getQuantidadeNotas())
            .portoOrigem(request.getPortoOrigem())
            .portoDestino(request.getPortoDestino())
            .valorCarga(request.getValorCarga())
            .numeroBooking(request.getNumeroBooking())
            .arquivoPdfPath(fileName)
            .nomeOriginalArquivo(request.getArquivoCte().getOriginalFilename())
            .arquivoAutorizacaoCustoPath(autorizacaoFileName)
            .nomeOriginalAutorizacao(nomeOriginalAutorizacao)
            .status(StatusCte.PENDENTE)
            .usuarioUpload(usuario)
            .build();
            
        Cte cteSalvo = cteRepository.save(novoCte);
        
        // Alerta DESCARGA se porto monitorado
        java.util.List<String> portosMonitorados = portoMonitoradoRepository.findByAtivoTrue().stream()
                .map(com.sistema.nforaprazo.model.PortoMonitorado::getNome)
                .toList();

        if (cteSalvo.isPortoMonitorado(portosMonitorados)) {
            log.info("CT-e {} registrado para porto monitorado ({}). Status: PENDENTE. Enviando alerta.",
                    cteSalvo.getId(), request.getPortoDestino());
            emailService.enviarAlertaDescarga(cteSalvo);
        }
        
        log.info("CT-e registrado com sucesso com status PENDENTE. ID: {}", cteSalvo.getId());
        return cteSalvo;
        } catch (Exception e) {
            log.error("Erro ao processar upload do CT-e. Efetuando limpeza de arquivos salvos...", e);
            if (fileName != null) storageService.delete(fileName);
            if (autorizacaoFileName != null) storageService.delete(autorizacaoFileName);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Cte> listarCtesPaginado(org.springframework.data.domain.Pageable pageable) {
        return cteRepository.findAll(pageable);
    }
}
