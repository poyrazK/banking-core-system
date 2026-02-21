# Core Banking System (CBS)

Modular Monolith Core Banking System built with Java 21 LTS, Spring Boot, PostgreSQL, and Kafka.

## Modular Architecture
The system is built as a single application (`cbs-application`) with strictly decoupled domain packages. Each module resides under `com.cbs`:

- **Auth**: JWT-based security and user management.
- **Customer**: Customer profile and KYC management.
- **Account**: Current and savings account management.
- **Transaction**: Core ledger posting and transaction processing.
- **Loan**: Loan origination, servicing, and amortization.
- **Deposit**: Term deposits and interest calculations.
- **Ledger**: General Ledger and financial reconciliation.
- **Card**: Debit/Credit card management and spending limits.
- **Fee**: Flexible fee engine for transactions.
- **Interest**: Parameter-driven interest rate management.
- **Payment**: Payment processing and bill pay.
- **FX**: Foreign exchange and currency conversion.
- **Notification**: Internal messaging and notifications.
- **Reporting**: Financial and regulatory reporting.
- **Common**: Shared domain primitives, utilities, and cross-cutting concerns (Audit, Exceptions).

## Standardized Error Handling
The system uses a centralized error handling strategy via `com.cbs.common.exception.GlobalExceptionHandler`.

- **ApiException**: Domain-specific exceptions.
  - Can specify a machine-readable `errorCode` and an `HttpStatus`.
  - If no `HttpStatus` is provided, it defaults to `400 Bad Request`.
- **Validation Errors**: Standardized handling of `MethodArgumentNotValidException`. 
  - Collects and joins all field-level validation failures (e.g., "fieldName: error message").
- **Catch-all**: Unhandled exceptions are logged as errors and return `500 Internal Server Error`.

### Error Response Structure
All error responses follow the consistent `ApiResponse` structure:

```json
{
  "success": false,
  "message": "Detailed error message or validation failures",
  "errorCode": "MACHINE_READABLE_CODE",
  "data": null,
  "timestamp": "2026-02-21T17:27:12.456Z"
}
```

## Shared Audit Logging
The `common` package includes reusable audit helpers: use `AuditLogHelper.success(...)` / `failure(...)` to build an `AuditEvent`, then log `AuditLogHelper.toStructuredFields(event)` for consistent structured audit entries.

## Quick Start (Makefile)
The easiest way to build and run the system:

```bash
# Build and start everything
make restart

# Just start existing build
make up

# Follow logs
make logs

# Shut down
make down
```

## Docker Environment
The system runs with a single application container and its dependencies:

```bash
docker compose up -d
```

This starts:
- **cbs-app**: The core modular monolith application (Port `8080`)
- **postgres**: PostgreSQL 16 database

## Local Development
If running the application manually:
1. Start infrastructure: `docker compose up -d postgres`
2. Run app from root: `mvn -pl cbs-application spring-boot:run`

## Build
```bash
mvn clean install
```

## Integration Tests
Run real (Docker-backed) PostgreSQL integration tests:

```bash
mvn test -P integration-test
```

This will:
- Spin up a temporary PostgreSQL container (via Testcontainers or scripts),
- Run `*PostgresIntegrationTest` classes with real DB connections,
- Ensure clean teardown.
