package com.sistema.nforaprazo.integration;

import com.sistema.nforaprazo.model.*;
import com.sistema.nforaprazo.model.enums.PerfilUsuario;
import com.sistema.nforaprazo.model.enums.Responsavel;
import com.sistema.nforaprazo.model.enums.StatusCte;
import com.sistema.nforaprazo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:nforaprazotestdb;MODE=PostgreSQL;DATABASE_TO_UPPER=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
class EndToEndIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean(name = "taskExecutor")
        public Executor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CteRepository cteRepository;

    @Autowired
    private LogAtividadeRepository logAtividadeRepository;

    @Autowired
    private LogAlertaRepository logAlertaRepository;

    @Autowired
    private PortoMonitoradoRepository portoMonitoradoRepository;

    private static final String EMAIL_FATURAMENTO = "user_faturamento@test.com";
    private static final String EMAIL_DESCARGA = "user_descarga@test.com";
    private static final String EMAIL_DOCS_FISCAL = "user_docs_fiscal@test.com";

    // Conteúdo dummy que passa na validação de Magic Number de PDF (%PDF-)
    private static final byte[] PDF_CONTENT = "%PDF-1.5 dummy content".getBytes();

    @BeforeEach
    void setUp() {
        cteRepository.deleteAll();
        usuarioRepository.deleteAll();
        logAtividadeRepository.deleteAll();
        logAlertaRepository.deleteAll();
        portoMonitoradoRepository.deleteAll();

        portoMonitoradoRepository.save(PortoMonitorado.builder().nome("MANAUS").ativo(true).criadoPor("test").build());
        portoMonitoradoRepository.save(PortoMonitorado.builder().nome("PECÉM").ativo(true).criadoPor("test").build());

        // Cadastra os usuários de teste com seus perfis no banco
        usuarioRepository.save(Usuario.builder()
                .nome("Faturador 1")
                .email(EMAIL_FATURAMENTO)
                .senha("senha123")
                .perfil(PerfilUsuario.FATURAMENTO)
                .ativo(true)
                .alterarSenha(false)
                .build());

        usuarioRepository.save(Usuario.builder()
                .nome("Descarregador 1")
                .email(EMAIL_DESCARGA)
                .senha("senha123")
                .perfil(PerfilUsuario.DESCARGA)
                .ativo(true)
                .alterarSenha(false)
                .build());

        usuarioRepository.save(Usuario.builder()
                .nome("Docs Fiscal 1")
                .email(EMAIL_DOCS_FISCAL)
                .senha("senha123")
                .perfil(PerfilUsuario.DOCS_FISCAL)
                .ativo(true)
                .alterarSenha(false)
                .build());
    }

    @Test
    @DisplayName("Fluxo A End-to-End: Upload CT-e -> Confirmar Desembaraco -> Auto Infracao -> Investigacao (CLIENTE) -> DAR/Comprovante -> Nota Debito -> Encerrado")
    void testFluxoA_Completo() throws Exception {
        // -------------------------------------------------------------
        // Passo 1: Upload de CT-e para Porto Monitorado (Faturamento)
        // -------------------------------------------------------------
        MockMultipartFile arquivoPdf = new MockMultipartFile(
                "arquivoCte", "cte1.pdf", "application/pdf", PDF_CONTENT);
        MockMultipartFile autorizacaoPdf = new MockMultipartFile(
                "arquivoAutorizacaoCusto", "autorizacao.pdf", "application/pdf", PDF_CONTENT);

        mockMvc.perform(multipart("/cte/upload")
                        .file(arquivoPdf)
                        .file(autorizacaoPdf)
                        .param("numeroCte", "9901")
                        .param("tomadorNome", "Tomador Teste")
                        .param("tomadorCnpj", "00.000.000/0001-00")
                        .param("navio", "Navio Teste")
                        .param("viagem", "001W")
                        .param("portoOrigem", "Santos")
                        .param("portoDestino", "Manaus") // Porto monitorado
                        .param("valorCarga", "150000.00")
                        .param("numeroBooking", "B12345")
                        .with(user(EMAIL_FATURAMENTO).roles("FATURAMENTO"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cte/lista"));

        // Validar no banco que foi salvo como PENDENTE
        Cte cte = cteRepository.findAll().stream()
                .filter(c -> c.getNumeroCte().equals("9901"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("CT-e não encontrado"));

        assertEquals(StatusCte.PENDENTE, cte.getStatus());
        java.util.List<String> portosAtivos = portoMonitoradoRepository.findByAtivoTrue().stream()
                .map(PortoMonitorado::getNome)
                .toList();
        assertTrue(cte.isPortoMonitorado(portosAtivos));

        // Verificar que disparou e-mail de alerta para descarga
        long alertasDescarga = logAlertaRepository.findAll().stream()
                .filter(a -> a.getDestinatario().contains("descarga"))
                .count();
        assertTrue(alertasDescarga > 0, "Alerta de e-mail para Descarga deve ter sido registrado");

        UUID cteId = cte.getId();

        // -------------------------------------------------------------
        // Passo 2: Confirmar Desembaraco (Descarga)
        // -------------------------------------------------------------
        mockMvc.perform(post("/descarga/confirmar-desembaraco")
                        .param("cteId", cteId.toString())
                        .with(user(EMAIL_DESCARGA).roles("DESCARGA"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/descarga/pendentes"));

        cte = cteRepository.findById(cteId).orElseThrow();
        assertEquals(StatusCte.DESEMBARACADO, cte.getStatus());

        // -------------------------------------------------------------
        // Passo 3: Registrar Auto de Infração (Descarga)
        // -------------------------------------------------------------
        MockMultipartFile autoPdf = new MockMultipartFile(
                "autoInfracaoPdf", "auto1.pdf", "application/pdf", PDF_CONTENT);

        mockMvc.perform(multipart("/descarga/auto-infracao")
                        .file(autoPdf)
                        .param("cteId", cteId.toString())
                        .with(user(EMAIL_DESCARGA).roles("DESCARGA"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/descarga/pendentes"));

        cte = cteRepository.findById(cteId).orElseThrow();
        assertEquals(StatusCte.AUTO_RECEBIDO, cte.getStatus());
        assertNotNull(cte.getAutoInfracao());

        // Verificar que disparou e-mail de alerta para Docs Fiscal
        long alertasDocs = logAlertaRepository.findAll().stream()
                .filter(a -> a.getDestinatario().contains("docs_fiscal") || a.getDestinatario().contains("docs-fiscal"))
                .count();
        assertTrue(alertasDocs > 0, "Alerta de e-mail para Docs Fiscal deve ter sido registrado");

        // -------------------------------------------------------------
        // Passo 4: Iniciar Investigação (Docs Fiscal)
        // -------------------------------------------------------------
        mockMvc.perform(post("/docs-fiscal/iniciar-investigacao")
                        .param("cteId", cteId.toString())
                        .with(user(EMAIL_DOCS_FISCAL).roles("DOCS_FISCAL"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/docs-fiscal/pendentes"));

        cte = cteRepository.findById(cteId).orElseThrow();
        assertEquals(StatusCte.EM_INVESTIGACAO, cte.getStatus());

        // -------------------------------------------------------------
        // Passo 5: Concluir Investigação (Docs Fiscal) -> CLIENTE é responsável
        // -------------------------------------------------------------
        MockMultipartFile ticketPdf = new MockMultipartFile(
                "arquivoTicket", "ticket.pdf", "application/pdf", PDF_CONTENT);

        mockMvc.perform(multipart("/docs-fiscal/concluir-investigacao")
                        .file(ticketPdf)
                        .param("cteId", cteId.toString())
                        .param("responsavel", Responsavel.CLIENTE.name())
                        .param("motivoErro", "Inconsistencia de prazos operacionais pelo cliente")
                        .param("numeroTicket", "TKT-1002")
                        .with(user(EMAIL_DOCS_FISCAL).roles("DOCS_FISCAL"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/docs-fiscal/pendentes"));

        cte = cteRepository.findById(cteId).orElseThrow();
        assertEquals(StatusCte.AGUARDANDO_PAGAMENTO, cte.getStatus());
        assertEquals(Responsavel.CLIENTE, cte.getAutoInfracao().getResponsavel());

        // -------------------------------------------------------------
        // Passo 6: Registrar DAR (Descarga)
        // -------------------------------------------------------------
        MockMultipartFile darPdf = new MockMultipartFile(
                "darPdf", "dar.pdf", "application/pdf", PDF_CONTENT);

        mockMvc.perform(multipart("/descarga/dar")
                        .file(darPdf)
                        .param("cteId", cteId.toString())
                        .with(user(EMAIL_DESCARGA).roles("DESCARGA"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/descarga/pendentes"));

        // -------------------------------------------------------------
        // Passo 7: Registrar Comprovante de Pagamento & Capa (Descarga)
        // -------------------------------------------------------------
        MockMultipartFile comprovantePdf = new MockMultipartFile(
                "comprovantePdf", "comprovante.pdf", "application/pdf", PDF_CONTENT);
        MockMultipartFile capaPdf = new MockMultipartFile(
                "capaPdf", "capa.pdf", "application/pdf", PDF_CONTENT);

        mockMvc.perform(multipart("/descarga/comprovante")
                        .file(comprovantePdf)
                        .file(capaPdf)
                        .param("cteId", cteId.toString())
                        .param("valorPago", "15000.00")
                        .param("dataPagamento", "2026-06-05")
                        .with(user(EMAIL_DESCARGA).roles("DESCARGA"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/descarga/pendentes"));

        cte = cteRepository.findById(cteId).orElseThrow();
        assertEquals(StatusCte.PAGO, cte.getStatus());

        // -------------------------------------------------------------
        // Passo 8: Emitir Nota de Débito (Faturamento)
        // -------------------------------------------------------------
        MockMultipartFile ndPdf = new MockMultipartFile(
                "arquivoPdf", "nd.pdf", "application/pdf", PDF_CONTENT);

        mockMvc.perform(multipart("/faturamento/nota-debito")
                        .file(ndPdf)
                        .param("cteId", cteId.toString())
                        .param("numeroNotaDebito", "ND-1002")
                        .param("dataEmissao", "2026-06-06")
                        .with(user(EMAIL_FATURAMENTO).roles("FATURAMENTO"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/faturamento/nota-debito"));

        cte = cteRepository.findById(cteId).orElseThrow();
        assertEquals(StatusCte.ENCERRADO_COM_AUTO, cte.getStatus());
        assertNotNull(cte.getNotaDebito());
        assertEquals("ND-1002", cte.getNotaDebito().getNumeroNotaDebito());

        // Verificar logs de atividade auditados via Spring AOP
        long logsGravados = logAtividadeRepository.findAll().stream()
                .filter(log -> log.getAcao().equals("Upload de CT-e") || log.getAcao().equals("Emissao de Nota de Debito"))
                .count();
        assertTrue(logsGravados >= 2, "Devem haver logs de auditoria gravados via AOP");
    }

    @Test
    @DisplayName("Fluxo B End-to-End: Upload CT-e -> Confirmar Desembaraco -> Encerrar Sem Auto de Infracao")
    void testFluxoB_Completo() throws Exception {
        // -------------------------------------------------------------
        // Passo 1: Upload de CT-e (Faturamento)
        // -------------------------------------------------------------
        MockMultipartFile arquivoPdf = new MockMultipartFile(
                "arquivoCte", "cte2.pdf", "application/pdf", PDF_CONTENT);
        MockMultipartFile autorizacaoPdf = new MockMultipartFile(
                "arquivoAutorizacaoCusto", "autorizacao2.pdf", "application/pdf", PDF_CONTENT);

        mockMvc.perform(multipart("/cte/upload")
                        .file(arquivoPdf)
                        .file(autorizacaoPdf)
                        .param("numeroCte", "9902")
                        .param("tomadorNome", "Tomador Teste 2")
                        .param("tomadorCnpj", "00.000.000/0001-00")
                        .param("navio", "Navio Teste 2")
                        .param("viagem", "002W")
                        .param("portoOrigem", "Santos")
                        .param("portoDestino", "Pecém") // Porto monitorado
                        .param("valorCarga", "100000.00")
                        .param("numeroBooking", "B54321")
                        .with(user(EMAIL_FATURAMENTO).roles("FATURAMENTO"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Cte cte = cteRepository.findAll().stream()
                .filter(c -> c.getNumeroCte().equals("9902"))
                .findFirst()
                .orElseThrow();

        UUID cteId = cte.getId();

        // -------------------------------------------------------------
        // Passo 2: Confirmar Desembaraco (Descarga)
        // -------------------------------------------------------------
        mockMvc.perform(post("/descarga/confirmar-desembaraco")
                        .param("cteId", cteId.toString())
                        .with(user(EMAIL_DESCARGA).roles("DESCARGA"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // -------------------------------------------------------------
        // Passo 3: Encerrar Sem Auto de Infração (Descarga)
        // -------------------------------------------------------------
        mockMvc.perform(post("/descarga/encerrar-sem-auto")
                        .param("cteId", cteId.toString())
                        .param("justificativa", "Mercadoria desembaraçada sem autuação fiscal dentro do prazo limite.")
                        .with(user(EMAIL_DESCARGA).roles("DESCARGA"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/descarga/pendentes"));

        cte = cteRepository.findById(cteId).orElseThrow();
        assertEquals(StatusCte.ENCERRADO_SEM_AUTO, cte.getStatus());
        assertNotNull(cte.getEncSemAuto());
        assertEquals("Mercadoria desembaraçada sem autuação fiscal dentro do prazo limite.", cte.getEncSemAuto().getJustificativa());

        // Verificar log de atividade
        boolean logEncontrado = logAtividadeRepository.findAll().stream()
                .anyMatch(l -> l.getAcao().equals("Encerramento Sem Auto de Infracao"));
        assertTrue(logEncontrado, "Deve haver um log registrado para o encerramento sem auto");
    }
}
