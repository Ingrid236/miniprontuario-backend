# Quickstart

To run this backend project locally with the proposed refinements:

## Prerequisites
- Java 21+
- PostgreSQL instance running on `localhost:5432` with a database named `miniprontuario`
- Maven (or use the provided `mvnw` wrapper)

## Environment Setup
Set up the following environment variables (or rely on default `application.yml` properties):
- `POSTGRES_USER`: Database username
- `POSTGRES_PASSWORD`: Database password

For JWT, the application uses a default `jwt.secret` in the properties file for development, but it should be overridden in production.

## Running the Application

1. Compile and build the application:
   ```bash
   ./mvnw clean compile
   ```
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The application will be accessible at `http://localhost:8080`.
The Swagger UI documentation will be available at `http://localhost:8080/swagger-ui.html`.
