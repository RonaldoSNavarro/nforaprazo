# NF Fora do Prazo — Sistema de Controle de CT-e Complementar

O **NF Fora do Prazo** é um sistema web corporativo desenvolvido para gerenciar o fluxo operacional e financeiro de inclusões extemporâneas de Notas Fiscais (NF) em Conhecimentos de Transporte Eletrônicos (CT-e) complementares. A ferramenta automatiza o controle de prazos da SEFAZ, apuração de responsabilidades, auditoria interna, leitura inteligente de PDFs e emissão de cobranças de reembolso de multas.

---

## 🛠️ Stack Tecnológica

- **Backend:** Java 21 / Spring Boot 3.4 / Maven
- **Segurança:** Spring Security (BCrypt, controle de acesso baseado em Roles por URL, troca obrigatória de senha e filtro de sessão)
- **Banco de Dados:** PostgreSQL 16 / Migrações automatizadas via Flyway (`V1` a `V17`)
- **Frontend:** Thymeleaf / Vanilla CSS / Tailwind CSS (via CDN) / Chart.js (via CDN)
- **Extração Inteligente de PDFs:** Apache PDFBox 3.x (extração automática em tempo real de DACTE/CT-e, DAR e Comprovantes via AJAX)
- **Exportação de Dados:** Apache POI (geração dinâmica de relatórios em Excel `.xlsx`)
- **Notificações:** Spring Mail (disparos de e-mail assíncronos via `@Async` e rastreabilidade em log)
- **Auditoria:** Spring AOP (aspectos customizados interceptando e registrando ações no banco)
- **Containers:** Docker / Docker Compose

---

## ⚙️ Máquina de Estados (Ciclo de Vida do CT-e)

O sistema é estruturado sobre uma máquina de estados rígida para garantir integridade e auditoria em todas as etapas de desembaraço e cobrança financeira:

```
                  [PENDENTE] (Upload com Autorização de Aceitação de Custo)
                      │
                      ├──► (Confirmação de Desembaraço) ──► [DESEMBARACADO]
                      │                                            │
                      │                                            ├──► [ENCERRADO_SEM_AUTO] (Justificativa)
                      │                                            │
                      └──► (SEFAZ Autuado com Multa) ──────────────┴──► [AUTO_RECEBIDO]
                                                                              │
                                                                              ▼
                                                                      [EM_INVESTIGACAO] (Docs Fiscal investiga)
                                                                              │
                                                                              ▼
                                                                    [AGUARDANDO_PAGAMENTO] (Criação do DAR)
                                                                              │
                                                                              ▼
                                                                           [PAGO] (Anexação DAR + Comprovante)
                                                                              │
                                                                              ├──► (Responsabilidade: CLIENTE) ──► [ENCERRADO_COM_AUTO] (Emissão de Nota de Débito)
                                                                              │
                                                                              └──► (Responsabilidade: EMPRESA_INTERNO / SISTEMA) ──► [ENCERRADO_COM_AUTO] (Custo Interno Absorvido)
```

---

## 📋 Funcionalidades Principais Recentes

1. **Leitura e Auto-Preenchimento Automático de CT-e (DACTE):**
   - No momento do upload, o sistema lê o arquivo PDF via AJAX/PDFBox em tempo real.
   - Preenche automaticamente: *Chave de Acesso*, *Número do CT-e*, *Nº Booking*, *Container* (formato `AAAA1234567`), *Qtd. NFs Incluídas*, *Valor da Carga*, *Navio / Viagem / Direção* e *Observações*.
   - Exige o envio obrigatório da **Autorização de Aceitação de Custo** (PDF ou e-mail) em modal de confirmação com validação HTML5 acessível (`disabled` dinâmico em campos ocultos para evitar erros de foco e `sr-only` em áreas de dropzone).

2. **Gestão de CT-es Pendentes e Pagamentos na Descarga:**
   - Rotas dedicadas na equipe de Descarga: `/descarga/pendentes` (processamento inicial e desembaraço) e `/descarga/pagamentos` (anexo de DAR e comprovante).
   - Botões na coluna **Ação Requerida** habilitados para registros em status `PENDENTE`, `AGUARDANDO_DESEMBARACO` e `REGISTRADO`.
   - Card dedicado no Dashboard Executivo e no menu lateral (`/faturamento/pendentes-pagamento` e `/descarga/pagamentos`).

