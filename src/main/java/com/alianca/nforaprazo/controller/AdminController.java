package com.alianca.nforaprazo.controller;

import com.alianca.nforaprazo.annotation.Auditable;
import com.alianca.nforaprazo.model.LogAlerta;
import com.alianca.nforaprazo.model.LogAtividade;
import com.alianca.nforaprazo.model.Usuario;
import com.alianca.nforaprazo.model.enums.PerfilUsuario;
import com.alianca.nforaprazo.repository.LogAlertaRepository;
import com.alianca.nforaprazo.repository.UsuarioRepository;
import com.alianca.nforaprazo.service.EmailService;
import com.alianca.nforaprazo.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final LogAlertaRepository logAlertaRepository;
    private final LogService logService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/admin/usuarios")
    public String gerenciarUsuarios(Model model, @RequestParam(defaultValue = "0") int page) {
        log.info("Acessando painel de gerenciamento de usuarios");
        Page<Usuario> usuarios = usuarioRepository.findAll(PageRequest.of(page, 10));
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("perfis", PerfilUsuario.values());
        return "admin/usuarios";
    }

    @PostMapping("/admin/usuarios/cadastrar")
    @Auditable(acao = "Cadastro de Usuario")
    public String cadastrarUsuario(@RequestParam String nome,
                                   @RequestParam String email,
                                   @RequestParam PerfilUsuario perfil,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        
        log.info("Cadastrando novo usuario: nome='{}', email='{}', perfil='{}'", nome, email, perfil);
        
        if (usuarioRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Já existe um usuário cadastrado com este e-mail.");
            return "redirect:/admin/usuarios";
        }

        // Gerar senha provisória aleatória de 8 caracteres
        String senhaProvisoria = UUID.randomUUID().toString().substring(0, 8);
        String senhaCriptografada = passwordEncoder.encode(senhaProvisoria);

        Usuario novoUsuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(senhaCriptografada)
                .perfil(perfil)
                .ativo(true)
                .alterarSenha(true) // Exige alteração de senha no primeiro acesso
                .build();

        usuarioRepository.save(novoUsuario);

        // Monta link de login com base no request corrente
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        String linkAcesso = scheme + "://" + serverName + (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort) + contextPath + "/login";

        // Envia o e-mail de forma assíncrona
        emailService.enviarSenhaProvisoria(email, nome, senhaProvisoria, linkAcesso);

        redirectAttributes.addFlashAttribute("success", "Usuário cadastrado com sucesso. E-mail de convite enviado!");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/admin/logs")
    public String visualizarLogs(Model model, 
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(required = false) String emailFiltro) {
        
        log.info("Acessando visualizacao de logs de auditoria");
        PageRequest pageRequest = PageRequest.of(page, 15);
        
        Page<LogAtividade> logsAtividade;
        if (emailFiltro != null && !emailFiltro.trim().isEmpty()) {
            logsAtividade = logService.filtrarPorUsuario(emailFiltro, pageRequest);
            model.addAttribute("emailFiltro", emailFiltro);
        } else {
            logsAtividade = logService.obterTodosLogs(pageRequest);
        }
        
        Page<LogAlerta> logsAlerta = logAlertaRepository.findAll(PageRequest.of(page, 15));

        model.addAttribute("logsAtividade", logsAtividade);
        model.addAttribute("logsAlerta", logsAlerta);
        return "admin/logs";
    }

    @GetMapping("/alterar-senha")
    public String exibirAlterarSenha() {
        return "alterar-senha";
    }

    @PostMapping("/alterar-senha")
    public String alterarSenha(@RequestParam String novaSenha,
                               @RequestParam String confirmarSenha,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        
        String email = principal.getName();
        log.info("Processando alteracao de senha obrigatoria para {}", email);

        if (novaSenha == null || novaSenha.trim().length() < 6) {
            redirectAttributes.addFlashAttribute("error", "A senha deve conter no mínimo 6 caracteres.");
            return "redirect:/alterar-senha";
        }

        if (!novaSenha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("error", "As senhas não coincidem.");
            return "redirect:/alterar-senha";
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário inválido"));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setAlterarSenha(false); // Flag removida
        usuarioRepository.save(usuario);

        // Registra o log manualmente, pois este é o método de desativação do filtro
        logService.registrarLog(email, "Alteracao de Senha Provisoria", "Senha provisória alterada com sucesso.", logService.obterIpCliente());

        redirectAttributes.addFlashAttribute("success", "Senha alterada com sucesso. Acesso liberado!");
        return "redirect:/home";
    }
}
