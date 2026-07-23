package com.sistema.nforaprazo.model.enums;

public enum Responsavel {
    SISTEMA("Interno/Empresa"),
    EMPRESA_INTERNO("Interno/Empresa"),
    CLIENTE("Cliente"),
    PENDENTE("Pendente");

    private final String descricao;

    Responsavel(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
