# Plan — Documentation Setup for Spec Driven Development (SDD)

This plan outlines the steps to create the foundational SDD documentation for the `nforaprazo` project.

## Milestones

| # | Name | Scope | Output Artifacts | Status |
|---|------|-------|------------------|--------|
| 1 | Create agents.md | Extract team details and SDD orchestration workflow from PROJECT_CONTEXT.md and docs/skill_orquestracao.md | `docs/agents.md` | DONE |
| 2 | Create constitution.md | Establish tech stack, coding standards, and ADRs from PROJECT_CONTEXT.md and REQUIREMENTS.md | `docs/constitution.md` | DONE |
| 3 | Create spec.md | Mapping the core capabilities, existing modules, and database structure from requirements | `docs/specs/core/spec.md` | DONE |
| 4 | Review & Verification | Verify the correctness, compliance, and completeness of the documents | Verification Report | DONE |

## Detailed Steps

### Step 1: Initial Setup and Plan Creation
- [x] Read `ORIGINAL_REQUEST.md`, `PROJECT_CONTEXT.md`, and `REQUIREMENTS.md`.
- [x] Create `plan.md` and initial `progress.md` and `context.md` files.
- [x] Setup liveness heartbeat cron.

### Step 2: Create `docs/agents.md` (Milestone 1)
- [x] Dispatch a Worker agent to generate `docs/agents.md`.
- [x] Verify content (CTO, Analista de Sistemas Sênior, UI/UX Designer Pleno, Dev Full Stack Sênior, Analista de Code Review, QA Sênior, Gerente de Projetos models/responsibilities/prompts, Cerimônias Ágeis, Pipeline handoff diagram, Erros Comuns).

### Step 3: Create `docs/constitution.md` (Milestone 2)
- [x] Dispatch a Worker agent to generate `docs/constitution.md`.
- [x] Verify content (Technical Stack, Project Structure, ADR-001 through ADR-004, Coding Standards).

### Step 4: Create `docs/specs/core/spec.md` (Milestone 3)
- [x] Create directory `docs/specs/core/`.
- [x] Dispatch a Worker agent to generate the initial capability spec mapping.
- [x] Verify content (RF01-RF14, RNF01-RNF08, RN01-RN07, Entidades do Banco, Enums, Relacionamentos, Fluxos A & B, UC01-UC08).

### Step 5: Verification & Review (Milestone 4)
- [x] Dispatch Reviewer 1 to audit all created documentation (Conv ID: 9b924001-6c94-4220-895d-90f1e88b46e8 - APPROVED).
- [x] Dispatch Reviewer 2 to audit all created documentation (Conv ID: 81996713-e1c6-4909-8e8a-5c24d713f06a - APPROVED).

### Step 6: Handoff and Completion
- [ ] Write handoff.md.
- [ ] Notify Sentinel agent.
