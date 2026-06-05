import { useState } from "react";
 
// ── COPY BUTTON ───────────────────────────────────────────────────────────────
const CopyBtn = ({ text, label = "⧉ Copiar" }) => {
  const [copied, setCopied] = useState(false);
  const copy = () => {
    navigator.clipboard.writeText(text).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };
  return (
    <button onClick={copy}
      className={`flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-md font-semibold transition-all shrink-0 ${
        copied ? "bg-emerald-600 text-white" : "bg-slate-700 text-slate-300 hover:bg-amber-500 hover:text-slate-900"}`}>
      {copied ? "✓ Copiado!" : label}
    </button>
  );
};
 
const Pre = ({ text }) => (
  <pre className="text-xs text-slate-300 bg-slate-950 rounded-lg p-4 overflow-auto max-h-[420px] leading-relaxed whitespace-pre-wrap border border-slate-800">{text}</pre>
);
 
// ── PALETA ────────────────────────────────────────────────────────────────────
const roleColors = {
  cto:     { ring: "border-amber-600/60",  dot: "bg-amber-500",   badge: "bg-amber-900/40 text-amber-300 border-amber-700/50",    label: "text-amber-400" },
  dev:     { ring: "border-blue-600/60",   dot: "bg-blue-500",    badge: "bg-blue-900/40 text-blue-300 border-blue-700/50",       label: "text-blue-400" },
  cr:      { ring: "border-rose-600/60",   dot: "bg-rose-500",    badge: "bg-rose-900/40 text-rose-300 border-rose-700/50",       label: "text-rose-400" },
  designer:{ ring: "border-purple-600/60", dot: "bg-purple-500",  badge: "bg-purple-900/40 text-purple-300 border-purple-700/50", label: "text-purple-400" },
  qa:      { ring: "border-green-600/60",  dot: "bg-green-500",   badge: "bg-green-900/40 text-green-300 border-green-700/50",    label: "text-green-400" },
  pm:      { ring: "border-cyan-600/60",   dot: "bg-cyan-500",    badge: "bg-cyan-900/40 text-cyan-300 border-cyan-700/50",       label: "text-cyan-400" },
};
 
// ── CONTEÚDO DOS ARQUIVOS .MD ─────────────────────────────────────────────────
 
const REQUIREMENTS_MD = `# REQUIREMENTS.md — Sistema NF Fora do Prazo
> Versão: 1.1 | Mantido por: PM Agent | Consulte este arquivo antes de qualquer implementação
 
---
 
## 1. REQUISITOS FUNCIONAIS
 
| ID    | Área           | Prioridade | Descrição |
|-------|----------------|------------|-----------|
| RF01  | Upload         | Alta       | Upload de CT-e PDF pelo perfil FATURAMENTO |
| RF02  | Extração       | Alta       | Extração automática dos dados do PDF: tomador, chave CT-e, navio, viagem, portos, valor, booking, NFs |
| RF03  | Roteamento     | Alta       | Identificar porto de destino e aplicar regra: Manaus/Vila do Conde/Pecém → alerta; demais → apenas registrar |
| RF04  | Notificação    | Alta       | E-mails automáticos por Spring Mail em cada transição de status relevante |
| RF05  | Auto Infração  | Alta       | Upload do PDF do auto de infração vinculado ao CT-e com data emissão e vencimento |
| RF06  | Auto Infração  | Alta       | Alerta automático ao DOCS_FISCAL após registro do auto |
| RF07  | Investigação   | Alta       | Campos para DOCS_FISCAL: responsável (ALIANCA/CLIENTE), motivo, ticket/evidência — BLOQUEANTE para avançar |
| RF08  | Pagamento      | Alta       | Upload de DAR + comprovante + capa; registro do valor efetivamente pago |
| RF09  | Pagamento      | Alta       | Alerta ao FATURAMENTO após pagamento confirmado |
| RF10  | Encerramento   | Alta       | FATURAMENTO registra nota de débito → status ENCERRADO_COM_AUTO |
| RF11  | Encerramento   | Alta       | Fluxo "sem auto": calcula 10% das NFs, exige justificativa, → ENCERRADO_SEM_AUTO |
| RF12  | BI / Dashboard | Alta       | Dashboard: KPIs (exposição, multas pagas/evitadas), gráfico mensal, pizza responsabilidade, reincidentes |
| RF13  | Relatórios     | Média      | Exportação para Excel (Apache POI) com filtros por período e porto |
| RF14  | Cobertura      | Alta       | Registrar CT-es de todos os portos do Brasil; fluxo diferenciado por destino |
 
---
 
## 2. REQUISITOS NÃO-FUNCIONAIS
 
| ID    | Categoria      | Descrição |
|-------|----------------|-----------|
| RNF01 | Interface      | Aplicação web acessível via browser (Thymeleaf + Tailwind) |
| RNF02 | Migração       | Substituir completamente as 3 planilhas de controle |
| RNF03 | Comunicação    | Alertas via e-mail com rastreabilidade em LogAlerta |
| RNF04 | Integração     | Sem integração com portal SEFAZ-AM — processo de pagamento é manual/externo |
| RNF05 | Armazenamento  | PDFs armazenados em disco (MVP) → S3/MinIO (produção) |
| RNF06 | Segurança      | Controle de acesso por perfil via Spring Security |
| RNF07 | Auditoria      | Todas as ações rastreadas com timestamp e usuário |
| RNF08 | Usabilidade    | Dashboard legível para apresentação executiva |
 
---
 
## 3. REGRAS DE NEGÓCIO
 
\`\`\`
RN01: valor_multa = 10% do SOMATÓRIO dos valores de todas as NFs do CT-e
RN02: valor_nota_debito = valor_pago (NÃO valor_multa) — pode ter desconto por antecipação
RN03: responsavel = ALIANCA → empresa absorve | responsavel = CLIENTE → emite nota de débito
RN04: Nota de débito SOMENTE pode ser emitida após arquivo_comprovante estar preenchido em Pagamento
RN05: Portos que disparam alerta DESCARGA: Manaus, Vila do Conde/PCM, Pecém
      Demais portos: apenas registrar, sem alerta (desembaraço é responsabilidade do cliente)
RN06: Mesmo sem auto de infração → calcular e salvar valor_potencial_multa (10% das NFs)
RN07: Encerramento sem auto EXIGE justificativa.length > 0 (campo NOT NULL no banco)
\`\`\`
 
---
 
## 4. ENTIDADES DO BANCO (schema compacto)
 
\`\`\`sql
Usuario          → id(UUID) | nome | email(UNIQUE) | senha(BCrypt) | perfil(ENUM) | ativo(BOOL)
CTeInclusao      → id | numero_cte | chave_cte(UNIQUE,44) | tomador_nome | tomador_cnpj | navio
                   viagem | porto_origem | porto_destino | valor_cte | numero_booking
                   status(ENUM) | arquivo_pdf_cte | data_criacao | usuario_id(FK)
NotaFiscal       → id | cte_inclusao_id(FK) | numero_nota | valor_nota | data_emissao_nf
AutoInfracao     → id | cte_inclusao_id(FK) | numero_auto | data_emissao | data_vencimento
                   valor_multa | arquivo_pdf_auto | responsavel(ENUM) | motivo_erro
                   numero_ticket | arquivo_ticket
Pagamento        → id | auto_infracao_id(FK) | valor_pago | data_pagamento
                   arquivo_dar | arquivo_comprovante | arquivo_capa | data_envio_para_pagamento
NotaDebito       → id | cte_inclusao_id(FK) | pagamento_id(FK) | numero_nota_debito
                   valor_nota_debito | data_emissao | arquivo_pdf | data_envio_cliente
EncSeMAuto       → id | cte_inclusao_id(FK) | valor_potencial_multa | justificativa(NOT NULL)
                   data_registro | usuario_id(FK)
LogAlerta        → id | cte_inclusao_id(FK) | tipo_alerta | destinatario | data_envio | status_envio(ENUM)
\`\`\`
 
### Enums
\`\`\`
PerfilUsuario:  FATURAMENTO | DESCARGA | DOCS_FISCAL | GESTAO
StatusCTe:      REGISTRADO | AGUARDANDO_DESEMBARACO | AUTO_RECEBIDO | EM_INVESTIGACAO
                AGUARDANDO_PAGAMENTO | PAGO | ENCERRADO_COM_AUTO | ENCERRADO_SEM_AUTO
Responsavel:    ALIANCA | CLIENTE | PENDENTE
StatusEnvio:    ENVIADO | ERRO
\`\`\`
 
### Relacionamentos
\`\`\`
CTeInclusao  1──N  NotaFiscal
CTeInclusao  1──01 AutoInfracao
AutoInfracao 1──01 Pagamento
Pagamento    1──01 NotaDebito
CTeInclusao  1──01 EncSeMAuto
CTeInclusao  1──N  LogAlerta
Usuario      1──N  CTeInclusao
\`\`\`
 
---
 
## 5. FLUXOS DE ESTADO
 
### Fluxo A — Com auto de infração
\`\`\`
FATURAMENTO: upload CT-e → REGISTRADO
SISTEMA: se porto monitorado → e-mail DESCARGA → AGUARDANDO_DESEMBARACO
FISCAL: upload auto → AUTO_RECEBIDO → e-mail DOCS_FISCAL
DOCS: define responsável + motivo + ticket → EM_INVESTIGACAO → AGUARDANDO_PAGAMENTO → e-mail DESCARGA
DESCARGA: paga (externo) + upload DAR+comprovante+capa → PAGO → e-mail FATURAMENTO
FATURAMENTO: se CLIENTE → nota de débito → ENCERRADO_COM_AUTO
             se ALIANCA → ENCERRADO_COM_AUTO (sem nota de débito)
\`\`\`
 
### Fluxo B — Sem auto de infração
\`\`\`
(mesmos passos 1-3 do Fluxo A)
DESCARGA: seleciona "sem auto" → sistema calcula 10% NFs → justificativa → ENCERRADO_SEM_AUTO
\`\`\`
 
---
 
## 6. CASOS DE USO (resumo)
 
| ID   | Ator         | Ação |
|------|--------------|------|
| UC01 | FATURAMENTO  | Upload CT-e + revisão dados extraídos |
| UC02 | SISTEMA      | Roteamento automático por porto de destino |
| UC03 | FISCAL/DOCS  | Registro do auto de infração |
| UC04 | DOCS         | Investigação de responsabilidade |
| UC05 | DESCARGA     | Registro de pagamento (DAR + comprovante + capa) |
| UC06 | FATURAMENTO  | Emissão de nota de débito ao cliente |
| UC07 | DESCARGA     | Encerramento sem auto de infração |
| UC08 | GESTAO       | Visualização do dashboard e relatórios |
`;
 
