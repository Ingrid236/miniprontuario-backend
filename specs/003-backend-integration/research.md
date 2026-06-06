# Research & Technical Decisions: Backend Integration Refinements

## 1. Double Token Authentication (Access & Refresh Tokens)

### Decision
Implement a custom Database-backed Refresh Token system.

### Rationale
To persist sessions on mobile clients securely and allow rotation, the server needs to issue both an `accessToken` (JWT) and a `refreshToken` (represented by a secure random UUID stored in the database).
A database-backed approach for the `refreshToken` allows the server to easily revoke tokens on logout and prevent reuse.

### Database Table Design (`refresh_token`):
```sql
CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    dentist_id UUID NOT NULL REFERENCES dentist(id),
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);
```

### Alternatives Considered
- **JWT-based Refresh Token without DB storage**: Rejected because JWTs cannot be easily revoked/blacklisted on logout without maintaining a separate blacklist. A DB-backed token model is cleaner for stateful revocation (e.g., logging out a specific session).

---

## 2. Business Rules Validation

### Decision 1: CPF Digit Verifier
Implement a helper class `CpfValidator` containing the official Brazilian CPF validation algorithm.
- First digit check: sum of weights 10 down to 2, multiplied by 10, mod 11.
- Second digit check: sum of weights 11 down to 2, multiplied by 10, mod 11.

### Decision 2: CRO Format Verification
RegEx verification for Dentist's CRO:
- Pattern: `^[A-Z]{2}-\d+$` (e.g. `SP-12345`, `RJ-99999`).

### Decision 3: FDI Tooth Notation Verification
Verification for tooth format:
- Pattern: `^[1-8][1-8]$` (exactly two digits, first digit is quadrant 1-8, second digit is dente 1-8).
- E.g. `11`, `23`, `55`, `82` are valid. `09`, `49`, `91` are invalid.

---

## 3. Procedure Status & Cost

### Decision
`Procedure` entity will receive:
- `status`: Enum with values `PENDING`, `COMPLETED`, `CANCELLED`.
- `cost`: `BigDecimal` (stored as `DECIMAL(10,2)` in DB).
