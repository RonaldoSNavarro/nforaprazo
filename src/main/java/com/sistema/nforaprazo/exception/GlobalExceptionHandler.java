package com.sistema.nforaprazo.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, HttpServletRequest request, Model model) {
        log.warn("Acesso negado: {} tentou acessar {}", request.getRemoteUser(), request.getRequestURI());
        model.addAttribute("mensagem", "Voce nao tem permissao para acessar esta pagina.");
        return "403";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, HttpServletRequest request, Model model) {
        log.error("Erro interno ao processar {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("mensagem", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        return "error";
    }
}
