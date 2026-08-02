package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.model.LogAlerta;
import com.sistema.nforaprazo.model.enums.StatusEnvio;
import com.sistema.nforaprazo.model.Usuario;
import com.sistema.nforaprazo.model.enums.PerfilUsuario;
import com.sistema.nforaprazo.model.enums.TipoAlerta;
import com.sistema.nforaprazo.repository.LogAlertaRepository;
import com.sistema.nforaprazo.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final LogAlertaRepository logAlertaRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${spring.mail.username:noreply@sistema.local}")
    private String emailRemetente;

    @Async
    public void enviarAlertaDescarga(Cte cte) {
        String assunto = "[ALERTA] Novo CT-e para Porto Monitorado: " + cte.getNumeroCte();
        String texto = String.format(
                "Olá Equipe de Descarga,\n\n" +
                "Um novo CT-e foi registrado com destino a um porto monitorado e necessita de desembaraço:\n\n" +
                "Número CT-e: %s\n" +
                "Chave de Acesso: %s\n" +
                "Tomador: %s\n" +
                "Destino: %s\n" +
                "Valor da Carga: R$ %,.2f\n" +
                "Booking: %s\n\n" +
                "Por favor, acesse o sistema para confirmar o desembaraço.",
                cte.getNumeroCte(),
                cte.getChaveAcesso(),
                cte.getTomadorNome(),
                cte.getPortoDestino(),
                cte.getValorCarga(),
                cte.getNumeroBooking()
        );
        List<Usuario> usuarios = usuarioRepository.findByPerfilAndAtivoTrue(PerfilUsuario.DESCARGA);
        if (usuarios.isEmpty()) log.warn("Nenhum usuario ativo no perfil DESCARGA para receber alerta.");
        for (Usuario u : usuarios) {
            enviarEmail(cte, TipoAlerta.NOVO_CTE_PORTO_MONITORADO, u.getEmail(), assunto, texto);
        }
    }

    @Async
    public void enviarAlertaDocsFiscal(Cte cte) {
        String assunto = "[ALERTA] Auto de Infração Recebido - CT-e: " + cte.getNumeroCte();
        String texto = String.format(
                "Olá Equipe de Docs Fiscal,\n\n" +
                "Um Auto de Infração foi anexado pelo time de descarga para o seguinte CT-e:\n\n" +
                "Número CT-e: %s\n" +
                "Chave de Acesso: %s\n" +
                "Tomador: %s\n" +
                "Destino: %s\n\n" +
                "Acesse o sistema e inicie o processo de investigação de responsabilidade.",
                cte.getNumeroCte(),
                cte.getChaveAcesso(),
                cte.getTomadorNome(),
                cte.getPortoDestino()
        );
        List<Usuario> usuarios = usuarioRepository.findByPerfilAndAtivoTrue(PerfilUsuario.DOCS_FISCAL);
        if (usuarios.isEmpty()) log.warn("Nenhum usuario ativo no perfil DOCS_FISCAL para receber alerta.");
        for (Usuario u : usuarios) {
            enviarEmail(cte, TipoAlerta.AUTO_INFRACAO_RECEBIDO, u.getEmail(), assunto, texto);
        }
    }

    @Async
    public void enviarAlertaPagamentoDescarga(Cte cte) {
        String responsavel = (cte.getAutoInfracao() != null && cte.getAutoInfracao().getResponsavel() != null)
                ? cte.getAutoInfracao().getResponsavel().name() : "PENDENTE";
        
        String assunto = "[ALERTA] Investigação Concluída - CT-e: " + cte.getNumeroCte();
        String texto = String.format(
                "Olá Equipe de Descarga,\n\n" +
                "A investigação de responsabilidade do Auto de Infração foi concluída:\n\n" +
                "Número CT-e: %s\n" +
                "Chave de Acesso: %s\n" +
                "Responsabilidade Definida: %s\n" +
                "Motivo: %s\n\n" +
                "O DAR está liberado para ser gerado e anexado para posterior confirmação do pagamento.",
                cte.getNumeroCte(),
                cte.getChaveAcesso(),
                responsavel,
                cte.getAutoInfracao() != null ? cte.getAutoInfracao().getMotivoErro() : ""
        );
        List<Usuario> usuarios = usuarioRepository.findByPerfilAndAtivoTrue(PerfilUsuario.DESCARGA);
        if (usuarios.isEmpty()) log.warn("Nenhum usuario ativo no perfil DESCARGA para receber alerta.");
        for (Usuario u : usuarios) {
            enviarEmail(cte, TipoAlerta.INVESTIGACAO_CONCLUIDA, u.getEmail(), assunto, texto);
        }
    }

    @Async
    public void enviarAlertaFaturamento(Cte cte) {
        String assunto = "[ALERTA] Pagamento Confirmado - CT-e: " + cte.getNumeroCte();
        String texto = String.format(
                "Olá Equipe de Faturamento,\n\n" +
                "O pagamento do Auto de Infração associado ao seguinte CT-e foi confirmado:\n\n" +
                "Número CT-e: %s\n" +
                "Chave de Acesso: %s\n" +
                "Tomador: %s\n" +
                "Responsável: %s\n\n" +
                "Verifique a necessidade de emissão de Nota de Débito para o cliente.",
                cte.getNumeroCte(),
                cte.getChaveAcesso(),
                cte.getTomadorNome(),
                (cte.getAutoInfracao() != null && cte.getAutoInfracao().getResponsavel() != null) ? cte.getAutoInfracao().getResponsavel().name() : ""
        );
        List<Usuario> usuarios = usuarioRepository.findByPerfilAndAtivoTrue(PerfilUsuario.FATURAMENTO);
        if (usuarios.isEmpty()) log.warn("Nenhum usuario ativo no perfil FATURAMENTO para receber alerta.");
        for (Usuario u : usuarios) {
            enviarEmail(cte, TipoAlerta.PAGAMENTO_CONFIRMADO, u.getEmail(), assunto, texto);
        }
    }

    @Async
    public void enviarAlertaEncerramentoSemAuto(Cte cte) {
        String assunto = "[ALERTA] Processo Encerrado Sem Auto de Infração - CT-e: " + cte.getNumeroCte();
        String texto = String.format(
                "Olá Equipe,\n\n" +
                "O processo do CT-e %s foi encerrado SEM auto de infração.\n\n" +
                "Chave de Acesso: %s\n" +
                "Multa Evitada (Estimada): R$ %,.2f\n" +
                "Justificativa: %s\n\n" +
                "Processo finalizado com sucesso.",
                cte.getNumeroCte(),
                cte.getChaveAcesso(),
                cte.getEncSemAuto() != null ? cte.getEncSemAuto().getValorPotencialMulta() : java.math.BigDecimal.ZERO,
                cte.getEncSemAuto() != null ? cte.getEncSemAuto().getJustificativa() : ""
        );
        List<Usuario> usuarios = usuarioRepository.findByPerfilAndAtivoTrue(PerfilUsuario.DESCARGA);
        if (usuarios.isEmpty()) log.warn("Nenhum usuario ativo no perfil DESCARGA para receber alerta.");
        for (Usuario u : usuarios) {
            enviarEmail(cte, TipoAlerta.ENCERRADO_SEM_AUTO, u.getEmail(), assunto, texto);
        }
    }

    @Async
    public void enviarAlertaEncerramentoComAuto(Cte cte) {
        String nDebito = (cte.getNotaDebito() != null) ? cte.getNotaDebito().getNumeroNotaDebito() : "N/A (Custo Absorvido)";
        String assunto = "[ALERTA] Processo Encerrado Com Auto de Infração - CT-e: " + cte.getNumeroCte();
        String texto = String.format(
                "Olá Equipe,\n\n" +
                "O processo do CT-e %s foi encerrado com auto de infração e pagamento confirmado:\n\n" +
                "Chave de Acesso: %s\n" +
                "Responsável: %s\n" +
                "Nota de Débito: %s\n\n" +
                "Processo finalizado com sucesso.",
                cte.getNumeroCte(),
                cte.getChaveAcesso(),
                (cte.getAutoInfracao() != null && cte.getAutoInfracao().getResponsavel() != null) ? cte.getAutoInfracao().getResponsavel().name() : "",
                nDebito
        );
        List<Usuario> usuarios = usuarioRepository.findByPerfilAndAtivoTrue(PerfilUsuario.FATURAMENTO);
        if (usuarios.isEmpty()) log.warn("Nenhum usuario ativo no perfil FATURAMENTO para receber alerta.");
        for (Usuario u : usuarios) {
            enviarEmail(cte, TipoAlerta.ENCERRADO_COM_AUTO, u.getEmail(), assunto, texto);
        }
    }

    @Async
    public void enviarSenhaProvisoria(String emailDestino, String nomeUsuario, String senhaProvisoria, String linkAcesso) {
        String assunto = "[NF Fora do Prazo] Cadastro de Usuário e Senha Provisória";
        String texto = String.format(
                "Olá %s,\n\n" +
                "Você foi cadastrado no sistema NF Fora do Prazo.\n\n" +
                "Suas credenciais de acesso temporárias são:\n" +
                "E-mail: %s\n" +
                "Senha Provisória: %s\n\n" +
                "Link de Acesso: %s\n\n" +
                "Importante: Por motivos de segurança, você deverá cadastrar uma nova senha logo após o primeiro acesso.",
                nomeUsuario,
                emailDestino,
                senhaProvisoria,
                linkAcesso
        );
        
        LogAlerta logAlerta = LogAlerta.builder()
                .cte(null)
                .tipoAlerta(TipoAlerta.SENHA_PROVISORIA)
                .destinatario(emailDestino)
                .assunto(assunto)
                .dataEnvio(LocalDateTime.now())
                .build();
                
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getRemetenteValido());
            message.setTo(emailDestino);
            message.setSubject(assunto);
            message.setText(texto);
            mailSender.send(message);
            logAlerta.setStatusEnvio(StatusEnvio.ENVIADO);
            log.info("E-mail de senha provisória enviado para {}", emailDestino);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de senha provisória para {}", emailDestino, e);
            logAlerta.setStatusEnvio(StatusEnvio.ERRO);
            logAlerta.setMensagemErro(e.getMessage());
        } finally {
            logAlertaRepository.save(logAlerta);
        }
    }

    private void enviarEmail(Cte cte, TipoAlerta tipo, String destinatario, String assunto, String texto) {
        LogAlerta logAlerta = LogAlerta.builder()
                .cte(cte)
                .tipoAlerta(tipo)
                .destinatario(destinatario)
                .assunto(assunto)
                .dataEnvio(LocalDateTime.now())
                .build();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getRemetenteValido());
            message.setTo(destinatario);
            message.setSubject(assunto);
            message.setText(texto);
            mailSender.send(message);
            logAlerta.setStatusEnvio(StatusEnvio.ENVIADO);
            log.info("E-mail do tipo {} enviado para {}", tipo, destinatario);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail do tipo {} para {}", tipo, destinatario, e);
            logAlerta.setStatusEnvio(StatusEnvio.ERRO);
            logAlerta.setMensagemErro(e.getMessage());
        } finally {
            logAlertaRepository.save(logAlerta);
        }
    }

    private String getRemetenteValido() {
        if (emailRemetente != null && !emailRemetente.trim().isEmpty()) {
            return emailRemetente.trim();
        }
        return "noreply@sistema.local";
    }
}
