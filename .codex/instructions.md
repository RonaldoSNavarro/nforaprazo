# OpenAI Codex Instructions for NforaPrazo

## Overview
NforaPrazo is a Java 21 Spring Boot application.

## Build Commands
- `mvn clean package -DskipTests`
- `mvn test`
- `docker-compose up -d --build`

## Cortex MCP Integration
Connected to Cortex SSE at `http://127.0.0.1:8080/mcp/sse`.
Use `query`, `capture`, `write_page`, `promote_rules` to interact with project memory.
