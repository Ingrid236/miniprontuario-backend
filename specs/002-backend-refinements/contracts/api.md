# API Contracts: Backend Refinements

## 1. Register User (Dentist)

**Endpoint:** `POST /api/auth/register`
**Description:** Registers a new dentist account.

**Request Body:**
```json
{
  "name": "Dr. John Doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123!",
  "cpf": "12345678901",
  "cro": "SP-12345",
  "phone": "+5511999999999"
}
```

**Response (Success - 201 Created):**
```json
{
  "message": "User registered successfully",
  "userId": "uuid-v4-string"
}
```

## 2. Login

**Endpoint:** `POST /api/auth/login`
**Description:** Authenticates a user and returns a JWT token.

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Response (Success - 200 OK):**
```json
{
  "token": "eyJhbG...",
  "type": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "uuid-v4-string",
    "name": "Dr. John Doe",
    "email": "john.doe@example.com"
  }
}
```

## 3. Current User Profile

**Endpoint:** `GET /api/auth/me`
**Description:** Returns the profile of the currently authenticated user.
**Headers:** `Authorization: Bearer <token>`

**Response (Success - 200 OK):**
```json
{
  "id": "uuid-v4-string",
  "name": "Dr. John Doe",
  "email": "john.doe@example.com",
  "cpf": "12345678901",
  "cro": "SP-12345",
  "phone": "+5511999999999"
}
```
