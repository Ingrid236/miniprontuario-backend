# Implementation Plan: Backend MVP

**Branch**: `001-backend-mvp` | **Date**: 2026-05-29 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-backend-mvp/spec.md`

## Summary

Implement the core MVP for MiniProntuario Backend, focusing on secure dentist authentication, patient management, and dental procedure tracking using Clean Architecture and RESTful APIs.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 3+, Spring Security, Spring Data JPA, JWT (io.jsonwebtoken), Flyway, PostgreSQL Driver

**Storage**: PostgreSQL (Production), H2 (Development/Testing)

**Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc

**Target Platform**: Backend API / Web Service

**Project Type**: Web Service

**Performance Goals**: <200ms for standard queries

**Constraints**: Must strictly adhere to Clean Architecture, SOLID principles, and 100% data isolation per dentist. Must use UUIDs for primary keys to support future offline-first syncing capabilities from the frontend.

**Scale/Scope**: Initial MVP focusing on Dentists, Patients, and Procedures.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] Does this plan propose well-documented REST APIs (API First)?
- [x] Does this plan maintain strict Layered Architecture & Clean Architecture separation?
- [x] Are business rules enforced exclusively in the Services layer?
- [x] Is Test-Driven Development (TDD) strategy defined?
- [x] Are Security, Authentication, and Input Validation considered?
- [x] Does the plan align with the defined Technical Stack & Constraints?
- [x] Does the plan include Observability (Logging/Error Handling)?

## Project Structure

### Documentation (this feature)

```text
specs/001-backend-mvp/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output
```

### Source Code (repository root)

```text
src/
├── main/java/com/miniprontuario/
│   ├── config/       # Security, Swagger, Global Exception Handler
│   ├── controller/   # REST Endpoints (Presentation)
│   ├── dto/          # Data Transfer Objects
│   ├── model/        # Entities (Domain)
│   ├── repository/   # Spring Data JPA Interfaces
│   ├── security/     # JWT filters, UserDetails
│   └── service/      # Business Logic
└── test/java/com/miniprontuario/
    ├── integration/
    └── unit/
```

**Structure Decision**: Standard Spring Boot Maven directory layout structured by layered architecture components to align with the Clean Architecture and API First principles.
