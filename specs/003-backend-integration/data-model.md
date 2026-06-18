# Data Model: Backend Integration Refinements

## 1. Database Migrations (`db/migration/V3__integration_refinements.sql`)

```sql
-- Add medications column to patient
ALTER TABLE patient ADD COLUMN medications TEXT;

-- Add status and cost columns to procedure
ALTER TABLE procedure ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE procedure ADD COLUMN cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

-- Create refresh token table
CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    dentist_id UUID NOT NULL REFERENCES dentist(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);
```

---

## 2. JPA Entity Updates

### `Patient.java`
- Added field:
  ```java
  @Column(columnDefinition = "TEXT")
  private String medications;
  ```

### `Procedure.java`
- Added fields:
  ```java
  @Column(nullable = false)
  private String status = "PENDING"; // Can map to Enum

  @Column(nullable = false, precision = 10, scale = 2)
  private java.math.BigDecimal cost = java.math.BigDecimal.ZERO;
  ```

### `RefreshToken.java` (New Entity)
- Attributes:
  - `id`: UUID (Primary Key)
  - `token`: String (Unique)
  - `dentist`: Dentist (ManyToOne, Lazy)
  - `expiryDate`: Instant/LocalDateTime
  - `revoked`: boolean
