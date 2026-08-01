package com.sistema.nforaprazo.dto;

import jakarta.validation.constraints.AssertTrue;
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

    private String navio;

    private String viagem;

    @NotBlank(message = "O porto de origem é obrigatório")
    private String portoOrigem;

    @NotBlank(message = "O porto de destino é obrigatório")
    private String portoDestino;

    @NotNull(message = "O valor da carga é obrigatório")
    @Positive(message = "O valor da carga deve ser maior que zero")
    private BigDecimal valorCarga;

    private String numeroBooking;

    private Integer quantidadeNotas;

    private String container;

    private String navioViagemDirecao;

    private String direcao;

    @AssertTrue(message = "Informe navio e viagem no formato Navio / Viagem / Direção")
    public boolean isNavioEViagemInformados() {
        if (navio != null && !navio.isBlank() && viagem != null && !viagem.isBlank()) {
            return true;
        }

        if (navioViagemDirecao == null || navioViagemDirecao.isBlank()) {
            return false;
        }

        String[] partes = navioViagemDirecao.split("/", 3);
        return partes.length >= 2 && !partes[0].isBlank() && !partes[1].isBlank();
    }

    @NotNull(message = "O arquivo de autorização de aceitação de custo é obrigatório")
    private MultipartFile arquivoAutorizacaoCusto;
}
