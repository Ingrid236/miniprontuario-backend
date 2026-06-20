# Feature Specification: Backend Integration Refinements

**Feature Branch**: `003-backend-integration`

**Created**: 2026-06-06

**Status**: Draft

**Input**: User description: "backend integration analysis"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Double Token Authentication (Priority: P1)

As a dentist, I want to log in to the application and receive both a short-lived access token and a long-lived refresh token so that my session can be automatically renewed without re-entering my credentials.

**Why this priority**: Required for secure session persistence and smooth mobile client integration.

**Independent Test**: Can be tested by executing a login request and verifying that the response contains both `accessToken` and `refreshToken` with correct expiration claims.

**Acceptance Scenarios**:

1. **Given** valid credentials, **When** the login request `/auth/login` is submitted, **Then** the system returns an `accessToken` (15-30 minutes expiration) and a `refreshToken` (minimum 7 days expiration).
2. **Given** an expired access token but a valid refresh token, **When** `/auth/refresh` is requested with the refresh token, **Then** the system returns a new pair of access and refresh tokens.
3. **Given** an invalid or expired refresh token, **When** `/auth/refresh` is requested, **Then** the system returns a `401 Unauthorized` error.

---

### User Story 2 - Secure Session Logout and Token Revocation (Priority: P1)

As a dentist, I want to log out of the application so that my session is terminated and my refresh token is invalidated on the server.

**Why this priority**: Crucial for security, preventing session hijacking or token reuse after logout.

**Independent Test**: Can be tested by logging out, then attempting to use the same refresh token to obtain a new access token, which must fail.

**Acceptance Scenarios**:

1. **Given** an authenticated dentist session, **When** `/auth/logout` is requested with the refresh token, **Then** the system revokes the refresh token on the server and returns a 204 No Content/200 OK.
2. **Given** a revoked refresh token, **When** a refresh request `/auth/refresh` is attempted, **Then** the system returns a `401 Unauthorized` error.

---

### User Story 3 - Patient & Procedure Entity Expansion (Priority: P1)

As a dentist, I want to store medication info on my patients and status/cost on procedures so that I have complete records.

**Why this priority**: Required to fulfill the 7+ attribute schema requirement for the main entity and complete the patient-procedure domain.

**Independent Test**: Can be tested by creating a patient and procedure with the new fields and verifying the returned payload structure.

**Acceptance Scenarios**:

1. **Given** a new patient request containing `medications`, **When** `/patients` is called, **Then** the patient is successfully stored with the medications field.
2. **Given** a new procedure request containing `status` and `cost`, **When** `/patients/{id}/procedures` is called, **Then** the procedure is stored with status and cost.

---

### User Story 4 - Data Validation & Business Rules (Priority: P2)

As a system administrator, I want the backend to validate all input data (CPF, Age, CRO, FDI notation) so that the integrity of the clinical records is maintained.

**Why this priority**: Necessary to prevent corrupt, invalid, or duplicate data entries.

**Independent Test**: Can be tested by sending malformed or invalid entries to the registration/patient/procedure endpoints and expecting validation failures.

**Acceptance Scenarios**:

1. **Given** a CPF with less than 11 digits, **When** a dentist registers or registers a patient, **Then** the system returns a `400 Bad Request` validation error.
2. **Given** a CPF that is already registered for another patient under the same dentist, **When** a patient is registered, **Then** the system returns a `400 Bad Request` validation error.
3. **Given** a patient birthdate in the future or older than 120 years, **When** a patient is registered, **Then** the system returns a `400 Bad Request`.
4. **Given** an invalid CRO format (non-alphanumeric, missing state abbreviation), **When** a dentist registers, **Then** the system returns a `400 Bad Request`.
5. **Given** a procedure with a tooth number not following FDI notation, **When** the procedure is added, **Then** the system returns a `400 Bad Request`.

---

### Edge Cases

- What happens when a refresh token is reused after it has been revoked or refreshed?
- What happens if a user submits a valid formatted CPF but it fails the digit verification algorithm?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST return both `accessToken` and `refreshToken` on login.
- **FR-002**: System MUST expose `/auth/refresh` to rotate tokens.
- **FR-003**: System MUST expose `/auth/logout` to blacklist/revoke the refresh token.
- **FR-004**: System MUST allow storing a `medications` field on `Patient` (bringing attributes to name, cpf, birthDate, phone, allergies, systemicDiseases, medications).
- **FR-005**: System MUST allow storing `status` and `cost` on `Procedure`.
- **FR-006**: System MUST validate that CPF is unique per dentist for patients, has 11 digits, and passes the verification digit algorithm.
- **FR-007**: System MUST validate that patient age is <= 120 and birthdate is in the past.
- **FR-008**: System MUST validate that Dentist CRO follows the format: State Abbreviation (2 letters) + digits (e.g. SP-12345).
- **FR-009**: System MUST validate that tooth numbers follow FDI notation (quadrants 1-4 for adults, 5-8 for children, teeth 1-8).

### Key Entities *(include if feature involves data)*

- **RefreshToken**: Stores active/revoked status, expiry, and owner dentist ID.
- **Patient**: Added `medications` (text).
- **Procedure**: Added `status` (text/enum) and `cost` (numeric).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: System invalidates 100% of revoked/expired refresh tokens, returning `401 Unauthorized` for subsequent uses.
- **SC-002**: 100% of duplicate or invalid CPFs are rejected at the service boundary.
- **SC-003**: All invalid tooth notation requests are rejected with a clear explanation in the response.

## Assumptions

- We assume token invalidation will be managed via a Database table (`refresh_tokens`).
- We assume that frontend developers will manage session persistence securely using `flutter_secure_storage`.
- AI integration and MCP server features are out of scope for this specification.
