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

## REST API Reference (Cortex Fallback)

If MCP SSE transport is unavailable, memory operations can be called via REST at `POST http://127.0.0.1:8080/api/execute`. All responses use `{ "status": "success"|"error", "output": "..." }`.

### Common Fields
- `project` (string, required): `"nforaprazo"`
- `command` (string, required): `query`, `capture`, `write_page`, `consolidate`, `lint`, `handoff`, `promote_rules`, `init`, `bootstrap`

### Examples

**query** — search project memory:
```json
{ "project": "nforaprazo", "command": "query", "terms": "monitoramento fiscal" }
```
> Note: `"query"` is also accepted as an alias for `"terms"`.

**capture** — save raw context:
```json
{ "project": "nforaprazo", "command": "capture", "type": "gotcha", "content": "Discovered that..." }
```

### Encoding Notice for Windows / PowerShell
Always send request bodies in UTF-8:
```powershell
$json = @{ project = 'nforaprazo'; command = 'query'; terms = 'monitoramento' } | ConvertTo-Json
$body = [System.Text.Encoding]::UTF8.GetBytes($json)
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/execute' -Method Post -ContentType 'application/json; charset=utf-8' -Body $body
```
