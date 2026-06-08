package com.sistema.nforaprazo.config.aop;

import com.sistema.nforaprazo.annotation.Auditable;
import com.sistema.nforaprazo.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaAspect {

    private final LogService logService;

    @AfterReturning(pointcut = "@annotation(com.sistema.nforaprazo.annotation.Auditable)", returning = "result")
    public void auditarMetodo(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Auditable auditable = method.getAnnotation(Auditable.class);
            
            String acao = auditable.acao();
            String email = logService.obterEmailUsuarioAutenticado();
            String ip = logService.obterIpCliente();
            
            // Montar detalhes a partir dos argumentos de entrada
            StringBuilder detalhes = new StringBuilder();
            Object[] args = joinPoint.getArgs();
            String[] parameterNames = signature.getParameterNames();
            
            if (args != null && parameterNames != null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] != null) {
                        String argName = parameterNames[i];
                        
                        // Proteção de dados confidenciais (ex: senhas) para não gravar nos logs
                        if (argName.toLowerCase().contains("senha") || 
                            argName.toLowerCase().contains("password") || 
                            argName.toLowerCase().contains("secret")) {
                            detalhes.append(argName).append("=[PROTEGIDO]; ");
                        } else {
                            detalhes.append(argName).append("=[").append(args[i].toString()).append("]; ");
                        }
                    }
                }
            }
            
            // Envia o registro de log de forma assíncrona
            logService.registrarLog(email, acao, detalhes.toString(), ip);
        } catch (Exception e) {
            log.error("Erro ao interceptar e registrar log de auditoria", e);
        }
    }
}
