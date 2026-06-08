-- V15: Exclui/Inativa usuarios de teste legados inseridos pela V2 antiga
-- Para nao violar a chave estrangeira (fk_cte_usuario) de CT-es ja criados por eles,
-- os registros sao anonimizados e inativados em vez de excluidos fisicamente.

UPDATE usuarios
SET email = 'removido_' || substring(id::text from 1 for 8) || '@sistema.local',
    nome = 'Usuario Removido',
    ativo = false
WHERE email IN (
    'faturamento@sistema.local',
    'descarga@sistema.local',
    'fiscal@sistema.local',
    'gestao@sistema.local',
    'operador.faturamento@sistema.local',
    'operador.descarga@sistema.local',
    'analista.fiscal@sistema.local',
    'gerente.operacional@sistema.local'
);

-- Insere o usuário Administrador padrão
INSERT INTO usuarios (id, nome, email, senha, perfil, ativo, alterar_senha) VALUES
(gen_random_uuid(), 'Administrador do Sistema', 'admin@sistema.local', '$2a$10$MHXPHbs/L3xSqQv8.oeMwujgyUqtk7wOCklhd1N9pD/FF4G2IIyfi', 'ADMINISTRADOR', true, false);
