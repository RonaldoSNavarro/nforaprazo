package com.sistema.nforaprazo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaDebitoRequest {

    @NotNull(message = "O ID do CT-e é obrigatório")
    private UUID cteId;

    @NotBlank(message = "O número da Nota de Débito é obrigatório")
    private String numeroNotaDebito;

    @NotNull(message = "A data de emissão é obrigatória")
    private LocalDate dataEmissao;

    @NotNull(message = "O arquivo PDF da Nota de Débito é obrigatório")
    private MultipartFile arquivoPdf;
}
