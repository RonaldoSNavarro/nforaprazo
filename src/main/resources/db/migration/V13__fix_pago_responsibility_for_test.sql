-- Migration V13: Fix PAGO auto_infracao responsibility for testing ND flow
UPDATE auto_infracao
SET responsavel = 'CLIENTE'
WHERE responsavel = 'PENDENTE' 
  AND cte_id IN (SELECT id FROM cte WHERE status = 'PAGO');
