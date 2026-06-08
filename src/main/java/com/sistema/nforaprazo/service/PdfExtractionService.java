package com.sistema.nforaprazo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PdfExtractionService {

    // Regex para extrair a chave de acesso (44 digitos)
    private static final Pattern CHAVE_ACESSO_PATTERN = Pattern.compile("\\b\\d{44}\\b");

    public String extrairChaveAcesso(File pdfFile) {
        log.info("Iniciando extracao de dados do PDF: {}", pdfFile.getName());
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(1);
                stripper.setEndPage(1); // Somente primeira pagina
                
                String text = stripper.getText(document);
                
                // Limpar formatacao
                String cleanText = text.replaceAll("[^a-zA-Z0-9]", "");
                Matcher matcher = CHAVE_ACESSO_PATTERN.compile("\\d{44}").matcher(cleanText);
                
                if (matcher.find()) {
                    String chave = matcher.group();
                    log.info("Chave de acesso extraida com sucesso.");
                    return chave;
                } else {
                    log.warn("Nenhuma chave de acesso de 44 digitos encontrada na primeira pagina do PDF.");
                }
            } else {
                log.warn("O arquivo PDF está encriptado e nao pode ser lido.");
            }
        } catch (IOException e) {
            log.error("Erro ao ler o arquivo PDF para extracao: {}", e.getMessage(), e);
        }
        return null;
    }
}
