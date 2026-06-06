# NF Fora do Prazo — Sistema de Controle de CT-e Complementar

O **NF Fora do Prazo** é um sistema web corporativo desenvolvido para gerenciar o fluxo operacional e financeiro de inclusões extemporâneas de Notas Fiscais (NF) em Conhecimentos de Transporte Eletrônicos (CT-e) complementares, automatizando o controle de prazos da SEFAZ, apuração de responsabilidades e emissão de cobranças de reembolso de multas.

---

## 🛠️ Stack Tecnológica

- **Backend:** Java 21 / Spring Boot 3.4 / Maven
- **Segurança:** Spring Security (BCrypt, controle de acesso baseado em Roles por URL)
- **Banco de Dados:** PostgreSQL 16 / Migrações automatizadas via Flyway
- **Frontend:** Thymeleaf / Tailwind CSS (via CDN) / Chart.js (via CDN)
- **Extração de Dados:** Apache PDFBox 3.x (extração de chaves de acesso a partir de PDFs de CT-e)
- **Notificações:** Spring Mail (disparos de e-mail assíncronos via `@Async`)
- **Containers:** Docker / Docker Compose

---

## ⚙️ Máquina de Estados (Ciclo de Vida do CT-e)

O sistema é estruturado sobre uma máquina de estados rígida para garantir integridade e auditoria em todas as etapas de desembaraço e cobrança financeira:

```
[AGUARDANDO_DESEMBARACO]
         │
         ├──► (SEFAZ Liberado sem multa) ──► [ENCERRADO_SEM_AUTO]
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
                     └──► (Responsabilidade: ALIANÇA) ──► [ENCERRADO_COM_AUTO] (Faturamento absorve o custo)
```

---

## 👥 Perfis de Acesso e Credenciais de Teste

Todos os usuários de teste compartilham a senha padrão: **`Test@123`**

| Perfil | E-mail | Escopo de Ação |
| :--- | :--- | :--- |
| **FATURAMENTO** | `faturamento@alianca.com` | Upload inicial de CT-e complementar, emissão de Nota de Débito (ND) e encerramento de custos absorvidos. |
| **DESCARGA** | `descarga@alianca.com` | Confirmação de desembaraço, registro de Auto de Infração, upload de DAR e upload do Comprovante de Pagamento. |
| **DOCS_FISCAL** | `fiscal@alianca.com` | Lógica de investigação: apuração de responsabilidade (Aliança ou Cliente), justificativa e amarração de ticket Movidesk. |
| **GESTAO** | `gestao@alianca.com` | Acesso ao Dashboard geral com gráficos, KPIs e relatórios gerenciais de performance operacional e financeira. |

---

## 🚀 Como Executar o Projeto

O projeto é configurado para rodar em containers Docker, persistindo os dados em um banco de dados PostgreSQL rodando na máquina host.

### Pré-requisitos
- Docker e Docker Compose instalados na máquina.
- PostgreSQL rodando localmente no host na porta `5432` com uma base de dados chamada `nfora_prazo_dev` (usuário: `postgres`, senha: `dev` ou conforme variáveis de ambiente).

### Passos para Inicialização

1. Certifique-se de que o PostgreSQL local está rodando no host e aceitando conexões.
2. Crie ou configure um arquivo `.env` na raiz do projeto contendo as credenciais de e-mail e diretório de uploads:
   ```env
   DB_USER=postgres
   DB_PASSWORD=sua_senha
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USER=seu.email@gmail.com
   MAIL_PASS=sua_senha_de_aplicativo
   ```
3. Suba o container da aplicação:
   ```bash
   docker compose up --build -d app
   ```
4. Acesse o sistema pelo navegador:
   - **URL:** [http://localhost:8080](http://localhost:8080)
   - A inicialização irá executar automaticamente todas as 13 migrações estruturais e de sementes do Flyway.

---

## 🧪 Como Executar os Testes Automatizados

O sistema conta com uma suíte de **23 testes unitários e de integração** validando todas as regras fiscais críticas de multas, o roteamento automático de portos monitorados e as restrições da máquina de estados do CT-e.

Para executar toda a suíte de testes utilizando o Docker (sem precisar do Maven instalado localmente no host Windows), execute o comando a seguir no terminal:

```bash
docker run --rm -v f:/Dev/Projetos/nforaprazo:/app -v C:/Users/ronal/.m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-21 mvn test
```

---

## 🔒 Regras de Negócio e Auditoria Críticas

1. **Reembolso Exato (RN02):** O valor cobrado na Nota de Débito ao cliente deve ser exatamente igual ao valor pago registrado no DAR correspondente.
2. **Justificativa de Fechamento:** Qualquer encerramento sem auto de infração exige uma justificativa de auditoria contendo no mínimo 15 caracteres.
3. **E-mails Assíncronos:** Toda transição crítica de status dispara alertas em segundo plano para os perfis operacionais e gera logs detalhados na tabela `log_alerta` (status `ENVIADO` ou `ERRO`).
4. **Validação de Entrada:** Todas as submissões de formulários utilizam Beans DTOs validados via Spring `@Valid` e `@ModelAttribute`, blindando a camada JPA.
