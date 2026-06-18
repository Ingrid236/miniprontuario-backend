import sys
import json
import urllib.request
import urllib.error
from datetime import date

if sys.version_info >= (3, 7):
    sys.stdout.reconfigure(encoding='utf-8')

JWT_TOKEN = ""
BACKEND_URL = "http://localhost:8080"

def log_debug(msg):
    sys.stderr.write(f"[DEBUG] {msg}\n")
    sys.stderr.flush()

def make_request(path, method="GET", data=None):
    url = f"{BACKEND_URL}{path}"
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json"
    }
    if JWT_TOKEN:
        headers["Authorization"] = f"Bearer {JWT_TOKEN}"

    req_data = None
    if data is not None:
        req_data = json.dumps(data).encode("utf-8")

    req = urllib.request.Request(url, data=req_data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as res:
            return json.loads(res.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        error_msg = e.read().decode("utf-8")
        log_debug(f"HTTPError: {e.code} - {error_msg}")
        try:
            return json.loads(error_msg)
        except Exception:
            return {"error": f"HTTP {e.code}: {e.reason}", "message": error_msg}
    except Exception as e:
        log_debug(f"Request Error: {str(e)}")
        return {"error": "Connection failed", "message": str(e)}

def handle_get_patient_anamnese(patient_id):
    patient = make_request(f"/patients/{patient_id}")
    if "error" in patient or "errors" in patient:
        return f"Erro ao buscar paciente: {patient.get('message', 'Não encontrado')}"

    procedures = make_request(f"/patients/{patient_id}/procedures")
    if isinstance(procedures, dict) and ("error" in procedures or "errors" in procedures):
        procedures = []

    lines = []
    lines.append(f"# Prontuário Clínico: {patient.get('name')}")
    lines.append(f"- **CPF**: {patient.get('cpf')}")
    lines.append(f"- **Data de Nascimento**: {patient.get('birthDate')}")
    lines.append(f"- **Telefone**: {patient.get('phone', 'Nenhum')}")
    lines.append(f"- **Alergias**: {patient.get('allergies', 'Nenhuma')}")
    lines.append(f"- **Doenças Sistêmicas**: {patient.get('systemicDiseases', 'Nenhuma')}")
    lines.append(f"- **Medicamentos em Uso**: {patient.get('medications', 'Nenhum')}")
    lines.append("")
    lines.append("## Histórico de Procedimentos")
    if not procedures:
        lines.append("- Nenhum procedimento registrado.")
    else:
        for p in procedures:
            tooth_str = f" (Dente: {p.get('tooth')})" if p.get('tooth') else ""
            cost_str = f" - R$ {p.get('cost')}" if p.get('cost') else ""
            lines.append(f"- **{p.get('date')}**: {p.get('description')}{tooth_str}{cost_str} [{p.get('status')}]")
            if p.get('notes'):
                lines.append(f"  *Observações: {p.get('notes')}*")

    return "\n".join(lines)

def handle_add_procedure(patient_id, description, tooth=None, cost=None):
    today = date.today().isoformat()
    payload = {
        "date": today,
        "description": description,
        "status": "COMPLETED"
    }
    if tooth:
        payload["tooth"] = str(tooth)
    if cost:
        payload["cost"] = float(cost)

    res = make_request(f"/patients/{patient_id}/procedures", method="POST", data=payload)
    if "error" in res or "errors" in res:
        return f"Erro ao criar procedimento: {res.get('message', 'Verifique os dados informados')}"

    tooth_info = f" dente {res.get('tooth')}" if res.get('tooth') else ""
    return f"Procedimento '{res.get('description')}'{tooth_info} registrado com sucesso para o paciente em {res.get('date')}."

def main():
    global JWT_TOKEN, BACKEND_URL
    import os
    JWT_TOKEN = os.environ.get("JWT_TOKEN", "")
    BACKEND_URL = os.environ.get("BACKEND_URL", "http://localhost:8080")

    log_debug("MCP Server started successfully.")

    for line in sys.stdin:
        if not line.strip():
            continue
        try:
            req = json.loads(line)
            req_id = req.get("id")
            method = req.get("method")

            if method == "initialize":
                res = {
                    "jsonrpc": "2.0",
                    "result": {
                        "protocolVersion": "2024-11-05",
                        "capabilities": {
                            "tools": {}
                        },
                        "serverInfo": {
                            "name": "miniprontuario-mcp",
                            "version": "1.0"
                        }
                    },
                    "id": req_id
                }
            elif method == "tools/list":
                res = {
                    "jsonrpc": "2.0",
                    "result": {
                        "tools": [
                            {
                                "name": "get_patient_anamnese",
                                "description": "Busca a anamnese, ficha clínica e histórico de procedimentos de um paciente pelo seu ID UUID.",
                                "inputSchema": {
                                    "type": "object",
                                    "properties": {
                                        "patientId": {
                                            "type": "string",
                                            "description": "ID UUID do paciente"
                                        }
                                    },
                                    "required": ["patientId"]
                                }
                            },
                            {
                                "name": "add_procedure",
                                "description": "Registra um novo procedimento odontológico concluído para um paciente no dia de hoje.",
                                "inputSchema": {
                                    "type": "object",
                                    "properties": {
                                        "patientId": {
                                            "type": "string",
                                            "description": "ID UUID do paciente"
                                        },
                                        "description": {
                                            "type": "string",
                                            "description": "Descrição do procedimento (ex: Restauração de resina, Limpeza, Profilaxia)"
                                        },
                                        "tooth": {
                                            "type": "string",
                                            "description": "Opcional: Número do dente no padrão FDI (ex: 18, 23, 44)"
                                        },
                                        "cost": {
                                            "type": "number",
                                            "description": "Opcional: Valor monetário do procedimento"
                                        }
                                    },
                                    "required": ["patientId", "description"]
                                }
                            }
                        ]
                    },
                    "id": req_id
                }
            elif method == "tools/call":
                params = req.get("params", {})
                tool_name = params.get("name")
                args = params.get("arguments", {})

                if tool_name == "get_patient_anamnese":
                    res_text = handle_get_patient_anamnese(args.get("patientId"))
                    res = {
                        "jsonrpc": "2.0",
                        "result": {
                            "content": [
                                {
                                    "type": "text",
                                    "text": res_text
                                }
                            ]
                        },
                        "id": req_id
                    }
                elif tool_name == "add_procedure":
                    res_text = handle_add_procedure(
                        args.get("patientId"),
                        args.get("description"),
                        tooth=args.get("tooth"),
                        cost=args.get("cost")
                    )
                    res = {
                        "jsonrpc": "2.0",
                        "result": {
                            "content": [
                                {
                                    "type": "text",
                                    "text": res_text
                                }
                            ]
                        },
                        "id": req_id
                    }
                else:
                    res = {
                        "jsonrpc": "2.0",
                        "error": {
                            "code": -32601,
                            "message": f"Method not found: {tool_name}"
                        },
                        "id": req_id
                    }
            else:
                res = {
                    "jsonrpc": "2.0",
                    "error": {
                        "code": -32601,
                        "message": f"Method not found: {method}"
                    },
                    "id": req_id
                }

            sys.stdout.write(json.dumps(res) + "\n")
            sys.stdout.flush()

        except Exception as e:
            log_debug(f"Error handling request: {str(e)}")

if __name__ == "__main__":
    main()
