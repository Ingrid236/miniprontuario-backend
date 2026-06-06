---
description: "Task list for Backend Refinements implementation"
---

# Tasks: Backend Refinements & Missing Specifications

**Input**: Design documents from `/specs/002-backend-refinements/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/api.md

**Tests**: Test tasks are included as required by the Constitution and Success Criteria.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Exact file paths are included in descriptions.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure.

- [x] T001 [P] Ensure PostgreSQL and H2 dependencies are configured in `pom.xml`
- [x] T002 [P] Create security exceptions and utility classes in `src/main/java/com/miniprontuario/miniprontuario_backend/security/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T003 Implement JWT Utility class (JwtService) in `src/main/java/com/miniprontuario/miniprontuario_backend/security/JwtService.java`
- [x] T004 Implement JwtAuthenticationFilter in `src/main/java/com/miniprontuario/miniprontuario_backend/security/JwtAuthenticationFilter.java`
- [x] T005 Configure Spring Security (SecurityConfig) in `src/main/java/com/miniprontuario/miniprontuario_backend/security/SecurityConfig.java`

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Dentist Registration (Priority: P1) 🎯 MVP

**Goal**: Allow dentists to register for an account with specific validation rules.

**Independent Test**: Can be fully tested by submitting a registration form via the API and verifying that the user is created in the database and a welcome response is received.

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T006 [P] [US1] Integration test for Registration in `src/test/java/com/miniprontuario/miniprontuario_backend/controller/AuthControllerRegistrationTest.java`

### Implementation for User Story 1

- [x] T007 [P] [US1] Update User model entity to include CPF, CRO, Phone in `src/main/java/com/miniprontuario/miniprontuario_backend/model/User.java`
- [x] T008 [P] [US1] Create UserRegistrationDTO in `src/main/java/com/miniprontuario/miniprontuario_backend/dto/UserRegistrationDTO.java`
- [x] T009 [US1] Update UserRepository to find by CPF/CRO in `src/main/java/com/miniprontuario/miniprontuario_backend/repository/UserRepository.java`
- [x] T010 [US1] Implement registration logic in AuthService in `src/main/java/com/miniprontuario/miniprontuario_backend/service/AuthService.java` (depends on T007, T008, T009)
- [x] T011 [US1] Expose POST `/api/auth/register` endpoint in `src/main/java/com/miniprontuario/miniprontuario_backend/controller/AuthController.java`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently.

---

## Phase 4: User Story 2 - Dentist Login and Token Issuance (Priority: P1)

**Goal**: Authenticate users securely using Email/Password and issue a JWT token.

**Independent Test**: Can be fully tested by submitting valid credentials to the login endpoint and verifying the returned JWT token.

### Tests for User Story 2

- [x] T012 [P] [US2] Integration test for Login in `src/test/java/com/miniprontuario/miniprontuario_backend/controller/AuthControllerLoginTest.java`

### Implementation for User Story 2

- [x] T013 [P] [US2] Create UserLoginDTO in `src/main/java/com/miniprontuario/miniprontuario_backend/dto/UserLoginDTO.java`
- [x] T014 [US2] Implement login logic in AuthService in `src/main/java/com/miniprontuario/miniprontuario_backend/service/AuthService.java`
- [x] T015 [US2] Expose POST `/api/auth/login` endpoint in `src/main/java/com/miniprontuario/miniprontuario_backend/controller/AuthController.java`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently.

---

## Phase 5: User Story 3 - Fetch Current User Profile (Priority: P2)

**Goal**: Fetch profile details of the authenticated dentist.

**Independent Test**: Can be tested by calling the `/auth/me` endpoint with a valid Bearer token and verifying the returned profile payload.

### Tests for User Story 3

- [x] T016 [P] [US3] Integration test for Current User in `src/test/java/com/miniprontuario/miniprontuario_backend/controller/AuthControllerMeTest.java`

### Implementation for User Story 3

- [x] T017 [P] [US3] Create UserProfileDTO in `src/main/java/com/miniprontuario/miniprontuario_backend/dto/UserProfileDTO.java`
- [x] T018 [US3] Implement fetch current user logic in AuthService in `src/main/java/com/miniprontuario/miniprontuario_backend/service/AuthService.java`
- [x] T019 [US3] Expose GET `/api/auth/me` endpoint in `src/main/java/com/miniprontuario/miniprontuario_backend/controller/AuthController.java`

**Checkpoint**: All user stories should now be independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories.

- [x] T020 [P] Create Flyway migration `V2__add_dentist_fields.sql` in `src/main/resources/db/migration/`
- [x] T021 [P] Update OpenAPI documentation annotations in `AuthController.java`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed sequentially in priority order (US1 → US2 → US3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - Depends on US1 for generating test data
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Depends on US2 for token generation

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Models/DTOs before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- All tests for a user story marked [P] can run in parallel
- DTOs and entity updates marked [P] can run in parallel

---

## Implementation Strategy

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories
