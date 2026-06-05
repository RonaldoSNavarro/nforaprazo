package com.alianca.nforaprazo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncSemAutoRequest {

    @NotNull(message = "O ID do CT-e é obrigatório")
    private UUID cteId;

    @NotBlank(message = "A justificativa é obrigatória")
    @Size(min = 15, message = "A justificativa deve ter pelo menos 15 caracteres para auditoria")
    private String justificativa;
}
