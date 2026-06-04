# PROJECT_CONTEXT.md — Sistema NF Fora do Prazo
> Status: 🟡 EM ANDAMENTO
> Fase atual: **FASE 2 — Desembaraço/Pagamento concluída, aguardando validação**
> Última atualização: 2026-06-04 | Atualizado por: PM Agent
> Para requisitos detalhados, consulte: REQUIREMENTS.md
 
---
 
## 1. STACK TÉCNICA (DEFINITIVA — não alterar sem ADR)
 
```
Backend:    Java 21 + Spring Boot 3.x + Maven
Frontend:   Thymeleaf + Tailwind CSS (CDN) + Chart.js (CDN)
Banco:      PostgreSQL 16
Migrations: Flyway (imutáveis após aplicadas)
Segurança:  Spring Security (BCrypt, perfis por URL)
E-mail:     Spring Mail / JavaMailSender
PDF read:   Apache PDFBox 3.x
Export:     Apache POI (Excel)
Deploy:     VPS Ubuntu 24 + Docker + Nginx (produção)
```
 
---
 
## 2. ESTRUTURA DO PROJETO
 
```
com.alianca.nforaprazo
├── config/       SecurityConfig, MailConfig, FileStorageConfig
├── controller/   Um por recurso — sem lógica de negócio
├── service/      Toda a lógica de negócio aqui
├── repository/   Interfaces JPA — sem lógica
├── model/        Entidades JPA + enums
├── dto/          Request/Response DTOs — entidades não expostas na web
└── exception/    GlobalExceptionHandler
```
 
---

## 3. TIME DE AGENTES
 
| Agente         | Modelo                | Responsabilidade |
|----------------|-----------------------|------------------|
| CTO            | Claude Opus 4.6       | Arquitetura, ADRs, sign-off de fase |
| Dev Sênior     | Gemini 2.5 Pro        | Implementação Spring Boot/Thymeleaf |
| Code Review    | Gemini 2.5 Pro (high) | Revisão técnica antes do QA |
| UI/UX Designer | Gemini 2.5 Flash      | Templates HTML/Tailwind |
| QA             | Gemini 2.5 Pro (high) | Testes, bug reports, sign-off QA |
| PM             | Gemini 2.5 Flash      | Documentação, atualização deste arquivo |
 
---
 
## 4. STATUS DAS FASES
 
```
[🟢] Fase 0 — Fundação         (Semanas 1-2)   → CONCLUÍDA
[🟢] Fase 1 — Upload CT-e      (Semanas 3-5)   → CONCLUÍDA (upload + extração PDFBox + listagem)
[🟡] Fase 2 — Desembaraço/Pgto (Semanas 6-9)   → IMPLEMENTADA, EM VALIDAÇÃO
[ ] Fase 3 — Auto de Infração  (Semanas 10-11) → não iniciada
[ ] Fase 4 — Sem Auto + Nota   (Semanas 12-13) → não iniciada
[ ] Fase 5 — Dashboard         (Semanas 14-16) → não iniciada
[ ] Fase 6 — Go-Live           (Semanas 17-18) → não iniciada
```
 
### Entregáveis da Fase 0 (CONCLUÍDA)
- [x] V1__create_usuarios.sql — migration criada
- [x] Usuario.java — entidade com enum PerfilUsuario
- [x] UsuarioRepository.java — findByEmail()
- [x] SecurityConfig.java — perfis e BCrypt
- [x] CustomUserDetailsService.java
- [x] V2__insert_usuarios_teste.sql — 4 usuários de teste
- [x] layout.html — navbar por perfil (sec:authorize)
- [x] login.html — tela de login Tailwind
- [x] home.html — cards por perfil

### Entregáveis da Fase 1 (CONCLUÍDA)
- [x] V3__create_cte_table.sql — tabela cte com FK para usuarios
- [x] StatusCte.java — enum de máquina de estado
- [x] Cte.java — entidade com UUID, chave_acesso, @Version
- [x] CteRepository.java — findByStatus, existsByChaveAcesso
- [x] StorageService.java — upload com validação Magic Number + UUID
- [x] PdfExtractionService.java — extração de chave via PDFBox
- [x] CteService.java — orquestrador transacional
- [x] CteController.java — upload e listagem
- [x] upload.html / lista.html — templates

