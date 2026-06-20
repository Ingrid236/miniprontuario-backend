# Relatório de Integração e Adequações de Backend e Frontend

Este relatório apresenta as especificações e as **alterações obrigatórias** a serem realizadas na API do Backend e no Aplicativo Móvel (Frontend) para atender integralmente aos requisitos de entrega do projeto acadêmico. 

---

## 1. Mudança de Paradigma: Sem Persistência Local do Domínio

A principal diretriz do projeto exige que **não exista persistência local dos dados do domínio** (Pacientes e Procedimentos) no dispositivo móvel. 
- **Situação Atual**: O aplicativo móvel foi desenvolvido com uma arquitetura offline-first usando SQLite (`sqflite_sqlcipher`).
- **Adequação**:
  - Removeremos o banco de dados SQLite local para entidades de domínio.
  - A camada de repositório do Flutter (`PatientRepository` e `ProcedureRepository`) será refatorada para fazer chamadas diretas via **Dio (HTTP REST Client)**.
  - O banco de dados local será utilizado **apenas** para o armazenamento seguro de credenciais e tokens de sessão (`access_token` e `refresh_token`) via `flutter_secure_storage`.

---

## 2. Autenticação e Persistência de Sessão (JWT)

A especificação exige um sistema de autenticação JWT duplo, com rota de rotação de token e invalidação no logout. O backend atual **não** possui suporte a refresh tokens, portanto, precisará de alterações significativas.

### 2.1. O que o Backend precisa implementar:
1. **Alteração na Resposta de Login (`/auth/login`)**:
   - Retornar um par de tokens em `AuthResponse`:
     - `accessToken`: Token JWT de curta duração (ex: 15 a 30 minutos).
     - `refreshToken`: Token JWT de longa duração (mínimo de 7 dias).
2. **Nova Rota de Refresh Token (`/auth/refresh`)**:
   - **Método**: `POST`
   - **Corpo da Requisição**: `{ "refreshToken": "string" }`
   - **Resposta**: `{ "accessToken": "novo_access_token", "refreshToken": "novo_refresh_token" }`
   - **Comportamento**: Validar o refresh token antigo e emitir um novo par.
3. **Invalidar Refresh Token no Logout (`/auth/logout` ou lógica de banco)**:
   - O backend precisa manter uma tabela ou blacklist de Refresh Tokens ativos.
   - Ao chamar a rota de logout, o refresh token deve ser marcado como revogado/deletado no servidor, impedindo que seja usado para gerar novos access tokens.

### 2.2. O que o Frontend precisa implementar:
1. **Armazenamento Seguro**:
   - Salvar o `accessToken` e o `refreshToken` utilizando `flutter_secure_storage` imediatamente após o login.
2. **Dio Interceptor (Refresh Automatizado)**:
   - Criar um `QueuedInterceptorsWrapper` no `Dio`:
     - `onRequest`: Insere automaticamente o header `Authorization: Bearer {accessToken}`.
     - `onError`: Se o backend retornar `401 Unauthorized` (access token expirado), o interceptor deve:
       1. Bloquear a fila de requisições.
       2. Realizar uma requisição síncrona para o endpoint `/auth/refresh` usando o `refreshToken` salvo.
       3. Se obtiver sucesso, salvar os novos tokens no secure storage e repetir a requisição original falhada com o novo `accessToken`.
       4. Se o refresh token também estiver expirado/inválido, limpar o storage e redirecionar o usuário para a tela de login.

---

## 3. CRUD da Entidade Principal (Mínimo de 7 Atributos, 1:N e Validações)

O relacionamento entre **Pacientes (`Patient`)** e **Procedimentos (`Procedure`)** atende ao requisito de relacionamento 1:N.

### 3.1. Paciente (Mínimo de 7 atributos)
Para garantir que a entidade principal possua 7+ atributos no payload de cadastro/banco do backend, adicionaremos o campo `medications` à API.

*Atributos propostos para o `PatientRequest` no Backend*:
1. `name` (Nome completo)
2. `cpf` (CPF - String formatada)
3. `birthDate` (Data de nascimento - YYYY-MM-DD)
4. `phone` (Telefone/Celular)
5. `allergies` (Alergias - String)
6. `systemicDiseases` (Doenças sistêmicas/crônicas)
7. `medications` (Medicamentos em uso - **Nova adição na API**)

### 3.2. Regras de Negócio Obrigatórias a Validar (Mapeamento)
A especificação exige **pelo menos 3 regras de negócio** validadas na manipulação dos dados. Implementaremos:

1. **Validação de CPF Único e Dígito Verificador (Regra 1)**:
   - **Frontend**: Validação visual usando máscara e algoritmo de dígito verificador na inserção de dados.
   - **Backend**: Verificar se o CPF possui exatamente 11 números e se não há duplicidade de CPF associado ao mesmo dentista. Retornar `400 Bad Request` com mensagem clara ("CPF já cadastrado").
2. **Consistência da Data de Nascimento (Regra 2)**:
   - **Frontend/Backend**: A data de nascimento deve ser no passado e menor que o dia atual. O usuário deve ter idade realista (ex: não permitir datas acima de 120 anos atrás).
3. **Formatação e Validação do CRO Profissional (Regra 3)**:
   - **Frontend/Backend**: O CRO deve estar atrelado a um estado da federação (ex: SP, RJ) e o número de registro deve conter apenas dígitos numéricos positivos. O backend deve rejeitar cadastros que não sigam esse padrão.
