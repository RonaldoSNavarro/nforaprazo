package com.alianca.nforaprazo.service;

import com.alianca.nforaprazo.dto.InvestigacaoRequest;
import com.alianca.nforaprazo.model.AutoInfracao;
import com.alianca.nforaprazo.model.Cte;
import com.alianca.nforaprazo.model.enums.Responsavel;
import com.alianca.nforaprazo.model.enums.StatusCte;
import com.alianca.nforaprazo.repository.AutoInfracaoRepository;
import com.alianca.nforaprazo.repository.CteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocsFiscalServiceTest {

    @Mock private CteRepository cteRepository;
    @Mock private AutoInfracaoRepository autoInfracaoRepository;
    @Mock private StorageService storageService;
    @Mock private EmailService emailService;

    @InjectMocks
    private DocsFiscalService docsFiscalService;

    private UUID cteId;
    private Cte cteTeste;

    @BeforeEach
    void setUp() {
        cteId = UUID.randomUUID();
        cteTeste = Cte.builder()
                .id(cteId)
                .status(StatusCte.AUTO_RECEBIDO)
                .build();
    }

    @Test
    @DisplayName("Deve iniciar investigacao com sucesso mudando status para EM_INVESTIGACAO")
    void deveIniciarInvestigacaoComSucesso() {
        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));
        when(cteRepository.save(any(Cte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cte resultado = docsFiscalService.iniciarInvestigacao(cteId, "docs@alianca.com.br");

        assertEquals(StatusCte.EM_INVESTIGACAO, resultado.getStatus());
        verify(cteRepository, times(1)).save(cteTeste);
    }

    @Test
    @DisplayName("Deve falhar ao iniciar investigacao se status do CT-e nao for AUTO_RECEBIDO")
    void deveFalharAoIniciarInvestigacaoSeStatusInvalido() {
        cteTeste.setStatus(StatusCte.REGISTRADO);
        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));

        assertThrows(IllegalStateException.class, () -> 
            docsFiscalService.iniciarInvestigacao(cteId, "docs@alianca.com.br")
        );
        verify(cteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao concluir investigacao se o responsavel for definido como PENDENTE")
    void deveFalharSeResponsavelForPendente() {
        cteTeste.setStatus(StatusCte.EM_INVESTIGACAO);
        InvestigacaoRequest request = new InvestigacaoRequest();
        request.setCteId(cteId);
        request.setResponsavel(Responsavel.PENDENTE);

        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));

        assertThrows(IllegalArgumentException.class, () -> 
            docsFiscalService.concluirInvestigacao(request, "docs@alianca.com.br")
        );
    }

    @Test
    @DisplayName("Deve falhar ao concluir investigacao se o motivo do erro for curto (menos de 10 caracteres)")
    void deveFalharSeMotivoForCurto() {
        cteTeste.setStatus(StatusCte.EM_INVESTIGACAO);
        InvestigacaoRequest request = new InvestigacaoRequest();
        request.setCteId(cteId);
        request.setResponsavel(Responsavel.CLIENTE);
        request.setMotivoErro("Curto");

        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            docsFiscalService.concluirInvestigacao(request, "docs@alianca.com.br")
        );
        assertTrue(ex.getMessage().contains("O motivo do erro deve ter pelo menos 10 caracteres."));
    }

    @Test
    @DisplayName("Deve falhar ao concluir investigacao se o ticket for nulo ou vazio")
    void deveFalharSeTicketForVazio() {
        cteTeste.setStatus(StatusCte.EM_INVESTIGACAO);
        InvestigacaoRequest request = new InvestigacaoRequest();
        request.setCteId(cteId);
        request.setResponsavel(Responsavel.CLIENTE);
        request.setMotivoErro("Motivo valido com mais de dez caracteres");
        request.setNumeroTicket("");

        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            docsFiscalService.concluirInvestigacao(request, "docs@alianca.com.br")
        );
        assertTrue(ex.getMessage().contains("O número do ticket é obrigatório."));
    }

    @Test
    @DisplayName("Deve concluir investigacao com sucesso e transicionar para AGUARDANDO_PAGAMENTO")
    void deveConcluirInvestigacaoComSucesso() {
        cteTeste.setStatus(StatusCte.EM_INVESTIGACAO);
        AutoInfracao auto = AutoInfracao.builder().cte(cteTeste).build();
        cteTeste.setAutoInfracao(auto);

        InvestigacaoRequest request = new InvestigacaoRequest();
        request.setCteId(cteId);
        request.setResponsavel(Responsavel.CLIENTE);
        request.setMotivoErro("Cliente enviou a nota fiscal apos o navio ter atracado no porto.");
        request.setNumeroTicket("TK-12345");
        request.setArquivoTicket(new MockMultipartFile("ticket", "ticket.pdf", "application/pdf", "dummy".getBytes()));

        when(cteRepository.findById(cteId)).thenReturn(Optional.of(cteTeste));
        when(storageService.store(any())).thenReturn("ticket_stored.pdf");
        when(cteRepository.save(any(Cte.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(autoInfracaoRepository.save(any(AutoInfracao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AutoInfracao autoResultado = docsFiscalService.concluirInvestigacao(request, "docs@alianca.com.br");

        assertNotNull(autoResultado);
        assertEquals(Responsavel.CLIENTE, autoResultado.getResponsavel());
        assertEquals("TK-12345", autoResultado.getNumeroTicket());
        assertEquals("ticket_stored.pdf", autoResultado.getArquivoTicket());
        assertEquals(StatusCte.AGUARDANDO_PAGAMENTO, cteTeste.getStatus());
        verify(emailService, times(1)).enviarAlertaPagamentoDescarga(cteTeste);
    }
}
