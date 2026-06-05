package com.alianca.nforaprazo.service;

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
import org.springframework.web.multipart.MultipartFile;

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
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public Cte processarUploadCte(MultipartFile file, String emailUsuario) {
        log.info("Processando novo upload de CT-e enviado por {}", emailUsuario);
        
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Usuario invalido: " + emailUsuario));

        // 1. Salvar arquivo no disco
        String fileName = storageService.store(file);
        
        // 2. Extrair dados via PDFBox
        File savedPdf = Paths.get(uploadDir, fileName).toFile();
        String chaveExtraida = pdfExtractionService.extrairChaveAcesso(savedPdf);
        
        // 3. Checar se a chave já existe
        if (chaveExtraida != null && cteRepository.existsByChaveAcesso(chaveExtraida)) {
            log.warn("Chave de acesso duplicada detectada: {}", chaveExtraida);
            // Poderíamos deletar o arquivo aqui, mas para log/auditoria podemos manter ou descartar
            throw new IllegalArgumentException("Já existe um CT-e registrado com a chave de acesso: " + chaveExtraida);
        }

        // 4. Salvar Entidade
        Cte novoCte = Cte.builder()
            .chaveAcesso(chaveExtraida) // Pode ser null se falhou na extracao, ai preenche manualmente
            .arquivoPdfPath(fileName)
            .nomeOriginalArquivo(file.getOriginalFilename())
            .status(StatusCte.AGUARDANDO_DESEMBARACO)
            .usuarioUpload(usuario)
            .build();
            
        Cte cteSalvo = cteRepository.save(novoCte);
        log.info("CT-e registrado com sucesso. ID: {}", cteSalvo.getId());
        
        return cteSalvo;
    }
}
