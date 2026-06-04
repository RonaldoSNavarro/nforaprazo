-- V5: Adicionar campo para Auto de Infração no pagamento
ALTER TABLE pagamento_sefaz ADD COLUMN path_auto_infracao_pdf VARCHAR(255) NOT NULL DEFAULT '';
