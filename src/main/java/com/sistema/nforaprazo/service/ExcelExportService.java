package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.model.Cte;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportarCtesParaExcel(List<Cte> ctes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Relatório CT-es");

            // Fonts & Styles
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
            dateCellStyle.setBorderBottom(BorderStyle.THIN);
            dateCellStyle.setBorderTop(BorderStyle.THIN);
            dateCellStyle.setBorderRight(BorderStyle.THIN);
            dateCellStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle currencyCellStyle = workbook.createCellStyle();
            currencyCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("R$ #,##0.00"));
            currencyCellStyle.setBorderBottom(BorderStyle.THIN);
            currencyCellStyle.setBorderTop(BorderStyle.THIN);
            currencyCellStyle.setBorderRight(BorderStyle.THIN);
            currencyCellStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle textCellStyle = workbook.createCellStyle();
            textCellStyle.setBorderBottom(BorderStyle.THIN);
            textCellStyle.setBorderTop(BorderStyle.THIN);
            textCellStyle.setBorderRight(BorderStyle.THIN);
            textCellStyle.setBorderLeft(BorderStyle.THIN);

            // Row 0: Headers
            String[] columns = {
                    "Chave de Acesso", "Número do CT-e", "Tomador", "Origem", "Destino",
                    "Status", "Responsável", "Valor Potencial da Multa", "Valor Pago", "Data do Pagamento"
            };

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Fill Data
            int rowIdx = 1;
            for (Cte cte : ctes) {
                Row row = sheet.createRow(rowIdx++);

                // Chave de Acesso
                Cell cellChave = row.createCell(0);
                cellChave.setCellValue(cte.getChaveAcesso());
                cellChave.setCellStyle(textCellStyle);

                // Número do CT-e
                Cell cellNum = row.createCell(1);
                cellNum.setCellValue(cte.getNumeroCte());
                cellNum.setCellStyle(textCellStyle);

                // Tomador
                Cell cellTomador = row.createCell(2);
                cellTomador.setCellValue(cte.getTomadorNome());
                cellTomador.setCellStyle(textCellStyle);

                // Origem
                Cell cellOrigem = row.createCell(3);
                cellOrigem.setCellValue(cte.getPortoOrigem());
                cellOrigem.setCellStyle(textCellStyle);

                // Destino
                Cell cellDestino = row.createCell(4);
                cellDestino.setCellValue(cte.getPortoDestino());
                cellDestino.setCellStyle(textCellStyle);

                // Status
                Cell cellStatus = row.createCell(5);
                cellStatus.setCellValue(cte.getStatus() != null ? cte.getStatus().name() : "");
                cellStatus.setCellStyle(textCellStyle);

                // Responsável
                Cell cellResp = row.createCell(6);
                String respStr = "";
                if (cte.getAutoInfracao() != null && cte.getAutoInfracao().getResponsavel() != null) {
                    respStr = cte.getAutoInfracao().getResponsavel().name();
                }
                cellResp.setCellValue(respStr);
                cellResp.setCellStyle(textCellStyle);

                // Valor Potencial da Multa
                Cell cellPotMulta = row.createCell(7);
                BigDecimal potMulta = cte.getValorPotencialMulta();
                cellPotMulta.setCellValue(potMulta != null ? potMulta.doubleValue() : 0.0);
                cellPotMulta.setCellStyle(currencyCellStyle);

                // Valor Pago
                Cell cellPago = row.createCell(8);
                BigDecimal pago = BigDecimal.ZERO;
                if (cte.getAutoInfracao() != null && cte.getAutoInfracao().getPagamento() != null) {
                    BigDecimal vp = cte.getAutoInfracao().getPagamento().getValorPago();
                    if (vp != null) {
                        pago = vp;
                    }
                }
                cellPago.setCellValue(pago.doubleValue());
                cellPago.setCellStyle(currencyCellStyle);

                // Data do Pagamento
                Cell cellDataPag = row.createCell(9);
                if (cte.getAutoInfracao() != null && cte.getAutoInfracao().getPagamento() != null 
                        && cte.getAutoInfracao().getPagamento().getDataPagamento() != null) {
                    cellDataPag.setCellValue(cte.getAutoInfracao().getPagamento().getDataPagamento());
                    cellDataPag.setCellStyle(dateCellStyle);
                } else {
                    cellDataPag.setCellValue("");
                    cellDataPag.setCellStyle(textCellStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
