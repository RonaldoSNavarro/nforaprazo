package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.dto.*;
import com.sistema.nforaprazo.model.*;
import com.sistema.nforaprazo.model.enums.Responsavel;
import com.sistema.nforaprazo.model.enums.StatusCte;
import com.sistema.nforaprazo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DescargaServiceTest {

    @Mock private CteRepository cteRepository;
    @Mock private AutoInfracaoRepository autoInfracaoRepository;
    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private EncSemAutoRepository encSemAutoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private StorageService storageService;
    @Mock private EmailService emailService;

    @InjectMocks
    private DescargaService descargaService;

    private UUID cteId;
    private Cte cteTeste;
    private Usuario usuarioTeste;
    private MockMultipartFile mockPdf;

    @BeforeEach
    void setUp() {
        cteId = UUID.randomUUID();
        cteTeste = Cte.builder()
                .id(cteId)
                .status(StatusCte.AGUARDANDO_DESEMBARACO)
                .portoDestino("Manaus")
                .build();
                
        usuarioTeste = new Usuario();
        usuarioTeste.setEmail("user_descarga@test.com");

        mockPdf = new MockMultipartFile("arquivo", "documento.pdf", "application/pdf", "dummy pdf content".getBytes());
    }

    @Test
    @DisplayName("Deve confirmar desembaraço com sucesso alterando status para DESEMBARACADO")
    void deveConfirmarDesembaracoComSucesso() {
        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));
        when(cteRepository.save(any(Cte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cte resultado = descargaService.confirmarDesembaraco(cteId, usuarioTeste.getEmail());

        assertEquals(StatusCte.DESEMBARACADO, resultado.getStatus());
        verify(cteRepository, times(1)).save(cteTeste);
    }

    @Test
    @DisplayName("Deve falhar ao confirmar desembaraço se o status do CT-e não for AGUARDANDO_DESEMBARACO")
    void deveFalharConfirmarDesembaracoSeStatusInvalido() {
        cteTeste.setStatus(StatusCte.REGISTRADO);
        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));

        assertThrows(IllegalStateException.class, () -> 
            descargaService.confirmarDesembaraco(cteId, usuarioTeste.getEmail())
        );
        verify(cteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve registrar Auto de Infração e alterar status para AUTO_RECEBIDO")
    void deveRegistrarAutoInfracaoComSucesso() {
        cteTeste.setStatus(StatusCte.DESEMBARACADO);
        AutoInfracaoRequest request = new AutoInfracaoRequest();
        request.setCteId(cteId);
        request.setAutoInfracaoPdf(mockPdf);

        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));
        when(storageService.store(mockPdf)).thenReturn("auto.pdf");
        when(cteRepository.save(any(Cte.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(autoInfracaoRepository.save(any(AutoInfracao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AutoInfracao auto = descargaService.registrarAutoInfracao(request, usuarioTeste.getEmail());

        assertNotNull(auto);
        assertEquals("auto.pdf", auto.getArquivoPdfAuto());
        assertEquals(Responsavel.PENDENTE, auto.getResponsavel());
        assertEquals(StatusCte.AUTO_RECEBIDO, cteTeste.getStatus());
        verify(emailService, times(1)).enviarAlertaDocsFiscal(cteTeste);
    }

    @Test
    @DisplayName("Deve bloquear registro de DAR se a investigação do DOCS_FISCAL estiver PENDENTE")
    void deveBloquearDarSeInvestigacaoPendente() {
        cteTeste.setStatus(StatusCte.AGUARDANDO_PAGAMENTO);
        AutoInfracao auto = AutoInfracao.builder().responsavel(Responsavel.PENDENTE).build();
        cteTeste.setAutoInfracao(auto);

        DarRequest request = new DarRequest();
        request.setCteId(cteId);

        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            descargaService.registrarDar(request, usuarioTeste.getEmail())
        );

        assertTrue(ex.getMessage().contains("A investigação da equipe DOCS_FISCAL ainda não foi concluída."));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve registrar DAR com sucesso se a investigação estiver concluída")
    void deveRegistrarDarComSucessoSeNaoPendente() {
        cteTeste.setStatus(StatusCte.AGUARDANDO_PAGAMENTO);
        AutoInfracao auto = AutoInfracao.builder().responsavel(Responsavel.CLIENTE).build();
        cteTeste.setAutoInfracao(auto);

        DarRequest request = new DarRequest();
        request.setCteId(cteId);
        request.setDarPdf(mockPdf);

        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));
        when(usuarioRepository.findByEmail(usuarioTeste.getEmail())).thenReturn(Optional.of(usuarioTeste));
        when(storageService.store(mockPdf)).thenReturn("dar.pdf");
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pagamento pag = descargaService.registrarDar(request, usuarioTeste.getEmail());

        assertNotNull(pag);
        assertEquals("dar.pdf", pag.getPathDarPdf());
        verify(pagamentoRepository, times(1)).save(any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve bloquear Comprovante se não houver DAR registrado antes")
    void deveBloquearComprovanteSeNaoHouverDar() {
        cteTeste.setStatus(StatusCte.AGUARDANDO_PAGAMENTO);
        AutoInfracao auto = AutoInfracao.builder().responsavel(Responsavel.CLIENTE).pagamento(null).build();
        cteTeste.setAutoInfracao(auto);

        ComprovanteRequest request = new ComprovanteRequest();
        request.setCteId(cteId);

        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            descargaService.registrarComprovante(request, usuarioTeste.getEmail())
        );

        assertTrue(ex.getMessage().contains("Nenhum DAR foi registrado para este CT-e ainda."));
    }

    @Test
    @DisplayName("Deve registrar Comprovante e transicionar para status PAGO")
    void deveRegistrarComprovanteComSucesso() {
        cteTeste.setStatus(StatusCte.AGUARDANDO_PAGAMENTO);
        Pagamento pagamentoExistente = Pagamento.builder().pathDarPdf("dar.pdf").build();
        AutoInfracao auto = AutoInfracao.builder().responsavel(Responsavel.CLIENTE).pagamento(pagamentoExistente).build();
        cteTeste.setAutoInfracao(auto);

        ComprovanteRequest request = new ComprovanteRequest();
        request.setCteId(cteId);
        request.setComprovantePdf(mockPdf);
        request.setValorPago(new BigDecimal("950.00"));
        request.setDataPagamento(LocalDate.now());

        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));
        when(storageService.store(mockPdf)).thenReturn("comprovante.pdf");
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pagamento pag = descargaService.registrarComprovante(request, usuarioTeste.getEmail());

        assertNotNull(pag);
        assertEquals(new BigDecimal("950.00"), pag.getValorPago());
        assertEquals("comprovante.pdf", pag.getPathComprovantePdf());
        assertEquals(StatusCte.PAGO, cteTeste.getStatus());
        verify(emailService, times(1)).enviarAlertaFaturamento(cteTeste);
    }

    @Test
    @DisplayName("Deve encerrar processo sem auto, calculando 10% das NFs e salvando justificativa")
    void deveEncerrarSemAutoCalculandoExposicao() {
        cteTeste.setStatus(StatusCte.DESEMBARACADO);
        
        // Criar nota fiscal vinculada
        NotaFiscal nf = new NotaFiscal();
        nf.setValorNota(new BigDecimal("50000.00"));
        cteTeste.setNotasFiscais(List.of(nf));

        EncSemAutoRequest request = new EncSemAutoRequest();
        request.setCteId(cteId);
        request.setJustificativa("Liberação concedida verbalmente pelo fiscal da SEFAZ");

        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));
        when(usuarioRepository.findByEmail(usuarioTeste.getEmail())).thenReturn(Optional.of(usuarioTeste));
        when(encSemAutoRepository.save(any(EncSemAuto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EncSemAuto resultado = descargaService.encerrarSemAuto(request, usuarioTeste.getEmail());

        assertNotNull(resultado);
        // Multa potencial de 10% = 50000 * 0.10 = 5000.00
        assertTrue(new BigDecimal("5000.000").compareTo(resultado.getValorPotencialMulta()) == 0);
        assertEquals("Liberação concedida verbalmente pelo fiscal da SEFAZ", resultado.getJustificativa());
        assertEquals(StatusCte.ENCERRADO_SEM_AUTO, cteTeste.getStatus());
        verify(emailService, times(1)).enviarAlertaEncerramentoSemAuto(cteTeste);
    }
}
