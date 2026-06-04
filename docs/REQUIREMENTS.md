# REQUIREMENTS.md — Sistema NF Fora do Prazo
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
 
```
RN01: valor_multa = 10% do SOMATÓRIO dos valores de todas as NFs do CT-e
RN02: valor_nota_debito = valor_pago (NÃO valor_multa) — pode ter desconto por antecipação
RN03: responsavel = ALIANCA → empresa absorve | responsavel = CLIENTE → emite nota de débito
RN04: Nota de débito SOMENTE pode ser emitida após arquivo_comprovante estar preenchido em Pagamento
RN05: Portos que disparam alerta DESCARGA: Manaus, Vila do Conde/PCM, Pecém
      Demais portos: apenas registrar, sem alerta (desembaraço é responsabilidade do cliente)
RN06: Mesmo sem auto de infração → calcular e salvar valor_potencial_multa (10% das NFs)
RN07: Encerramento sem auto EXIGE justificativa.length > 0 (campo NOT NULL no banco)
```
 
---
 
## 4. ENTIDADES DO BANCO (schema compacto)
 
```sql
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
```
 
### Enums
```
PerfilUsuario:  FATURAMENTO | DESCARGA | DOCS_FISCAL | GESTAO
StatusCTe:      REGISTRADO | AGUARDANDO_DESEMBARACO | AUTO_RECEBIDO | EM_INVESTIGACAO
                AGUARDANDO_PAGAMENTO | PAGO | ENCERRADO_COM_AUTO | ENCERRADO_SEM_AUTO
Responsavel:    ALIANCA | CLIENTE | PENDENTE
StatusEnvio:    ENVIADO | ERRO
```
 
### Relacionamentos
```
CTeInclusao  1──N  NotaFiscal
CTeInclusao  1──01 AutoInfracao
AutoInfracao 1──01 Pagamento
Pagamento    1──01 NotaDebito
CTeInclusao  1──01 EncSeMAuto
CTeInclusao  1──N  LogAlerta
Usuario      1──N  CTeInclusao
```
 
---
 
## 5. FLUXOS DE ESTADO
 
### Fluxo A — Com auto de infração
```
FATURAMENTO: upload CT-e → REGISTRADO
SISTEMA: se porto monitorado → e-mail DESCARGA → AGUARDANDO_DESEMBARACO
FISCAL: upload auto → AUTO_RECEBIDO → e-mail DOCS_FISCAL
DOCS: define responsável + motivo + ticket → EM_INVESTIGACAO → AGUARDANDO_PAGAMENTO → e-mail DESCARGA
DESCARGA: paga (externo) + upload DAR+comprovante+capa → PAGO → e-mail FATURAMENTO
FATURAMENTO: se CLIENTE → nota de débito → ENCERRADO_COM_AUTO
             se ALIANCA → ENCERRADO_COM_AUTO (sem nota de débito)
```
 
### Fluxo B — Sem auto de infração
```
(mesmos passos 1-3 do Fluxo A)
DESCARGA: seleciona "sem auto" → sistema calcula 10% NFs → justificativa → ENCERRADO_SEM_AUTO
```
 
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
