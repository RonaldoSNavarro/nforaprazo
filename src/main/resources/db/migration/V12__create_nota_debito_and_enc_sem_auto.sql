-- Migration V12: create enc_sem_auto and nota_debito tables
CREATE TABLE enc_sem_auto (
    id UUID PRIMARY KEY,
    cte_id UUID NOT NULL UNIQUE,
    valor_potencial_multa NUMERIC(15, 2) NOT NULL,
    justificativa TEXT NOT NULL,
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id UUID NOT NULL,
    CONSTRAINT fk_enc_sem_auto_cte FOREIGN KEY (cte_id) REFERENCES cte(id) ON DELETE CASCADE,
    CONSTRAINT fk_enc_sem_auto_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE nota_debito (
    id UUID PRIMARY KEY,
    cte_id UUID NOT NULL UNIQUE,
    pagamento_id UUID NOT NULL UNIQUE,
    numero_nota_debito VARCHAR(100) NOT NULL,
    valor_nota_debito NUMERIC(15, 2) NOT NULL,
    data_emissao DATE NOT NULL,
    arquivo_pdf VARCHAR(255) NOT NULL,
    data_envio_cliente TIMESTAMP,
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nota_debito_cte FOREIGN KEY (cte_id) REFERENCES cte(id) ON DELETE CASCADE,
    CONSTRAINT fk_nota_debito_pagamento FOREIGN KEY (pagamento_id) REFERENCES pagamento(id) ON DELETE CASCADE
);
