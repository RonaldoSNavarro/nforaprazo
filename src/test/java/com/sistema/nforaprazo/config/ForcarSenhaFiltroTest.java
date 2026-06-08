package com.sistema.nforaprazo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForcarSenhaFiltroTest {

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private ForcarSenhaFiltro forcarSenhaFiltro;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Deve ignorar o filtro para recursos estáticos")
    void deveIgnorarFiltroParaRecursosEstaticos() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/css/main.css");

        forcarSenhaFiltro.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("Deve ignorar o filtro para rota de login ou alteração de senha")
    void deveIgnorarFiltroParaRotasIsentas() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/alterar-senha");

        forcarSenhaFiltro.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("Deve redirecionar para alterar-senha se o usuario autenticado tiver flag de alteracao pendente")
    void deveRedirecionarSeAlteracaoPendente() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/cte/lista");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        CustomUserDetails userDetails = new CustomUserDetails(
                "roberto@sistema.local",
                "senha",
                Collections.emptyList(),
                true
        );
        when(authentication.getPrincipal()).thenReturn(userDetails);

        forcarSenhaFiltro.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).sendRedirect("/alterar-senha");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve deixar passar no filtro se o usuario nao precisar alterar a senha")
    void deveDeixarPassarSeNaoPrecisarAlterarSenha() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/cte/lista");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        CustomUserDetails userDetails = new CustomUserDetails(
                "roberto@sistema.local",
                "senha",
                Collections.emptyList(),
                false
        );
        when(authentication.getPrincipal()).thenReturn(userDetails);

        forcarSenhaFiltro.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }
}
