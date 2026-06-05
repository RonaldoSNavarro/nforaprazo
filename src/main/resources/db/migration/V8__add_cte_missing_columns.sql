-- V8: Adiciona colunas faltantes na tabela cte conforme REQUIREMENTS.md §4
-- Campos essenciais extraídos das entrevistas: tomador, navio, portos, booking

ALTER TABLE cte ADD COLUMN numero_cte VARCHAR(50);
ALTER TABLE cte ADD COLUMN tomador_nome VARCHAR(255);
ALTER TABLE cte ADD COLUMN tomador_cnpj VARCHAR(18);
ALTER TABLE cte ADD COLUMN navio VARCHAR(255);
ALTER TABLE cte ADD COLUMN viagem VARCHAR(100);
ALTER TABLE cte ADD COLUMN porto_origem VARCHAR(255);
ALTER TABLE cte ADD COLUMN porto_destino VARCHAR(255);
ALTER TABLE cte ADD COLUMN numero_booking VARCHAR(100);

-- Índice para consultas frequentes por porto de destino (roteamento de alerta RN05)
CREATE INDEX idx_cte_porto_destino ON cte(porto_destino);

-- Índice para consultas por tomador (dashboard de reincidentes)
CREATE INDEX idx_cte_tomador_cnpj ON cte(tomador_cnpj);
