package com.alianca.nforaprazo.config;

import com.alianca.nforaprazo.model.Usuario;
import com.alianca.nforaprazo.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ForcarSenhaFiltro extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String uri = request.getRequestURI();
        
        // Ignorar caminhos de login, logout, recursos estáticos e a própria tela de alteração de senha
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/fonts/") ||
            uri.startsWith("/images/") || uri.startsWith("/webjars/") || 
            uri.equals("/login") || uri.equals("/logout") || uri.equals("/alterar-senha") ||
            uri.startsWith("/actuator/")) { // permitir actuator sem interceptação se necessário
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            
            if (usuarioOpt.isPresent() && Boolean.TRUE.equals(usuarioOpt.get().getAlterarSenha())) {
                response.sendRedirect("/alterar-senha");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
