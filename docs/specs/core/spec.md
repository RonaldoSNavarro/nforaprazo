# Especificação de Capacidades Core (System Requirements & Domain Model)

Este documento descreve os requisitos funcionais, não-funcionais, as regras de negócio, o modelo de banco de dados, os fluxos de estado da máquina de estados e os casos de uso para o sistema **NF Fora do Prazo**.

---

## 1. Requisitos Funcionais

A tabela abaixo detalha as funcionalidades implementadas no sistema:

| ID | Área | Prioridade | Descrição |
| :--- | :--- | :--- | :--- |
| **RF01** | Upload | Alta | Upload de arquivo PDF de CT-e pelo perfil `FATURAMENTO`, com anexação obrigatória de autorização de aceitação de custo em modal dedicado. |
| **RF02** | Extração | Alta | Extração automática via AJAX e PDFBox dos dados do PDF do CT-e no momento da seleção do arquivo: tomador (nome e CNPJ), chave CT-e (44 dígitos), número do CT-e, navio/viagem/direção, porto de origem, porto de destino, valor do CT-e, número do booking, container (AAAA1234567) e quantidade de NFs. |
| **RF03** | Status | Alta | Status inicial do CT-e recém-cadastrado é `PENDENTE`. Identificação de portos monitorados para roteamento operacional de desembaraço e liberação imediata das ações da equipe de Descarga (`Confirmar Desembaraço`, `Anexar Auto`, `Encerrar Sem Auto`). |
| **RF04** | Notificação | Alta | Disparo de e-mails automáticos via Spring Mail em cada transição relevante de status do ciclo do CT-e. |
| **RF05** | Auto Infração | Alta | Upload do PDF do Auto de Infração vinculado ao CT-e, com registro da data de emissão, data de vencimento e valor da multa. |
| **RF06** | Auto Infração | Alta | Disparo de alerta automático por e-mail para o perfil `DOCS_FISCAL` logo após o registro do Auto de Infração no sistema. |
| **RF07** | Investigação | Alta | Registro da investigação pelo perfil `DOCS_FISCAL`: preenchimento imediato via modal com apuração de responsável (`EMPRESA_INTERNO` vs `CLIENTE`), motivo do erro e número do ticket/evidência. |
| **RF08** | Pagamento | Alta | Rotas dedicadas `/descarga/pendentes` e `/descarga/pagamentos`, com upload dos arquivos DAR (com extração automática de valor pago por PDFBox), comprovante de pagamento e capa do processo, registrando o valor efetivamente pago pelo perfil `DESCARGA`. |
| **RF09** | Pagamento | Alta | Disparo de alerta automático por e-mail para o perfil `FATURAMENTO` após a confirmação e registro do pagamento. |
| **RF10** | Encerramento | Alta | Emissão de Nota de Débito exclusiva para responsabilidade `CLIENTE`. Quando a responsabilidade for `EMPRESA_INTERNO` ou `SISTEMA`, encerramento com contabilização de custo interno absorvido. |
| **RF11** | Encerramento | Alta | Fluxo sem auto de infração: encerramento motivado pelo perfil `DESCARGA` com justificativa obrigatória e status `ENCERRADO_SEM_AUTO`. |
| **RF12** | BI / Dashboard | Alta | Dashboard operacional e analítico com DTOs serializáveis: exposição financeira total, multas pagas, multas evitadas, evolução mensal, distribuição de responsabilidade (Interno/Empresa vs Cliente) e top 5 tomadores reincidentes. |
| **RF13** | Relatórios | Média | Geração e exportação de relatórios em formato Excel (`.xlsx`) com suporte a filtros por período e porto. |
| **RF14** | Pendentes | Alta | Tela dedicada e menu lateral para Autos Analisados e Pendentes de Envio para Pagamento (`/faturamento/pendentes-pagamento`). |

---

## 2. Requisitos Não-Funcionais

