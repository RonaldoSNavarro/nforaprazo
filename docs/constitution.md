# Constituição do Projeto (Technical Stack & Standards)

Este documento estabelece as bases tecnológicas, a arquitetura de pacotes, as decisões arquiteturais (ADRs) e os padrões de codificação adotados no desenvolvimento do sistema **NF Fora do Prazo**.

---

## 1. Stack Técnica (Definitiva)

A stack técnica foi acordada e não deve ser alterada sem uma nova Architectural Decision Record (ADR) aprovada pelo CTO:

*   **Backend:** Java 25 LTS + Spring Boot 4.1.0 + Maven
*   **Frontend:** Thymeleaf + Tailwind CSS (via CDN) + Chart.js (via CDN)
*   **Banco de Dados:** PostgreSQL 16
*   **Migrations de Banco:** Flyway (comportamento estritamente imutável após aplicadas)
*   **Segurança:** Spring Security (com autenticação via BCrypt, autorização e controle de acesso RBAC baseado em perfis mapeados por URLs)
*   **Serviço de E-mail:** Spring Mail / JavaMailSender (envios assíncronos)
*   **Leitura de PDF:** Apache PDFBox 3.x (CT-e, DAR e comprovantes de pagamento)
*   **Exportação de Relatórios:** Apache POI (geração de arquivos Excel `.xlsx`)
*   **Deploy / Infraestrutura:** VPS Ubuntu 24 + Docker + Nginx (ambiente de produção)

---

## 2. Estrutura do Projeto

A organização de pacotes segue o padrão Spring Boot sob o pacote raiz `com.sistema.nforaprazo`:

```
com.sistema.nforaprazo
├── config/       # Configurações globais (SecurityConfig, MailConfig, FileStorageConfig, etc.)
├── controller/   # Controladores Web Thymeleaf — apenas roteamento, sem lógica de negócio
├── service/      # Camada de serviços — onde reside toda a lógica de negócio da aplicação
├── repository/   # Interfaces JPA de persistência de dados (Spring Data JPA) — sem lógica
├── model/        # Entidades de banco de dados JPA e enums associados
├── dto/          # Objetos de Transferência de Dados (Request/Response DTOs) — impede exposição direta das entidades na camada web
└── exception/    # Mecanismo global de tratamento de erros (GlobalExceptionHandler)
```

---

## 3. Decisões Técnicas (ADRs)

### ADR-001 — Extração de dados do CT-e PDF
*   **Status:** DECIDIDO
*   **Decisão:** Opção C — Interface com formulário manual como MVP, com extração parcial automatizada (chave de acesso com 44 dígitos extraída via regex com Apache PDFBox).
*   **Justificativa:** Os layouts de CT-e variam significativamente entre diferentes emissores e concessionárias de transporte, o que inviabiliza uma extração 100% automatizada e livre de falhas no curto prazo. O preenchimento/revisão manual garante que a operação não seja travada por erros de parsing de arquivos.

### ADR-002 — Armazenamento de PDFs
*   **Status:** DECIDIDO
*   **Decisão:** Utilização do sistema de arquivos local do host ou container Docker, utilizando UUIDs gerados aleatoriamente como nomes de arquivo.
*   **Justificativa:** É a opção mais simples e rápida de implementar para o MVP.
*   **Consequências / Migração futura:** O design do `FileStorageService` deve ser abstrato o suficiente para permitir a migração transparente para um provedor de object storage em nuvem (como AWS S3 ou MinIO) sem impactar as camadas de negócio.

### ADR-003 — Optimistic Locking para eventos financeiros
*   **Status:** DECIDIDO
*   **Decisão:** Implementação de optimistic locking nas entidades de controle de pagamento adicionando uma coluna `versao` marcada com `@Version` do Hibernate/JPA.
*   **Justificativa:** Previne condições de corrida (*race conditions*) e duplo-submit em operações financeiras e de desembaraço sem incorrer no custo de performance de travas de banco (pessimistic locks) ou travamento de threads.

### ADR-004 — Acesso em Rede Local (LAN) para Uso Residencial
*   **Status:** DECIDIDO (Aprovado pelo CTO e usuário)
*   **Decisão:** Utilizar a porta padrão mapeada `8080:8080` vinculada a todas as interfaces de rede (`0.0.0.0`) do host containerizado para possibilitar o acesso local a partir de outros computadores/dispositivos móveis conectados na mesma rede (LAN).
*   **Justificativa:** Permite testes e validação integrada sem os custos e a complexidade de configuração de infraestrutura em nuvem pública ou contratação de domínios externos.
*   **Consequências:** Como o tráfego residencial trafega via protocolo HTTP sem criptografia SSL/TLS nativa, a solução é limitada a ambientes locais controlados. Recomenda-se configurar IP estático para o servidor host no roteador local para estabilidade do endpoint de acesso.

---

## 4. Padrões de Código (Coding Standards)

A equipe deve seguir estritamente as regras de codificação e nomenclatura abaixo:

*   **Regra #1:** Lógica de negócio pertence **APENAS** à camada `@Service`. Os controladores (`@Controller`) devem atuar unicamente como orquestradores de requisição/resposta e roteamento de templates HTML.
*   **Regra #2:** Credenciais de banco de dados, servidores de e-mail e caminhos de diretório devem ser configurados obrigatoriamente através de variáveis de ambiente (`${VAR_NAME}`) no Spring Boot, nunca sendo expostas no código (hardcoded).
*   **Regra #3:** As migrations de banco de dados Flyway são estritamente **IMUTÁVEIS** após aplicadas em qualquer ambiente. Correções ou alterações de banco devem sempre ser aplicadas criando uma nova migration sequencial (`V{n+1}__descricao.sql`).
*   **Regra #4:** Transferência de dados entre a camada de apresentação (Controller) e serviços (Service) deve ser feita prioritariamente por **DTOs**, evitando a exposição e vazamento direto de Entidades JPA para a camada web Thymeleaf.
*   **Regra #5:** Exceções devem ser capturadas e tratadas de forma unificada e transparente na classe anotada com `@ControllerAdvice` (`GlobalExceptionHandler`), proibindo-se o uso de blocos `catch` vazios ou silenciosos que escondam erros.

### Padrão de Nomenclatura
*   **Classes Java:** Deve ser usado `PascalCase` em Português (Ex: `Usuario`, `CteInclusao`).
*   **Métodos e Variáveis:** Deve ser usado `camelCase` em Português (Ex: `buscarPorEmail()`, `chaveAcesso`).
*   **Migrations Flyway:** Deve ser usado o prefixo `V{n}__` seguido do nome em `snake_case` (Ex: `V12__create_nota_debito_and_enc_sem_auto.sql`).
