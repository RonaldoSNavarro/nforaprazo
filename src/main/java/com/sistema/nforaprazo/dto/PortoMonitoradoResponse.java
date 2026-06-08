package com.sistema.nforaprazo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PortoMonitoradoResponse {
    private UUID id;
    private String nome;
    private boolean ativo;
    private String criadoPor;
    private LocalDateTime dataCriacao;
}
