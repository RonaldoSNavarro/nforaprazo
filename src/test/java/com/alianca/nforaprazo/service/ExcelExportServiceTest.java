package com.alianca.nforaprazo.service;

import com.alianca.nforaprazo.model.AutoInfracao;
import com.alianca.nforaprazo.model.Cte;
import com.alianca.nforaprazo.model.NotaFiscal;
import com.alianca.nforaprazo.model.Pagamento;
import com.alianca.nforaprazo.model.enums.Responsavel;
import com.alianca.nforaprazo.model.enums.StatusCte;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelExportServiceTest {

    private final ExcelExportService excelExportService = new ExcelExportService();

    @Test
    @DisplayName("Deve exportar lista de CT-es para Excel com sucesso, contendo cabecalhos e dados")
    void deveExportarParaExcelComSucesso() throws IOException {
        // Mocking dados
        NotaFiscal nf = new NotaFiscal();
        nf.setValorNota(new BigDecimal("10000.00"));

        Pagamento pag = Pagamento.builder()
                .valorPago(new BigDecimal("800.00"))
                .dataPagamento(LocalDate.now())
                .build();

        AutoInfracao auto = AutoInfracao.builder()
                .responsavel(Responsavel.CLIENTE)
                .pagamento(pag)
                .build();

        Cte cte = Cte.builder()
                .chaveAcesso("35260600000000000000000000000000000000000001")
                .numeroCte("1001")
                .tomadorNome("Cliente Exemplo S/A")
                .portoOrigem("Santos")
                .portoDestino("Manaus")
                .status(StatusCte.PAGO)
                .autoInfracao(auto)
                .notasFiscais(List.of(nf))
                .build();

        byte[] result = excelExportService.exportarCtesParaExcel(List.of(cte));

        assertNotNull(result);
        assertTrue(result.length > 0);

        // Validar se o array de bytes e de fato um Workbook do Excel valido
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Relatório CT-es");
            assertNotNull(sheet);
            
            // Verifica se a primeira linha e o cabecalho
            assertEquals("Chave de Acesso", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Número do CT-e", sheet.getRow(0).getCell(1).getStringCellValue());
            
            // Verifica se a segunda linha contem os dados mockados
            assertEquals("35260600000000000000000000000000000000000001", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("1001", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Cliente Exemplo S/A", sheet.getRow(1).getCell(2).getStringCellValue());
            assertEquals("PAGO", sheet.getRow(1).getCell(5).getStringCellValue());
            assertEquals("CLIENTE", sheet.getRow(1).getCell(6).getStringCellValue());
            
            // Valor Potencial da Multa (10% de 10000 = 1000.00)
            assertEquals(1000.0, sheet.getRow(1).getCell(7).getNumericCellValue());
            // Valor Pago (800.00)
            assertEquals(800.0, sheet.getRow(1).getCell(8).getNumericCellValue());
        }
    }
}