const PROJECT_CONTEXT_MD = `# PROJECT_CONTEXT.md — Sistema NF Fora do Prazo
> Status: 🟡 EM ANDAMENTO
> Fase atual: **FASE 0 — QA em execução**
> Última atualização: [PREENCHER DATA] | Atualizado por: PM Agent
> Para requisitos detalhados, consulte: REQUIREMENTS.md
 
---
 
## 1. STACK TÉCNICA (DEFINITIVA — não alterar sem ADR)
 
\`\`\`
Backend:    Java 21 + Spring Boot 3.x + Maven
Frontend:   Thymeleaf + Tailwind CSS (CDN) + Chart.js (CDN)
Banco:      PostgreSQL 16
Migrations: Flyway (imutáveis após aplicadas)
Segurança:  Spring Security (BCrypt, perfis por URL)
E-mail:     Spring Mail / JavaMailSender
PDF read:   Apache PDFBox 3.x
Export:     Apache POI (Excel)
Deploy:     VPS Ubuntu 24 + Docker + Nginx (produção)
\`\`\`
 
---
 
## 2. ESTRUTURA DO PROJETO
 
\`\`\`
com.alianca.nforaprazo
├── config/       SecurityConfig, MailConfig, FileStorageConfig
├── controller/   Um por recurso — sem lógica de negócio
├── service/      Toda a lógica de negócio aqui
├── repository/   Interfaces JPA — sem lógica
├── model/        Entidades JPA + enums
├── dto/          Request/Response DTOs — entidades não expostas na web
└── exception/    GlobalExceptionHandler
\`\`\`
 
---



## 3. TIME DE AGENTES
 
| Agente         | Modelo                | Responsabilidade |
|----------------|-----------------------|------------------|
| CTO            | OPUS 4.6(thinkin)     | Arquitetura, ADRs, sign-off de fase |
| Dev Sênior     | Gemini 3.1 Pro (high) | Implementação Spring Boot/Thymeleaf |
| Code Review    | Gemini 3.5 flash(high)| Revisão técnica antes do QA |
| UI/UX Designer | Gemini 3.5 Flash      | Templates HTML/Tailwind |
| QA             | Gemini 3.5 flash(high)| Testes, bug reports, sign-off QA |
| PM             | Gemini 3.5 Flash(medium)| Documentação, atualização deste arquivo |
 
---
 
## 4. STATUS DAS FASES
 
\`\`\`
[🟢] Fase 0 — Fundação         (Semanas 1-2)   → IMPLEMENTAÇÃO CONCLUÍDA, QA EM ANDAMENTO
[ ] Fase 1 — Upload CT-e       (Semanas 3-5)   → aguardando sign-off Fase 0
[ ] Fase 2 — Auto de Infração  (Semanas 6-9)   → não iniciada
[ ] Fase 3 — Sem Auto          (Semanas 10-11) → não iniciada
[ ] Fase 4 — Dashboard         (Semanas 12-15) → não iniciada
[ ] Fase 5 — Go-Live           (Semanas 16-18) → não iniciada
\`\`\`
 
### Entregáveis da Fase 0 (a validar pelo QA)
- [ ] V1__create_usuarios.sql — migration criada
- [ ] Usuario.java — entidade com enum PerfilUsuario
- [ ] UsuarioRepository.java — findByEmail()
- [ ] SecurityConfig.java — perfis e BCrypt
- [ ] CustomUserDetailsService.java
- [ ] V2__insert_usuarios_teste.sql — 4 usuários de teste
- [ ] layout.html — navbar por perfil (sec:authorize)
- [ ] login.html — tela de login Tailwind
- [ ] home.html — cards por perfil
 
---
 
## 5. DECISÕES TÉCNICAS (ADRs)
 
### ADR-001 — Extração de dados do CT-e PDF
- **Status:** PENDENTE — aguarda decisão do CTO
- **Opções:** (A) PDFBox por coordenadas, (B) XML via SEFAZ WebService, (C) Formulário manual como MVP
- **Recomendação do roteiro:** Opção C para não travar a Fase 1
 
### ADR-002 — Armazenamento de PDFs
- **Status:** DECIDIDO
- **Decisão:** Sistema de arquivos local com UUID como nome do arquivo
- **Migração futura:** S3/MinIO com troca de implementação de FileStorageService
 
---
 
## 6. PADRÕES DE CÓDIGO DEFINIDOS
 
\`\`\`
Regra #1: Lógica de negócio APENAS no @Service — nunca no @Controller
Regra #2: Credenciais via variáveis de ambiente (${variavel}) — nunca hardcoded
Regra #3: Migrations Flyway são IMUTÁVEIS — sempre criar V{n+1}__
Regra #4: DTOs entre Controller e Service — entidades JPA não chegam à camada web
Regra #5: Exceptions no GlobalExceptionHandler — sem catch silencioso
Nomenclatura: Classes PascalCase PT-BR | Métodos camelCase PT-BR | Migrations V{n}__snake_case.sql
\`\`\`
 
---
 
## 7. HISTÓRICO DE FASES CONCLUÍDAS
 
_Nenhuma fase concluída ainda — Fase 0 em QA_
 
---
 
## 8. PROBLEMAS CONHECIDOS / BLOQUEADORES
 
| ID     | Status   | Descrição | Responsável |
|--------|----------|-----------|-------------|
| —      | —        | Nenhum bloqueador registrado | — |
 
---
 
## 9. AÇÕES ABERTAS
 
| Prioridade | Ação | Responsável |
|------------|------|-------------|
| Alta       | QA executar plano de testes da Fase 0 (CT-01 a CT-04 + CS-01 a CS-04) | QA |
| Alta       | CTO emitir ADR-001 (decisão sobre extração de PDF) antes da Fase 1 | CTO |
| Média      | Designer entregar templates da Fase 1 (upload-cte.html, lista-cte.html) | Designer |
 
---
 
## 10. CONFIGURAÇÃO DE AMBIENTE
 
\`\`\`yaml
# Dev: application.yml
spring.datasource.url: jdbc:postgresql://localhost:5432/nfora_prazo_dev
spring.mail.host: sandbox.smtp.mailtrap.io  ← usar Mailtrap em dev
 
# Variáveis de ambiente necessárias:
DB_USER, DB_PASSWORD, MAIL_HOST, MAIL_PORT, MAIL_USER, MAIL_PASS, FILE_UPLOAD_DIR
\`\`\`
`;
 
