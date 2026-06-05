-- V6: Refatoração para máquina de estados - Auto de Infração e Pagamento Sequencial

-- 1. Tabela Auto de Infração
CREATE TABLE auto_infracao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cte_id UUID NOT NULL UNIQUE,
    numero_auto VARCHAR(100),
    data_emissao DATE,
    data_vencimento DATE,
    valor_multa NUMERIC(15, 2),
    arquivo_pdf_auto VARCHAR(255),
    responsavel VARCHAR(30) DEFAULT 'PENDENTE',
    motivo_erro TEXT,
    numero_ticket VARCHAR(100),
    arquivo_ticket VARCHAR(255),
    data_registro TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_auto_infracao_cte FOREIGN KEY (cte_id) REFERENCES cte(id)
);

-- 2. Limpar dados existentes de pagamento (dev env) para evitar quebra de FK no rename
DELETE FROM pagamento_sefaz;

-- 3. Renomear pagamento_sefaz para pagamento e ajustar para apontar para auto_infracao
ALTER TABLE pagamento_sefaz RENAME TO pagamento;

ALTER TABLE pagamento DROP CONSTRAINT fk_pagamento_cte;
ALTER TABLE pagamento RENAME COLUMN cte_id TO auto_infracao_id;
ALTER TABLE pagamento ADD CONSTRAINT fk_pagamento_auto FOREIGN KEY (auto_infracao_id) REFERENCES auto_infracao(id);

-- 4. Ajustar colunas de pagamento para permitir o fluxo sequencial (nulls)
ALTER TABLE pagamento RENAME COLUMN valor_multa TO valor_pago;
ALTER TABLE pagamento ALTER COLUMN valor_pago DROP NOT NULL;
ALTER TABLE pagamento ALTER COLUMN data_pagamento DROP NOT NULL;
ALTER TABLE pagamento ALTER COLUMN path_dar_pdf DROP NOT NULL;
ALTER TABLE pagamento ALTER COLUMN path_comprovante_pdf DROP NOT NULL;

-- 5. Remover coluna de auto de infração que agora fica na tabela própria
ALTER TABLE pagamento DROP COLUMN path_auto_infracao_pdf;
