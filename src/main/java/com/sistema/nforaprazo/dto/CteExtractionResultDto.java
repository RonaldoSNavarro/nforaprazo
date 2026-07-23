package com.sistema.nforaprazo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CteExtractionResultDto {
    private String chaveAcesso;
    private String numeroCte;
    private String tomadorNome;
    private String tomadorCnpj;
    private String navioViagemDirecao;
    private String numeroBooking;
    private String container;
    private Integer quantidadeNotas;
    private BigDecimal valorCarga;
    private String portoOrigem;
    private String portoDestino;
    private String observacoes;
}