| ID | Categoria | Descrição |
| :--- | :--- | :--- |
| **RNF01** | Interface | Interface web responsiva construída com Thymeleaf + Vanilla/Tailwind CSS para acesso via navegadores modernos. |
| **RNF02** | Migração | O sistema substitui completamente as planilhas de controle legadas. |
| **RNF03** | Comunicação | Envio assíncrono de notificações de e-mail com histórico auditado em `LogAlerta`. |
| **RNF04** | Integração | Leitura automática de PDFs locais via Apache PDFBox sem dependência de APIs externas de OCR. |
| **RNF05** | Armazenamento | Arquivos PDF armazenados localmente em disco em `/app/uploads`, vinculados por UUID. |
| **RNF06** | Segurança | RBAC por endpoints gerenciado pelo Spring Security com hashes BCrypt. |
| **RNF07** | Auditoria | Trilha de auditoria transversal via Spring AOP interceptando ações anotadas com `@Auditable`. |
| **RNF08** | Usabilidade | Gráficos interativos com `Chart.js` e auto-preenchimento AJAX de formulários. |

---

## 3. Regras de Negócio (RNs)

*   **RN01 (Cálculo da Multa):** O valor nominal da multa é equivalente a **10% do somatório** das NFs ou valor da carga do CT-e. Para os indicadores de exposição e de multa evitada, o sistema utiliza a soma das NFs quando ela for maior que zero; na ausência desse valor, utiliza o valor da carga.
*   **RN02 (Valor da Nota de Débito):** O valor da Nota de Débito gerada ao cliente é exatamente igual ao **valor efetivamente pago**.
*   **RN03 (Absorção de Multa):** Quando a responsabilidade for `EMPRESA_INTERNO` ou `SISTEMA`, a empresa assume o custo interno sem gerar Nota de Débito. Quando for `CLIENTE`, a emissão de Nota de Débito é habilitada.
*   **RN04 (Pré-requisito da Nota de Débito):** Uma Nota de Débito só pode ser emitida após o comprovante de pagamento ser registrado.
*   **RN05 (Roteamento por Portos Monitorados):** Roteamento operacional dinâmico parametrizável pela tela administrativa `/admin/portos`.
*   **RN06 (Multa Potencial Sem Auto):** Cálculo projetado de 10% nos encerramentos sem autuação.
*   **RN07 (Obrigatoriedade de Justificativa):** O encerramento no fluxo "Sem Auto" exige justificativa textual não nula.

---

## 4. Entidades do Banco (Schema Compacto)

```sql
-- Cadastro de CT-es
CTe (
    id UUID PRIMARY KEY,
    numero_cte VARCHAR(50) NOT NULL,
    chave_acesso VARCHAR(44) UNIQUE,
    tomador_nome VARCHAR(255) NOT NULL,
    tomador_cnpj VARCHAR(20) NOT NULL,
    navio VARCHAR(255),
    viagem VARCHAR(50),
    direcao VARCHAR(100),
    container VARCHAR(11),
    quantidade_notas INT,
    porto_origem VARCHAR(100),
    porto_destino VARCHAR(100) NOT NULL,
    valor_carga DECIMAL(15, 2) NOT NULL,
    numero_booking VARCHAR(50),
    id UUID PRIMARY KEY,
    cte_inclusao_id UUID NOT NULL REFERENCES CTeInclusao(id) ON DELETE CASCADE,
    numero_nota VARCHAR(50) NOT NULL,
    valor_nota DECIMAL(15, 2) NOT NULL,
    data_emissao_nf DATE NOT NULL
);

-- Registro físico do Auto de Infração recebido do Fisco
AutoInfracao (
    id UUID PRIMARY KEY,
    cte_inclusao_id UUID NOT NULL UNIQUE REFERENCES CTeInclusao(id),
    numero_auto VARCHAR(100) NOT NULL,
    data_emissao DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    valor_multa DECIMAL(15, 2) NOT NULL,
    arquivo_pdf_auto VARCHAR(255) NOT NULL, -- UUID no File System
    responsavel VARCHAR(50) NOT NULL, -- ENUM Responsavel
    motivo_erro TEXT,
    numero_ticket VARCHAR(100),
    arquivo_ticket VARCHAR(255) -- UUID no File System
);

-- Controle de pagamentos operacionais efetuados
Pagamento (
    id UUID PRIMARY KEY,
    auto_infracao_id UUID NOT NULL UNIQUE REFERENCES AutoInfracao(id),
    valor_pago DECIMAL(15, 2) NOT NULL,
    data_pagamento DATE NOT NULL,
    arquivo_dar VARCHAR(255) NOT NULL,
    arquivo_comprovante VARCHAR(255) NOT NULL,
    arquivo_capa VARCHAR(255) NOT NULL,
    data_envio_para_pagamento DATE
);

-- Faturamento de prejuízos causados pelo cliente
NotaDebito (
    id UUID PRIMARY KEY,
    cte_inclusao_id UUID NOT NULL REFERENCES CTeInclusao(id),
    pagamento_id UUID NOT NULL UNIQUE REFERENCES Pagamento(id),
    numero_nota_debito VARCHAR(100) NOT NULL,
    valor_nota_debito DECIMAL(15, 2) NOT NULL,
    data_emissao DATE NOT NULL,
    arquivo_pdf VARCHAR(255) NOT NULL,
    data_envio_cliente DATE
);

-- Encerramento preventivo sem recebimento de multa
EncSemAuto (
    id UUID PRIMARY KEY,
    cte_inclusao_id UUID NOT NULL UNIQUE REFERENCES CTeInclusao(id),
    valor_potencial_multa DECIMAL(15, 2) NOT NULL,
    justificativa TEXT NOT NULL,
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id UUID NOT NULL REFERENCES Usuario(id)
);

-- Histórico e auditoria de envio de e-mails/alertas
LogAlerta (
    id UUID PRIMARY KEY,
    cte_inclusao_id UUID REFERENCES CTeInclusao(id),
    tipo_alerta VARCHAR(100) NOT NULL,
    destinatario VARCHAR(255) NOT NULL,
    data_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status_envio VARCHAR(50) NOT NULL -- ENUM StatusEnvio
);
```