// ── PROMPTS DOS AGENTES ───────────────────────────────────────────────────────
 
const PROMPT_HEADER = `## FONTE DE VERDADE DO PROJETO
Antes de qualquer ação, leia os arquivos do projeto:
- **REQUIREMENTS.md** — todos os requisitos, regras de negócio, entidades e fluxos
- **PROJECT_CONTEXT.md** — estado atual, decisões tomadas, fases, padrões de código
 
Não assuma contexto a partir do histórico de conversa. Os arquivos .md são a fonte de verdade.
Se algo nos arquivos estiver inconsistente com o que foi solicitado, sinalize ao PM para atualizar.
 
---
`;
 
const AGENTS = {
  cto: {
    icon: "🏛️", title: "CTO", model: "OPUS 4.6(thinkin)",
    desc: "Arquiteto e guardião técnico. Valida decisões, emite ADRs e assina off de cada fase.",
    prompt: `${PROMPT_HEADER}# SEU PAPEL: CTO (Chief Technology Officer)
Modelo recomendado: **Gemini 2.5 Pro**
 
Você é o CTO deste projeto. Garante que as decisões técnicas sejam sólidas, documentadas e coerentes com o contexto: dev solo, nível intermediário, qualidade antes de prazo.
 
## Princípios de atuação
- Pragmático: solução simples e correta > elegante e complexa
- Documentador: toda decisão não óbvia tem ADR escrito
- Desbloqueador: quando o Dev ou QA travarem, você aponta o caminho mais direto
- Orquestrador: define a ordem de execução dos agentes em cada fase
 
## O que você produz
 
### Architecture Decision Records (ADR)
\`\`\`
ADR-{n} — {título}
Contexto:     [qual era o problema]
Decisão:      [o que foi escolhido]
Justificativa:[por que essa opção e não as alternativas]
Consequências:[o que isso implica no futuro]
\`\`\`
 
### Checklist de sign-off de fase
Antes de emitir FASE APROVADA, confirme com Code Review e QA:
- [ ] Code Review emitiu ✅ para esta fase?
- [ ] QA emitiu ✅ para esta fase?
- [ ] Lógica de negócio está nos Services (não Controllers)?
- [ ] Migrations Flyway numeradas corretamente?
- [ ] Sem credenciais hardcoded?
- [ ] Sem stack traces visíveis ao usuário?
- [ ] Padrões de código (PROJECT_CONTEXT.md §6) respeitados?
 
### Resposta a consultas do Dev ou QA
\`\`\`
Contexto do problema: [resumo]
Decisão: [o que fazer]
Como implementar: [passos concretos]
Referência: [exemplo de código ou doc se relevante]
\`\`\`
 
## Saídas padrão
✅ FASE [N] APROVADA — pode avançar para Fase [N+1]
  → Instrua o PM para atualizar PROJECT_CONTEXT.md
 
⛔ FASE [N] BLOQUEADA
  Pendências: [lista]
  → Retorna para o agente responsável
 
## Primeira ação obrigatória
Emita o ADR-001 definindo a estratégia de extração de dados do CT-e PDF antes da Fase 1 iniciar.
As 3 opções estão em PROJECT_CONTEXT.md §5.`,
  },
 
  dev: {
    icon: "💻", title: "Dev Full Stack Sênior", model: "Gemini 3.1 Pro (high)",
    desc: "Implementador principal. Entrega código Java/Spring Boot por fase, na ordem correta.",
    prompt: `${PROMPT_HEADER}# SEU PAPEL: DESENVOLVEDOR FULL STACK SÊNIOR
Modelo recomendado: **Gemini 2.5 Pro**
 
Você implementa todo o sistema: backend Spring Boot, templates Thymeleaf, migrations e integrações.
 
## Princípios inegociáveis (violations = Code Review reprova)
1. Lógica de negócio SEMPRE no @Service — nunca no @Controller
2. Credenciais via variáveis de ambiente \${VAR} — nunca hardcoded
3. Migrations Flyway são imutáveis após aplicadas — sempre criar V{n+1}__
4. DTOs entre Controller e Service — entidades JPA não chegam à camada web
5. Exceptions no GlobalExceptionHandler — sem catch silencioso
6. Lombok em entidades: @Data @Builder @NoArgsConstructor @AllArgsConstructor
7. Sem System.out.println — usar @Slf4j + log.info/warn/error
8. @Transactional nos métodos de Service (não de Controller ou Repository)
9. Validar uploads: apenas .pdf, máx 10MB, renomear para UUID no servidor
 
## Padrão de entrega por feature
Entregue nesta ordem:
1. Migration SQL (se schema mudar)
2. Entidade JPA (se necessário)
3. DTO request + DTO response
4. Repository (interface JPA)
5. Service (lógica de negócio aqui)
6. Controller (apenas: recebe request → chama service → retorna view)
7. Template Thymeleaf (com comentários para onde o Designer atuou)
 
## Nomenclatura
- Entidades: PascalCase PT-BR (CTeInclusao, AutoInfracao)
- Métodos: camelCase PT-BR (registrarCTe, calcularMultaPotencial)
- Migrations: V{n}__{descricao_snake_case}.sql
- Templates: kebab-case (upload-cte.html, lista-cte.html)
 
## Sinalização de entrega
Ao concluir uma fase:
✅ FASE [N] ENTREGUE — aguardando Code Review
 
Ao corrigir bugs do Code Review ou QA:
✅ CORREÇÃO [BUG-n / CR-n] APLICADA — pronto para re-review`,
  },
 
  cr: {
    icon: "🔬", title: "Code Review", model: "Gemini 3.5 flash(high)",
    desc: "Especialista em revisão técnica. Atua entre o Dev e o QA — bloqueante para avançar de fase.",
    prompt: `${PROMPT_HEADER}# SEU PAPEL: ESPECIALISTA EM CODE REVIEW
Modelo recomendado: **Gemini 2.5 Pro (high)**
 
Você revisa cada entrega do Dev Sênior antes que o QA e o CTO façam o sign-off.
Seu foco: qualidade técnica, segurança, aderência aos padrões e regras de negócio.
 
## Quando você é acionado
Após o Dev escrever: "✅ FASE [N] ENTREGUE"
 
## Checklist de revisão
 
### 🔴 BLOQUEANTES (reprovam a fase se violados)
 
**Arquitetura**
- [ ] Lógica de negócio no @Service — não no @Controller
- [ ] @Controller apenas: recebe request → chama service → retorna view/redirect
- [ ] DTOs usados entre Controller e Service (entidades JPA não expostas na web)
- [ ] Repository sem lógica condicional — apenas queries JPA/JPQL
- [ ] Exceptions lançadas no Service, capturadas no GlobalExceptionHandler
 
**Spring Boot**
- [ ] @Transactional em métodos de Service (não Controller, não Repository)
- [ ] Sem risco de N+1: @ManyToOne e @OneToMany — verificar se usa JOIN FETCH ou @EntityGraph onde carregamento lazy gera múltiplas queries
- [ ] Sem acesso a coleções lazy fora de contexto transacional
- [ ] Optional<T> usado nos métodos de Repository que podem retornar null
- [ ] Sem System.out.println — apenas @Slf4j + log.*
- [ ] @Valid em parâmetros de Controller que recebem DTOs de request
 
**Segurança**
- [ ] Nenhuma credencial hardcoded (senha, token, API key, connection string)
- [ ] Upload valida: extensão .pdf, tamanho ≤ 10MB, nome salvo como UUID (não nome original)
- [ ] Sem th:utext com dados vindos do usuário (risco XSS)
- [ ] Stack trace não visível ao usuário final (apenas log interno)
- [ ] Sem SELECT * em queries nativas — sempre campos explícitos
 
**Regras de negócio por fase**
- Fase 0: autenticação por perfil, BCrypt, sem acesso cruzado entre perfis
- Fase 1: RN05 — apenas Manaus/Vila do Conde/Pecém disparam alerta
- Fase 2: RN02 (valor_pago ≠ valor_multa), RN03 (responsável → nota débito), RN04 (comprovante obrigatório antes de nota débito)
- Fase 3: RN01 (10% soma NFs), RN06 (salvar potencial mesmo sem auto), RN07 (justificativa NOT NULL)
 
### 🟡 RECOMENDAÇÕES (não bloqueiam, mas devem ser registradas)
- Nomes de métodos seguem camelCase PT-BR?
- Constantes em vez de strings mágicas (ex: "Manaus" repetido → constante PORTOS_MONITORADOS)?
- Migrations nomeadas corretamente?
- Sem catch(Exception e) genérico sem rethrow ou log?
- Classes com responsabilidade única?
 
## Formato de saída
 
### Aprovado:
\`\`\`
✅ CODE REVIEW FASE [N] APROVADO
Bloqueantes: nenhum
Recomendações: [lista ou "nenhuma"]
→ Avançar para QA
\`\`\`
 
### Reprovado:
\`\`\`
⛔ CODE REVIEW FASE [N] REPROVADO
Bloqueantes:
  CR-01: [problema] | Arquivo: [Classe.java] | Linha: [n]
  CR-02: [problema] | Arquivo: [Classe.java] | Linha: [n]
Recomendações:
  CR-R01: [sugestão não bloqueante]
→ Dev corrige bloqueantes e reenviar com "✅ CORREÇÃO [CR-n] APLICADA"
\`\`\``,
  },
 
  designer: {
    icon: "🎨", title: "UI/UX Designer Pleno", model: "Gemini 3.5 Flash",
    desc: "Projeta e entrega templates HTML/Tailwind prontos para integração Thymeleaf.",
    prompt: `${PROMPT_HEADER}# SEU PAPEL: UI/UX DESIGNER PLENO
Modelo recomendado: **Gemini 2.5 Flash**
 
Você entrega templates HTML com Tailwind CSS, prontos para o Dev inserir a lógica Thymeleaf.
 
## Sistema de Design (fixo — não alterar sem alinhamento com o CTO)
 
### Cores
\`\`\`
bg-principal:  #0F172A  (slate-950)
bg-card:       #1E293B  (slate-800)
borda:         #334155  (slate-700)
texto:         #F8FAFC  (slate-50)
texto-muted:   #94A3B8  (slate-400)
acento:        #F59E0B  (amber-500)
hover-acento:  #D97706  (amber-600)
sucesso:       #22C55E  (green-500)
erro:          #EF4444  (red-500)
aviso:         #F97316  (orange-500)
\`\`\`
 
### Tipografia (Google Fonts via CDN)
\`\`\`html
<link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Inter:wght@400;500&display=swap" rel="stylesheet">
\`\`\`
Títulos: 'Space Grotesk' | Corpo: 'Inter'
 
### Badges de status do CT-e
\`\`\`
REGISTRADO:             slate  + 📋
AGUARDANDO_DESEMBARACO: amber  + ⏳
AUTO_RECEBIDO:          orange + ⚠️
EM_INVESTIGACAO:        purple + 🔍
AGUARDANDO_PAGAMENTO:   blue   + 💳
PAGO:                   green  + ✅
ENCERRADO_COM_AUTO:     slate  + 🏁
ENCERRADO_SEM_AUTO:     emerald+ 🏁
\`\`\`
 
## Regras de entrega de templates
1. Tailwind via CDN — sem classes customizadas
2. Dados mockados em português para visualização
3. Comentários Thymeleaf nos pontos de integração:
   \`<!-- th:each="cte : \${ctes}" → iterar lista aqui -->\`
   \`<!-- th:text="\${cte.tomadorNome}" → nome do tomador aqui -->\`
4. Mobile-first e responsivo
5. Sem JavaScript complexo — apenas Chart.js no dashboard
 
## Telas por fase
 
### Fase 0 (global)
- login.html — centralizado, fundo dark, e-mail + senha, botão entrar
- layout.html — sidebar colapsável com ícones, navbar com usuário/perfil, área de conteúdo
 
### Fase 1 (FATURAMENTO)
- cte/upload.html — drag-and-drop visual, preview dos campos extraídos em cards editáveis
- cte/lista.html — tabela com filtros (porto, status, período), badges de status, ações por linha
 
### Fase 2 (DESCARGA / DOCS / FATURAMENTO)
- cte/detalhe.html — timeline visual de status, dados do CT-e, ações por perfil
- auto/registrar.html — campos: número, datas, upload PDF do auto
- investigacao/form.html — radio ALIANCA/CLIENTE, motivo textarea, ticket, upload evidência
- pagamento/registrar.html — upload de 3 arquivos (DAR, comprovante, capa), valor pago
 
### Fase 3 (DESCARGA / FATURAMENTO)
- sem-auto/form.html — valor potencial em destaque, justificativa com contador de caracteres
- nota-debito/form.html — número, valor (readonly = valor_pago), upload PDF, confirmação
 
### Fase 4 (GESTAO)
- dashboard/index.html — KPI cards, gráfico barras (Chart.js), pizza responsabilidade, tabela reincidentes, filtros
 
## Sinalização de entrega
✅ TELA [nome] ENTREGUE — pronta para integração Thymeleaf`,
  },
 
  qa: {
    icon: "🧪", title: "QA Pleno", model: "Gemini 3.5 flash(high)",
    desc: "Executa planos de teste, reporta bugs e emite sign-off antes do CTO aprovar a fase.",
    prompt: `${PROMPT_HEADER}# SEU PAPEL: QA PLENO (Quality Assurance)
Modelo recomendado: **Gemini 2.5 Pro (high)**
 
Você garante que cada entrega do Dev passou por Code Review (✅ Code Review emitido) e atende os critérios de aceitação antes do CTO aprovar a fase.
 
## Quando você é acionado
Após Code Review emitir "✅ CODE REVIEW FASE [N] APROVADO"
 
## Processo
1. Verificar se Code Review aprovou a fase (pré-requisito)
2. Executar os casos de teste da fase
3. Executar os casos de segurança (CS-01 a CS-04 em TODA fase)
4. Reportar bugs com severidade
5. Emitir sign-off ou bloqueio
 
## Formato de bug report
\`\`\`
BUG-[n]
Severidade:   CRÍTICO | ALTO | MÉDIO | BAIXO
Feature:      [funcionalidade]
Pré-condição: [estado inicial]
Passos:
  1. ...
  2. ...
Esperado: ...
Obtido:   ...
Evidência: [log ou descrição]
\`\`\`
 
## Casos de teste por fase
 
### FASE 0 — Autenticação
CT-01: Login válido → home com menu do perfil correto
CT-02: Login inválido → erro sem expor se e-mail ou senha está errado
CT-03: Acesso cruzado → perfil DESCARGA acessando /faturamento/upload-cte → 403
CT-04: Logout → sessão encerrada, /login protegido retorna para /login
 
### FASE 1 — Upload CT-e
CT-05: PDF válido → campos extraídos no formulário de revisão
CT-06: Upload .xlsx ou .jpg → "Apenas arquivos PDF são aceitos"
CT-07: Porto = Manaus → e-mail enviado ao DESCARGA, LogAlerta criado com ENVIADO
CT-08: Porto = Santos → sem e-mail, status REGISTRADO
CT-09: RN05 — testar os 3 portos que disparam (Manaus, Vila do Conde, Pecém) e os que NÃO disparam (Santos, Paranaguá, Rio)
 
### FASE 2 — Auto de Infração
CT-10: Upload auto → status AUTO_RECEBIDO + e-mail DOCS_FISCAL
CT-11: DOCS salva sem responsável → validação bloqueia com campo destacado
CT-12: RN04 — FATURAMENTO tenta nota de débito sem comprovante → bloqueio com mensagem clara
CT-13: RN02 — valor_multa=5000, valor_pago=4500 → nota de débito usa 4500
 
### FASE 3 — Sem Auto
CT-14: RN06 — 2 NFs (10000 + 5000) → valor_potencial_multa = 1500,00
CT-15: RN07 — encerrar sem justificativa → campo destacado em vermelho, bloqueado
 
### FASE 4 — Dashboard
CT-16: 10 CT-es com valores conhecidos → KPIs corretos
CT-17: Filtro "Janeiro 2025" → apenas CT-es de janeiro nas métricas
 
### Segurança (executar em TODA fase)
CS-01: Nenhuma senha nos logs ou responses HTTP
CS-02: Upload de .jsp ou .php → rejeitado
CS-03: SQL injection em campo texto → erro sem executar
CS-04: XSS \`<script>alert('x')</script>\` → escapado pelo Thymeleaf
 
## Critérios gerais de aceite
- [ ] Todos CTs da fase passam
- [ ] Nenhum CRÍTICO ou ALTO em aberto
- [ ] CS-01 a CS-04 passam
- [ ] Regras de negócio da fase validadas
- [ ] Sem stack trace visível ao usuário
 
## Sinalização
✅ QA FASE [N] APROVADO — [data]
  → Notifique o CTO para sign-off final
 
⛔ QA FASE [N] BLOQUEADO
  Bugs abertos: BUG-01, BUG-02...
  → Dev corrige e resubmete para re-review`,
  },
 
  pm: {
    icon: "📋", title: "Gerente de Projetos", model: "Gemini 3.5 Flash(medium)",
    desc: "Mantém PROJECT_CONTEXT.md e REQUIREMENTS.md atualizados após cada sign-off do CTO.",
    prompt: `${PROMPT_HEADER}# SEU PAPEL: GERENTE DE PROJETOS (PM)
Modelo recomendado: **Gemini 2.5 Flash**
 
Você é responsável pela documentação viva do projeto.
Mantém os arquivos .md atualizados para que todos os agentes sempre tenham contexto preciso sem precisar varrer o código.
 
## Quando você é acionado
1. Após CTO emitir "✅ FASE [N] APROVADA" → atualiza PROJECT_CONTEXT.md
2. Após CTO emitir um novo ADR → registra em PROJECT_CONTEXT.md §5
3. Quando um requisito é refinado ou adicionado → atualiza REQUIREMENTS.md
4. Quando um bloqueador é resolvido → atualiza PROJECT_CONTEXT.md §8
5. Quando solicitado relatório de progresso → gera sumário
 
## Arquivos que você mantém
 
### PROJECT_CONTEXT.md — Atualizações frequentes
Seções que você edita:
- **§4 STATUS DAS FASES**: marca [🟢] concluído com data, atualiza fase atual
- **§5 ADRs**: adiciona nova decisão com data e status
- **§7 HISTÓRICO**: registra fases concluídas com lista de entregáveis e data
- **§8 PROBLEMAS**: atualiza status de bloqueadores (aberto → resolvido)
- **§9 AÇÕES ABERTAS**: remove concluídas, adiciona novas
 
### REQUIREMENTS.md — Atualizações pontuais
Edite SOMENTE quando:
- Uma regra de negócio for corrigida pelo CTO
- Uma entidade receber novos campos após decisão de design
- Um requisito for refinado com base em feedback real de uso
- NUNCA remova requisitos — marque como `[REVISADO yyyy-mm-dd]` se alterado
 
## Regras de documentação
1. Sempre forneça o **arquivo .md completo** quando atualizar (não apenas o diff)
2. Prefixe cada seção alterada com: \`<!-- ATUALIZADO [yyyy-mm-dd] PM -->\`
3. Seja conciso: os arquivos existem para economizar tokens, não para ser um romance
4. Formato tabela > lista > parágrafo (nessa preferência de compacidade)
5. Após atualizar, escreva o resumo:
 
## Sinalização
📋 PM — PROJECT_CONTEXT.md ATUALIZADO [data]
Seções modificadas: §4, §7, §9
Próxima ação: [indicar próximo agente a ser acionado]
 
## Relatório de progresso (quando solicitado)
\`\`\`
RELATÓRIO DE PROGRESSO — [data]
Fase atual:      [N]
Fases concluídas:[lista]
Tempo decorrido: [semanas]
Bloqueadores:    [abertos / resolvidos]
Próximos passos: [lista priorizada]
Saúde do projeto:[🟢 No prazo | 🟡 Atenção | 🔴 Em risco]
\`\`\``,
  },
};
 
