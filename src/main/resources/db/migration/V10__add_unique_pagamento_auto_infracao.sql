-- V10: Adiciona constraint UNIQUE em pagamento.auto_infracao_id
-- Previne pagamento duplicado para o mesmo auto de infração (1:1)

ALTER TABLE pagamento ADD CONSTRAINT uq_pagamento_auto_infracao UNIQUE (auto_infracao_id);
