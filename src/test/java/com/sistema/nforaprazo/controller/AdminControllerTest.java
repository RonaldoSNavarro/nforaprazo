package com.sistema.nforaprazo.controller;

import com.sistema.nforaprazo.model.Usuario;
import com.sistema.nforaprazo.model.enums.PerfilUsuario;
import com.sistema.nforaprazo.repository.LogAlertaRepository;
import com.sistema.nforaprazo.repository.UsuarioRepository;
import com.sistema.nforaprazo.service.EmailService;
import com.sistema.nforaprazo.service.LogService;
import com.sistema.nforaprazo.service.PortoMonitoradoService;
import com.sistema.nforaprazo.dto.PortoMonitoradoRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import java.util.List;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private LogAlertaRepository logAlertaRepository;
    @Mock private LogService logService;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private HttpServletRequest request;
    @Mock private RedirectAttributes redirectAttributes;
    @Mock private PortoMonitoradoService portoMonitoradoService;

    @InjectMocks
    private AdminController adminController;

    private Usuario usuarioTeste;

    @BeforeEach
    void setUp() {
        usuarioTeste = Usuario.builder()
                .email("roberto@sistema.local")
                .nome("Roberto")
                .senha("encoded-pass")
                .perfil(PerfilUsuario.FATURAMENTO)
                .alterarSenha(true)
                .build();
    }

    @Test
    @DisplayName("Deve falhar no cadastro se o e-mail do usuario ja existir")
    void deveFalharSeEmailJaExistir() {
        when(usuarioRepository.existsByEmail("roberto@sistema.local")).thenReturn(true);

        String view = adminController.cadastrarUsuario("Roberto", "roberto@sistema.local", PerfilUsuario.FATURAMENTO, request, redirectAttributes);

        assertEquals("redirect:/admin/usuarios", view);
        verify(redirectAttributes).addFlashAttribute("error", "Já existe um usuário cadastrado com este e-mail.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cadastrar usuario com sucesso, gerando senha provisoria e disparando e-mail")
    void deveCadastrarUsuarioComSucesso() {
        when(usuarioRepository.existsByEmail("roberto@sistema.local")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-provisoria");
        
        // Mocking HttpServletRequest
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getContextPath()).thenReturn("");

        String view = adminController.cadastrarUsuario("Roberto", "roberto@sistema.local", PerfilUsuario.FATURAMENTO, request, redirectAttributes);

        assertEquals("redirect:/admin/usuarios", view);
        verify(usuarioRepository, times(1)).save(argThat(u -> 
            u.getNome().equals("Roberto") &&
            u.getEmail().equals("roberto@sistema.local") &&
            u.getSenha().equals("hashed-provisoria") &&
            u.getAlterarSenha() // Deve ser true
        ));
        
        verify(emailService, times(1)).enviarSenhaProvisoria(
                eq("roberto@sistema.local"), 
                eq("Roberto"), 
                anyString(), // Senha provisoria aleatoria
                eq("http://localhost:8080/login")
        );
        verify(redirectAttributes).addFlashAttribute("success", "Usuário cadastrado com sucesso. E-mail de convite enviado!");
    }

    @Test
    @DisplayName("Deve falhar na alteracao de senha se a nova senha for menor que 6 caracteres")
    void deveFalharSeSenhaForCurta() {
        Principal principal = () -> "roberto@sistema.local";

        String view = adminController.alterarSenha("123", "123", principal, redirectAttributes);

        assertEquals("redirect:/alterar-senha", view);
        verify(redirectAttributes).addFlashAttribute("error", "A senha deve conter no mínimo 6 caracteres.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar na alteracao de senha se as senhas nao coincidirem")
    void deveFalharSeSenhasDiferentes() {
        Principal principal = () -> "roberto@sistema.local";

        String view = adminController.alterarSenha("senha123", "senha321", principal, redirectAttributes);

        assertEquals("redirect:/alterar-senha", view);
        verify(redirectAttributes).addFlashAttribute("error", "As senhas não coincidem.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve alterar senha com sucesso, atualizando flag no banco e gravando log")
    void deveAlterarSenhaComSucesso() {
        Principal principal = () -> "roberto@sistema.local";
        when(usuarioRepository.findByEmail("roberto@sistema.local")).thenReturn(Optional.of(usuarioTeste));
        when(passwordEncoder.encode("novasenha123")).thenReturn("hashed-nova");
        when(logService.obterIpCliente()).thenReturn("127.0.0.1");

        String view = adminController.alterarSenha("novasenha123", "novasenha123", principal, redirectAttributes);

        assertEquals("redirect:/home", view);
        assertFalse(usuarioTeste.getAlterarSenha());
        assertEquals("hashed-nova", usuarioTeste.getSenha());
        verify(usuarioRepository, times(1)).save(usuarioTeste);
        verify(logService, times(1)).registrarLog(
                eq("roberto@sistema.local"), 
                eq("Alteracao de Senha Provisoria"), 
                anyString(), 
                eq("127.0.0.1")
        );
        verify(redirectAttributes).addFlashAttribute("success", "Senha alterada com sucesso. Acesso liberado!");
    }

    @Test
    @DisplayName("Deve exibir a tela de gerenciamento de portos")
    void deveExibirTelaGerenciamentoPortos() {
        Model model = mock(Model.class);
        when(portoMonitoradoService.listarTodos()).thenReturn(List.of());

        String view = adminController.gerenciarPortos(model);

        assertEquals("admin/portos", view);
        verify(model).addAttribute(eq("portos"), any());
        verify(model).addAttribute(eq("portoRequest"), any(PortoMonitoradoRequest.class));
    }

    @Test
    @DisplayName("Deve cadastrar um porto monitorado com sucesso")
    void deveCadastrarPortoComSucesso() {
        PortoMonitoradoRequest req = new PortoMonitoradoRequest();
        req.setNome("Manaus");
        req.setAtivo(true);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        Principal principal = () -> "admin@email.com";

        String view = adminController.cadastrarPorto(req, bindingResult, principal, redirectAttributes);

        assertEquals("redirect:/admin/portos", view);
        verify(portoMonitoradoService).cadastrarPorto(req, "admin@email.com");
        verify(redirectAttributes).addFlashAttribute("success", "Porto monitorado cadastrado com sucesso.");
    }

    @Test
    @DisplayName("Deve excluir um porto monitorado com sucesso")
    void deveExcluirPortoComSucesso() {
        UUID id = UUID.randomUUID();

        String view = adminController.excluirPorto(id, redirectAttributes);

        assertEquals("redirect:/admin/portos", view);
        verify(portoMonitoradoService).excluirPorto(id);
        verify(redirectAttributes).addFlashAttribute("success", "Porto monitorado removido com sucesso.");
    }
}
