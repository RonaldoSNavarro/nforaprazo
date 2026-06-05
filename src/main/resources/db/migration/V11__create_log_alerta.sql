-- V11: Cria tabela log_alerta para rastreabilidade de e-mails (RNF03 + RNF07)

CREATE TABLE log_alerta (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cte_id UUID NOT NULL,
    tipo_alerta VARCHAR(50) NOT NULL,
    destinatario VARCHAR(255) NOT NULL,
    assunto VARCHAR(500),
    data_envio TIMESTAMP NOT NULL DEFAULT NOW(),
    status_envio VARCHAR(20) NOT NULL DEFAULT 'ENVIADO',
    mensagem_erro TEXT,
    CONSTRAINT fk_log_alerta_cte FOREIGN KEY (cte_id) REFERENCES cte(id),
    CONSTRAINT ck_log_alerta_status CHECK (status_envio IN ('ENVIADO', 'ERRO'))
);

CREATE INDEX idx_log_alerta_cte_id ON log_alerta(cte_id);
CREATE INDEX idx_log_alerta_tipo ON log_alerta(tipo_alerta);
