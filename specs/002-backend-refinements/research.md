# Research & Decisions

## Context
This document resolves any uncertainties and outlines technical decisions for the Backend Refinements feature. 

## Finding 1: JWT Authentication
- **Decision**: Use `io.jsonwebtoken` (jjwt) library versions `0.12.5` to generate and validate JWT tokens.
- **Rationale**: `jjwt` is an industry standard for Java applications, fully compatible with Spring Security. The dependencies are already present in the `pom.xml`.
- **Alternatives considered**: Auth0's `java-jwt` was considered, but `jjwt` is already part of the project.

## Finding 2: Password Encryption
- **Decision**: Use `BCryptPasswordEncoder` provided by Spring Security.
- **Rationale**: It is the default recommended standard for securely hashing passwords in Spring Boot applications, supporting varying work factors.

## Finding 3: Database & Migrations
- **Decision**: Use PostgreSQL for persistent storage, H2 for testing, and Flyway for schema migrations.
- **Rationale**: This adheres to the Technical Stack & Constraints defined in the Constitution. Dependencies are also already present.
