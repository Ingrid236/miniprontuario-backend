# Quickstart: Backend MVP

This document provides context for developers working on the `001-backend-mvp` feature.

## Feature Overview

The MVP backend for MiniProntuario Odontológico is a Spring Boot REST API that provides:
1. Dentist Registration and Authentication (JWT).
2. Patient Management (Create, Read, Soft Delete).
3. Procedure Tracking (Create, Read, Edit within 24h).

## Architecture

- **Clean Architecture**: Use Controllers (Presentation), Services (Business Rules), Repositories (Persistence).
- **Data Model**: `Dentist`, `Patient`, `Procedure`. All models use `UUID` for IDs to support future offline sync.
- **Security**: JWT tokens are issued on login. The `dentist_id` from the JWT context is used to enforce strict data isolation for all endpoints.

## Local Setup

The project uses Maven and Spring Boot 3+.

1. **Database**: By default, the application should run tests using H2. Production uses PostgreSQL. Ensure you have a running PostgreSQL instance for local manual testing (e.g., via Docker).
2. **Migrations**: Flyway manages database migrations. Place initial V1 scripts in `src/main/resources/db/migration/`.

## API Contracts

All endpoints are defined in `specs/001-backend-mvp/contracts/api.yaml`. Use tools like Swagger UI (springdoc-openapi) to visualize them locally once implemented.
