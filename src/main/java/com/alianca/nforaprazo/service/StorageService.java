package com.alianca.nforaprazo.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    private final Path rootLocation;

    public StorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Nao foi possivel inicializar o diretorio de upload", e);
        }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Falha ao salvar arquivo vazio.");
        }
        
        // Verifica se a extensao declarada é pdf
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Apenas arquivos PDF sao permitidos.");
        }

        try {
            // Validacao de segurança por Magic Number (%PDF-)
            byte[] header = new byte[5];
            try (InputStream is = file.getInputStream()) {
                if (is.read(header) != 5 || 
                    header[0] != 0x25 || header[1] != 0x50 || 
                    header[2] != 0x44 || header[3] != 0x46 || header[4] != 0x2D) {
                    throw new IllegalArgumentException("Arquivo nao possui o formato PDF valido (Magic Number invalido).");
                }
            }

            // Evita Directory Traversal gerando um UUID
            String newFilename = UUID.randomUUID().toString() + ".pdf";
            Path destinationFile = this.rootLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new SecurityException("Nao é permitido armazenar arquivo fora do diretorio atual.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Arquivo {} salvo com sucesso como {}", originalFilename, newFilename);
            return newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao armazenar o arquivo.", e);
        }
    }
}
