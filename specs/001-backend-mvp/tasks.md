# Implementation Tasks: Backend MVP

**Feature**: Backend MVP (`001-backend-mvp`)
**Status**: Ready for implementation
**Strategy**: TDD, MVP first, incremental delivery

## Phase 1: Setup
*Goal: Initialize the Spring Boot project and base structure.*

- [x] T001 Initialize Spring Boot project structure (Maven, Java 21, Web, JPA, Security, PostgreSQL, Flyway) in `pom.xml`
- [x] T002 Configure `src/main/resources/application.yml` (PostgreSQL, Flyway, JWT properties)
- [x] T003 Configure `src/test/resources/application.yml` (H2 database for testing)
- [x] T004 Create base package structure (`config`, `controller`, `dto`, `model`, `repository`, `security`, `service`) in `src/main/java/com/miniprontuario/`

## Phase 2: Foundational
*Goal: Implement cross-cutting concerns, security, and database baseline.*

- [x] T005 Create base Entity class with UUID ID and audit fields in `src/main/java/com/miniprontuario/model/BaseEntity.java`
- [x] T006 Set up Spring Security config in `src/main/java/com/miniprontuario/security/SecurityConfig.java`
- [x] T007 Implement JWT utility class in `src/main/java/com/miniprontuario/security/JwtUtil.java`
- [x] T008 Implement JWT authentication filter in `src/main/java/com/miniprontuario/security/JwtAuthFilter.java`
- [x] T009 Add global exception handler in `src/main/java/com/miniprontuario/config/GlobalExceptionHandler.java`
- [x] T010 Create Flyway V1 baseline migration in `src/main/resources/db/migration/V1__init_schema.sql`

## Phase 3: User Story 1 - Dentist Registration and Login
*Goal: Dentists can register and log in to receive a JWT.*
*Independent Test Criteria: Can register a new dentist and login successfully. Duplicate CPF/Email returns 400.*

- [x] T011 [P] [US1] Create Dentist entity mapping in `src/main/java/com/miniprontuario/model/Dentist.java`
- [x] T012 [P] [US1] Create DentistRepository interface in `src/main/java/com/miniprontuario/repository/DentistRepository.java`
- [x] T013 [P] [US1] Create Auth DTOs (RegisterRequest, LoginRequest, AuthResponse) in `src/main/java/com/miniprontuario/dto/AuthDTOs.java`
- [x] T014 [US1] Implement AuthService for registration and login in `src/main/java/com/miniprontuario/service/AuthService.java`
- [x] T015 [US1] Create AuthController endpoints (`/auth/register`, `/auth/login`) in `src/main/java/com/miniprontuario/controller/AuthController.java`
- [x] T016 [P] [US1] Write integration tests for AuthController in `src/test/java/com/miniprontuario/integration/AuthControllerTest.java`

## Phase 4: User Story 2 - Patient Management
*Goal: Dentists can manage their own patients.*
*Independent Test Criteria: Can create, list, view, and soft-delete patients. Data isolation is strictly enforced.*

- [x] T017 [P] [US2] Create Patient entity mapping in `src/main/java/com/miniprontuario/model/Patient.java`
- [x] T018 [P] [US2] Create PatientRepository interface in `src/main/java/com/miniprontuario/repository/PatientRepository.java`
- [x] T019 [P] [US2] Create Patient DTOs in `src/main/java/com/miniprontuario/dto/PatientDTOs.java`
- [x] T020 [US2] Implement PatientService for CRUD with isolation in `src/main/java/com/miniprontuario/service/PatientService.java`
- [x] T021 [US2] Create PatientController endpoints in `src/main/java/com/miniprontuario/controller/PatientController.java`
- [x] T022 [P] [US2] Write unit tests for PatientService in `src/test/java/com/miniprontuario/unit/PatientServiceTest.java`
- [x] T023 [P] [US2] Write integration tests for PatientController in `src/test/java/com/miniprontuario/integration/PatientControllerTest.java`

## Phase 5: User Story 3 - Dental Procedure Tracking
*Goal: Dentists can track procedures for their patients.*
*Independent Test Criteria: Can add, list, and edit procedures. Edits blocked after 24 hours. Future dates rejected.*

- [x] T024 [P] [US3] Create Procedure entity mapping in `src/main/java/com/miniprontuario/model/Procedure.java`
- [x] T025 [P] [US3] Create ProcedureRepository interface in `src/main/java/com/miniprontuario/repository/ProcedureRepository.java`
- [x] T026 [P] [US3] Create Procedure DTOs in `src/main/java/com/miniprontuario/dto/ProcedureDTOs.java`
- [x] T027 [US3] Implement ProcedureService with 24h validation in `src/main/java/com/miniprontuario/service/ProcedureService.java`
- [x] T028 [US3] Create ProcedureController endpoints in `src/main/java/com/miniprontuario/controller/ProcedureController.java`
- [x] T029 [P] [US3] Write unit tests for ProcedureService (focus on 24h edit rule) in `src/test/java/com/miniprontuario/unit/ProcedureServiceTest.java`
- [x] T030 [P] [US3] Write integration tests for ProcedureController in `src/test/java/com/miniprontuario/integration/ProcedureControllerTest.java`

## Phase 6: Polish
*Goal: Finalize documentation and end-to-end functionality.*

- [x] T031 Add springdoc-openapi dependency and annotations in controllers
- [x] T032 Verify full MVP workflow manually via Swagger UI

## Dependencies
- Phase 1 must be completed before anything else.
- Phase 2 must be completed before Phase 3, 4, 5.
- Phase 3 (Auth) should ideally be completed before Phase 4 & 5 to allow real token-based testing.
- Phase 4 (Patient) must be completed before Phase 5 (Procedure) because procedures depend on patients.

## Parallel Execution Examples
- **US1 (Auth)**: T011, T012, and T013 can be implemented in parallel.
- **US2 (Patient)**: T017, T018, and T019 can be implemented in parallel.
- **US3 (Procedure)**: T024, T025, and T026 can be implemented in parallel.
