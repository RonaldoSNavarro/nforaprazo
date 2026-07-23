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
    private static final Pattern CONTAINER_PATTERN = Pattern.compile("(?i)Container\\s*-[^:]*:\\s*([A-Z]{4}\\d{7})|\\b([A-Z]{4}\\d{7})\\b");
    private static final Pattern VALOR_CARGA_PATTERN = Pattern.compile("(?i)VALOR\\s+TOTAL\\s+DA\\s+CARGA[\\r\\n\\s]*([\\d\\.\\,]+)");
    private static final Pattern VALOR_COMERCIAL_PATTERN = Pattern.compile("(?i)Valor\\s+Comercial:\\s*([\\d\\.\\,]+)");
    private static final Pattern VALOR_SERVICO_PATTERN = Pattern.compile("(?i)(?:VALOR\\s+TOTAL\\s+DO\\s+SERVIÇO|VALOR\\s+TOTAL\\s+A\\s+RECEBER)[\\r\n\\s]*([\\d\\.\\,]+)");
    private static final Pattern NAVIO_PATTERN = Pattern.compile("(?i)Navio/Viagem\\s*:\\s*([A-Z0-9\\s\\/\\-\\_]+)");
    private static final Pattern NAVIO_FALLBACK_PATTERN = Pattern.compile("(?i)(?:Navio|Viagem)\\s*:\\s*([A-Z0-9\\s\\/\\-\\_]+)");
    private static final Pattern CHAVES_OBS_PATTERN = Pattern.compile("(\\d{44})");
    private static final Pattern TOMADOR_BLOCO_PATTERN = Pattern.compile("(?i)([^\\n\\r]+?)TOMADOR\\s+DO\\s+SERVI[ÇC]O\\s*:");
    private static final Pattern CNPJ_PATTERN = Pattern.compile("CNPJ\\s*/\\s*CPF\\s*:\\s*([\\d\\.\\/\\-]+)");
    private static final Pattern REMETENTE_PATTERN = Pattern.compile("(?i)([^\\n\\r]+?)REMETENTE\\s*:");
    private static final Pattern PRESTACAO_PORTOS_PATTERN = Pattern.compile("(?i)([A-ZÁÉÍÓÚÂÊÔÃÕÇ\\s]+?\\s*-\\s*[A-Z]{2})\\s+([A-ZÁÉÍÓÚÂÊÔÃÕÇ\\s]+?\\s*-\\s*[A-Z]{2})[\\r\\n\\s]*IN[IÍ]CIO\\s+DA\\s+PRESTA");
    private static final Pattern MUNICIPIO_PATTERN = Pattern.compile("(?i)MUNIC[IÍ]PIO\\s*:\\s*([^\\n\\r]+)");

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
            Matcher matcherNavio = NAVIO_PATTERN.matcher(fullText);
            if (matcherNavio.find()) {
                String navioStr = matcherNavio.group(1).replaceAll("[\\r\\n]+", " ").replaceAll("\\s+", " ").trim();
                navioStr = navioStr.replaceAll("(?i)\\s+As:?.*$", "").trim();
                result.setNavioViagemDirecao(navioStr);
            } else {
                Matcher matcherFallbackNavio = NAVIO_FALLBACK_PATTERN.matcher(fullText);
                if (matcherFallbackNavio.find()) {
                    String navioStr = matcherFallbackNavio.group(1).replaceAll("[\\r\\n]+", " ").replaceAll("\\s+", " ").trim();
                    navioStr = navioStr.replaceFirst("(?i)^Navio/Viagem\\s*:\\s*", "").trim();
                    navioStr = navioStr.replaceAll("(?i)\\s+As:?.*$", "").trim();
                    result.setNavioViagemDirecao(navioStr);
                }
            }

            // 4. Container (4 letras e 7 números)
            Matcher matcherContainer = CONTAINER_PATTERN.matcher(fullText);
            if (matcherContainer.find()) {
                String cont = matcherContainer.group(1) != null ? matcherContainer.group(1) : matcherContainer.group(2);
                result.setContainer(cont != null ? cont.toUpperCase() : null);
            }

            // 5. Quantidade de NFs (chaves de 44 dígitos originárias adicionais)
            Matcher matcherChavesObs = CHAVES_OBS_PATTERN.matcher(fullText.replaceAll("[^0-9]", " "));
            Set<String> chavesUnicas = new HashSet<>();
            while (matcherChavesObs.find()) {
                chavesUnicas.add(matcherChavesObs.group(1));
            }
            if (result.getChaveAcesso() != null) {
                chavesUnicas.remove(result.getChaveAcesso());
            }
            int qtdNotas = Math.max(1, chavesUnicas.size());
            result.setQuantidadeNotas(qtdNotas);

            // 6. Valor Total da Carga
            Matcher matcherValorCarga = VALOR_CARGA_PATTERN.matcher(fullText);
            if (matcherValorCarga.find()) {
                result.setValorCarga(converterValor(matcherValorCarga.group(1)));
            } else {
                Matcher matcherValorComercial = VALOR_COMERCIAL_PATTERN.matcher(fullText);
                if (matcherValorComercial.find()) {
                    result.setValorCarga(converterValor(matcherValorComercial.group(1)));
                } else {
                    Matcher matcherValorServico = VALOR_SERVICO_PATTERN.matcher(fullText);
                    if (matcherValorServico.find()) {
                        result.setValorCarga(converterValor(matcherValorServico.group(1)));
                    }
                }
            }

            // 7. Tomador (Nome e CNPJ)
            Matcher mTomadorBloco = TOMADOR_BLOCO_PATTERN.matcher(fullText);
            if (mTomadorBloco.find()) {
                String candidate = mTomadorBloco.group(1).trim();
                String[] lines = candidate.split("[\\r\\n]+");
                String nome = lines[lines.length - 1].trim();
                if (!nome.isBlank() && nome.length() > 3 && !nome.toUpperCase().contains("GLOBALIZADO")) {
                    result.setTomadorNome(nome);
                }
            }

            int idxTomador = fullText.toUpperCase().indexOf("TOMADOR DO SERVIÇO:");
            if (idxTomador == -1) {
                idxTomador = fullText.toUpperCase().indexOf("TOMADOR DO SERVI");
            }
            if (idxTomador != -1) {
                int endTomadorBlock = Math.min(fullText.length(), idxTomador + 400);
                String tomadorBlock = fullText.substring(idxTomador, endTomadorBlock);
                Matcher mCnpj = CNPJ_PATTERN.matcher(tomadorBlock);
                if (mCnpj.find()) {
                    result.setTomadorCnpj(mCnpj.group(1).trim());
                }
            }

            if (result.getTomadorNome() == null || result.getTomadorNome().isBlank()) {
                Matcher mRem = REMETENTE_PATTERN.matcher(fullText);
                if (mRem.find()) {
                    String nomeRem = mRem.group(1).trim();
                    if (nomeRem.length() > 3) {
                        result.setTomadorNome(nomeRem);
                    }
                }
            }
            if (result.getTomadorCnpj() == null || result.getTomadorCnpj().isBlank()) {
                Matcher mCnpjGeral = CNPJ_PATTERN.matcher(fullText);
                if (mCnpjGeral.find()) {
                    result.setTomadorCnpj(mCnpjGeral.group(1).trim());
                }
            }

            // 8. Porto de Origem e Destino
            Matcher mPrestacao = PRESTACAO_PORTOS_PATTERN.matcher(fullText);
            if (mPrestacao.find()) {
                String orig = mPrestacao.group(1).trim();
                String dest = mPrestacao.group(2).trim();
                String[] origLines = orig.split("[\\r\\n]+");
                String[] destLines = dest.split("[\\r\\n]+");
                result.setPortoOrigem(origLines[origLines.length - 1].trim());
                result.setPortoDestino(destLines[destLines.length - 1].trim());
            } else {
                Matcher mMun = MUNICIPIO_PATTERN.matcher(fullText);
                if (mMun.find()) {
                    String orig = mMun.group(1).replaceAll("(?i)CEP\\s*:.*", "").trim();
                    result.setPortoOrigem(orig);
                }
                if (mMun.find()) {
                    String dest = mMun.group(1).replaceAll("(?i)CEP\\s*:.*", "").trim();
                    result.setPortoDestino(dest);
                }
            }

            // 9. Observações
            int obsIndex = fullText.toUpperCase().indexOf("OBSERVAÇÕES");
            if (obsIndex == -1) {
                obsIndex = fullText.toUpperCase().indexOf("OBSERVACOES");
            }
            if (obsIndex != -1) {
                int endObs = Math.min(fullText.length(), obsIndex + 300);
                result.setObservacoes(fullText.substring(obsIndex, endObs).trim());
            }

            log.info("Extracao do CT-e concluida. Tomador: {} ({}), Navio: {}, ValorCarga: {}, Origem: {}, Destino: {}",
                    result.getTomadorNome(), result.getTomadorCnpj(), result.getNavioViagemDirecao(),
                    result.getValorCarga(), result.getPortoOrigem(), result.getPortoDestino());

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

        Pattern pChaveBloco = Pattern.compile("(?i)CHAVE\\s+DE\\s+ACESSO[\\s\\S]{1,100}?((?:\\d[\\s\\-]*){44})");
        Matcher mBloco = pChaveBloco.matcher(text);
        if (mBloco.find()) {
            String digits = mBloco.group(1).replaceAll("[^0-9]", "");
            if (digits.length() == 44) {
                return digits;
            }
        }

        Matcher mSpaced = CHAVE_SPACED_PATTERN.matcher(text);
        while (mSpaced.find()) {
            String digits = mSpaced.group().replaceAll("[^0-9]", "");
            if (digits.length() == 44) {
                return digits;
            }
        }

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