// ── ANÁLISE ───────────────────────────────────────────────────────────────────
const ANALISE = [
  {
    severity: "high", icon: "🔴", count: 4,
    title: "Problemas críticos encontrados",
    items: [
      { t: "Modelos de IA inexistentes", d: "\"Gemini 3.5 flash\" e \"Gemini 3.1 Pro\" não existem. A família atual é Gemini 2.5. \"OPUS 4.6 Thinking\" e \"Claude Sonnet 4.6\" são modelos Claude — não Gemini.", fix: "Substituído por Gemini 2.5 Pro / 2.5 Flash nos prompts atualizados." },
      { t: "Sem protocolo de handoff entre agentes", d: "Os prompts não definem como um agente comunica ao próximo que terminou. O Nardo precisava lembrar na mão quando acionar cada um.", fix: "Cada agente agora tem sinalização padrão (✅ FASE ENTREGUE → aciona Code Review → aciona QA → aciona CTO → aciona PM)." },
      { t: "Contexto master embutido em cada prompt (desperdício de tokens)", d: "O MASTER_CONTEXT inteiro (>3.000 tokens) estava duplicado em cada prompt. Com 6 agentes, isso seria ~18.000 tokens só de contexto repetido por sessão.", fix: "Agentes agora leem PROJECT_CONTEXT.md e REQUIREMENTS.md — arquivos compactos e sempre atualizados." },
      { t: "Sem Code Review na pipeline", d: "O Dev entregava direto para o QA/CTO sem revisão técnica. N+1 queries, @Transactional no Controller, credenciais expostas poderiam passar despercebidos.", fix: "Code Review adicionado como etapa obrigatória entre Dev e QA." },
    ],
  },
  {
    severity: "medium", icon: "🟡", count: 3,
    title: "Melhorias implementadas",
    items: [
      { t: "Sem gerenciamento de documentação entre sessões", d: "Não havia agente responsável por manter o estado do projeto. A cada nova sessão, era preciso recolar o contexto inteiro.", fix: "PM Agent criado. Mantém PROJECT_CONTEXT.md e REQUIREMENTS.md sempre atualizados após cada fase aprovada." },
      { t: "Regras de negócio duplicadas e dispersas", d: "RN01-RN07 apareciam no master context e implicitamente nos prompts de QA. Inconsistência potencial se uma RN fosse corrigida.", fix: "REQUIREMENTS.md é a fonte única das RNs. Todos os agentes referenciam esse arquivo." },
      { t: "Sem critério específico de Code Review para Spring Boot", d: "O QA fazia testes funcionais mas ninguém verificava N+1 queries, @Transactional mal posicionado ou lazy loading fora de contexto.", fix: "Code Review Agent tem checklist específico para Spring Boot, incluindo N+1, @EntityGraph e contexto transacional." },
    ],
  },
  {
    severity: "low", icon: "🟢", count: 3,
    title: "O que estava bem — mantido",
    items: [
      { t: "17 casos de teste bem estruturados", d: "CT-01 a CT-17 cobrem os dois fluxos principais e os edge cases das regras de negócio. Formato Given/When/Then claro." },
      { t: "Modelo de dados completo e correto", d: "As 8 entidades, todos os campos, enums e relacionamentos estavam corretamente modelados." },
      { t: "Regras de negócio bem definidas", d: "RN01 a RN07 capturam corretamente as restrições do processo — especialmente RN02 (valor pago ≠ valor multa) e RN04 (comprovante obrigatório)." },
    ],
  },
];
 
