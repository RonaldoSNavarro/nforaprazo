package com.sistema.nforaprazo.config;

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

@Component
@RequiredArgsConstructor
public class ForcarSenhaFiltro extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String uri = request.getRequestURI();
        
        // Ignorar caminhos de login, logout, recursos estáticos e a própria tela de alteração de senha
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/fonts/") ||
            uri.startsWith("/images/") || uri.startsWith("/webjars/") || 
            uri.equals("/login") || uri.equals("/logout") || uri.equals("/alterar-senha")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            if (userDetails.isAlterarSenha()) {
                response.sendRedirect("/alterar-senha");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