### Enums do Sistema

*   `PerfilUsuario`: `ADMINISTRADOR` | `FATURAMENTO` | `DESCARGA` | `DOCS_FISCAL` | `GESTAO`
*   `StatusCTe`: `PENDENTE` | `REGISTRADO` | `AGUARDANDO_DESEMBARACO` | `DESEMBARACADO` | `AUTO_RECEBIDO` | `EM_INVESTIGACAO` | `AGUARDANDO_PAGAMENTO` | `PAGO` | `ENCERRADO_COM_AUTO` | `ENCERRADO_SEM_AUTO`
*   `Responsavel`: `PENDENTE` | `SISTEMA` | `EMPRESA_INTERNO` | `CLIENTE`
*   `StatusEnvio`: `ENVIADO` | `ERRO`

### Relacionamentos Lógicos

*   `CTeInclusao` **1 ── N** `NotaFiscal`
*   `CTeInclusao` **1 ── 0..1** `AutoInfracao`
*   `AutoInfracao` **1 ── 0..1** `Pagamento`
*   `Pagamento` **1 ── 0..1** `NotaDebito`
*   `CTeInclusao` **1 ── 0..1** `EncSemAuto`
*   `CTeInclusao` **1 ── N** `LogAlerta`
*   `Usuario` **1 ── N** `CTeInclusao`

---

## 5. Fluxos de Estado (Máquina de Estados)

### Fluxo A — Com auto de infração
1.  **Início:** O perfil `FATURAMENTO` faz upload do arquivo de CT-e. O status é inicializado como `REGISTRADO`.
2.  **Roteamento:** O sistema avalia se o destino é um porto monitorado (RN05).
    *   **Porto Monitorado:** Envia alerta por e-mail para o time de descarga e altera o status para `AGUARDANDO_DESEMBARACO`.
    *   **Porto Não-Monitorado:** Permanece registrado e aguarda de forma passiva.
3.  **Recebimento da Multa:** O setor fiscal realiza o upload do arquivo PDF do Auto de Infração. O status passa para `AUTO_RECEBIDO` e dispara um alerta por e-mail ao time `DOCS_FISCAL`.
4.  **Investigação de Culpa:** O time `DOCS_FISCAL` apura a culpa pelo atraso.
    *   Após preencher os dados de responsável, motivo e ticket de evidência, o status passa para `EM_INVESTIGACAO`.
    *   Definido o responsável final, o status passa para `AGUARDANDO_PAGAMENTO` e alerta a `DESCARGA`.