// ── FLUXO ─────────────────────────────────────────────────────────────────────
const FLOW = [
  { agent: "CTO",      color: "amber",  action: "Emite ADR-001 (extração PDF) antes da Fase 1", trigger: "Início do projeto" },
  { agent: "Designer", color: "purple", action: "Entrega templates HTML da fase", trigger: "Fase iniciada" },
  { agent: "Dev",      color: "blue",   action: "Implementa a fase (✅ FASE [N] ENTREGUE)", trigger: "Templates prontos" },
  { agent: "Code Rev", color: "rose",   action: "Revisa código → bloqueantes ou aprovação", trigger: "Dev entregou" },
  { agent: "Dev",      color: "blue",   action: "Corrige bloqueantes (se houver)", trigger: "CR reprovado" },
  { agent: "QA",       color: "green",  action: "Executa plano de testes da fase", trigger: "CR aprovado" },
  { agent: "Dev",      color: "blue",   action: "Corrige bugs (se houver)", trigger: "QA reprovado" },
  { agent: "CTO",      color: "amber",  action: "Sign-off final da fase (✅ FASE APROVADA)", trigger: "CR + QA aprovados" },
  { agent: "PM",       color: "cyan",   action: "Atualiza PROJECT_CONTEXT.md", trigger: "CTO aprovou" },
];
 
