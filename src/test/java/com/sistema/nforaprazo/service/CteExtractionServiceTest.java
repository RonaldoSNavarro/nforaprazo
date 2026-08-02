package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.dto.ComprovantePagamentoExtractionResultDto;
import com.sistema.nforaprazo.dto.CteExtractionResultDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CteExtractionServiceTest {

    private final PdfExtractionService pdfExtractionService = new PdfExtractionService();

    @Test
    @DisplayName("Deve extrair com sucesso todos os dados do CT-e PDF real")
    void deveExtrairDadosDeCteReal() {
        File pdfReal = new File("F:\\Dev\\Projetos\\assets\\modelos reais\\42260702427026000901570040001998431019984397 (1).pdf");
        if (!pdfReal.exists()) {
            return; // ignora caso executado fora do ambiente contendo os modelos
        }

        CteExtractionResultDto dto = pdfExtractionService.extrairDadosCte(pdfReal);

        assertNotNull(dto);
        assertEquals("42260702427026000901570040001998431019984397", dto.getChaveAcesso());
        assertEquals("199843", dto.getNumeroCte());
        assertEquals("6AITK1378", dto.getNumeroBooking());
        assertEquals("MSKU1507628", dto.getContainer());
        assertEquals("COPOBRAS S/A IND. E COM. DE EMBALAGENS", dto.getTomadorNome());
        assertEquals("86.445.822/0001-00", dto.getTomadorCnpj());
        assertEquals("VICENTE PINZON/618N", dto.getNavioViagemDirecao());
        assertEquals(new BigDecimal("5870.00"), dto.getValorCarga());
        assertEquals("SAO LUDGERO - SC", dto.getPortoOrigem());
        assertEquals("MANAUS - AM", dto.getPortoDestino());
        assertNotNull(dto.getQuantidadeNotas());
        assertTrue(dto.getQuantidadeNotas() >= 1);
    }

    @Test
    @DisplayName("Deve extrair valor e data de um comprovante de pagamento")
    void deveExtrairDadosDoComprovante() throws Exception {
        MockMultipartFile comprovante = new MockMultipartFile(
                "comprovantePdf", "comprovante.pdf", "application/pdf",
                criarPdf("Valor pago: R$ 1.234,56\nData do pagamento: 05/06/2026"));

        ComprovantePagamentoExtractionResultDto resultado = pdfExtractionService.extrairDadosComprovante(comprovante);

        assertEquals(new BigDecimal("1234.56"), resultado.valorPago());
        assertEquals(LocalDate.of(2026, 6, 5), resultado.dataPagamento());
    }

    private byte[] criarPdf(String conteudo) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            try (PDPageContentStream stream = new PDPageContentStream(document, document.getPage(0))) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                for (String linha : conteudo.split("\\n")) {
                    stream.showText(linha);
                    stream.newLineAtOffset(0, -18);
                }
                stream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
