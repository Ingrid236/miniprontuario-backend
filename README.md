# MiniProntuario - Backend

O **MiniProntuario** é uma API REST desenvolvida em Java com Spring Boot para auxiliar dentistas no gerenciamento de prontuários clínicos odontológicos. A aplicação facilita o controle de pacientes, anamneses, procedimentos realizados e traz uma camada inovadora de inteligência artificial para processamento de consultas e transcrição de áudios.

---

## 🚀 Tecnologias Utilizadas

- **Java 17 / 21**
- **Spring Boot 3.3.4**
- **Spring Security** (Autenticação Stateless com JWT e rotação de Refresh Tokens no banco de dados)
- **Spring AI** (Integração com LLMs e Whisper para transcrição e análise clínica)
- **Spring Data JPA** (Persistência e mapeamento objeto-relacional)
- **Flyway** (Gerenciamento automatizado de migrações de banco de dados)
- **PostgreSQL** (Banco de dados relacional para desenvolvimento e produção)
- **H2 Database** (Banco em memória usado para a suíte de testes)
- **Springdoc OpenAPI (Swagger)** (Documentação e testes interativos da API)
- **Lombok** (Otimização e redução de código boilerplate)

---

## 📋 Funcionalidades Principais

1. **Autenticação Avançada (Segurança)**:
   - Registro de dentistas com validação de CRO (Conselho Regional de Odontologia).
   - Autenticação JWT com tempo de expiração curto para segurança adicional.
   - Mecanismo de **Refresh Token rotation** com persistência no banco de dados.
   - Endpoint `/auth/logout` para revogação imediata de tokens ativos.
   - Endpoint `/auth/me` para retornar os dados do perfil autenticado.

2. **Cadastro e Gestão de Pacientes**:
   - Registro de pacientes associados diretamente a um dentista.
   - Validação algorítmica rigorosa de CPF (dígitos verificadores).
   - Verificação de idade limite (máximo de 120 anos) e datas de nascimento no passado.
   - Mapeamento de condições de saúde (alergias e medicamentos em uso).

3. **Procedimentos Clínicos**:
   - Registro dos procedimentos com controle de dente tratado, status e custo.
   - Validação do dente utilizando a **notação internacional FDI** (`^[1-8][1-8]$`).

4. **Processamento Inteligente com IA (Spring AI)**:
   - **Transcrição de Áudios** (`POST /api/ia/transcrever-audio`): Envie arquivos de áudio contendo consultas ou anotações e obtenha a transcrição textual instantânea (usando o modelo *Whisper* via API).
   - **Processamento de Consultas** (`POST /api/ia/processar-consulta`): Analisa relatos ou transcrições de consultas e extrai de forma estruturada:
     - Resumo conciso da queixa principal.
     - Lista automatizada de sintomas detectados.
     - Recomendação de especialidades médicas correlacionadas.
   - **Conformidade de Segurança e Privacidade (LGPD)**: Antes de enviar dados aos modelos de IA, o sistema detecta e anonimiza automaticamente CPFs e números de telefone no texto da consulta.
   - **Mecanismo de Fallback**: Caso a conexão com a API de IA falhe, o sistema possui um parser estático inteligente para classificar termos básicos de saúde locais e prevenir interrupções de serviço.

---

## 🛠️ Configuração e Instalação

### Pré-requisitos

Antes de iniciar, você precisará ter instalado:
1. **Java Development Kit (JDK)** versão 17 ou superior.
2. **PostgreSQL** (banco de dados relacional rodando localmente).
3. **Git** (para versionamento/clonagem).

---

### Passo a Passo para Execução

#### 1. Clonar o Repositório
```bash
git clone https://github.com/Ingrid236/miniprontuario-backend.git
cd miniprontuario-backend
```

#### 2. Configurar o Banco de Dados
Abra o seu PostgreSQL Client (pgAdmin, DBeaver, psql) e crie um banco de dados:
```sql
CREATE DATABASE miniprontuario_db;
```

Ajuste as variáveis de conexão com o banco de dados no arquivo [application.yml](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/resources/application.yml):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/miniprontuario_db
    username: seu_usuario_postgres
    password: sua_senha_postgres
```

#### 3. Configurar a Chave da IA (Opcional)
No mesmo arquivo `application.yml`, o sistema já vem configurado por padrão com uma chave de testes da API Groq (`llama-3.1-8b-instant` e `whisper-large-v3`). Caso deseje customizar, insira sua própria API Key e configurações em:
```yaml
  ai:
    openai:
      api-key: SUA_API_KEY
      base-url: https://api.groq.com/openai
```

#### 4. Executar a Aplicação
O projeto utiliza o wrapper do Maven (`mvnw`), portanto não há necessidade de ter o Maven instalado globalmente.

- **No Windows**:
  ```bash
  .\mvnw.cmd spring-boot:run
  ```
- **No Linux ou macOS**:
  ```bash
  ./mvnw spring-boot:run
  ```

Após a inicialização do Spring Boot, as migrações do banco de dados (tabelas e chaves) serão executadas automaticamente via **Flyway**.

A aplicação rodará por padrão no endereço: **`http://localhost:8080`**.

---

## 📖 Swagger & Testes de Endpoints

Com a aplicação ativa, você pode visualizar e interagir com todos os endpoints documentados:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

---

## 🧪 Rodando os Testes Automatizados

Para executar os testes unitários e de integração (que utilizam o banco de dados in-memory H2 de forma segura):

- **No Windows**:
  ```bash
  .\mvnw.cmd test
  ```
- **No Linux/macOS**:
  ```bash
  ./mvnw test
  ```
