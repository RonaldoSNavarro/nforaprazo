# CLAUDE.md - NforaPrazo

O **NforaPrazo** é um projeto Java 25 LTS com Spring Boot 4.1.0 para gerenciamento e notificação de notas fiscais emitidas fora do prazo legal.

---

## 🛠️ Comandos de Desenvolvimento

### Build e Testes (Maven / Java 25)
```bash
# Compilar o projeto e gerar o JAR
mvn clean package -DskipTests

# Executar testes unitários
mvn test
```

### Execução da Aplicação
```bash
# Rodar via Spring Boot
mvn spring-boot:run

# Subir via Docker Compose (Porta 8082)
docker-compose up -d --build
```

---

## 🔌 Conexão ao Cérebro de Memória (Cortex MCP)

Este projeto utiliza o **Cortex** como servidor de memória ativa. As ferramentas MCP (`query`, `capture`, `write_page`, `promote_rules`) estão conectadas via SSE em `http://localhost:8080/mcp/sse`.

## 📜 Diretrizes de Código
- Java 25 LTS, Spring Boot 4.1.0, Spring Data JPA, Lombok.
- Manter o Jackson configurado com `FAIL_ON_UNKNOWN_PROPERTIES = false` para requisições de IA.
