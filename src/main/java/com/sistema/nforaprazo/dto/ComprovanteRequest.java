package com.sistema.nforaprazo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprovanteRequest {

    @NotNull(message = "O ID do CT-e é obrigatório")
    private UUID cteId;

    @NotNull(message = "O valor pago é obrigatório")
    @Positive(message = "O valor pago deve ser maior que zero")
    private BigDecimal valorPago;

    @NotNull(message = "A data do pagamento é obrigatória")
    private LocalDate dataPagamento;

    @NotNull(message = "O arquivo PDF do comprovante é obrigatório")
    private MultipartFile comprovantePdf;

    private MultipartFile capaPdf;
}
