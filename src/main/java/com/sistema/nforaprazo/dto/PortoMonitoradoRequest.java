package com.sistema.nforaprazo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class PortoMonitoradoRequest {
    
    private UUID id;

    @NotBlank(message = "O nome do porto é obrigatório")
    @Size(max = 100, message = "O nome do porto deve ter no máximo 100 caracteres")
    private String nome;

    private Boolean ativo = true;
}
