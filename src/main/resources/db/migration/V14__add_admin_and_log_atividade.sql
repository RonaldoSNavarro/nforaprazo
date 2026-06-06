-- V14: Adicao de Logs de Atividade, Senha Temporaria e Perfil Administrador

-- Alterar a check constraint de perfis para incluir ADMINISTRADOR
ALTER TABLE usuarios DROP CONSTRAINT ck_usuarios_perfil;
ALTER TABLE usuarios ADD CONSTRAINT ck_usuarios_perfil CHECK (
    perfil IN ('FATURAMENTO', 'DESCARGA', 'DOCS_FISCAL', 'GESTAO', 'ADMINISTRADOR')
);

-- Adicionar flag para exigir alteracao de senha
ALTER TABLE usuarios ADD COLUMN alterar_senha BOOLEAN NOT NULL DEFAULT FALSE;

-- Permitir que log_alerta tenha cte_id nulo para e-mails gerais (ex: senha provisória)
ALTER TABLE log_alerta ALTER COLUMN cte_id DROP NOT NULL;

-- Criar tabela de log de atividades de auditoria
CREATE TABLE log_atividade (
    id UUID PRIMARY KEY,
    usuario_email VARCHAR(255) NOT NULL,
    acao VARCHAR(100) NOT NULL,
    detalhes TEXT,
    ip_origem VARCHAR(50),
    data_criacao TIMESTAMP NOT NULL
);

-- Indexar os logs de atividades por e-mail do usuario e data
CREATE INDEX idx_log_atividade_email ON log_atividade(usuario_email);
CREATE INDEX idx_log_atividade_data ON log_atividade(data_criacao);
