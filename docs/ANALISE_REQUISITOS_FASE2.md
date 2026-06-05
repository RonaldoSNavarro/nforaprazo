# ANÁLISE DE REQUISITOS - FASE 2 (Desembaraço SEFAZ)
> Status: 🔴 GAP CRÍTICO IDENTIFICADO
> Data: 2026-06-04
> Autor: Analista de Sistemas Sênior / CTO

## 1. Resumo Executivo
A análise da transcrição revelou um erro arquitetural na implementação atual. O processo de desembaraço e pagamento na vida real **não é atômico**. É uma cadeia de eventos sequenciais dependentes de terceiros (SEFAZ e TAX) que pode levar dias ou semanas. A implementação de um formulário único de envio (DAR + Comprovante + Auto) impossibilita a operação do sistema, pois os documentos não são gerados simultaneamente.

## 2. Fluxo Real do Processo (Máquina de Estados)
De acordo com os timestamps da transcrição:
1. **Envio para SEFAZ (`00:13 - 00:26`)**: Equipe envia CT-e + NF por e-mail.
2. **Aguardando SEFAZ (`00:26 - 00:44`)**: Prazo de até 72h úteis.
3. **Desembaraço Confirmado (`00:46 - 01:05`)**: SEFAZ retorna e operador valida no portal.
4. **Espera do Auto de Infração (`01:05 - 01:17`)**: Tempo indeterminado. Depende da fiscalização.
5. **Recebimento do Auto (`01:17 - 01:36`)**: TAX recebe e envia para Descarga. Somente aqui o PDF do Auto é anexado ao sistema.
6. **Emissão de Guia/DAR (`01:36 - 01:50`)**: Com o Auto em mãos, emite-se o boleto (DAR).
7. **Pagamento Realizado (`01:50 - 02:07`)**: Upload do Comprovante.

*"Porque são processos separados, um é independente do outro e é em sequência, em cadeia de sequência." (01:58)*

## 3. Requisitos Funcionais Extraídos
| ID | Descrição | Prioridade |
|---|---|---|
| RF-F2-01 | O sistema deve permitir sinalizar o desembaraço do CT-e de forma independente da emissão de pagamentos. | Alta |
| RF-F2-02 | O sistema deve permitir o upload do PDF do Auto de Infração como evento distinto. | Alta |
| RF-F2-03 | O sistema deve permitir o upload do PDF do DAR isoladamente. | Alta |
| RF-F2-04 | O sistema deve permitir o upload do Comprovante de Pagamento isoladamente. | Alta |

## 4. Regras de Negócio Extraídas
* **RN-F2-01**: O DAR só pode ser gerado/anexado APÓS a existência do Auto de Infração no sistema.
* **RN-F2-02**: O Comprovante só pode ser anexado APÓS a existência do DAR.

## 5. Dependências Externas
* **SEFAZ Amazonas**: Resposta em até 72h úteis (Seg-Qui 09h-15h, Sex 10h-14h).
* **Equipe TAX Interna**: Envio do Auto de Infração (sem SLA definido).

## 6. GAP Analysis (Atual vs. Realidade)
| O que o sistema faz (Fase 2 atual) | O que o processo exige | GAP |
|---|---|---|
| Exige Upload de DAR, Comprovante e Auto de Infração de uma só vez para salvar. | Os arquivos chegam em momentos diferentes. | **Crítico.** O sistema trava a operação. O operador não terá todos os PDFs no dia 1 para preencher o formulário. |
| Status pula de PENDENTE_DESCARGA direto para DESEMBARACADO. | Existem estados intermediários: Aguardando Auto, Aguardando DAR, Aguardando Pagamento. | O dashboard e os relatórios refletirão informações irreais de tempo e gargalo. |

## 7. Riscos Identificados
* **Risco Operacional**: Operadores serão forçados a fazer uploads de PDFs "dummy/vazios" só para passar pela tela e salvar o estado no sistema, destruindo a integridade dos dados e a auditoria de pagamentos.
* **Risco Financeiro**: Pagamentos de autos sem controle adequado do prazo da SEFAZ, impossibilidade de medir o tempo de reposta da SEFAZ vs tempo da equipe interna.

## 8. Próximos Passos Recomendados
1. Destruir a concepção de formulário único para pagamento.
2. Re-arquitetar o `StatusCte` para suportar a granularidade:
   * `AGUARDANDO_DESEMBARACO_SEFAZ`
   * `DESEMBARACADO` (Sem auto)
   * `AGUARDANDO_AUTO_INFRACAO`
   * `AGUARDANDO_DAR`
   * `AGUARDANDO_PAGAMENTO`
   * `PAGO`
3. Criar views distintas ou um fluxo step-by-step (timeline) na tela de detalhe do CT-e para cada etapa do processo.
