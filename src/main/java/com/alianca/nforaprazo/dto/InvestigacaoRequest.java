package com.alianca.nforaprazo.dto;

import com.alianca.nforaprazo.model.enums.Responsavel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigacaoRequest {

    @NotNull(message = "O ID do CT-e é obrigatório")
    private UUID cteId;

    @NotNull(message = "O responsável da multa deve ser definido")
    private Responsavel responsavel;

    @NotBlank(message = "O motivo do erro é obrigatório")
    @Size(min = 10, message = "O motivo do erro deve ter pelo menos 10 caracteres")
    private String motivoErro;

    @NotBlank(message = "O número do ticket da evidência é obrigatório")
    private String numeroTicket;

    private MultipartFile arquivoTicket;
}
