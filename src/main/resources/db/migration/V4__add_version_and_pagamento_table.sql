-- V4: Adicionando Optimistic Locking ao CT-e e criando tabela PagamentoSefaz

ALTER TABLE cte ADD COLUMN versao INTEGER NOT NULL DEFAULT 0;

CREATE TABLE pagamento_sefaz (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cte_id UUID NOT NULL UNIQUE,
    valor_multa NUMERIC(15, 2) NOT NULL,
    data_pagamento DATE NOT NULL,
    
    path_dar_pdf VARCHAR(255) NOT NULL,
    path_comprovante_pdf VARCHAR(255) NOT NULL,
    path_capa_pdf VARCHAR(255),
    
    usuario_registro_id UUID NOT NULL,
    data_registro TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_pagamento_cte FOREIGN KEY (cte_id) REFERENCES cte(id),
    CONSTRAINT fk_pagamento_usuario FOREIGN KEY (usuario_registro_id) REFERENCES usuarios(id)
);
