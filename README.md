# MiniProntuario - Backend

O **MiniProntuario** é um sistema de gerenciamento de prontuários clínicos odontológicos desenvolvido para facilitar o dia a dia dos dentistas no cadastro de pacientes, anamnese e controle de procedimentos realizados.

Esta é a API Backend do projeto, construída com Java, Spring Boot e banco de dados PostgreSQL.

---

## 🚀 Tecnologias Utilizadas

- **Java 17/21**
- **Spring Boot 3.3.4**
- **Spring Security** (Autenticação Stateless com JWT)
- **Spring Data JPA** (Persistência de dados)
- **Flyway** (Gerenciamento de Migrações de Banco de Dados)
- **PostgreSQL** (Banco de dados de produção/desenvolvimento local)
- **H2 Database** (Banco de dados em memória para testes)
- **Springdoc OpenAPI (Swagger)** (Documentação interativa da API)
- **Lombok** (Redução de boilerplate code)

---

## 📋 Funcionalidades Principais

1. **Autenticação e Registro**:
   - Cadastro de dentistas com validação de CPF, CRO (Conselho Regional de Odontologia) e e-mail único.
   - Autenticação via JWT (JSON Web Tokens).
   - Endpoint `/auth/me` para recuperar informações do perfil do dentista logado.
2. **Gerenciamento de Pacientes**:
   - Cadastro e listagem de pacientes vinculados a um dentista específico.
   - Registro de informações de saúde (Alergias e Doenças Sistêmicas).
3. **Procedimentos Clínicos**:
   - Registro detalhado de procedimentos realizados nos pacientes, incluindo data, descrição, dente tratado e anotações clínicas.

---

## 🛠️ Configuração e Instalação

### Pré-requisitos

Certifique-se de ter instalado em sua máquina:
- **Java 17** ou superior.
- **Maven** (opcional, pois o projeto contém o wrapper `mvnw`).
- **PostgreSQL** rodando localmente (com banco criado).

### 1. Configurar Banco de Dados

Crie um banco de dados no seu PostgreSQL chamado `miniprontuario_db` (ou o nome de sua preferência).

No arquivo [application.yml](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/resources/application.yml), configure as credenciais de acesso ao seu banco PostgreSQL:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/miniprontuario_db
    username: seu_usuario_postgres
    password: sua_senha_postgres
```

### 2. Rodar Migrações do Banco de Dados

As tabelas são gerenciadas de forma automatizada pelo **Flyway** na inicialização do app.
Ao rodar a aplicação pela primeira vez, as migrations localizadas na pasta `src/main/resources/db/migration/` serão aplicadas de forma sequencial.

### 3. Rodando o Projeto Localmente

Na raiz do projeto, execute o seguinte comando para compilar e iniciar a aplicação:

No Windows:
```bash
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:
```bash
./mvnw spring-boot:run
```

A API estará acessível por padrão em `http://localhost:8080`.

---

## 📖 Documentação da API (Swagger)

Com a aplicação em execução, acesse a documentação interativa através do link:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

Aqui você poderá testar todos os endpoints disponíveis diretamente no navegador.

---

## 🧪 Testes Automatizados

O projeto utiliza **JUnit 5** e **MockMvc** para testes de integração e testes unitários das regras de negócio.
Para rodar os testes e garantir que tudo está funcionando corretamente, execute:

```bash
.\mvnw.cmd test
```

Os testes de integração sob o profile de `test` utilizam um banco H2 em memória, de forma isolada, não afetando seu banco PostgreSQL local.
