package com.sistema.nforaprazo.controller;

import com.sistema.nforaprazo.dto.DashboardDataDto;
import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.service.DashboardService;
import com.sistema.nforaprazo.service.ExcelExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestaoControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private ExcelExportService excelExportService;

    @Mock
    private Model model;

    @InjectMocks
    private GestaoController gestaoController;

    @Test
    @DisplayName("Deve exibir dashboard carregando os dados corretos")
    void deveExibirDashboard() {
        DashboardDataDto mockData = DashboardDataDto.builder().build();
        when(dashboardService.obterDadosDashboard()).thenReturn(mockData);

        String view = gestaoController.dashboard(model);

        assertEquals("gestao/dashboard", view);
        verify(dashboardService, times(1)).obterDadosDashboard();
        verify(model, times(1)).addAttribute("dashboardData", mockData);
    }

    @Test
    @DisplayName("Deve exibir relatorios carregando os CT-es filtrados e parametros no modelo")
    void deveExibirRelatoriosComFiltros() {
        String porto = "Manaus";
        LocalDate inicio = LocalDate.of(2026, 6, 1);
        LocalDate fim = LocalDate.of(2026, 6, 7);
        List<Cte> mockCtes = List.of(new Cte());

        when(dashboardService.obterCtesParaRelatorio(porto, inicio, fim)).thenReturn(mockCtes);

        String view = gestaoController.relatorios(porto, inicio, fim, model);

        assertEquals("gestao/relatorios", view);
        verify(dashboardService, times(1)).obterCtesParaRelatorio(porto, inicio, fim);
        verify(model, times(1)).addAttribute("ctes", mockCtes);
        verify(model, times(1)).addAttribute("portoDestino", porto);
        verify(model, times(1)).addAttribute("dataInicio", inicio);
        verify(model, times(1)).addAttribute("dataFim", fim);
    }

    @Test
    @DisplayName("Deve exportar relatorio Excel com sucesso")
    void deveExportarExcelComSucesso() throws IOException {
        String porto = "Pecem";
        LocalDate inicio = LocalDate.of(2026, 6, 1);
        LocalDate fim = LocalDate.of(2026, 6, 7);
        List<Cte> mockCtes = List.of(new Cte());
        byte[] mockBytes = new byte[]{1, 2, 3};

        when(dashboardService.obterCtesParaRelatorio(porto, inicio, fim)).thenReturn(mockCtes);
        when(excelExportService.exportarCtesParaExcel(mockCtes)).thenReturn(mockBytes);

        ResponseEntity<byte[]> response = gestaoController.exportarRelatorio(porto, inicio, fim);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBytes, response.getBody());
        verify(dashboardService, times(1)).obterCtesParaRelatorio(porto, inicio, fim);
        verify(excelExportService, times(1)).exportarCtesParaExcel(mockCtes);
    }

    @Test
    @DisplayName("Deve retornar erro interno do servidor se falhar ao exportar Excel")
    void deveRetornarErroServidorAoExportarFalhar() throws IOException {
        String porto = "Pecem";
        LocalDate inicio = LocalDate.of(2026, 6, 1);
        LocalDate fim = LocalDate.of(2026, 6, 7);
        List<Cte> mockCtes = List.of(new Cte());

        when(dashboardService.obterCtesParaRelatorio(porto, inicio, fim)).thenReturn(mockCtes);
        when(excelExportService.exportarCtesParaExcel(mockCtes)).thenThrow(new IOException("Disk error"));

        ResponseEntity<byte[]> response = gestaoController.exportarRelatorio(porto, inicio, fim);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
