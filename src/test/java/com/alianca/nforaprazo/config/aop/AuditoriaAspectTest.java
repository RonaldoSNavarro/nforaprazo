package com.alianca.nforaprazo.config.aop;

import com.alianca.nforaprazo.annotation.Auditable;
import com.alianca.nforaprazo.service.LogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaAspectTest {

    @Mock private LogService logService;
    @Mock private JoinPoint joinPoint;
    @Mock private MethodSignature methodSignature;

    @InjectMocks
    private AuditoriaAspect auditoriaAspect;

    // Interface / método fictício para testar reflexão
    interface TestService {
        @Auditable(acao = "Acao Ficticia")
        void executarAcao(String parametro, String senha);
    }

    @Test
    @DisplayName("Deve interceptar metodo anotado, extrair parametros ocultando senhas e gravar log")
    void deveAuditarMetodoComSucesso() throws NoSuchMethodException {
        // Obter método mockado do test service
        Method method = TestService.class.getMethod("executarAcao", String.class, String.class);
        
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        
        when(logService.obterEmailUsuarioAutenticado()).thenReturn("usuario@alianca.com.br");
        when(logService.obterIpCliente()).thenReturn("192.168.0.1");
        
        when(joinPoint.getArgs()).thenReturn(new Object[]{"valor_param", "senha_secreta_123"});
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"parametro", "senha"});

        // Chama o aspecto
        auditoriaAspect.auditarMetodo(joinPoint, null);

        // Verifica se gravou o log ocultando a senha
        verify(logService, times(1)).registrarLog(
                eq("usuario@alianca.com.br"),
                eq("Acao Ficticia"),
                contains("parametro=[valor_param]; senha=[PROTEGIDO];"),
                eq("192.168.0.1")
        );
    }
}