5.  **Ação de Pagamento:** O operador de `DESCARGA` efetua o pagamento no banco/SEFAZ externamente e anexa os PDFs de DAR, Comprovante e Capa. O status é alterado para `PAGO` e um alerta de e-mail é enviado ao time de `FATURAMENTO`.
6.  **Encerramento do Processo:**
    *   Se a responsabilidade do atraso for do **CLIENTE**: O operador de `FATURAMENTO` cadastra a Nota de Débito faturada ao cliente. O status migra para `ENCERRADO_COM_AUTO`.
    *   Se a responsabilidade do atraso for do **SISTEMA**: A empresa assume o prejuízo, o status transiciona diretamente para `ENCERRADO_COM_AUTO` sem a necessidade de emissão de Nota de Débito.

```
[FATURAMENTO] Upload CT-e ──> (REGISTRADO) 
    │ (Sistema avalia porto de destino)
    └──> se Porto Monitorado ──> Envia e-mail ──> (AGUARDANDO_DESEMBARACO)
            │ (Fiscal faz upload do Auto)
            └──> (AUTO_RECEBIDO) ──> Alerta e-mail para DOCS_FISCAL
                    │ (Docs_Fiscal apura e registra responsável)
                    └──> (EM_INVESTIGACAO) ──> (AGUARDANDO_PAGAMENTO) ──> Alerta e-mail Descarga
                            │ (Descarga insere DAR + Comprovante + Capa)
                            └──> (PAGO) ──> Alerta e-mail Faturamento
                                    │
                                    ├──> se responsabilidade = CLIENTE ──> Emite Nota Débito ──> (ENCERRADO_COM_AUTO)
                                    └──> se responsabilidade = SISTEMA ──> Absorve Custo ──────> (ENCERRADO_COM_AUTO)
```

### Fluxo B — Sem auto de infração
1.  **Etapas Iniciais:** Segue as mesmas etapas iniciais 1, 2 e 3 descritas no Fluxo A.
2.  **Constatação de Não-Autuação:** Caso a mercadoria seja liberada sem autuação do fisco estadual, o operador do perfil `DESCARGA` seleciona a opção "Sem Auto".
3.  **Encerramento Manual:** O sistema realiza o cálculo projetado de 10% do valor total das Notas Fiscais para fins estatísticos (multa evitada), exige o fornecimento de uma justificativa formal obrigatória e altera o status final do CT-e para `ENCERRADO_SEM_AUTO`.

```
[FATURAMENTO] Upload CT-e ──> (REGISTRADO)
    │ (Sistema avalia porto)
    └──> se Porto Monitorado ──> (AGUARDANDO_DESEMBARACO)
            │ (Descarga constata liberação sem autuação do fisco)
            └──> Descarga escolhe "Sem Auto" ──> Calcula 10% e exige justificativa ──> (ENCERRADO_SEM_AUTO)
```

---

## 6. Casos de Uso (Casos de Uso Principais)

| ID | Ator | Nome do Caso de Uso | Descrição |
| :--- | :--- | :--- | :--- |
| **UC01** | `FATURAMENTO` | Registrar Inclusão de CT-e | Upload do PDF do CT-e, extração via regex e revisão manual dos metadados extraídos pelo operador. |
| **UC02** | `SISTEMA` | Roteamento por Porto de Destino | Análise do porto de destino do CT-e recém-cadastrado para tomada de decisão quanto aos alertas de e-mail operacionais. |
| **UC03** | `DOCS_FISCAL` / Fiscal | Registrar Recebimento do Auto | Registro da autuação fiscal associada ao CT-e informando valores, datas e PDF do Auto de Infração. |
| **UC04** | `DOCS_FISCAL` | Registrar Investigação Fiscal | Definição da culpa (SISTEMA vs CLIENTE), preenchimento do motivo, ticket e evidência para destravar o pagamento. |
| **UC05** | `DESCARGA` | Registrar Comprovação de Pagamento | Inserção dos dados financeiros do pagamento (DAR, Comprovante e Capa) para alteração de status do processo para PAGO. |
| **UC06** | `FATURAMENTO` | Registrar Emissão de Nota de Débito | Cadastro de dados e anexo da Nota de Débito emitida para ressarcimento financeiro do cliente. |
| **UC07** | `DESCARGA` | Registrar Encerramento Sem Auto | Liberação do processo sem multa, aplicando o cálculo projetado e inserção de justificativa obrigatória. |
| **UC08** | `GESTAO` | Analisar Dashboard e Exportar Dados | Acesso visual aos gráficos de KPIs gerenciais de desempenho financeiro e exportação de relatórios dinâmicos Excel (.xlsx). |