3. **Leitura Automática de DAR & Comprovantes:**
   - Extração automática do valor pago diretamente dos PDFs de guias DAR/DARE anexadas via regex especialista em PDFBox.

4. **Apuração de Responsabilidade & Encerramento:**
   - Classificação entre **Interno/Empresa** (`EMPRESA_INTERNO`) e **Cliente** (`CLIENTE`).
   - Bloqueio de emissão de Nota de Débito quando o custo for interno/empresa (apenas contabiliza custo absorvido). Habilitação de emissão exclusiva para cobranças do cliente.

5. **Painel Executivo e DTOs Serializáveis:**
   - DTOs concretos (`ReincidenteDto`, `AutoInfracaoMensalDto`, `ResponsabilidadeDto`) garantindo serialização limpa de custos acumulados e gráficos sem valores nulos.

---

## 👥 Perfis de Acesso e Credenciais

| Perfil | E-mail Padrão | Escopo de Ação |
| :--- | :--- | :--- |
| **ADMINISTRADOR** | `admin@sistema.local` / `admin123` | Gerenciamento de usuários, cadastro de portos monitorados dinâmicos, visualização de logs de auditoria e painel de observabilidade. |
| **FATURAMENTO** | *(Cadastrado pelo Admin)* | Upload de CT-e com autorização de custo, autos pendentes de pagamento, emissão de Nota de Débito (ND) e encerramento de custos internos absorvidos. |
| **DESCARGA** | *(Cadastrado pelo Admin)* | Confirmação de desembaraço, registro de Auto de Infração, upload de DAR com leitura automática, upload de Comprovante e encerramento sem auto. |
| **DOCS_FISCAL** | *(Cadastrado pelo Admin)* | Lógica de investigação: abertura direta de modal, apuração de responsabilidade (Interno/Empresa ou Cliente), justificativa e ticket. |
| **GESTAO** | *(Cadastrado pelo Admin)* | Acesso ao painel gerencial consolidado e relatórios analíticos/exportação Excel. |

---

## 🚀 Como Executar o Projeto

A aplicação é executada via Docker Compose na porta `8082`.

### Pré-requisitos
- Docker e Docker Compose instalados.
- PostgreSQL rodando localmente no host na porta `5432` com a base `nfora_prazo_dev` (usuário: `postgres`, senha: `dev`).

### Passos para Inicialização

1. Configure o arquivo `.env` na raiz do projeto contendo as credenciais do banco e servidor de e-mail:
   ```env
   SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/nfora_prazo_dev
   DB_USER=postgres
   DB_PASSWORD=dev
   FILE_UPLOAD_DIR=/app/uploads
   MAIL_HOST=smtp.terra.com.br
   MAIL_PORT=587
   MAIL_USER=seu-email@terra.com.br
   MAIL_PASS=sua-senha
   ```
2. Execute a construção e inicialização do container Docker:
   ```bash
   docker compose up --build -d
   ```
3. Acesse a aplicação na URL: [http://localhost:8082](http://localhost:8082)
   - O Flyway executará automaticamente todas as migrações estruturais (`V1` a `V17`) na inicialização.

---

## 🧪 Como Executar os Testes Automatizados

A aplicação conta com uma suíte com **45 testes unitários e de integração** cobrindo a extração de dados de PDFs reais, a máquina de estados, auditoria via AOP e segurança.

### Executando pelo Maven
```bash
mvn test
```

---

## 🔒 Auditoria, Segurança e Observabilidade

1. **Trilha de Auditoria (AOP):** Interceptores baseados no aspecto `@Auditable` capturam o usuário, a ação e os parâmetros de entrada gravando no banco na tabela `log_atividade`.
2. **Troca Obrigatória de Senha:** Intercepção pelo `ForcarSenhaFiltro` exigindo troca de senha temporária no primeiro acesso.
3. **Observabilidade:** Métricas e saúde via Spring Boot Actuator (`/actuator/health` e `/admin/observabilidade`).
