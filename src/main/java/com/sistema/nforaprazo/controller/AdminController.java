package com.sistema.nforaprazo.controller;

import com.sistema.nforaprazo.annotation.Auditable;
import com.sistema.nforaprazo.model.LogAlerta;
import com.sistema.nforaprazo.model.LogAtividade;
import com.sistema.nforaprazo.model.Usuario;
import com.sistema.nforaprazo.model.enums.PerfilUsuario;
import com.sistema.nforaprazo.repository.LogAlertaRepository;
import com.sistema.nforaprazo.repository.UsuarioRepository;
import com.sistema.nforaprazo.service.EmailService;
import com.sistema.nforaprazo.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.sistema.nforaprazo.config.CustomUserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sistema.nforaprazo.dto.PortoMonitoradoRequest;
import com.sistema.nforaprazo.dto.PortoMonitoradoResponse;
import com.sistema.nforaprazo.service.PortoMonitoradoService;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final PortoMonitoradoService portoMonitoradoService;

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

    @PostMapping("/admin/usuarios/editar")
    @Auditable(acao = "Edicao de Usuario")
    public String editarUsuario(@RequestParam UUID id,
                                @RequestParam String nome,
                                @RequestParam PerfilUsuario perfil,
                                @RequestParam(required = false) Boolean ativo,
                                RedirectAttributes redirectAttributes) {
        log.info("Editando usuario id='{}', nome='{}', perfil='{}', ativo='{}'", id, nome, perfil, ativo);
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null) {
            usuario.setNome(nome);
            usuario.setPerfil(perfil);
            usuario.setAtivo(ativo != null ? ativo : false);
            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("success", "Usuário atualizado com sucesso.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuário não encontrado.");
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/excluir")
    @Auditable(acao = "Exclusao de Usuario")
    public String excluirUsuario(@RequestParam UUID id, RedirectAttributes redirectAttributes) {
        log.info("Excluindo usuario id='{}'", id);
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Usuário excluído com sucesso.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuário não encontrado.");
        }
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

        // Atualiza a autenticação na sessão com o novo principal com alterarSenha = false
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            CustomUserDetails novoUserDetails = new CustomUserDetails(
                usuario.getEmail(),
                usuario.getSenha(),
                auth.getAuthorities(),
                false
            );
            UsernamePasswordAuthenticationToken novaAuth = new UsernamePasswordAuthenticationToken(
                novoUserDetails,
                auth.getCredentials(),
                auth.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(novaAuth);
        }

        // Registra o log manualmente, pois este é o método de desativação do filtro
        logService.registrarLog(email, "Alteracao de Senha Provisoria", "Senha provisória alterada com sucesso.", logService.obterIpCliente());

        redirectAttributes.addFlashAttribute("success", "Senha alterada com sucesso. Acesso liberado!");
        return "redirect:/home";
    }

    @GetMapping("/admin/observabilidade")
    public String observabilidade(Model model) {
        log.info("Acessando painel de observabilidade");
        return "admin/observabilidade";
    }

    @GetMapping("/admin/portos")
    public String gerenciarPortos(Model model) {
        log.info("Acessando painel de gerenciamento de portos");
        model.addAttribute("portos", portoMonitoradoService.listarTodos());
        model.addAttribute("portoRequest", new PortoMonitoradoRequest());
        return "admin/portos";
    }

    @PostMapping("/admin/portos/cadastrar")
    public String cadastrarPorto(@Valid @ModelAttribute("portoRequest") PortoMonitoradoRequest request,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        log.info("Recebida requisição para cadastrar porto: {}", request.getNome());
        if (bindingResult.hasErrors()) {
            String erroMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("error", "Erro de validação: " + erroMsg);
            return "redirect:/admin/portos";
        }
        try {
            portoMonitoradoService.cadastrarPorto(request, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Porto monitorado cadastrado com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao cadastrar porto", e);
            redirectAttributes.addFlashAttribute("error", "Erro: " + e.getMessage());
        }
        return "redirect:/admin/portos";
    }

    @PostMapping("/admin/portos/editar")
    public String editarPorto(@Valid @ModelAttribute("portoRequest") PortoMonitoradoRequest request,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        log.info("Recebida requisição para editar porto ID: {}", request.getId());
        if (bindingResult.hasErrors()) {
            String erroMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("error", "Erro de validação: " + erroMsg);
            return "redirect:/admin/portos";
        }
        try {
            portoMonitoradoService.editarPorto(request);
            redirectAttributes.addFlashAttribute("success", "Porto monitorado atualizado com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao editar porto", e);
            redirectAttributes.addFlashAttribute("error", "Erro: " + e.getMessage());
        }
        return "redirect:/admin/portos";
    }

    @PostMapping("/admin/portos/excluir")
    public String excluirPorto(@RequestParam UUID id, RedirectAttributes redirectAttributes) {
        log.info("Recebida requisição para excluir porto ID: {}", id);
        try {
            portoMonitoradoService.excluirPorto(id);
            redirectAttributes.addFlashAttribute("success", "Porto monitorado removido com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao excluir porto", e);
            redirectAttributes.addFlashAttribute("error", "Erro: " + e.getMessage());
        }
        return "redirect:/admin/portos";
    }
}