### Entregáveis da Fase 2 (EM VALIDAÇÃO)
- [x] V4__add_version_and_pagamento_table.sql — Optimistic Locking + pagamento_sefaz
- [x] PagamentoSefaz.java — entidade 1:1 com Cte
- [x] DescargaService.java — validação de status + upload triplo
- [x] DescargaController.java — pendentes + formulário pagamento
- [x] pendentes.html / pagamento.html — templates descarga
 
---
 
## 5. DECISÕES TÉCNICAS (ADRs)
 
### ADR-001 — Extração de dados do CT-e PDF
- **Status:** DECIDIDO
- **Decisão:** Opção C — Formulário manual como MVP, com extração parcial (chave de acesso 44 dígitos via regex PDFBox)
- **Justificativa:** Layouts de CT-e variam muito entre emissores. Extração manual não trava a entrega.
 
### ADR-002 — Armazenamento de PDFs
- **Status:** DECIDIDO
- **Decisão:** Sistema de arquivos local com UUID como nome do arquivo
- **Migração futura:** S3/MinIO com troca de implementação de FileStorageService
 
### ADR-003 — Optimistic Locking para eventos financeiros
- **Status:** DECIDIDO
- **Decisão:** Coluna `versao` com @Version no Hibernate para prevenir Race Conditions em pagamentos
- **Justificativa:** Impede duplo-submit e pagamentos duplicados sem custo de lock pessimista

---
 
## 6. PADRÕES DE CÓDIGO DEFINIDOS
 
```
Regra #1: Lógica de negócio APENAS no @Service — nunca no @Controller
Regra #2: Credenciais via variáveis de ambiente (${variavel}) — nunca hardcoded
Regra #3: Migrations Flyway são IMUTÁVEIS — sempre criar V{n+1}__
Regra #4: DTOs entre Controller e Service — entidades JPA não chegam à camada web
Regra #5: Exceptions no GlobalExceptionHandler — sem catch silencioso
Nomenclatura: Classes PascalCase PT-BR | Métodos camelCase PT-BR | Migrations V{n}__snake_case.sql
```
 
---
 
## 7. HISTÓRICO DE FASES CONCLUÍDAS
 
| Fase | Data Conclusão | Entregáveis |
|------|---------------|-------------|
| Fase 0 | 2026-06-02 | Autenticação, RBAC, layout base, migrations V1-V2 |
| Fase 1 | 2026-06-02 | Upload CT-e, PDFBox, StorageService, listagem paginada |
| Fase 2 | 2026-06-02 | Pagamento SEFAZ, Optimistic Locking, upload triplo |
 
---
 
## 8. PROBLEMAS CONHECIDOS / BLOQUEADORES
 
| ID     | Status   | Descrição | Responsável |
|--------|----------|-----------|-------------|
| BUG-01 | RESOLVIDO | Links da sidebar DESCARGA apontavam para rotas inexistentes (/cte/pendentes) | Dev |
 
---
 
## 9. AÇÕES ABERTAS
 
| Prioridade | Ação | Responsável |
|------------|------|-------------|
| Alta       | Validar fluxo completo Fase 2 após fix de links | QA |
| Alta       | Implementar Fase 3 — Auto de Infração e Investigação | Dev |
| Média      | Alinhar modelo de dados com REQUIREMENTS.md (StatusCte expandido) | CTO/Dev |
 
---
 
## 10. CONFIGURAÇÃO DE AMBIENTE
 
```yaml
# Dev: application.yml
spring.datasource.url: jdbc:postgresql://localhost:5432/nfora_prazo_dev
spring.mail.host: sandbox.smtp.mailtrap.io  ← usar Mailtrap em dev
 
# Variáveis de ambiente necessárias:
DB_USER, DB_PASSWORD, MAIL_HOST, MAIL_PORT, MAIL_USER, MAIL_PASS, FILE_UPLOAD_DIR
```