4. **Validação da Notação Dentária FDI nos Procedimentos (Regra Opcional/Adicional)**:
   - **Frontend/Backend**: Se o dente for informado no procedimento, ele deve seguir a notação internacional FDI (quadrantes 1 a 4 para adultos, 5 a 8 para crianças, dentes de 1 a 8).

---

## 4. Integração de IA e Servidor MCP (Requisitos Obrigatórios Adicionais)

### 4.1. Integração Obrigatória de IA no Aplicativo
Desenvolveremos uma funcionalidade em que a IA analise a ficha do paciente diretamente na tela.

*Fluxo Recomendado*:
1. Criar um endpoint `/ai/analyze-patient` no Backend.
2. O Flutter envia a ficha do paciente (idade, alergias, doenças crônicas/sistêmicas, medicamentos em uso e histórico de procedimentos).
3. O Backend consulta um modelo de IA (LLM via Gemini API, por exemplo) e gera um **Relatório de Risco Clínico** detalhando:
   - Possíveis interações medicamentosas com anestésicos comuns.
   - Cuidados especiais devido a alergias ou doenças sistêmicas (ex: hipertensão, diabetes).
   - Recomendações de plano de cuidado.
4. Exibir o resultado em um painel visual destacado na tela de detalhes do paciente.

### 4.2. Servidor MCP (Model Context Protocol)
Implementação de um servidor MCP no backend que exponha ferramentas (Tools) para que um modelo de IA externo possa interagir com o prontuário.

*Ferramentas Propostas para o Servidor MCP*:
1. `get_patient_anamnese(patientId)`: Retorna as informações médicas do paciente para a IA analisar.
2. `recommend_treatment_plan(patientId)`: Combina o histórico de procedimentos com as restrições de saúde do paciente para sugerir os próximos passos de tratamento ao dentista.

---

## 5. Tabela de Endpoints e Alterações no Backend

Abaixo está o mapeamento dos endpoints da API e quais alterações/implementações devem ser realizadas no backend:

| Endpoint | Método HTTP | Descrição do Endpoint | Ajustes Necessários no Backend |
| :--- | :---: | :--- | :--- |
| `/auth/register` | `POST` | Cadastro do Profissional | Nenhuma alteração. |
| `/auth/login` | `POST` | Login e geração de Tokens | **Modificar a resposta** para retornar `{ accessToken, refreshToken }`. |
| `/auth/refresh` | `POST` | **[NOVO]** Rotação do Token JWT | Criar endpoint para validar o refresh token e emitir novo par de tokens. |
| `/auth/logout` | `POST` | **[NOVO]** Revogação de Token | Criar endpoint para receber o `refreshToken` e invalidá-lo no servidor. |
| `/patients` | `GET` | Lista de Pacientes | Ajustar esquema de resposta para incluir o campo `medications`. |
| `/patients` | `POST` | Cadastro de Paciente | **Validar regras de negócio** (CPF único e data válida). Incluir campo `medications` no request. |
| `/patients/{id}` | `GET` / `DELETE` | Consulta e Exclusão | Validar posse do prontuário (bloquear se o paciente pertencer a outro dentista). |
| `/patients/{patientId}/procedures` | `GET` | Lista de Procedimentos | Adicionar no retorno os campos `status` e `cost` (Double). |
| `/patients/{patientId}/procedures` | `POST` | Cadastro de Procedimento | Adicionar no request os campos `status` e `cost`. **Validar a notação dentária (FDI)**. |
| `/procedures/{id}` | `PUT` | Atualização de Procedimento | Adicionar no request e response os campos `status` e `cost`. |
| `/ai/analyze-patient` | `POST` | **[NOVO]** Relatório de Risco Clínico por IA | Criar serviço que integra com LLM (Gemini API) para analisar o prontuário e gerar o sumário clínico. |

---

## 6. Plano de Implementação Técnica (Passo a Passo)

### Fase 1: Atualização do Banco de Dados e API do Backend
1. **Migrations**: Adicionar `medications` na tabela `patients`; adicionar `status` e `cost` na tabela `procedures`.
2. **Token Blacklist**: Criar tabela para rastrear refresh tokens ativos.
3. **Controladores**: Criar endpoints `/auth/refresh`, `/auth/logout` e `/ai/analyze-patient`.

### Fase 2: Configuração do Cliente HTTP no Frontend
1. Excluir o banco de dados SQLite local no Flutter (remover `database_helper.dart` e dependência de domínio).
2. Criar `lib/core/network/api_client.dart` configurando o **Dio** com timeouts, cabeçalhos de segurança e interceptor de refresh token automático.
3. Tratar erros HTTP 4xx, 5xx, timeouts de rede e ausência de sinal com interceptores e popups visuais amigáveis.

### Fase 3: Refatoração de Repositórios no Flutter
1. Alterar `PatientRepository` e `ProcedureRepository` para efetuar requisições REST direct com o `ApiClient` em vez de consultar as tabelas SQLite.
2. Adaptar os models Dart (`Patient` e `Procedure`) para bater com a assinatura exata dos payloads da API REST.

### Fase 4: Integração de Funcionalidades de IA e Interface
1. Adicionar o card de Relatório de Risco por IA na ficha do paciente.
2. Configurar a interface para exibir erros amigáveis ao usuário em caso de queda de internet.
