-- V9: Cria tabela nota_fiscal (1:N com cte)
-- Essencial para RN01: valor_multa = 10% do somatório dos valores de todas as NFs do CT-e

CREATE TABLE nota_fiscal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cte_id UUID NOT NULL,
    numero_nota VARCHAR(50) NOT NULL,
    valor_nota NUMERIC(15, 2) NOT NULL,
    data_emissao_nf DATE,
    data_registro TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_nota_fiscal_cte FOREIGN KEY (cte_id) REFERENCES cte(id)
);

CREATE INDEX idx_nota_fiscal_cte_id ON nota_fiscal(cte_id);
