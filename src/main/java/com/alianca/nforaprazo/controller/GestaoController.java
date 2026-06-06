package com.alianca.nforaprazo.controller;

import com.alianca.nforaprazo.dto.DashboardDataDto;
import com.alianca.nforaprazo.model.Cte;
import com.alianca.nforaprazo.service.DashboardService;
import com.alianca.nforaprazo.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GestaoController {

    private final DashboardService dashboardService;
    private final ExcelExportService excelExportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        log.info("Acessando painel de gestao /dashboard");
        DashboardDataDto dashboardData = dashboardService.obterDadosDashboard();
        model.addAttribute("dashboardData", dashboardData);
        return "gestao/dashboard";
    }

    @GetMapping("/relatorios")
    public String relatoriosRedirect() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard/exportar")
    public ResponseEntity<byte[]> exportarRelatorio(
            @RequestParam(required = false) String portoDestino,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        log.info("Exportando relatorio Excel com filtros: portoDestino={}, dataInicio={}, dataFim={}",
                portoDestino, dataInicio, dataFim);
        
        try {
            List<Cte> ctes = dashboardService.obterCtesParaRelatorio(portoDestino, dataInicio, dataFim);
            byte[] excelBytes = excelExportService.exportarCtesParaExcel(ctes);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_nforaprazo.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelBytes.length)
                    .body(excelBytes);
        } catch (IOException e) {
            log.error("Erro ao gerar relatorio Excel", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
