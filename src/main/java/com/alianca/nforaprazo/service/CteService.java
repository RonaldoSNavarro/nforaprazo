package com.alianca.nforaprazo.service;

import com.alianca.nforaprazo.dto.CteUploadRequest;
import com.alianca.nforaprazo.model.Cte;
import com.alianca.nforaprazo.model.Usuario;
import com.alianca.nforaprazo.model.enums.StatusCte;
import com.alianca.nforaprazo.repository.CteRepository;
import com.alianca.nforaprazo.repository.UsuarioRepository;
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
    public Cte processarUploadCte(CteUploadRequest request, String emailUsuario) {
        log.info("Processando novo upload de CT-e enviado por {}", emailUsuario);
        
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Usuario invalido: " + emailUsuario));

        // 1. Salvar arquivo no disco
        String fileName = storageService.store(request.getArquivoCte());
        
        // 2. Extrair dados via PDFBox
        File savedPdf = Paths.get(uploadDir, fileName).toFile();
        String chaveExtraida = pdfExtractionService.extrairChaveAcesso(savedPdf);
        
        // 3. Checar se a chave já existe
        if (chaveExtraida != null && cteRepository.existsByChaveAcesso(chaveExtraida)) {
            log.warn("Chave de acesso duplicada detectada: {}", chaveExtraida);
            throw new IllegalArgumentException("Já existe um CT-e registrado com a chave de acesso: " + chaveExtraida);
        }

        // 4. Determinar status inicial por RN05
        Cte novoCte = Cte.builder()
            .numeroCte(request.getNumeroCte())
            .chaveAcesso(chaveExtraida)
            .tomadorNome(request.getTomadorNome())
            .tomadorCnpj(request.getTomadorCnpj())
            .navio(request.getNavio())
            .viagem(request.getViagem())
            .portoOrigem(request.getPortoOrigem())
            .portoDestino(request.getPortoDestino())
            .valorCarga(request.getValorCarga())
            .numeroBooking(request.getNumeroBooking())
            .arquivoPdfPath(fileName)
            .nomeOriginalArquivo(request.getArquivoCte().getOriginalFilename())
            .status(StatusCte.REGISTRADO)
            .usuarioUpload(usuario)
            .build();
            
        Cte cteSalvo = cteRepository.save(novoCte);
        
        // 5. Se porto monitorado, transicionar para AGUARDANDO_DESEMBARACO e alertar DESCARGA
        if (cteSalvo.isPortoMonitorado()) {
            cteSalvo.setStatus(StatusCte.AGUARDANDO_DESEMBARACO);
            cteSalvo = cteRepository.save(cteSalvo);
            log.info("CT-e {} com porto monitorado ({}). Status: AGUARDANDO_DESEMBARACO. Alerta DESCARGA pendente.",
                    cteSalvo.getId(), request.getPortoDestino());
            emailService.enviarAlertaDescarga(cteSalvo);
        } else {
            log.info("CT-e {} registrado para porto '{}'. Sem alerta (desembaraço é responsabilidade do cliente).",
                    cteSalvo.getId(), request.getPortoDestino());
        }
        
        log.info("CT-e registrado com sucesso. ID: {}", cteSalvo.getId());
        return cteSalvo;
    }
}
