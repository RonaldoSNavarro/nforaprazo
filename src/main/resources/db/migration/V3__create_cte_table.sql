-- V3: Criacao da tabela cte
CREATE TABLE cte (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chave_acesso VARCHAR(44) UNIQUE,
    valor_carga NUMERIC(15, 2),
    status VARCHAR(30) NOT NULL,
    arquivo_pdf_path VARCHAR(255) NOT NULL,
    nome_original_arquivo VARCHAR(255) NOT NULL,
    usuario_upload_id UUID NOT NULL,
    data_upload TIMESTAMP NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_cte_usuario FOREIGN KEY (usuario_upload_id) REFERENCES usuarios(id),
    CONSTRAINT ck_cte_status CHECK (
        status IN ('PENDENTE_DESCARGA', 'DESEMBARACADO', 'AUTUADO', 'CANCELADO')
    )
);

CREATE INDEX idx_cte_status ON cte(status);
CREATE INDEX idx_cte_chave_acesso ON cte(chave_acesso);
