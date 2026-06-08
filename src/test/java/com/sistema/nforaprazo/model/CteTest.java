package com.sistema.nforaprazo.model;

import com.sistema.nforaprazo.model.enums.StatusCte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CteTest {

    @Test
    @DisplayName("Deve calcular o valor total das notas fiscais como zero quando nao houver notas")
    void deveRetornarZeroQuandoNaoHouverNotas() {
        Cte cte = new Cte();
        cte.setNotasFiscais(List.of());

        assertTrue(BigDecimal.ZERO.compareTo(cte.getValorTotalNotas()) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(cte.getValorPotencialMulta()) == 0);
    }

    @Test
    @DisplayName("Deve calcular corretamente o valor total das notas fiscais")
    void deveCalcularTotalDasNotas() {
        Cte cte = new Cte();
        
        NotaFiscal nf1 = new NotaFiscal();
        nf1.setValorNota(new BigDecimal("1500.00"));
        
        NotaFiscal nf2 = new NotaFiscal();
        nf2.setValorNota(new BigDecimal("2500.50"));

        cte.setNotasFiscais(List.of(nf1, nf2));

        assertEquals(new BigDecimal("4000.50"), cte.getValorTotalNotas());
    }

    @Test
    @DisplayName("Deve calcular o valor potencial da multa como exatamente 10% do total das notas")
    void deveCalcularValorPotencialMulta() {
        Cte cte = new Cte();
        
        NotaFiscal nf1 = new NotaFiscal();
        nf1.setValorNota(new BigDecimal("10000.00"));

        cte.setNotasFiscais(List.of(nf1));

        // 10% de 10000.00 = 1000.000
        BigDecimal multaEsperada = new BigDecimal("1000.000"); // dependendo de como e instanciado
        // getValorPotencialMulta usa multiply(new BigDecimal("0.10"))
        // 10000.00 * 0.10 = 1000.000
        assertTrue(multaEsperada.compareTo(cte.getValorPotencialMulta()) == 0);
    }

    @Test
    @DisplayName("Deve identificar portos monitorados de forma dinâmica com nomes mistos")
    void deveIdentificarPortoMonitorado() {
        List<String> portos = List.of("MANAUS", "MAO", "VILA DO CONDE", "PCM", "PECEM", "PECÉM");
        Cte cteManaus = Cte.builder().portoDestino("Manaus").build();
        Cte cteMao = Cte.builder().portoDestino("MAO").build();
        Cte cteVilaConde = Cte.builder().portoDestino(" Vila do Conde  ").build();
        Cte ctePcm = Cte.builder().portoDestino("PCM").build();
        Cte ctePecem = Cte.builder().portoDestino("Pecém").build();
        Cte ctePecemSemAcento = Cte.builder().portoDestino("pecem").build();
        
        Cte cteSantos = Cte.builder().portoDestino("Santos").build();
        Cte cteItapoa = Cte.builder().portoDestino("Itapoá").build();
        Cte cteNulo = Cte.builder().portoDestino(null).build();

        assertTrue(cteManaus.isPortoMonitorado(portos));
        assertTrue(cteMao.isPortoMonitorado(portos));
        assertTrue(cteVilaConde.isPortoMonitorado(portos));
        assertTrue(ctePcm.isPortoMonitorado(portos));
        assertTrue(ctePecem.isPortoMonitorado(portos));
        assertTrue(ctePecemSemAcento.isPortoMonitorado(portos));

        assertFalse(cteSantos.isPortoMonitorado(portos));
        assertFalse(cteItapoa.isPortoMonitorado(portos));
        assertFalse(cteNulo.isPortoMonitorado(portos));
    }

    @Test
    @DisplayName("Deve identificar portos monitorados de forma dinâmica")
    void deveIdentificarPortoMonitoradoDinamico() {
        List<String> portosAtivos = List.of("MANAUS", "PECEM");
        Cte cteManaus = Cte.builder().portoDestino("Porto de Manaus").build();
        Cte ctePecem = Cte.builder().portoDestino("pecem").build();
        Cte cteSantos = Cte.builder().portoDestino("Santos").build();

        assertTrue(cteManaus.isPortoMonitorado(portosAtivos));
        assertTrue(ctePecem.isPortoMonitorado(portosAtivos));
        assertFalse(cteSantos.isPortoMonitorado(portosAtivos));
    }
}
