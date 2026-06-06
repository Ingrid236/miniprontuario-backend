# Research & Technical Decisions: Backend MVP

## 1. Primary Key Strategy

- **Decision**: Use `UUID` (v4) for all primary keys (`Dentist`, `Patient`, `Procedure`).
- **Rationale**: The user mentioned "Offline-First Operation" via SQLite on the frontend client (Flutter). UUIDs are crucial for offline-first architectures to prevent ID collisions when multiple clients or offline clients sync data back to the central backend.
- **Alternatives considered**: `Long` (Auto-Increment / Sequence). Rejected because sequential IDs will collide during offline sync merges.

## 2. Authentication Flow

- **Decision**: Stateless JWT Authentication.
- **Rationale**: Mandatory per the Constitution. Simplifies scaling and client integration. The Flutter app will store the JWT securely and append it as a Bearer token.
- **Alternatives considered**: Stateful Session cookies. Rejected as they are less suitable for mobile APIs and violate the JWT mandate.

## 3. Data Isolation (Multi-Tenancy)

- **Decision**: Discriminator column approach (`dentist_id` on all top-level entities).
- **Rationale**: Ensures that a dentist can only access their own patients and procedures. Spring Security context will inject the authenticated `dentist_id` into queries automatically where applicable, or service layers will validate ownership.
- **Alternatives considered**: Schema per tenant. Rejected due to complexity overhead for an MVP and overkill for this scale.

## 4. Soft Delete

- **Decision**: Implement `@SQLRestriction("deleted = false")` and soft-delete flag on `Patient` and `Procedure` entities.
- **Rationale**: Clarified in the spec that deleted patients should be preserved for clinical/audit integrity but hidden from active UI.
- **Alternatives considered**: Cascade physical delete. Rejected per clinical compliance rules.
