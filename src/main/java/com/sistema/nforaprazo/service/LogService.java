package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.model.LogAtividade;
import com.sistema.nforaprazo.repository.LogAtividadeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final LogAtividadeRepository logAtividadeRepository;

    /**
     * Registra uma ação de auditoria de forma assíncrona.
     * Propagation.REQUIRES_NEW garante que o log seja salvo mesmo se a transação do serviço falhar.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLog(String usuarioEmail, String acao, String detalhes, String ipOrigem) {
        log.debug("Registrando log de auditoria: acao='{}', usuario='{}'", acao, usuarioEmail);
        
        LogAtividade logAtividade = LogAtividade.builder()
                .usuarioEmail(usuarioEmail)
                .acao(acao)
                .detalhes(detalhes)
                .ipOrigem(ipOrigem)
                .build();
                
        logAtividadeRepository.save(logAtividade);
    }

    /**
     * Tenta obter o IP do cliente a partir do request corrente.
     */
    public String obterIpCliente() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "SISTEMA";
        }
        
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Em ambientes de teste locais, o IP do cliente pode vir duplicado ou como IPv6
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Tenta obter o e-mail do usuário autenticado no contexto.
     */
    public String obterEmailUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "ANONYMOUS";
        }
        return auth.getName();
    }

    @Transactional(readOnly = true)
    public Page<LogAtividade> obterTodosLogs(Pageable pageable) {
        return logAtividadeRepository.findAllByOrderByDataCriacaoDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<LogAtividade> filtrarPorUsuario(String email, Pageable pageable) {
        return logAtividadeRepository.findByUsuarioEmailContainingIgnoreCaseOrderByDataCriacaoDesc(email, pageable);
    }
}
