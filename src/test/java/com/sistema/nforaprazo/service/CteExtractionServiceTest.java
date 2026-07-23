package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.dto.CteExtractionResultDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;

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
}
