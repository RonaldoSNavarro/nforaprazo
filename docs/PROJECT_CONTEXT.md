# PROJECT_CONTEXT.md — Sistema NF Fora do Prazo
> Status: 🟢 FASE 6 CONCLUÍDA
> Fase atual: **Fases Completas — Pronto para Homologação**
> Última atualização: 2026-06-08 | Atualizado por: PM Agent
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
com.sistema.nforaprazo
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
 
| Agente                      | Modelo                  | Responsabilidade                                        |
|-----------------------------|-------------------------|---------------------------------------------------------|
| CTO                         | Claude Opus 4.6         | Arquitetura, ADRs, sign-off de fase                     |
| Dev Sênior                  | Gemini 3.1 Pro          | Implementação Spring Boot/Thymeleaf                     |
| Code Review                 | Gemini 3.5 flash (high) | Revisão técnica antes do QA                             |
| UI/UX Designer              | Gemini 3.5 Flash        | Templates HTML/Tailwind                                 |
| QA                          | Gemini 3.5 flash (high) | Testes, bug reports, sign-off QA                        |
| Analista de Sistemas Sênior | Gemini 3.5 flash (high) | Análise e levantamento de requisitos, regras de negócio |
| Project Manager             | Gemini 3.5 Flash        | Documentação, atualização deste arquivo                 |

 
---
 
## 4. STATUS DAS FASES
 
