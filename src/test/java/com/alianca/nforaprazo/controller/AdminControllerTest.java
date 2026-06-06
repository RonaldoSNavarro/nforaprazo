package com.alianca.nforaprazo.controller;

import com.alianca.nforaprazo.model.Usuario;
import com.alianca.nforaprazo.model.enums.PerfilUsuario;
import com.alianca.nforaprazo.repository.LogAlertaRepository;
import com.alianca.nforaprazo.repository.UsuarioRepository;
import com.alianca.nforaprazo.service.EmailService;
import com.alianca.nforaprazo.service.LogService;
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

    @InjectMocks
    private AdminController adminController;

    private Usuario usuarioTeste;

    @BeforeEach
    void setUp() {
        usuarioTeste = Usuario.builder()
                .email("roberto@alianca.com.br")
                .nome("Roberto")
                .senha("encoded-pass")
                .perfil(PerfilUsuario.FATURAMENTO)
                .alterarSenha(true)
                .build();
    }

    @Test
    @DisplayName("Deve falhar no cadastro se o e-mail do usuario ja existir")
    void deveFalharSeEmailJaExistir() {
        when(usuarioRepository.existsByEmail("roberto@alianca.com.br")).thenReturn(true);

        String view = adminController.cadastrarUsuario("Roberto", "roberto@alianca.com.br", PerfilUsuario.FATURAMENTO, request, redirectAttributes);

        assertEquals("redirect:/admin/usuarios", view);
        verify(redirectAttributes).addFlashAttribute("error", "Já existe um usuário cadastrado com este e-mail.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cadastrar usuario com sucesso, gerando senha provisoria e disparando e-mail")
    void deveCadastrarUsuarioComSucesso() {
        when(usuarioRepository.existsByEmail("roberto@alianca.com.br")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-provisoria");
        
        // Mocking HttpServletRequest
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getContextPath()).thenReturn("");

        String view = adminController.cadastrarUsuario("Roberto", "roberto@alianca.com.br", PerfilUsuario.FATURAMENTO, request, redirectAttributes);

        assertEquals("redirect:/admin/usuarios", view);
        verify(usuarioRepository, times(1)).save(argThat(u -> 
            u.getNome().equals("Roberto") &&
            u.getEmail().equals("roberto@alianca.com.br") &&
            u.getSenha().equals("hashed-provisoria") &&
            u.getAlterarSenha() // Deve ser true
        ));
        
        verify(emailService, times(1)).enviarSenhaProvisoria(
                eq("roberto@alianca.com.br"), 
                eq("Roberto"), 
                anyString(), // Senha provisoria aleatoria
                eq("http://localhost:8080/login")
        );
        verify(redirectAttributes).addFlashAttribute("success", "Usuário cadastrado com sucesso. E-mail de convite enviado!");
    }

    @Test
    @DisplayName("Deve falhar na alteracao de senha se a nova senha for menor que 6 caracteres")
    void deveFalharSeSenhaForCurta() {
        Principal principal = () -> "roberto@alianca.com.br";

        String view = adminController.alterarSenha("123", "123", principal, redirectAttributes);

        assertEquals("redirect:/alterar-senha", view);
        verify(redirectAttributes).addFlashAttribute("error", "A senha deve conter no mínimo 6 caracteres.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar na alteracao de senha se as senhas nao coincidirem")
    void deveFalharSeSenhasDiferentes() {
        Principal principal = () -> "roberto@alianca.com.br";

        String view = adminController.alterarSenha("senha123", "senha321", principal, redirectAttributes);

        assertEquals("redirect:/alterar-senha", view);
        verify(redirectAttributes).addFlashAttribute("error", "As senhas não coincidem.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve alterar senha com sucesso, atualizando flag no banco e gravando log")
    void deveAlterarSenhaComSucesso() {
        Principal principal = () -> "roberto@alianca.com.br";
        when(usuarioRepository.findByEmail("roberto@alianca.com.br")).thenReturn(Optional.of(usuarioTeste));
        when(passwordEncoder.encode("novasenha123")).thenReturn("hashed-nova");
        when(logService.obterIpCliente()).thenReturn("127.0.0.1");

        String view = adminController.alterarSenha("novasenha123", "novasenha123", principal, redirectAttributes);

        assertEquals("redirect:/home", view);
        assertFalse(usuarioTeste.getAlterarSenha());
        assertEquals("hashed-nova", usuarioTeste.getSenha());
        verify(usuarioRepository, times(1)).save(usuarioTeste);
        verify(logService, times(1)).registrarLog(
                eq("roberto@alianca.com.br"), 
                eq("Alteracao de Senha Provisoria"), 
                anyString(), 
                eq("127.0.0.1")
        );
        verify(redirectAttributes).addFlashAttribute("success", "Senha alterada com sucesso. Acesso liberado!");
    }
}