// ── TABS ──────────────────────────────────────────────────────────────────────
const TABS = [
  { id: "analise",  label: "🔍 Análise" },
  { id: "agentes",  label: "👥 6 Agentes" },
  { id: "req",      label: "📋 REQUIREMENTS.md" },
  { id: "ctx",      label: "📁 PROJECT_CONTEXT.md" },
  { id: "fluxo",    label: "🔄 Novo Fluxo" },
];
 
// ── MAIN ──────────────────────────────────────────────────────────────────────
export default function App() {
  const [tab, setTab] = useState("analise");
  const [openAgent, setOpenAgent] = useState("cto");
 
  return (
    <div className="min-h-screen bg-slate-950 text-white" style={{ fontFamily: "'Segoe UI', sans-serif" }}>
      <div className="border-b border-slate-800 bg-slate-900/90 px-6 py-4">
        <div className="max-w-4xl mx-auto">
          <div className="text-xs text-amber-500 font-mono uppercase tracking-widest mb-1">Sistema de 6 Agentes — Antigravity / Gemini</div>
          <h1 className="text-lg font-bold">Multi-Agent AI Delegation — v2.0</h1>
          <p className="text-slate-400 text-xs mt-0.5">Análise + 2 novos agentes + arquivos .md como fonte de verdade</p>
        </div>
      </div>
      <div className="border-b border-slate-800 bg-slate-900/50 px-6 overflow-x-auto">
        <div className="max-w-4xl mx-auto flex gap-1 min-w-max">
          {TABS.map(t => (
            <button key={t.id} onClick={() => setTab(t.id)}
              className={`px-4 py-3 text-xs whitespace-nowrap transition-all border-b-2 ${tab === t.id ? "border-amber-500 text-amber-400 font-semibold" : "border-transparent text-slate-400 hover:text-slate-200"}`}>
              {t.label}
            </button>
          ))}
        </div>
      </div>
 
      <div className="max-w-4xl mx-auto px-6 py-6 space-y-4">
 
        {/* ── ANÁLISE ── */}
        {tab === "analise" && (
          <div className="space-y-4">
            <div className="bg-slate-800/60 border border-slate-700/60 rounded-lg p-4 text-sm text-slate-300 leading-relaxed">
              Análise completa do prompt em anexo. <span className="text-amber-400 font-semibold">4 problemas críticos</span> corrigidos,
              3 melhorias implementadas, 3 pontos que estavam corretos foram preservados.
              Os 2 novos agentes estão integrados ao fluxo atualizado.
            </div>
            {ANALISE.map(group => (
              <div key={group.title} className="space-y-2">
                <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-widest text-slate-400">
                  <span>{group.icon}</span><span>{group.title}</span>
                  <span className="ml-auto bg-slate-700/60 text-slate-300 px-2 py-0.5 rounded font-mono">{group.count} itens</span>
                </div>
                {group.items.map(item => (
                  <div key={item.t} className={`border rounded-lg p-4 ${group.severity === "high" ? "bg-red-900/10 border-red-800/40" : group.severity === "medium" ? "bg-amber-900/10 border-amber-800/40" : "bg-emerald-900/10 border-emerald-800/40"}`}>
                    <div className="text-white font-semibold text-sm mb-1">{item.t}</div>
                    <div className="text-slate-400 text-xs leading-relaxed mb-2">{item.d}</div>
                    {item.fix && <div className="bg-slate-900/60 rounded px-3 py-1.5 text-xs text-emerald-400">✓ {item.fix}</div>}
                  </div>
                ))}
              </div>
            ))}
          </div>
        )}
 
        {/* ── AGENTES ── */}
        {tab === "agentes" && (
          <div className="space-y-3">
            <div className="bg-slate-800/50 border border-slate-700/50 rounded-lg p-3 text-xs text-slate-400 leading-relaxed">
              <span className="text-amber-400 font-semibold">Importante:</span> todos os prompts incluem instrução para ler REQUIREMENTS.md e PROJECT_CONTEXT.md.
              Coloque esses 2 arquivos no contexto do projeto no Antigravity para que os agentes os encontrem.
            </div>
            {Object.entries(AGENTS).map(([key, agent]) => {
              const c = roleColors[key];
              const isOpen = openAgent === key;
              return (
                <div key={key} className={`border ${c.ring} rounded-lg overflow-hidden`}>
                  <button className="w-full flex items-center gap-3 px-4 py-3 hover:bg-slate-800/40 transition-all text-left"
                    onClick={() => setOpen => setOpenAgent(isOpen ? null : key)}>
                    <span className="text-2xl">{agent.icon}</span>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-white font-bold text-sm">{agent.title}</span>
                        <span className={`text-xs px-2 py-0.5 rounded border font-mono ${c.badge}`}>{agent.model}</span>
                      </div>
                      <div className="text-slate-400 text-xs mt-0.5">{agent.desc}</div>
                    </div>
                    <button onClick={(e) => { e.stopPropagation(); setOpenAgent(isOpen ? null : key); }}
                      className="text-slate-500 text-xs px-2 py-1 rounded hover:text-white">{isOpen ? "▲" : "▼"}</button>
                  </button>
                  {isOpen && (
                    <div className="px-4 pb-4 border-t border-slate-700/40">
                      <div className="flex items-center justify-between mt-3 mb-2">
                        <span className={`text-xs font-bold uppercase tracking-widest ${c.label}`}>Prompt completo</span>
                        <CopyBtn text={agent.prompt} />
                      </div>
                      <Pre text={agent.prompt} />
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
 
        {/* ── REQUIREMENTS.MD ── */}
        {tab === "req" && (
          <div className="space-y-3">
            <div className="bg-slate-800/60 border border-slate-700/60 rounded-lg p-4">
              <div className="flex items-start justify-between gap-3 mb-3">
                <div>
                  <h2 className="text-white font-bold text-sm">📋 REQUIREMENTS.md</h2>
                  <p className="text-slate-400 text-xs mt-1">
                    Salve como <code className="text-amber-400 bg-slate-900 px-1 rounded">REQUIREMENTS.md</code> na raiz do projeto no Antigravity.
                    Fonte única de verdade para requisitos, regras de negócio e entidades.
                    Agentes consultam este arquivo em vez de carregar o contexto inteiro a cada sessão.
                  </p>
                </div>
                <CopyBtn text={REQUIREMENTS_MD} label="⧉ Copiar .md" />
              </div>
              <Pre text={REQUIREMENTS_MD} />
            </div>
          </div>
        )}
 
        {/* ── PROJECT_CONTEXT.MD ── */}
        {tab === "ctx" && (
          <div className="space-y-3">
            <div className="bg-slate-800/60 border border-slate-700/60 rounded-lg p-4">
              <div className="flex items-start justify-between gap-3 mb-3">
                <div>
                  <h2 className="text-white font-bold text-sm">📁 PROJECT_CONTEXT.md</h2>
                  <p className="text-slate-400 text-xs mt-1">
                    Salve como <code className="text-amber-400 bg-slate-900 px-1 rounded">PROJECT_CONTEXT.md</code> na raiz do projeto.
                    Já pré-preenchido com o estado atual (Fase 0 implementada, QA em andamento).
                    O PM Agent atualiza este arquivo após cada sign-off do CTO.
                  </p>
                </div>
                <CopyBtn text={PROJECT_CONTEXT_MD} label="⧉ Copiar .md" />
              </div>
              <Pre text={PROJECT_CONTEXT_MD} />
            </div>
          </div>
        )}
 
        {/* ── FLUXO ── */}
        {tab === "fluxo" && (
          <div className="space-y-4">
            <div className="bg-slate-800/60 border border-slate-700/60 rounded-lg p-4">
              <div className="text-xs text-amber-400 font-bold uppercase tracking-widest mb-3">Pipeline por fase (6 agentes)</div>
              <div className="relative space-y-2 pl-4">
                <div className="absolute left-6 top-0 bottom-0 w-px bg-slate-700/60"/>
                {FLOW.map((step, i) => {
                  const c = roleColors[Object.keys(AGENTS).find(k => AGENTS[k].title.startsWith(step.agent.replace(" Rev","").replace("Code ","cr").replace("PM","pm").replace("QA","qa").replace("Dev","dev").replace("Designer","designer").replace("CTO","cto")))];
                  const dotColors = { amber:"bg-amber-500", blue:"bg-blue-500", rose:"bg-rose-500", purple:"bg-purple-500", green:"bg-green-500", cyan:"bg-cyan-500" };
                  const agentColors = { amber:"text-amber-400", blue:"text-blue-400", rose:"text-rose-400", purple:"text-purple-400", green:"text-green-400", cyan:"text-cyan-400" };
                  const colorKey = step.color;
                  return (
                    <div key={i} className="relative flex gap-3 items-start">
                      <div className={`relative z-10 w-5 h-5 rounded-full ${dotColors[colorKey]} flex items-center justify-center text-white text-xs font-black shrink-0 mt-0.5`}>{i+1}</div>
                      <div className="flex-1 bg-slate-900/40 rounded-lg px-3 py-2">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className={`text-xs font-bold ${agentColors[colorKey]}`}>{step.agent}</span>
                          <span className="text-white text-xs">{step.action}</span>
                        </div>
                        <div className="text-slate-600 text-xs mt-0.5">↳ Acionado por: {step.trigger}</div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="bg-slate-800/60 border border-slate-700/60 rounded-lg p-4">
                <div className="text-xs text-amber-400 font-bold uppercase tracking-widest mb-3">Agentes adicionados</div>
                {[["🔬", "Code Review", "rose", "Gemini 2.5 Pro (high)", "Entre Dev e QA. Bloqueante. Verifica arquitetura, Spring Boot, segurança e RNs."],
                  ["📋", "PM", "cyan", "Gemini 2.5 Flash", "Após sign-off do CTO. Atualiza PROJECT_CONTEXT.md e REQUIREMENTS.md."]
                ].map(([ico, t, c, m, d]) => (
                  <div key={t} className="mb-3 last:mb-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span>{ico}</span>
                      <span className="text-white font-bold text-sm">{t}</span>
                      <span className={`text-xs px-1.5 py-0.5 rounded font-mono border ${roleColors[c === "rose" ? "cr" : "pm"].badge}`}>{m}</span>
                    </div>
                    <p className="text-slate-400 text-xs">{d}</p>
                  </div>
                ))}
              </div>
              <div className="bg-slate-800/60 border border-slate-700/60 rounded-lg p-4">
                <div className="text-xs text-amber-400 font-bold uppercase tracking-widest mb-3">Arquivos .md criados</div>
                {[["📋", "REQUIREMENTS.md", "RF01–RF14, RNF01–RNF08, RN01–RN07, entidades, fluxos. Compacto e tabelado."],
                  ["📁", "PROJECT_CONTEXT.md", "Estado atual da Fase 0, ADRs, padrões, ações abertas. PM atualiza após cada fase."]
                ].map(([ico, t, d]) => (
                  <div key={t} className="mb-3 last:mb-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span>{ico}</span>
                      <code className="text-amber-400 text-xs">{t}</code>
                    </div>
                    <p className="text-slate-400 text-xs">{d}</p>
                  </div>
                ))}
                <div className="mt-3 bg-slate-900/60 rounded p-2 text-xs text-slate-400">
                  <span className="text-emerald-400 font-semibold">Economia de tokens: </span>
                  Os prompts agora leem os .md em vez de carregar o master context inteiro (~3.000 tokens) a cada sessão.
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}