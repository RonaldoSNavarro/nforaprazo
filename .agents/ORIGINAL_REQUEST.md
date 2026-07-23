# Original User Request

## Initial Request — 2026-07-04T21:38:28Z

# Teamwork Project Prompt — Draft

> Status: Launched
> Goal: Craft prompt → get user approval → delegate to teamwork_preview

Create the foundational documentation for Spec Driven Development (SDD) in the `nforaprazo` project, extracting knowledge from `docs/PROJECT_CONTEXT.md`.

Working directory: f:/Dev/Projetos/nforaprazo
Integrity mode: development

## Requirements

### R1. Define Agents (agents.md)
Create `docs/agents.md` outlining the team of agents, their models, and responsibilities, as well as the SDD orchestration workflow, directly based on Section 3 of `PROJECT_CONTEXT.md`.

### R2. Establish Constitution (constitution.md)
Create `docs/constitution.md` to solidify the technical stack, architectural decisions (ADRs), and coding standards from `PROJECT_CONTEXT.md`. 

### R3. Draft Initial Specs
Create the `docs/specs/` directory and an initial capability specification (e.g., `docs/specs/core/spec.md`) mapping the existing modules and workflows described in the project history.

## Acceptance Criteria

### Verification
- [ ] `docs/agents.md` is created and accurately reflects the team structure.
- [ ] `docs/constitution.md` is created and contains the coding standards and stack rules.
- [ ] At least one `spec.md` is created under `docs/specs/`.
- [ ] No existing code in `src/` is modified (documentation-only task).
