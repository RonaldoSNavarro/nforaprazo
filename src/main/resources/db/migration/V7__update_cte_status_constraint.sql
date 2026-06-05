-- V7: Atualização da restrição de status do CT-e para a nova máquina de estados

ALTER TABLE cte DROP CONSTRAINT ck_cte_status;

ALTER TABLE cte ADD CONSTRAINT ck_cte_status CHECK (
    status IN (
        'REGISTRADO', 
        'AGUARDANDO_DESEMBARACO', 
        'DESEMBARACADO', 
        'AUTO_RECEBIDO', 
        'EM_INVESTIGACAO', 
        'AGUARDANDO_PAGAMENTO', 
        'PAGO', 
        'ENCERRADO_COM_AUTO', 
        'ENCERRADO_SEM_AUTO',
        'PENDENTE_DESCARGA', -- Mantido temporariamente para compatibilidade com registros antigos no dev
        'AUTUADO',
        'CANCELADO'
    )
);

-- Atualiza eventuais registros antigos de PENDENTE_DESCARGA para AGUARDANDO_DESEMBARACO
UPDATE cte SET status = 'AGUARDANDO_DESEMBARACO' WHERE status = 'PENDENTE_DESCARGA';
