-- V17: Adiciona campo quantidade_notas na tabela cte

ALTER TABLE cte ADD COLUMN IF NOT EXISTS quantidade_notas INT;
