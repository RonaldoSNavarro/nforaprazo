-- V2: Insercao de usuarios de teste
-- Flyway migration - NAO ALTERAR apos aplicada
-- Senha para todos: Test@123
-- Hash gerado via BCryptPasswordEncoder (strength 10)
-- Se o login falhar, regenere o hash com PasswordHashGenerator.java

INSERT INTO usuarios (id, nome, email, senha, perfil) VALUES
(
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Operador Faturamento',
    'faturamento@alianca.com',
    '$2a$10$GzgerXQvOLATN3qZUqYxQu0INJ5ZPthZlsbwcDuj6KxUmInxnEL5e',
    'FATURAMENTO'
),
(
    'b2c3d4e5-f6a7-8901-bcde-f12345678901',
    'Operador Descarga',
    'descarga@alianca.com',
    '$2a$10$GzgerXQvOLATN3qZUqYxQu0INJ5ZPthZlsbwcDuj6KxUmInxnEL5e',
    'DESCARGA'
),
(
    'c3d4e5f6-a7b8-9012-cdef-123456789012',
    'Analista Docs Fiscal',
    'fiscal@alianca.com',
    '$2a$10$GzgerXQvOLATN3qZUqYxQu0INJ5ZPthZlsbwcDuj6KxUmInxnEL5e',
    'DOCS_FISCAL'
),
(
    'd4e5f6a7-b8c9-0123-defa-234567890123',
    'Gestor Operacional',
    'gestao@alianca.com',
    '$2a$10$GzgerXQvOLATN3qZUqYxQu0INJ5ZPthZlsbwcDuj6KxUmInxnEL5e',
    'GESTAO'
);
