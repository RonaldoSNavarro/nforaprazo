## 2026-07-04T21:39:40Z
Create the following three foundational documentation files under the f:\Dev\Projetos\nforaprazo\docs\ directory:

1. docs/agents.md
Outlining the team of agents, their models, responsibilities, and the SDD orchestration workflow.
- Source material: Section 3 of f:\Dev\Projetos\nforaprazo\docs\PROJECT_CONTEXT.md and the detailed content of f:\Dev\Projetos\nforaprazo\docs\skill_orquestracao.md.
- Ensure you include:
  - The team of agents table (CTO, Analista de Sistemas Sênior, UI/UX Designer Pleno, Dev Full Stack Sênior, Analista de Code Review, QA Sênior, Gerente de Projetos).
  - Verbatim or detailed prompts of acting for each agent from docs/skill_orquestracao.md.
  - Cerimônias Ágeis Obrigatórias (Briefing/Daily, Sprint Review, Retrospective).
  - The mermaid flow diagram for the "Fluxo de Trabalho (Pipeline Handoff)".
  - "Erros Comuns a Evitar".

2. docs/constitution.md
Solidifying the technical stack, project architecture, ADRs, and coding standards.
- Source material: Sections 1, 2, 5, and 6 of f:\Dev\Projetos\nforaprazo\docs\PROJECT_CONTEXT.md.
- Ensure you include:
  - Technical Stack (definitive).
  - Project Structure (com.sistema.nforaprazo).
  - Technical Decisions (ADR-001, ADR-002, ADR-003, ADR-004).
  - Coding Standards (Regras 1-5, Nomenclatura).

3. docs/specs/core/spec.md
Creating the docs/specs/core/ directory if it does not exist, and drafting the initial capability specification.
- Source material: f:\Dev\Projetos\nforaprazo\docs\REQUIREMENTS.md.
- Ensure you include:
  - Requisitos Funcionais table (RF01 to RF14).
  - Requisitos Não-Funcionais table (RNF01 to RNF08).
  - Regras de Negócio (RN01 to RN07).
  - Entidades do Banco (schema compacto), Enums (PerfilUsuario, StatusCTe, etc.), and Relacionamentos.
  - Fluxos de Estado (Fluxo A: Com auto de infração, Fluxo B: Sem auto de infração).
  - Casos de Uso (UC01 to UC08).

Important Constraints:
- DO NOT edit or modify any files in src/. This is a documentation-only task.
- DO NOT commit to git.
- Write progress.md and handoff.md inside f:\Dev\Projetos\nforaprazo\.agents\worker_documentation_1\.
- Regularly update progress.md to show your liveness heartbeat.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.
