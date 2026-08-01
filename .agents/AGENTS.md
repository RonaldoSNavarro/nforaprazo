# Project Rules: NforaPrazo

- **Build**: `mvn clean package -DskipTests`
- **Testes**: `mvn test`
- **MCP Server**: Conectado ao Cortex via SSE em `http://localhost:8080/mcp/sse`.
- **Jackson Deserialization**: Configurar `FAIL_ON_UNKNOWN_PROPERTIES = false`.
