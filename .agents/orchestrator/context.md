# Context — Sistema NF Fora do Prazo

This file tracks the active context, technology definitions, and configuration settings of the project.

## Key Information

- **Goal**: Establish the SDD documentation structure inside the `docs/` directory of the `nforaprazo` project.
- **Source Material**:
  - `docs/PROJECT_CONTEXT.md` (Tech stack, phase status, team table)
  - `docs/REQUIREMENTS.md` (Requirements table, schemas, enums, workflow paths)
  - `docs/skill_orquestracao.md` (Detailed agent definitions, workflow, and sequence diagram)
- **Target Files**:
  - `docs/agents.md` (Agent team, models, roles, orchestration workflow)
  - `docs/constitution.md` (Architecture, standards, stack, ADRs)
  - `docs/specs/core/spec.md` (Capability specs: models, enums, DB schema, use cases, workflows)

## Code & Environment Constraints
- NO modifications to code in `src/`.
- NO Git commits without explicit permission.
- The project runs on Java 25 LTS + Spring Boot 4.1.0 + Thymeleaf + PostgreSQL 16.
