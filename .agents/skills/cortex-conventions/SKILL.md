---
name: cortex-conventions
description: Regras globais de memória para o projeto Cortex. Envolve leitura de contexto no começo e escrita de descobertas/regras no final do trabalho usando Cortex MCP.
---

# Cortex Conventions (Skill Automática)

Você está trabalhando em um projeto governado pelo **Cortex**, um sistema de memória e wiki com base em Markdown.

## Regras de Execução de Tarefa

### 1. Inicialização (Read Context)
Antes de começar a editar arquivos ou fazer planos, use a ferramenta `query` para buscar conhecimento ativo relevante ao seu objetivo.
- Busque por páginas do tipo `rule` ou `gotcha` se achar que pode haver convenções específicas da base.

### 2. Ao Concluir (Capture Context)
No fim da sua tarefa, se você tropeçou em um bug não documentado (Gotcha), tomou uma decisão técnica arquitetural (Decision) ou descobriu um fato novo importante para agentes futuros (Fact), use a ferramenta `capture`.

### 3. Promoção de Regras
Se você criar uma regra ouro (`rule`) e consolidá-la usando `write_page`, lembre-se de promover a regra em seguida usando a tool `promote_rules`.

**Atenção:** Siga a filosofia de que o Cortex é o cérebro persistente. Documente decisões no momento em que ocorrerem.
