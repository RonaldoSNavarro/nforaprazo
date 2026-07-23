package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.dto.CteUploadRequest;
import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.model.Usuario;
import com.sistema.nforaprazo.model.enums.StatusCte;
import com.sistema.nforaprazo.repository.CteRepository;
import com.sistema.nforaprazo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.Optional;

import com.sistema.nforaprazo.repository.PortoMonitoradoRepository;
import com.sistema.nforaprazo.model.PortoMonitorado;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CteServiceTest {

    @Mock
    private StorageService storageService;

    @Mock
    private PdfExtractionService pdfExtractionService;

    @Mock
    private CteRepository cteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PortoMonitoradoRepository portoMonitoradoRepository;

    @InjectMocks
    private CteService cteService;

    private Usuario usuarioTeste;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        usuarioTeste = new Usuario();
        usuarioTeste.setEmail("test@sistema.local");
        usuarioTeste.setNome("Usuario Teste");

        mockFile = new MockMultipartFile("arquivoCte", "cte.pdf", "application/pdf", "dummy pdf content".getBytes());
        ReflectionTestUtils.setField(cteService, "uploadDir", "uploads");
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar processar upload com usuario inexistente")
    void deveLancarExcecaoQuandoUsuarioInexistente() {
        CteUploadRequest request = new CteUploadRequest();
        request.setArquivoCte(mockFile);

        when(usuarioRepository.findByEmail("invalido@email.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            cteService.processarUploadCte(request, "invalido@email.com")
        );

        verify(cteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao quando a chave de acesso extraida do PDF ja existir no sistema")
    void deveLancarExcecaoQuandoChaveAcessoDuplicada() {
        CteUploadRequest request = new CteUploadRequest();
        request.setArquivoCte(mockFile);

        when(usuarioRepository.findByEmail(usuarioTeste.getEmail())).thenReturn(Optional.of(usuarioTeste));
        when(storageService.store(mockFile)).thenReturn("stored-uuid.pdf");
        when(pdfExtractionService.extrairChaveAcesso(any(File.class))).thenReturn("12345678901234567890123456789012345678901234");
        when(cteRepository.existsByChaveAcesso("12345678901234567890123456789012345678901234")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            cteService.processarUploadCte(request, usuarioTeste.getEmail())
        );

        assertTrue(exception.getMessage().contains("Já existe um CT-e registrado com a chave de acesso"));
        verify(cteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve salvar como AGUARDANDO_DESEMBARACO e enviar e-mail se porto de destino for monitorado")
    void deveSalvarComoAguardandoDesembaracoSePortoMonitorado() {
        CteUploadRequest request = new CteUploadRequest();
        request.setArquivoCte(mockFile);
        request.setPortoDestino("Manaus");
        request.setNumeroCte("1001");
        request.setTomadorNome("Tomador Ltda");

        when(usuarioRepository.findByEmail(usuarioTeste.getEmail())).thenReturn(Optional.of(usuarioTeste));
        when(storageService.store(mockFile)).thenReturn("stored-uuid.pdf");
        when(pdfExtractionService.extrairChaveAcesso(any(File.class))).thenReturn("35260600000000000000000000000000000000000001");
        when(cteRepository.existsByChaveAcesso(any())).thenReturn(false);
        
        PortoMonitorado pm = PortoMonitorado.builder().nome("MANAUS").ativo(true).build();
        when(portoMonitoradoRepository.findByAtivoTrue()).thenReturn(java.util.List.of(pm));

        // Simulando comportamento de salvar
        when(cteRepository.save(any(Cte.class))).thenAnswer(invocation -> {
            Cte c = invocation.getArgument(0);
            if (c.getId() == null) {
                // Mockando o primeiro save que retorna a entidade com ID
                c.setId(java.util.UUID.randomUUID());
            }
            return c;
        });

        Cte resultado = cteService.processarUploadCte(request, usuarioTeste.getEmail());

        assertNotNull(resultado.getId());
        assertEquals(StatusCte.PENDENTE, resultado.getStatus());
        verify(emailService, times(1)).enviarAlertaDescarga(resultado);
        verify(cteRepository, times(1)).save(any(Cte.class));
    }

    @Test
    @DisplayName("Deve salvar como PENDENTE e nao enviar e-mail se porto de destino nao for monitorado")
    void deveSalvarComoRegistradoSePortoNaoMonitorado() {
        CteUploadRequest request = new CteUploadRequest();
        request.setArquivoCte(mockFile);
        request.setPortoDestino("Santos");
        request.setNumeroCte("1002");
        request.setTomadorNome("Outro Tomador");

        when(usuarioRepository.findByEmail(usuarioTeste.getEmail())).thenReturn(Optional.of(usuarioTeste));
        when(storageService.store(mockFile)).thenReturn("stored-uuid2.pdf");
        when(pdfExtractionService.extrairChaveAcesso(any(File.class))).thenReturn("35260600000000000000000000000000000000000002");
        when(cteRepository.existsByChaveAcesso(any())).thenReturn(false);
        
        when(portoMonitoradoRepository.findByAtivoTrue()).thenReturn(java.util.List.of());

        when(cteRepository.save(any(Cte.class))).thenAnswer(invocation -> {
            Cte c = invocation.getArgument(0);
            c.setId(java.util.UUID.randomUUID());
            return c;
        });

        Cte resultado = cteService.processarUploadCte(request, usuarioTeste.getEmail());

        assertNotNull(resultado.getId());
        assertEquals(StatusCte.PENDENTE, resultado.getStatus());
        verify(emailService, never()).enviarAlertaDescarga(any());
        verify(cteRepository, times(1)).save(any(Cte.class));
    }
}
