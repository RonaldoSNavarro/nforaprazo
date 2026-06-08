# NF Fora do Prazo — Sistema de Controle de CT-e Complementar

O **NF Fora do Prazo** é um sistema web corporativo desenvolvido para gerenciar o fluxo operacional e financeiro de inclusões extemporâneas de Notas Fiscais (NF) em Conhecimentos de Transporte Eletrônicos (CT-e) complementares. A ferramenta automatiza o controle de prazos da SEFAZ, apuração de responsabilidades, auditoria interna e emissão de cobranças de reembolso de multas.

---

## 🛠️ Stack Tecnológica

- **Backend:** Java 21 / Spring Boot 3.4 / Maven
- **Segurança:** Spring Security (BCrypt, controle de acesso baseado em Roles por URL, troca obrigatória de senha e filtro de sessão)
- **Banco de Dados:** PostgreSQL 16 / Migrações automatizadas via Flyway
- **Frontend:** Thymeleaf / Tailwind CSS (via CDN) / Chart.js (via CDN)
- **Extração de Dados:** Apache PDFBox 3.x (extração de chaves de acesso a partir de PDFs de CT-e)
- **Exportação de Dados:** Apache POI (geração dinâmica de relatórios em Excel `.xlsx`)
- **Notificações:** Spring Mail (disparos de e-mail assíncronos via `@Async` e rastreabilidade em log)
- **Auditoria:** Spring AOP (aspectos customizados interceptando e registrando ações no banco)
- **Containers:** Docker / Docker Compose

---

## ⚙️ Máquina de Estados (Ciclo de Vida do CT-e)

O sistema é estruturado sobre uma máquina de estados rígida para garantir integridade e auditoria em todas as etapas de desembaraço e cobrança financeira:

```
[AGUARDANDO_DESEMBARACO]
         │
         ├──► (SEFAZ Liberado sem multa) ──► [ENCERRADO_SEM_AUTO] (Exige justificativa)
         │
         └──► (SEFAZ Autuado com Multa)
                     │
                     ▼
             [AUTO_RECEBIDO]
                     │
                     ▼
             [EM_INVESTIGACAO] (Docs Fiscal investiga responsabilidade)
                     │
                     ▼
           [AGUARDANDO_PAGAMENTO] (Criação de DAR/Guia de pagamento)
                     │
                     ▼
                  [PAGO] (Descarga anexa DAR + Comprovante de pagamento)
                     │
                     ├──► (Responsabilidade: CLIENTE) ──► [ENCERRADO_COM_AUTO] (Faturamento emite Nota de Débito)
                     │
                     └──► (Responsabilidade: SISTEMA) ──► [ENCERRADO_COM_AUTO] (Faturamento absorve o custo)
```

---

## 👥 Perfis de Acesso e Credenciais

O controle de acesso é baseado em privilégios específicos para cada perfil de atuação:

| Perfil | E-mail Padrão | Escopo de Ação |
| :--- | :--- | :--- |
| **ADMINISTRADOR** | `admin@sistema.local` / `admin123` | Gerenciamento de usuários, cadastro de portos monitorados dinâmicos, visualização de logs de auditoria e painel de observabilidade. |
| **FATURAMENTO** | *(Cadastrado pelo Admin)* | Upload inicial de CT-e complementar, emissão de Nota de Débito (ND) e encerramento de custos absorvidos. |
| **DESCARGA** | *(Cadastrado pelo Admin)* | Confirmação de desembaraço, registro de Auto de Infração, upload de DAR, upload de Comprovante de Pagamento e encerramento sem auto. |
| **DOCS_FISCAL** | *(Cadastrado pelo Admin)* | Lógica de investigação: apuração de responsabilidade (Sistema ou Cliente), justificativa e amarração de ticket de suporte. |
| **GESTAO** | *(Cadastrado pelo Admin)* | Acesso ao painel gerencial separado e relatórios analíticos/exportações. |

---

## 📊 Painel de Gestão e Relatórios

A área de Gestão foi desmembrada em duas telas independentes para otimizar o fluxo de análise operacional e financeira:

