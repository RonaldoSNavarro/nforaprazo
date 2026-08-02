package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.model.LogAlerta;
import com.sistema.nforaprazo.model.Usuario;
import com.sistema.nforaprazo.model.enums.PerfilUsuario;
import com.sistema.nforaprazo.model.enums.StatusEnvio;
import com.sistema.nforaprazo.model.enums.TipoAlerta;
import com.sistema.nforaprazo.repository.LogAlertaRepository;
import com.sistema.nforaprazo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private LogAlertaRepository logAlertaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EmailService emailService;

    private Cte cteMock;
    private Usuario usuarioDescarga;

    @BeforeEach
    void setUp() {
        cteMock = Cte.builder()
                .numeroCte("1001")
                .chaveAcesso("35260600000000000000000000000000000000000001")
                .tomadorNome("Tomador Teste Ltda")
                .portoDestino("MANAUS")
                .valorCarga(new BigDecimal("15000.00"))
                .numeroBooking("BK-999")
                .build();

        usuarioDescarga = Usuario.builder()
                .nome("Operador Descarga")
                .email("descarga@sistema.local")
                .perfil(PerfilUsuario.DESCARGA)
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve enviar e-mail de alerta de descarga com remetente válido e registrar status ENVIADO")
    void deveEnviarAlertaDescargaComSucesso() {
        ReflectionTestUtils.setField(emailService, "emailRemetente", "sistema@empresa.com");
        when(usuarioRepository.findByPerfilAndAtivoTrue(PerfilUsuario.DESCARGA))
                .thenReturn(List.of(usuarioDescarga));

        emailService.enviarAlertaDescarga(cteMock);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertEquals("sistema@empresa.com", messageCaptor.getValue().getFrom());
        assertEquals("descarga@sistema.local", messageCaptor.getValue().getTo()[0]);

        ArgumentCaptor<LogAlerta> logCaptor = ArgumentCaptor.forClass(LogAlerta.class);
        verify(logAlertaRepository).save(logCaptor.capture());
        assertEquals(StatusEnvio.ENVIADO, logCaptor.getValue().getStatusEnvio());
    }

    @Test
    @DisplayName("Deve usar noreply@sistema.local como fallback quando emailRemetente estiver vazio")
    void deveUsarFallbackRemetenteQuandoVazio() {
        ReflectionTestUtils.setField(emailService, "emailRemetente", "");
        when(usuarioRepository.findByPerfilAndAtivoTrue(PerfilUsuario.DESCARGA))
                .thenReturn(List.of(usuarioDescarga));

        emailService.enviarAlertaDescarga(cteMock);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertEquals("noreply@sistema.local", messageCaptor.getValue().getFrom());
    }

    @Test
    @DisplayName("Deve capturar exceção no envio e registrar status ERRO com mensagem de falha")
    void deveRegistrarStatusErroQuandoEnvioFalha() {
        ReflectionTestUtils.setField(emailService, "emailRemetente", "noreply@sistema.local");
        when(usuarioRepository.findByPerfilAndAtivoTrue(PerfilUsuario.DESCARGA))
                .thenReturn(List.of(usuarioDescarga));
        doThrow(new MailSendException("Falha na conexão SMTP"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        emailService.enviarAlertaDescarga(cteMock);

        ArgumentCaptor<LogAlerta> logCaptor = ArgumentCaptor.forClass(LogAlerta.class);
        verify(logAlertaRepository).save(logCaptor.capture());
        assertEquals(StatusEnvio.ERRO, logCaptor.getValue().getStatusEnvio());
        assertEquals("Falha na conexão SMTP", logCaptor.getValue().getMensagemErro());
    }

    @Test
    @DisplayName("Deve enviar senha provisória com sucesso")
    void deveEnviarSenhaProvisoriaComSucesso() {
        ReflectionTestUtils.setField(emailService, "emailRemetente", "noreply@sistema.local");

        emailService.enviarSenhaProvisoria("novo.usuario@empresa.com", "Novo Usuario", "Temp#123", "http://localhost:8080/login");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertEquals("novo.usuario@empresa.com", messageCaptor.getValue().getTo()[0]);
        assertTrue(messageCaptor.getValue().getText().contains("Temp#123"));

        ArgumentCaptor<LogAlerta> logCaptor = ArgumentCaptor.forClass(LogAlerta.class);
        verify(logAlertaRepository).save(logCaptor.capture());
        assertEquals(StatusEnvio.ENVIADO, logCaptor.getValue().getStatusEnvio());
        assertEquals(TipoAlerta.SENHA_PROVISORIA, logCaptor.getValue().getTipoAlerta());
    }
}
