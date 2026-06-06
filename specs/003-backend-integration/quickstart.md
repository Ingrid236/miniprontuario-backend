# Quickstart: Integration & API Testing

This guide describes how to run and test the backend integration refinements manually.

## Database Setup

1. Run migrations to create the new tables/columns:
   The migrations are executed automatically by Flyway upon application start.

## Manual Verification Steps

You can verify the API behavior using any HTTP client (e.g. `cURL` or Postman).

### 1. Test Login & Double Tokens
Send a POST request to `/auth/login`:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"gabriel@example.com", "password":"password123"}'
```
Expected output contains `"accessToken"`, `"refreshToken"`, `"type": "Bearer"`, and `"expiresIn"`.

### 2. Test Refresh Token
Send a POST request to `/auth/refresh` using the `refreshToken` received:
```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<received-refresh-token>"}'
```
Expected output contains a new `accessToken` and `refreshToken`.

### 3. Test Logout (Revocation)
Send a POST request to `/auth/logout` to revoke the token:
```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<received-refresh-token>"}'
```
Expected output: HTTP 204 No Content.
Attempting `/auth/refresh` again with the same token must fail with HTTP 401 Unauthorized.

### 4. Test Patient Registration Business Rules
- **Invalid CPF Format**: Send `< 11` digits or invalid algorithm digits -> Expected `400 Bad Request`.
- **Realistic Age**: Birthdate in the future or > 120 years -> Expected `400 Bad Request`.
- **Unique CPF per Dentist**: Registering the same CPF twice under the same dentist -> Expected `400 Bad Request`.

### 5. Test FDI Tooth Notation
Add a procedure with `tooth` value set to `99` -> Expected `400 Bad Request`.
Add a procedure with `tooth` value set to `11` -> Expected `201 Created` or `200 OK`.
