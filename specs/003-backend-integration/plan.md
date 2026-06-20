# Implementation Plan: Backend Integration Refinements

**Branch**: `003-backend-integration` | **Date**: 2026-06-06 | **Spec**: [specs/003-backend-integration/spec.md](spec.md)

**Input**: Feature specification from `/specs/003-backend-integration/spec.md`

## Summary
Refactor the authentication mechanism to return both access and refresh tokens, implement refresh token rotation and revocation (logout) in the database, expand the patient and procedure models, and enforce robust business validations (valid CPF digit verifier, realistic age, CRO format, and FDI tooth notation).

## Technical Context

**Language/Version**: Java 21/17 (Spring Boot 3.3.4)

**Primary Dependencies**: `spring-boot-starter-security`, `jjwt` (0.12.5), `flyway-core`, `spring-boot-starter-validation`

**Storage**: PostgreSQL (production/dev), H2 (in-memory for tests)

**Testing**: JUnit 5, MockMvc, Spring Boot Test

**Target Platform**: Java Virtual Machine (JVM)

**Project Type**: REST API Web Service

**Performance Goals**: JWT rotation and validations must respond in < 100ms.

**Constraints**: Stateless JWT access tokens (15-30 mins); Stateful refresh tokens stored in DB (revokable, min 7 days).

## Constitution Check

- [x] Does this plan propose well-documented REST APIs (API First)?
- [x] Does this plan maintain strict Layered Architecture & Clean Architecture separation?
- [x] Are business rules enforced exclusively in the Services layer?
- [x] Is Test-Driven Development (TDD) strategy defined?
- [x] Are Security, Authentication, and Input Validation considered?
- [x] Does the plan align with the defined Technical Stack & Constraints?
- [x] Does the plan include Observability (Logging/Error Handling)?

## Project Structure

### Documentation
```text
specs/003-backend-integration/
├── plan.md              # This file
├── research.md          # Technical decisions and database designs
├── data-model.md        # Database schema modifications and entity updates
├── quickstart.md        # Commands for testing endpoints manually
└── contracts/
    └── api.md           # API endpoints payloads and response formats
```

### Source Code
```text
src/
├── main/
│   ├── java/com/miniprontuario/miniprontuario_backend/
│   │   ├── config/              # OpenApiConfig, SecurityConfig, ExceptionHandling
│   │   ├── controller/          # AuthController, PatientController, ProcedureController
│   │   ├── dto/                 # AuthDTOs, PatientDTOs, ProcedureDTOs
│   │   ├── exception/           # BusinessException, DuplicateResourceException
│   │   ├── model/               # Dentist, Patient, Procedure, RefreshToken
│   │   ├── repository/          # DentistRepository, PatientRepository, RefreshTokenRepository
│   │   ├── security/            # JwtUtil, JwtAuthFilter, DentistPrincipal
│   │   └── service/             # AuthService, PatientService, ProcedureService
│   └── resources/
│       ├── db/migration/        # V3__integration_refinements.sql
│       └── application.yml
└── test/
    └── java/com/miniprontuario/miniprontuario_backend/
        ├── integration/         # AuthControllerTest, PatientControllerTest, ProcedureControllerTest
        └── unit/                # Validation unit tests, Service tests
```

---

## Proposed Changes

### Database Migration

#### [NEW] [V3__integration_refinements.sql](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/resources/db/migration/V3__integration_refinements.sql)
Create the database changes as defined in `data-model.md`:
- Add `medications` column to `patient` table.
- Add `status` and `cost` columns to `procedure` table.
- Create `refresh_token` table linked to `dentist`.

---

### Authentication Refinements

#### [NEW] [RefreshToken.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/model/RefreshToken.java)
Entity representing the database-backed refresh tokens.

#### [NEW] [RefreshTokenRepository.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/repository/RefreshTokenRepository.java)
Repository for `RefreshToken` table with query helper `Optional<RefreshToken> findByToken(String token)`.

#### [MODIFY] [AuthDTOs.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/dto/AuthDTOs.java)
- Update `AuthResponse` to return `accessToken` and `refreshToken` instead of a single `token`.
- Add `RefreshRequest` containing `refreshToken`.
- Add `LogoutRequest` containing `refreshToken`.

#### [MODIFY] [AuthService.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/service/AuthService.java)
- Update `login` method to generate a secure random string or UUID for `refreshToken`, store it in the database with a 7-day expiration time, and return it.
- Implement `refresh(RefreshRequest request)` to validate the refresh token (not expired, not revoked), rotate it (generate a new `accessToken` and a new `refreshToken`), and revoke the old refresh token.
- Implement `logout(LogoutRequest request)` to revoke the refresh token (set `revoked = true`).
- Implement validation for Dentist CRO format (RegEx check).

#### [MODIFY] [AuthController.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/controller/AuthController.java)
- Expose `POST /auth/refresh`.
- Expose `POST /auth/logout`.

---

### Patient Refinements & Validations

#### [MODIFY] [Patient.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/model/Patient.java)
- Add the `medications` column field.

#### [MODIFY] [PatientDTOs.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/dto/PatientDTOs.java)
- Add `medications` to `PatientRequest` and `PatientResponse` DTO structures.

#### [NEW] [CpfValidator.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/util/CpfValidator.java)
Helper class to validate Brazilian CPF digits verificador algorithm.

#### [MODIFY] [PatientService.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/service/PatientService.java)
- Enforce business validations before save:
  - Check if CPF format is exactly 11 digits and matches the `CpfValidator` algorithm.
  - Validate that CPF is unique for the dentist.
  - Check if birthDate is in the past and dentist patient age <= 120.

---

### Procedure Refinements & Tooth validation

#### [MODIFY] [Procedure.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/model/Procedure.java)
- Add `status` and `cost` fields.

#### [MODIFY] [ProcedureDTOs.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/dto/ProcedureDTOs.java)
- Add `status` and `cost` to `ProcedureRequest` and `ProcedureResponse`.

#### [MODIFY] [ProcedureService.java](file:///c:/miniprontuario/miniprontuario-backend/miniprontuario-backend/src/main/java/com/miniprontuario/miniprontuario_backend/service/ProcedureService.java)
- Validate FDI tooth notation format `^[1-8][1-8]$` if the `tooth` attribute is supplied.

---

## Verification Plan

### Automated Tests
- Create tests for token rotation and invalidation:
  - `AuthControllerTest` -> `/auth/refresh` validation, `/auth/logout` validation.
- Create tests for patient creation validation rules:
  - `PatientServiceTest` / `PatientControllerTest` -> Invalid CPF digits, future birthDate, age > 120.
- Create tests for procedure creation validation:
  - `ProcedureServiceTest` / `ProcedureControllerTest` -> Invalid FDI notation.

Run all tests:
```bash
.\mvnw.cmd test
```

### Manual Verification
- Run the server local instance:
  ```bash
  .\mvnw.cmd spring-boot:run
  ```
- Send HTTP requests using `curl` as defined in `quickstart.md` to check:
  - Double token login structure
  - Token refresh rotation
  - Logout token invalidation
  - Rejection of invalid CPF and tooth notations
