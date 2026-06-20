# Model Context Protocol (MCP) Server - MiniProntuário

Este diretório contém o servidor MCP do **MiniProntuário Odontológico**, desenvolvido em Python utilizando a especificação de comunicação JSON-RPC via entrada/saída padrão (`stdio`).

Ele permite que assistentes baseados em Inteligência Artificial (como o Claude Desktop ou outros clientes MCP) acessem informações clínicas de pacientes e registrem novos procedimentos utilizando linguagem natural.

## Ferramentas Expostas (Tools)

O servidor MCP expõe duas ferramentas principais:

1. **`get_patient_anamnese`**:
   - **Descrição**: Busca a ficha de anamnese, doenças sistêmicas, medicamentos em uso, alergias e todo o histórico de procedimentos registrados de um paciente a partir do seu ID UUID.
   - **Parâmetros**:
     - `patientId` (string, obrigatório): ID UUID do paciente no banco de dados.

2. **`add_procedure`**:
   - **Descrição**: Registra um novo procedimento odontológico como concluído para um paciente na data atual.
   - **Parâmetros**:
     - `patientId` (string, obrigatório): ID UUID do paciente.
     - `description` (string, obrigatório): Descrição do procedimento realizado (ex: "Limpeza profilática", "Restauração de resina dente 24").
     - `tooth` (string, opcional): Número do dente de 11 a 85 no padrão de notação FDI.
     - `cost` (number, opcional): Valor cobrado pelo procedimento.

---

## Pré-requisitos

- Python 3.7 ou superior instalado na máquina.
- Variáveis de ambiente configuradas para autenticação.

---

## Configuração e Inicialização

O servidor lê a entrada padrão e responde via saída padrão (`stdin`/`stdout`). Para realizar as requisições à API REST do backend, ele precisa de um token JWT válido correspondente ao login de um cirurgião-dentista.

### Variáveis de Ambiente Necessárias

- `JWT_TOKEN`: Token JWT (Bearer) gerado na autenticação.
- `BACKEND_URL`: URL base do servidor Spring Boot (padrão: `http://localhost:8080`).

### Exemplo de Execução no Terminal

Para testar ou rodar manualmente a partir do terminal:

```bash
# Windows (PowerShell)
$env:JWT_TOKEN="seu_token_jwt_aqui"
$env:BACKEND_URL="http://localhost:8080"
python mcp_server.py

# Linux / macOS
export JWT_TOKEN="seu_token_jwt_aqui"
export BACKEND_URL="http://localhost:8080"
python3 mcp_server.py
```

Após iniciar, o servidor responderá a mensagens JSON-RPC. Por exemplo, enviando o seguinte JSON no console:

```json
{"jsonrpc": "2.0", "method": "tools/list", "id": 1}
```

O servidor retornará a lista de ferramentas disponíveis formatadas de acordo com o protocolo MCP.

---

## Integração com o Claude Desktop

Para integrar este servidor MCP diretamente no seu **Claude Desktop**, edite o arquivo de configuração `claude_desktop_config.json`:

- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`

Adicione a seguinte entrada sob a chave `mcpServers`:

```json
{
  "mcpServers": {
    "miniprontuario-mcp": {
      "command": "python",
      "args": [
        "C:/miniprontuario/miniprontuario-backend/miniprontuario-backend/mcp-server/mcp_server.py"
      ],
      "env": {
        "JWT_TOKEN": "INSIRA_SEU_TOKEN_JWT_AQUI",
        "BACKEND_URL": "http://localhost:8080"
      }
    }
  }
}
```

> [!IMPORTANT]
> Certifique-se de atualizar o valor de `JWT_TOKEN` após realizar o login do dentista na API para permitir que o Claude acesse os prontuários de forma autorizada.