1. **Dashboard Executivo (`/dashboard`):** Focado exclusivamente na consolidação e visualização de dados executivos:
   - **KPIs Resumidos:** Exposição Total (multas potenciais ativas), Multas Efetivamente Pagas e Multas Evitadas (custos mitigados no fluxo sem auto).
   - **Gráficos Dinâmicos (Chart.js):** Evolução Mensal de Autuações (valores e quantidades) e Distribuição de Responsabilidade (Prejuízo Sistema vs Reembolso Cliente vs Pendentes).
   - **Top 5 Tomadores Reincidentes:** Ranking ordenado de clientes com maior volume de infrações geradas.

2. **Relatórios Consolidados (`/relatorios`):** Tela voltada a filtros e extração de dados:
   - **Filtros Avançados:** Busca em tempo real combinando Porto de Destino, Data de Início e Data de Fim (período de upload).
   - **Tabela de Pré-visualização:** Grid com número do CT-e, chave de acesso de 44 dígitos, tomador, portos de origem/destino, valor do CT-e, valor potencial da multa, booking, data de upload e status atual.
   - **Exportação Dinâmica:** Botão de exportação para planilha Excel (`.xlsx`) respeitando exatamente os filtros de busca aplicados em tela.

---

## 🔧 Portos Monitorados Dinâmicos

Diferente do comportamento anterior onde as regras de portos que disparavam o fluxo de descarga eram fixas em código, agora o administrador do sistema dispõe de uma interface administrativa dedicada (`/admin/portos`):
- Cadastro de portos ativos e inativos em tempo real.
- Roteamento automático de novos uploads de CT-e de acordo com a lista parametrizada no banco de dados.

---

## 🚀 Como Executar o Projeto

A aplicação é executada em um container Docker, acessando a base de dados PostgreSQL local.

### Pré-requisitos
- Docker e Docker Compose instalados.
- PostgreSQL rodando localmente no host na porta `5432` com uma base de dados chamada `nfora_prazo_dev` (usuário: `postgres`, senha: `dev` ou conforme variáveis de ambiente).

### Passos para Inicialização

1. Certifique-se de que o PostgreSQL local está rodando no host e aceitando conexões externas (ou via IP/gateway).
2. Crie ou configure um arquivo `.env` na raiz do projeto contendo as credenciais de e-mail e diretório de uploads:
   ```env
   DB_USER=postgres
   DB_PASSWORD=dev
   MAIL_HOST=smtp.mailtrap.io
   MAIL_PORT=2525
   MAIL_USER=seu_usuario
   MAIL_PASS=sua_senha
   ```
3. Execute a construção e inicialização do contêiner da aplicação:
   ```bash
   docker compose up --build -d
   ```
4. Acesse o sistema pelo navegador na URL: [http://localhost:8080](http://localhost:8080)
   - O Flyway executará automaticamente as migrações estruturais e de dados no banco de dados.

---

## 🧪 Como Executar os Testes Automatizados

O sistema possui uma suíte robusta com **43 testes unitários e de integração** cobrindo todas as regras fiscais críticas de multas, a máquina de estados do CT-e, auditoria por AOP, portos monitorados e segurança de troca de senhas.

### Executando pelo Maven Wrapper (Local)
No diretório raiz do projeto, utilize o executável do Maven Wrapper:
```bash
.mvn\wrapper\maven\bin\mvn.cmd test
```

### Executando via Docker
Caso não possua o Maven instalado localmente na máquina, você pode rodar os testes em um contêiner temporário:
```bash
docker run --rm -v "f:/Dev/Projetos/nforaprazo:/app" -v "C:/Users/ronal/.m2:/root/.m2" -w /app maven:3.9-eclipse-temurin-21 mvn test
```

---

## 🔒 Auditoria, Segurança e Observabilidade

1. **Trilha de Auditoria (AOP):** Interceptores baseados no aspecto `@Auditable` capturam o usuário, a ação, os parâmetros de entrada (ocultando credenciais e senhas) e gravam no banco na tabela `log_atividade`.
2. **Troca Obrigatória de Senha:** Usuários criados com senha temporária são interceptados pelo `ForcarSenhaFiltro` e redirecionados para `/alterar-senha`, bloqueando qualquer acesso a outras áreas do sistema até que cadastrem uma nova credencial segura.
3. **Observabilidade de Saúde:** Integração com o Spring Boot Actuator exposto nas rotas `/actuator/health` e `/actuator/metrics`, com uma interface amigável de monitoramento no painel de administração (`/admin/observabilidade`).
