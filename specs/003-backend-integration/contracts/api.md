# API Contracts: Backend Integration Refinements

## 1. Login (Updated Response)

**Endpoint:** `POST /auth/login`
**Description:** Authenticates a user and returns both an access token and a refresh token.

**Request Body:** (Unchanged)
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Response (Success - 200 OK):**
```json
{
  "accessToken": "access-token-string",
  "refreshToken": "refresh-token-string",
  "type": "Bearer",
  "expiresIn": 1800,
  "user": {
    "id": "uuid-v4-string",
    "name": "Dr. John Doe",
    "email": "john.doe@example.com"
  }
}
```

---

## 2. Refresh Token (New Endpoint)

**Endpoint:** `POST /auth/refresh`
**Description:** Rotates access and refresh tokens.

**Request Body:**
```json
{
  "refreshToken": "refresh-token-string"
}
```

**Response (Success - 200 OK):**
```json
{
  "accessToken": "new-access-token-string",
  "refreshToken": "new-refresh-token-string"
}
```

---

## 3. Logout (New Endpoint)

**Endpoint:** `POST /auth/logout`
**Description:** Revokes a refresh token on the server.

**Request Body:**
```json
{
  "refreshToken": "refresh-token-string"
}
```

**Response (Success - 204 No Content):** (No body returned)

---

## 4. Patient (Updated Schema)

### Create Patient (`POST /patients`) & Update Patient (`PUT /patients/{id}`)
**Request Body:**
```json
{
  "name": "Jane Doe",
  "cpf": "12345678901",
  "birthDate": "1990-05-15",
  "phone": "11999999999",
  "allergies": "Penicillin",
  "systemicDiseases": "None",
  "medications": "Antihistamines"
}
```

### Response Profile (`GET /patients/{id}`)
```json
{
  "id": "uuid-v4-string",
  "name": "Jane Doe",
  "cpf": "12345678901",
  "birthDate": "1990-05-15",
  "phone": "11999999999",
  "allergies": "Penicillin",
  "systemicDiseases": "None",
  "medications": "Antihistamines",
  "createdAt": "2026-06-06T20:00:00Z",
  "updatedAt": "2026-06-06T20:00:00Z"
}
```

---

## 5. Procedure (Updated Schema)

### Create Procedure (`POST /patients/{patientId}/procedures`) & Update Procedure (`PUT /procedures/{id}`)
**Request Body:**
```json
{
  "date": "2026-06-06",
  "description": "Tooth extraction",
  "tooth": "18",
  "notes": "Simple extraction of third molar.",
  "status": "COMPLETED",
  "cost": 150.00
}
```

### Response (`GET /patients/{patientId}/procedures`)
```json
[
  {
    "id": "uuid-v4-string",
    "date": "2026-06-06",
    "description": "Tooth extraction",
    "tooth": "18",
    "notes": "Simple extraction of third molar.",
    "status": "COMPLETED",
    "cost": 150.00,
    "createdAt": "2026-06-06T20:00:00Z",
    "updatedAt": "2026-06-06T20:00:00Z"
  }
]
```
