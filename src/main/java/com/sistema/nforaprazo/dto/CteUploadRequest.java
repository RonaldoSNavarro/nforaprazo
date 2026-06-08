package com.sistema.nforaprazo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CteUploadRequest {

    @NotNull(message = "O arquivo PDF do CT-e é obrigatório")
    private MultipartFile arquivoCte;

    @NotBlank(message = "O número do CT-e é obrigatório")
    private String numeroCte;

    @NotBlank(message = "O nome do tomador é obrigatório")
    private String tomadorNome;

    @NotBlank(message = "O CNPJ do tomador é obrigatório")
    private String tomadorCnpj;

    @NotBlank(message = "O nome do navio é obrigatório")
    private String navio;

    @NotBlank(message = "A viagem é obrigatória")
    private String viagem;

    @NotBlank(message = "O porto de origem é obrigatório")
    private String portoOrigem;

    @NotBlank(message = "O porto de destino é obrigatório")
    private String portoDestino;

    @NotNull(message = "O valor da carga é obrigatório")
    @Positive(message = "O valor da carga deve ser maior que zero")
    private BigDecimal valorCarga;

    @NotBlank(message = "O número do booking é obrigatório")
    private String numeroBooking;
}