```
[🟢] Fase 0 — Fundação         (Semanas 1-2)   → CONCLUÍDA
[🟢] Fase 1 — Upload CT-e      (Semanas 3-5)   → CONCLUÍDA
[🟢] Fase 2 — Desembaraço/Pgto (Semanas 6-9)   → CONCLUÍDA (Refatorada para Máquina de Estados)
[🟢] Fase 3 — Auto de Infração  (Semanas 10-11) → CONCLUÍDA (Investigação e Notificações)
[🟢] Fase 4 — Sem Auto + Nota   (Semanas 12-13) → CONCLUÍDA (Faturamento e DTO Validation)
[🟢] Fase 5 — Dashboard         (Semanas 14-16) → CONCLUÍDA
[🟢] Fase 6 — Go-Live           (Semanas 17-18) → CONCLUÍDA (Portos Dinâmicos e Refatoração Global)
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

### Entregáveis da Fase 2 (CONCLUÍDA)
- [x] V4__add_version_and_pagamento_table.sql — Optimistic Locking + pagamento_sefaz
- [x] PagamentoSefaz.java — entidade 1:1 com Cte
- [x] DescargaService.java — validação de status + upload triplo
- [x] DescargaController.java — pendentes + formulário pagamento
- [x] pendentes.html / pagamento.html — templates descarga

### Entregáveis da Fase 3 (CONCLUÍDA)
- [x] EmailService.java — envio assíncrono de alertas por e-mail + persistência de LogAlerta (RNF03)
- [x] DocsFiscalService.java — lógica de investigação (responsável, motivo, ticket)
- [x] DocsFiscalController.java — endpoints para iniciar/concluir investigação
- [x] pendentes.html (docs-fiscal) — lista de investigações pendentes e modal de preenchimento
- [x] @EnableAsync habilitado em NforaPrazoApplication.java

### Entregáveis da Fase 4 (CONCLUÍDA)
- [x] V12__create_nota_debito_and_enc_sem_auto.sql — migration para tabelas nota_debito e enc_sem_auto
- [x] V13__fix_pago_responsibility_for_test.sql — migration para correção de dados legados inconsistentes
- [x] V14__add_admin_and_log_atividade.sql — migration para logs de atividade, flag de alteração de senha e suporte ao perfil ADMINISTRADOR
- [x] EncSemAuto.java e NotaDebito.java — entidades JPA mapeadas
- [x] Request DTOs com validação Bean Validation (Size, NotBlank, NotNull)
- [x] FaturamentoService.java e FaturamentoController.java — fluxo de Notas de Débito e Absorção de Custos
- [x] Refatoração completa dos controladores para recebimento e validação via `@Valid DTO`
- [x] nota-debito.html — tela de faturamento / descarga/pendentes.html — modal de encerramento sem auto
- [x] Correção de links quebrados nos painéis de cards de home.html e layout.html

### Entregáveis da Fase 5 (CONCLUÍDA)
- [x] DashboardService.java e GestaoController.java — endpoints de controle e serviço
- [x] gestao/dashboard.html — painel visual de KPIs, evolução mensal e pizza de responsabilidades (Chart.js)
- [x] ExcelExportService.java — serviço para geração de relatórios .xlsx com filtros por porto e data
- [x] Criação de suíte de testes unitários automatizados em src/test/java validando regras fiscais críticas de multas, máquina de estados, roteamento, controle de acesso e auditoria (33 testes no total: CteTest, CteServiceTest, DescargaServiceTest, DocsFiscalServiceTest, ExcelExportServiceTest, AdminControllerTest, ForcarSenhaFiltroTest, AuditoriaAspectTest)
- [x] Painel Administrativo de Usuários — Cadastro de integrantes, atribuição de perfis, geração de senha provisória aleatória de 8 caracteres e e-mail automático.
- [x] Troca Obrigatória de Senha — Bloqueio e redirecionamento de URLs operacionais caso o usuário logado esteja com flag de alteração ativa.
- [x] Trilha de Auditoria via Spring AOP — Aspecto customizado @Auditable interceptando e persistindo ações e parâmetros de entrada (com ocultação de senhas) no banco.
- [x] Observabilidade de Saúde — Integração com o Spring Boot Actuator exposto nas rotas /actuator/health e /actuator/metrics protegidas via RBAC.

### Entregáveis da Fase 6 (CONCLUÍDA)
- [x] Criação da tabela `porto_monitorado` na base de dados (`V1__create_usuarios.sql`)
- [x] Nova entidade `PortoMonitorado.java` e repositório `PortoMonitoradoRepository.java`
- [x] CRUD e painel administrativo dinâmico de portos monitorados em `/admin/portos`
- [x] Substituição total das referências a "alianca" e "aliança" em todo o código-fonte, DTOs, migrations, pom.xml, Dockerfile, docker-compose.yml e templates HTML
- [x] Refatoração do pacote principal Java para `com.sistema.nforaprazo`
- [x] Correção e execução de toda a suíte de testes com 39 testes com sucesso
- [x] Reconstrução da imagem Docker e deploy bem-sucedido via Docker Compose com verificação de health check (status healthy)
- [x] Correção de vazamento de credenciais ocultando a senha de `Usuario.java` no `toString()` do Lombok
- [x] Remoção do método depreciado/hardcoded `isPortoMonitorado()` de `Cte.java` e atualização de testes associados
- [x] Otimização do filtro de segurança `ForcarSenhaFiltro.java` para cachear a flag `alterarSenha` em `CustomUserDetails`, reduzindo queries recorrentes no banco de dados

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

### ADR-004 — Acesso em Rede Local (LAN) para Uso Residencial
- **Status:** DECIDIDO (Aprovado pelo CTO e usuário)
- **Decisão:** Utilizar a porta padrão mapeada `8080:8080` vinculada a todas as interfaces (0.0.0.0) do host para acesso via IP da rede local.
- **Justificativa:** Solução sem custos adicionais de infraestrutura ou riscos de segurança associados à exposição à internet pública. Mapeamento padrão do Docker Compose já permite que outros dispositivos na LAN acessem a aplicação pelo IP do host.
- **Consequências:** Tráfego residencial trafegará via HTTP normal (sem SSL), o que é aceitável para ambiente local controlado. É recomendado configurar IP estático para o servidor host no roteador.

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
| Fase 2 | 2026-06-04 | Pagamento SEFAZ, Optimistic Locking, upload triplo, Máquina de Estados Refatorada |
| Fase 3 | 2026-06-05 | Investigação DOCS_FISCAL, Notificações EmailService, Logs de Alertas |
| Fase 4 | 2026-06-05 | Encerramento Sem Auto, Emissão de Notas de Débito, Validação Bean Validation com DTOs |
| Fase 5 | 2026-06-06 | Dashboard Executivo, Relatório Excel e Suíte Completa de Testes Automatizados |
| Fase 6 | 2026-06-08 | Portos Monitorados Dinâmicos, Remoção Global do nome "alianca", docker compose build |

---
 
## 8. PROBLEMAS CONHECIDOS / BLOQUEADORES
 
| ID     | Status   | Descrição | Responsável |
|--------|----------|-----------|-------------|
| BUG-01 | RESOLVIDO | Links da sidebar DESCARGA apontavam para rotas inexistentes (/cte/pendentes) | Dev |
| BUG-02 | RESOLVIDO | Restrição de check ck_cte_status no BD barrando novos status | QA/CTO |
 
---
 
## 9. AÇÕES ABERTAS
 
| Prioridade | Ação | Responsável |
|------------|------|-------------|
| Baixa      | Homologação e testes de faturamento com usuário real | Todos |
 
---
 
## 10. CONFIGURAÇÃO DE AMBIENTE
 
```yaml
# Dev: application.yml (rodando nativo)
spring.datasource.url: jdbc:postgresql://localhost:5432/nfora_prazo_dev

# Docker Compose: docker-compose.yml (acessando banco local da máquina host)
spring.datasource.url: jdbc:postgresql://host.docker.internal:5432/nfora_prazo_dev

# Variáveis de ambiente necessárias (.env ou variáveis do SO):
SPRING_DATASOURCE_URL, DB_USER, DB_PASSWORD, MAIL_HOST, MAIL_PORT, MAIL_USER, MAIL_PASS, FILE_UPLOAD_DIR
```
