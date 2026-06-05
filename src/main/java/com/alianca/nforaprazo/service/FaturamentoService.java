package com.alianca.nforaprazo.service;

import com.alianca.nforaprazo.dto.NotaDebitoRequest;
import com.alianca.nforaprazo.model.Cte;
import com.alianca.nforaprazo.model.NotaDebito;
import com.alianca.nforaprazo.model.Pagamento;
import com.alianca.nforaprazo.model.enums.Responsavel;
import com.alianca.nforaprazo.model.enums.StatusCte;
import com.alianca.nforaprazo.repository.CteRepository;
import com.alianca.nforaprazo.repository.NotaDebitoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaturamentoService {

    private final CteRepository cteRepository;
    private final NotaDebitoRepository notaDebitoRepository;
    private final StorageService storageService;
    private final EmailService emailService;

    @Transactional
    public NotaDebito emitirNotaDebito(NotaDebitoRequest request, String emailUsuario) {
        Cte cte = cteRepository.findById(request.getCteId())
                .orElseThrow(() -> new IllegalArgumentException("CT-e não encontrado"));

        if (cte.getStatus() != StatusCte.PAGO) {
            throw new IllegalStateException("O processo deve estar PAGO para emitir Nota de Débito. Status atual: " + cte.getStatus());
        }

        if (cte.getAutoInfracao() == null || cte.getAutoInfracao().getResponsavel() != Responsavel.CLIENTE) {
            throw new IllegalStateException("Nota de Débito só pode ser emitida se o responsável pela infração for o CLIENTE.");
        }

        Pagamento pagamento = cte.getAutoInfracao().getPagamento();
        if (pagamento == null || pagamento.getPathComprovantePdf() == null) {
            throw new IllegalStateException("Comprovante de pagamento não encontrado.");
        }

        String pathPdf = storageService.store(request.getArquivoPdf());

        NotaDebito nota = NotaDebito.builder()
                .cte(cte)
                .pagamento(pagamento)
                .numeroNotaDebito(request.getNumeroNotaDebito())
                .valorNotaDebito(pagamento.getValorPago()) // O valor cobrado é exatamente o valor pago no DAR (RN02)
                .dataEmissao(request.getDataEmissao())
                .arquivoPdf(pathPdf)
                .build();

        cte.setStatus(StatusCte.ENCERRADO_COM_AUTO);
        cteRepository.save(cte);
        NotaDebito notaSalva = notaDebitoRepository.save(nota);

        log.info("Nota de Débito {} emitida para CT-e {} por {}. Status: ENCERRADO_COM_AUTO.", 
                request.getNumeroNotaDebito(), cte.getId(), emailUsuario);

        emailService.enviarAlertaEncerramentoComAuto(cte);

        return notaSalva;
    }

    @Transactional
    public Cte encerrarCustoAbsorvido(UUID cteId, String emailUsuario) {
        Cte cte = cteRepository.findById(cteId)
                .orElseThrow(() -> new IllegalArgumentException("CT-e não encontrado"));

        if (cte.getStatus() != StatusCte.PAGO) {
            throw new IllegalStateException("O processo deve estar PAGO para ser encerrado. Status atual: " + cte.getStatus());
        }

        if (cte.getAutoInfracao() == null || cte.getAutoInfracao().getResponsavel() != Responsavel.ALIANCA) {
            throw new IllegalStateException("Apenas processos com responsabilidade da ALIANCA podem ser encerrados sem cobrança.");
        }

        cte.setStatus(StatusCte.ENCERRADO_COM_AUTO);
        Cte cteSalvo = cteRepository.save(cte);

        log.info("Processo do CT-e {} encerrado sem cobrança (Custo Absorvido pela Aliança) por {}.", cteId, emailUsuario);

        emailService.enviarAlertaEncerramentoComAuto(cte);

        return cteSalvo;
    }
}
