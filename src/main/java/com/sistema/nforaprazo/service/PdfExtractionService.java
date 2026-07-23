package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.dto.CteExtractionResultDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PdfExtractionService {

    private static final Pattern CHAVE_SPACED_PATTERN = Pattern.compile("\\b(?:\\d{4}[\\s\\-]*){11}\\b");
    private static final Pattern CHAVE_DIGITOS_PATTERN = Pattern.compile("\\b\\d{44}\\b");
    private static final Pattern BOOKING_PATTERN = Pattern.compile("(?i)Booking:(?:\\s*Booking:)*\\s*([A-Z0-9]{4,20})");
    private static final Pattern NAVIO_VIAGEM_PATTERN = Pattern.compile("(?i)(?:Navio/Viagem|Navio\\s*:\\s*|Viagem\\s*:\\s*)\\s*([^\n\r]+)");
    private static final Pattern CONTAINER_PATTERN = Pattern.compile("(?i)Container\\s*-[^:]*:\\s*([A-Z]{4}\\d{7})|\\b([A-Z]{4}\\d{7})\\b");
    private static final Pattern VALOR_COMERCIAL_PATTERN = Pattern.compile("(?i)Valor\\s+Comercial:\\s*([\\d\\.\\,]+)");
    private static final Pattern VALOR_SERVICO_PATTERN = Pattern.compile("(?i)(?:VALOR\\s+TOTAL\\s+DO\\s+SERVIÇO|VALOR\\s+TOTAL\\s+A\\s+RECEBER)[\\r\n\\s]*([\\d\\.\\,]+)");

    public String extrairChaveAcesso(File pdfFile) {
        log.info("Iniciando extracao de dados do PDF: {}", pdfFile.getName());
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(1);
                stripper.setEndPage(1);
                
                String text = stripper.getText(document);
                String chave = extrairChaveTexto(text);
                if (chave != null) {
                    log.info("Chave de acesso extraida com sucesso: {}", chave);
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

    public CteExtractionResultDto extrairDadosCte(File pdfFile) {
        log.info("Iniciando extracao completa de dados do CT-e PDF: {}", pdfFile.getName());
        CteExtractionResultDto result = new CteExtractionResultDto();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            if (document.isEncrypted()) {
                log.warn("PDF do CT-e está encriptado: {}", pdfFile.getName());
                return result;
            }

            PDFTextStripper stripperPage1 = new PDFTextStripper();
            stripperPage1.setStartPage(1);
            stripperPage1.setEndPage(1);
            String page1Text = stripperPage1.getText(document);

            PDFTextStripper stripperFull = new PDFTextStripper();
            String fullText = stripperFull.getText(document);

            // 1. Chave de Acesso
            String chave = extrairChaveTexto(page1Text);
            if (chave == null) {
                chave = extrairChaveTexto(fullText);
            }

            if (chave != null) {
                result.setChaveAcesso(chave);
                
                // Extrair número do CT-e (posições 26 a 34 da chave de 44 dígitos no padrão SEFAZ)
                if (chave.length() == 44) {
                    try {
                        String numCteStr = chave.substring(25, 34);
                        long numCte = Long.parseLong(numCteStr);
                        result.setNumeroCte(String.valueOf(numCte));
                    } catch (Exception e) {
                        log.debug("Nao foi possivel converter numero do CT-e da chave: {}", e.getMessage());
                    }
                }
            }

            // 2. Booking
            Matcher matcherBooking = BOOKING_PATTERN.matcher(fullText);
            if (matcherBooking.find()) {
                result.setNumeroBooking(matcherBooking.group(1).trim());
            }

            // 3. Navio / Viagem / Direção
            Matcher matcherNavio = NAVIO_VIAGEM_PATTERN.matcher(fullText);
            if (matcherNavio.find()) {
                result.setNavioViagemDirecao(matcherNavio.group(1).trim());
            }

            // 4. Container (4 letras e 7 números)
            Matcher matcherContainer = CONTAINER_PATTERN.matcher(fullText);
            if (matcherContainer.find()) {
                String cont = matcherContainer.group(1) != null ? matcherContainer.group(1) : matcherContainer.group(2);
                result.setContainer(cont != null ? cont.toUpperCase() : null);
            }

            // 5. Quantidade de NFs (chaves de 44 dígitos originárias adicionais)
            Matcher matcherChavesObs = Pattern.compile("(\\d{44})").matcher(fullText.replaceAll("[^0-9]", " "));
            Set<String> chavesUnicas = new HashSet<>();
            while (matcherChavesObs.find()) {
                chavesUnicas.add(matcherChavesObs.group(1));
            }
            if (result.getChaveAcesso() != null) {
                chavesUnicas.remove(result.getChaveAcesso());
            }
            int qtdNotas = Math.max(1, chavesUnicas.size());
            result.setQuantidadeNotas(qtdNotas);

            // 6. Valor da Carga / Valor do CT-e
            Matcher matcherValorComercial = VALOR_COMERCIAL_PATTERN.matcher(fullText);
            if (matcherValorComercial.find()) {
                result.setValorCarga(converterValor(matcherValorComercial.group(1)));
            } else {
                Matcher matcherValorServico = VALOR_SERVICO_PATTERN.matcher(fullText);
                if (matcherValorServico.find()) {
                    result.setValorCarga(converterValor(matcherValorServico.group(1)));
                }
            }

            // 7. Observações
            int obsIndex = fullText.toUpperCase().indexOf("OBSERVAÇÕES");
            if (obsIndex == -1) {
                obsIndex = fullText.toUpperCase().indexOf("OBSERVACOES");
            }
            if (obsIndex != -1) {
                int endObs = Math.min(fullText.length(), obsIndex + 300);
                result.setObservacoes(fullText.substring(obsIndex, endObs).trim());
            }

            log.info("Extracao do CT-e concluida com sucesso. Chave: {}, Numero: {}, Container: {}, Booking: {}, QtdNfs: {}",
                    result.getChaveAcesso(), result.getNumeroCte(), result.getContainer(), result.getNumeroBooking(), result.getQuantidadeNotas());

        } catch (Exception e) {
            log.error("Erro na extracao automatica do PDF do CT-e: {}", e.getMessage(), e);
        }

        return result;
    }

    public BigDecimal extrairValorPagoDar(File pdfFile) {
        log.info("Iniciando extracao do valor pago no DAR PDF: {}", pdfFile.getName());
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                Pattern valorPattern = Pattern.compile("(?i)(?:valor\\s*pago|valor\\s*total|total|valor)\\s*:?\\s*R?\\$?\\s*([\\d]{1,3}(?:\\.[\\d]{3})*,[\\d]{2})");
                Matcher matcher = valorPattern.matcher(text);
                if (matcher.find()) {
                    return converterValor(matcher.group(1));
                }

                Pattern fallbackPattern = Pattern.compile("R\\$\\s*([\\d]{1,3}(?:\\.[\\d]{3})*,[\\d]{2})");
                Matcher fallbackMatcher = fallbackPattern.matcher(text);
                if (fallbackMatcher.find()) {
                    return converterValor(fallbackMatcher.group(1));
                }
            }
        } catch (Exception e) {
            log.error("Erro ao ler PDF do DAR para extracao de valor: {}", e.getMessage(), e);
        }
        return null;
    }

    private String extrairChaveTexto(String text) {
        if (text == null || text.isBlank()) return null;

        // 1. Procurar por bloco contendo CHAVE DE ACESSO
        Pattern pChaveBloco = Pattern.compile("(?i)CHAVE\\s+DE\\s+ACESSO[\\s\\S]{1,100}?((?:\\d[\\s\\-]*){44})");
        Matcher mBloco = pChaveBloco.matcher(text);
        if (mBloco.find()) {
            String digits = mBloco.group(1).replaceAll("[^0-9]", "");
            if (digits.length() == 44) {
                return digits;
            }
        }

        // 2. Procurar padrão de 44 dígitos espaçados
        Matcher mSpaced = CHAVE_SPACED_PATTERN.matcher(text);
        while (mSpaced.find()) {
            String digits = mSpaced.group().replaceAll("[^0-9]", "");
            if (digits.length() == 44) {
                return digits;
            }
        }

        // 3. Procurar 44 dígitos contínuos
        Matcher mDigits = CHAVE_DIGITOS_PATTERN.matcher(text);
        if (mDigits.find()) {
            return mDigits.group();
        }

        return null;
    }

    private BigDecimal converterValor(String valorStr) {
        try {
            String limpo = valorStr.replace(".", "").replace(",", ".");
            return new BigDecimal(limpo);
        } catch (Exception e) {
            log.warn("Erro ao converter valor decimal '{}': {}", valorStr, e.getMessage());
            return null;
        }
    }
}
