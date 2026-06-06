<!--
Sync Impact Report:
- Version change: 1.0.0 → 2.0.0
- List of modified principles: Fully replaced with MiniProntuario Backend specific 18 principles (API First, Clean Architecture, Domain-Driven Design, Business Rules, SOLID, Modular Design, TDD, Security by Design, Auth, REST Standards, Database Standards, AI Readiness, Auditability, Data Protection, Performance, Documentation, Observability, Secret Management)
- Added sections: Technical Stack & Constraints, Development Workflow, Development & Review Gates, Governance
- Removed sections: None
- Templates requiring updates:
  - .specify/templates/plan-template.md ✅ updated
  - .specify/templates/spec-template.md ✅ reviewed (no changes needed)
  - .specify/templates/tasks-template.md ✅ reviewed (no changes needed)
-->

# MiniProntuario Backend Constitution

## Core Principles

### I. API First Design

The backend exists primarily to expose REST APIs consumed by the Flutter application.

All features MUST be exposed through documented REST endpoints.

Every endpoint MUST:

* Follow RESTful conventions
* Return standardized JSON responses
* Use meaningful HTTP status codes
* Be documented using OpenAPI/Swagger

API contracts SHOULD be defined before implementation.

---

### II. Clean Architecture & Layer Separation

The backend MUST strictly follow Clean Architecture principles.

The application MUST be organized into the following layers:

* Controllers (Presentation Layer)
* Services (Business Layer)
* Repositories (Persistence Layer)
* Domain Models / Entities
* DTOs
* Configuration Layer
* Security Layer

Responsibilities:

#### Controllers

* Receive requests
* Validate input
* Return responses
* Never contain business logic

#### Services

* Contain business rules
* Handle transactions
* Coordinate domain operations

#### Repositories

* Handle persistence only
* Never contain business logic

Dependencies MUST point inward toward abstractions and business rules.

---

### III. Domain-Driven Design & Business Modeling

The architecture MUST be centered around the business domain.

Current domain model:

```text
User (Dentist)
 └── Patients
      ├── Procedures
      ├── ClinicalNotes (Future)
      ├── Attachments (Future)
      └── TreatmentPlans (Future)
```

Relationships:

* One User owns many Patients
* One Patient owns many Procedures
* One Procedure belongs to one Patient

The domain model MUST drive architectural decisions.

---

### IV. Business Rules Enforcement

Business rules are first-class citizens.

All validations MUST occur server-side.

Frontend validation is considered a convenience layer only.

Examples:

* Patient CPF must be unique per dentist.
* Procedure dates cannot be in the future.
* Procedures require a valid patient.
* A dentist may only access their own patients.
* A dentist may only access procedures linked to their patients.
* Mandatory clinical information must be validated before persistence.

Business rules MUST reside in Services.

---

### V. SOLID Principles Enforcement

The project MUST follow SOLID principles.

#### Single Responsibility Principle

Every class must have one reason to change.

#### Open/Closed Principle

Components must be extendable without modifying stable implementations.

#### Liskov Substitution Principle

Implementations must be safely interchangeable.

#### Interface Segregation Principle

Interfaces must remain cohesive and focused.

#### Dependency Inversion Principle

High-level modules must depend on abstractions.

---

### VI. Modular Design

The application MUST be organized by business capabilities.

Expected modules:

```text
auth
users
patients
procedures
security
shared
```

Future modules:

```text
clinical-notes
attachments
treatment-plans
appointments
ai-assistant
odontogram
financial
```

Modules MUST remain loosely coupled.

---

### VII. Test-Driven & Quality Focus

Code quality is non-negotiable.

The codebase MUST:

* Compile successfully
* Pass static analysis
* Pass all tests
* Avoid duplicated code
* Avoid dead code
* Follow project conventions

Required test types:

#### Unit Tests

* Services
* Validators
* Domain Rules

#### Integration Tests

* Controllers
* Security
* Repositories

Recommended tools:

* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc

Pull Requests with failing tests MUST NOT be merged.

---

### VIII. Security by Design

Security is mandatory because the application stores healthcare information.

The system MUST implement:

* Password hashing
* JWT authentication
* Authorization validation
* Input validation
* Secure credential storage

Sensitive data MUST NEVER be exposed.

---

### IX. Authentication & Authorization

Authentication is required.

#### User Registration

Required fields:

* Full Name
* Email
* Password
* CPF
* CRO
* Phone Number

Optional fields:

* Clinic Name
* Profile Picture

#### Login

Authentication MUST use:

* Email
* Password

