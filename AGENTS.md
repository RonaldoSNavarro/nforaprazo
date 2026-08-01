# AGENTS.md - NforaPrazo

Diretrizes e regras de desenvolvimento para o projeto **NforaPrazo**.

---

## 🛠️ Comandos de Build & Execução
- **Build**: `mvn clean package -DskipTests`
- **Testes**: `mvn test`
- **Docker**: `docker-compose up -d --build`

---

## 📋 Regras de Desenvolvimento
1. **Jackson MCP Compatibility**: Sempre manter `FAIL_ON_UNKNOWN_PROPERTIES = false` em mapeamentos de mensagens MCP.
2. **Integração com Cortex**: Memória e wiki do projeto gerenciadas via Cortex SSE em `http://localhost:8080/mcp/sse`.

Para mais detalhes, consulte `.agents/AGENTS.md` se disponível.
