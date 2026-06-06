# Data Model: Backend Refinements & Missing Specifications

## Entities

### `User` (Dentist)
Represents the professional using the system.

**Fields**:
- `id`: UUID (Primary Key)
- `name`: String (Required, max length 255)
- `email`: String (Required, Unique, proper email format)
- `password`: String (Required, hashed, min length 8 for raw password before hashing)
- `cpf`: String (Required, Unique, exactly 11 digits)
- `cro`: String (Required, Unique)
- `phone`: String (Required)
- `createdAt`: Timestamp
- `updatedAt`: Timestamp

**Relationships**:
- One-to-Many with `Patient` (Future)
- One-to-Many with `Procedure` (Future)

## Validation Rules
- **Email**: Must follow a valid email regex structure. Cannot exist in the database already.
- **Password**: Must be at least 8 characters long before BCrypt hashing.
- **CPF**: Must be unique.
- **CRO**: Must be unique.
