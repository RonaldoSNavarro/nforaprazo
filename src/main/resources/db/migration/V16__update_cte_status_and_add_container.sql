-- V16: Atualização da restrição de status do CT-e (inclui PENDENTE) e adição de campos de Container e Autorização de Custo

ALTER TABLE cte DROP CONSTRAINT IF EXISTS ck_cte_status;

ALTER TABLE cte ADD CONSTRAINT ck_cte_status CHECK (
    status IN (
        'PENDENTE',
        'REGISTRADO', 
        'AGUARDANDO_DESEMBARACO', 
        'DESEMBARACADO', 
        'AUTO_RECEBIDO', 
        'EM_INVESTIGACAO', 
        'AGUARDANDO_PAGAMENTO', 
        'PAGO', 
        'ENCERRADO_COM_AUTO', 
        'ENCERRADO_SEM_AUTO',
        'AUTUADO',
        'CANCELADO'
    )
);

UPDATE cte SET status = 'PENDENTE' WHERE status IN ('AGUARDANDO_DESEMBARACO', 'REGISTRADO');

ALTER TABLE cte ADD COLUMN IF NOT EXISTS container VARCHAR(11);
ALTER TABLE cte ADD COLUMN IF NOT EXISTS direcao VARCHAR(100);
ALTER TABLE cte ADD COLUMN IF NOT EXISTS arquivo_autorizacao_custo_path VARCHAR(255);
ALTER TABLE cte ADD COLUMN IF NOT EXISTS nome_original_autorizacao VARCHAR(255);
