# Feature Specification: Backend MVP

**Feature Branch**: `001-backend-mvp`

**Created**: 2026-05-29

**Status**: Draft

**Input**: User description: "MiniProntuário Odontológico — Product Vision & Functional Scope..."

## Clarifications

### Session 2026-05-29
- Q: How should the system handle patient deletion given healthcare data retention constraints? → A: Soft Delete (Patient and associated procedures are marked as deleted but remain in the database for auditing and legal protection).
- Q: Are there any time-based restrictions on editing clinical procedure records to maintain medical/legal integrity? → A: 24-Hour Window (Procedures can only be edited within 24 hours of creation, ensuring historical clinical records remain immutable).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Dentist Registration and Login (Priority: P1)

A dentist needs to register for an account and log securely into the system to manage their clinic's patients.

**Why this priority**: Without authentication, no other workflows can be secured or attributed to a specific dentist.

**Independent Test**: Can be fully tested by sending a registration payload and then logging in to receive a valid JWT token.

**Acceptance Scenarios**:

1. **Given** valid registration details, **When** the registration endpoint is called, **Then** the account is created and a success response is returned.
2. **Given** valid login credentials, **When** the login endpoint is called, **Then** a valid JWT token is returned.
3. **Given** an invalid password, **When** the login endpoint is called, **Then** a 401 Unauthorized response is returned.

---

### User Story 2 - Patient Registration (Priority: P1)

A dentist needs to register a new patient, including their basic contact information, clinical background (allergies, medications), and odontological history.

**Why this priority**: Patients are the core entity. Without patients, no procedures can be recorded.

**Independent Test**: Can be fully tested by creating a patient and retrieving their details via the API using the dentist's JWT token.

**Acceptance Scenarios**:

1. **Given** valid patient data with a unique CPF, **When** the create patient endpoint is called, **Then** the patient is saved and linked to the logged-in dentist.
2. **Given** a CPF that already exists for this dentist, **When** the create patient endpoint is called, **Then** a validation error is returned.

---

### User Story 3 - Procedure Registration (Priority: P1)

A dentist needs to record a dental procedure (e.g., restoration, cleaning) for a specific patient, including the date, tooth involved, and clinical observations.

**Why this priority**: Recording procedures is the primary daily action performed by the dentist during consultations.

**Independent Test**: Can be fully tested by adding a procedure to an existing patient and validating it appears in the database.

**Acceptance Scenarios**:

1. **Given** an existing patient, **When** a valid procedure is submitted, **Then** the procedure is saved and associated with the patient.
2. **Given** a procedure with a future date, **When** submitted, **Then** a validation error is returned preventing the save.
3. **Given** a patient that belongs to a different dentist, **When** a procedure is submitted, **Then** a 403/404 error is returned ensuring data isolation.

---

### User Story 4 - Patient History Search (Priority: P2)

A dentist needs to quickly search for a patient and view their chronological history of procedures to understand past treatments.

**Why this priority**: Fast access to patient history is essential for efficient consultations.

**Independent Test**: Can be fully tested by querying the patient list and procedure history endpoints and validating the returned order and content.

**Acceptance Scenarios**:

1. **Given** multiple patients and procedures, **When** the patient search endpoint is called, **Then** matching patients are returned quickly.
2. **Given** a specific patient, **When** their procedure history is requested, **Then** a chronological list of past procedures is returned.

---

### Edge Cases

- What happens when a patient is deleted? (System implements soft-delete to preserve historical clinical data for audit and legal compliance, while hiding them from active UI).
- How does the system handle concurrent patient registrations with the same CPF? (Database unique constraints must enforce integrity).
- How are unexpected server errors formatted? (Must be caught by global exception handler and formatted as a standardized JSON error response).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow dentists to register with their Name, Email, Password, CPF, CRO, and Phone.
- **FR-002**: System MUST authenticate dentists via Email and Password, issuing a JWT token for session management.
- **FR-003**: System MUST enforce that dentists can only view, edit, and delete their own patients and procedures.
- **FR-004**: System MUST allow dentists to register patients with Basic, Clinical (allergies, systemic diseases), and Odontological data.
- **FR-005**: System MUST enforce CPF uniqueness per dentist for patients.
- **FR-006**: System MUST allow dentists to add, edit, and view dental procedures linked to specific patients, restricting edits to within 24 hours of procedure creation.
- **FR-007**: System MUST validate that procedure dates cannot be in the future.
- **FR-008**: System MUST support standard HTTP REST methods and JSON responses with meaningful HTTP status codes.

### Key Entities *(include if feature involves data)*

- **User (Dentist)**: Represents the authenticated professional. Contains credentials, CRO, and clinic info.
- **Patient**: Represents the patient. Contains personal, clinical, and odontological history. Relates to one User.
- **Procedure**: Represents a clinical action (e.g., Restoration, Extraction). Contains date, tooth involved, notes, and status. Relates to one Patient.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The API ensures 100% data isolation; a dentist's requests can never access or modify another dentist's data.
- **SC-002**: Core operations (Authentication, Patient Creation, Procedure Creation) can be completed successfully using standard REST clients.
- **SC-003**: The system handles invalid inputs (e.g., duplicate CPF, future procedure dates) gracefully, returning standardized error JSONs 100% of the time.
- **SC-004**: Endpoints respond to standard CRUD operations within acceptable latency targets (e.g., <200ms under normal load) to support fast frontend interactions.

## Assumptions

- The backend MVP will use PostgreSQL as the primary database, while the offline-first logic (SQLite) will be entirely managed by the Flutter frontend client.
- The frontend client will sync data to the backend REST API when an internet connection is available.
- Image attachments, odontogram visual mapping, and appointment scheduling are out of scope for the MVP backend implementation.
- Auditing (tracking who created/updated records) will be supported via simple timestamping (`createdAt`, `updatedAt`) for the MVP.
