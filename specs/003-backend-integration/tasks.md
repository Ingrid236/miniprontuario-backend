---
description: "Task list for Backend Integration Refinements implementation"
---

# Tasks: Backend Integration Refinements

**Input**: Design documents from `/specs/003-backend-integration/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/api.md

**Tests**: Test tasks are included as required by the Constitution and Success Criteria.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Exact file paths are included in descriptions.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project database migrations and setup.

- [x] T001 [P] Ensure PostgreSQL and Flyway dependencies are present in `pom.xml`
- [x] T002 Create Flyway migration `V3__integration_refinements.sql` in `src/main/resources/db/migration/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core entities and tables that block user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T003 [P] Create RefreshToken model in `src/main/java/com/miniprontuario/miniprontuario_backend/model/RefreshToken.java`
- [x] T004 [P] Create RefreshTokenRepository in `src/main/java/com/miniprontuario/miniprontuario_backend/repository/RefreshTokenRepository.java`

**Checkpoint**: Foundation ready ✅

---

## Phase 3: User Story 1 - Double Token Authentication (Priority: P1) 🎯 MVP

**Goal**: Return access/refresh token pair on login and support token rotation.

### Tests for User Story 1

- [x] T005 [P] [US1] Integration tests for token refresh in `AuthControllerTest.java`

### Implementation for User Story 1

- [x] T006 [P] [US1] Create RefreshRequest DTO in `AuthDTOs.java`
- [x] T007 [US1] Modify login response DTO in `AuthDTOs.java` (accessToken + refreshToken)
- [x] T008 [US1] Implement token generation & rotation logic in `AuthService.java`
- [x] T009 [US1] Implement POST `/auth/refresh` endpoint in `AuthController.java`

**Checkpoint**: User Story 1 fully functional ✅

---

## Phase 4: User Story 2 - Secure Session Logout and Token Revocation (Priority: P1)

**Goal**: Support session logout and blacklist/revoke refresh tokens.

### Tests for User Story 2

- [x] T010 [P] [US2] Integration tests for logout in `AuthControllerTest.java`

### Implementation for User Story 2

- [x] T011 [P] [US2] Create LogoutRequest DTO in `AuthDTOs.java`
- [x] T012 [US2] Implement logout token revocation in `AuthService.java`
- [x] T013 [US2] Expose POST `/auth/logout` endpoint in `AuthController.java`

**Checkpoint**: User Stories 1 AND 2 both work independently ✅

---

## Phase 5: User Story 3 - Patient & Procedure Entity Expansion (Priority: P1)

**Goal**: Support medications on patients and status/cost on procedures.

### Tests for User Story 3

- [x] T014 [P] [US3] Integration tests verifying medications, status, and cost in `PatientControllerTest.java`

### Implementation for User Story 3

- [x] T015 [P] [US3] Add medications field to Patient model and DTOs
- [x] T016 [P] [US3] Add status and cost fields to Procedure model and DTOs

**Checkpoint**: All entity additions verified ✅

---

## Phase 6: User Story 4 - Data Validation & Business Rules (Priority: P2)

**Goal**: Support CPF, Age, CRO, and FDI Tooth validations in service layers.

### Tests for User Story 4

- [x] T017 [P] [US4] Unit tests for CPF validation and age validation in `PatientServiceTest.java`

### Implementation for User Story 4

- [x] T018 [P] [US4] Implement CpfValidator utility in `util/CpfValidator.java`
- [x] T019 [US4] Integrate CPF and birthDate validation in `PatientService.java`
- [x] T020 [US4] Integrate Dentist CRO format validation in `AuthService.java`
- [x] T021 [US4] Integrate tooth FDI notation validation in `ProcedureService.java`

**Checkpoint**: All business validation rules active and verified ✅

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Documentation and OpenAPI updates.

- [x] T022 [P] Update Swagger OpenAPI annotations in controllers
- [x] T023 Tests pass - BUILD SUCCESS (36 tests, 0 failures, 0 errors) ✅

---

## Final Result

**All 36 tests pass. BUILD SUCCESS.**
