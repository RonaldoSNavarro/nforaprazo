package com.alianca.nforaprazo.dto;

import jakarta.validation.constraints.NotNull;
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
public class DarRequest {

    @NotNull(message = "O ID do CT-e é obrigatório")
    private UUID cteId;

    @NotNull(message = "O arquivo PDF do DAR é obrigatório")
    private MultipartFile darPdf;
}
