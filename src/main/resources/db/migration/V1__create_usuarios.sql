-- V1: Criacao da tabela de usuarios
-- Flyway migration - NAO ALTERAR apos aplicada

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_perfil CHECK (
        perfil IN ('FATURAMENTO', 'DESCARGA', 'DOCS_FISCAL', 'GESTAO')
    )
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_perfil ON usuarios(perfil);