Passwords MUST be encrypted using BCrypt.

Authentication MUST issue JWT tokens.

Protected endpoints MUST require authentication.

Authorization MUST validate data ownership.

A dentist MUST NEVER access another dentist's information.

---

### X. REST API Standards

Endpoint naming MUST remain consistent.

Examples:

```text
POST   /api/auth/register
POST   /api/auth/login

GET    /api/patients
GET    /api/patients/{id}
POST   /api/patients
PUT    /api/patients/{id}
DELETE /api/patients/{id}

GET    /api/patients/{id}/procedures
POST   /api/patients/{id}/procedures
PUT    /api/procedures/{id}
DELETE /api/procedures/{id}
```

Error responses MUST be standardized.

Example:

```json
{
  "timestamp": "2026-05-29T18:00:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Procedure date cannot be in the future"
}
```

---

### XI. Database & Persistence Standards

Persistence concerns MUST remain isolated from business logic.

Production database:

* PostgreSQL

Development/Test database:

* H2

Database migrations MUST use:

* Flyway

Direct SQL inside Controllers or Services is prohibited.

Repositories are solely responsible for persistence.

---

### XII. AI Integration Readiness

The project MUST be prepared for future AI integration.

AI providers MUST be abstracted behind interfaces.

Business rules MUST NEVER directly depend on:

* OpenAI
* Gemini
* Claude
* Groq

Possible future use cases:

* Clinical note generation
* Procedure summaries
* Treatment suggestions
* Patient history summarization

Changing AI providers MUST NOT require domain changes.

---

### XIII. Auditability & Traceability

The architecture SHOULD support future auditing.

Trackable events:

* User registration
* Login attempts
* Patient creation
* Patient updates
* Procedure creation
* Procedure updates
* Procedure deletion

Audit implementation should be additive and not require architectural refactoring.

---

### XIV. Healthcare Data Protection

Patient information is sensitive healthcare data.

The system MUST be prepared for:

* LGPD compliance
* Data export requests
* Data deletion requests
* Data anonymization

Only necessary information should be exposed through APIs.

---

### XV. Performance & Scalability

The backend MUST prioritize scalability.

Required practices:

* Pagination
* Query optimization
* Database indexing
* Efficient joins
* Lazy loading when appropriate

The system MUST avoid:

* N+1 queries
* Excessive memory usage
* Unbounded collections

---

### XVI. Documentation Standards

The project MUST maintain:

* README.md
* ARCHITECTURE.md
* API Documentation
* Database Documentation
* Authentication Flow Documentation

Swagger/OpenAPI MUST remain synchronized with implementation.

---

### XVII. Observability & Reliability

Centralized logging MUST be implemented.

Recommended:

* SLF4J
* Logback

Global exception handling MUST use:

```java
@ControllerAdvice
```

All errors MUST return meaningful responses.

System failures MUST be traceable through logs.

---

### XVIII. Security & Secret Management

The following MUST NEVER be committed:

* Database passwords
* JWT secrets
* API keys
* Environment variables
* Production credentials

Secrets MUST be managed through:

* Environment Variables
* Spring Configuration
* Secret Managers (future)

Stack traces MUST NOT be exposed in production.

---

## Technical Stack & Constraints

Mandatory stack:

* Java 21
* Spring Boot 3+
* Spring Security
* Spring Data JPA
* PostgreSQL
* Flyway
* Maven
* Swagger/OpenAPI

Recommended:

* Lombok
* MapStruct

---

## Development Workflow

### Branch Strategy

```text
main
develop
feature/*
fix/*
hotfix/*
```

### Commit Convention

Conventional Commits are mandatory.

Examples:

```text
feat: create patient registration endpoint
fix: validate duplicate cpf
refactor: simplify procedure service
docs: update architecture documentation
test: add integration tests for authentication
```

Minimum repository quality:

* Meaningful commit history
* Pull Requests
* Code Reviews

---

## Development & Review Gates

Every Pull Request MUST include:

* Implementation plan
* Unit tests
* Integration tests (when applicable)
* Updated documentation
* Passing CI/CD pipeline

Mandatory validations:

* Build success
* Static analysis
* Unit tests
* Integration tests
* Code review approval

---

## Governance

This Constitution defines the non-negotiable backend development standards for the MiniProntuário Odontológico project.

All contributors MUST comply with these principles.

Amendments require:

1. Team consensus
2. Documentation update
3. Semantic version increment
4. Updated Sync Impact Report

---

**Version:** 2.0.0
**Ratified:** 2026-05-29
**Last Amended:** 2026-05-29
