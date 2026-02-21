# Core Banking System (CBS)

Microservices-based Core Banking System built with Java 21 LTS, Spring Boot, Spring Cloud, PostgreSQL, and Kafka.

## Modules (Scaffold)
- `cbs-common`
- `cbs-config-server`
- `cbs-discovery-server`
- `cbs-api-gateway`
- `cbs-auth-service`
- `cbs-customer-service`
- `cbs-account-service`
- `cbs-transaction-service`
- `cbs-ledger-service`
- `cbs-loan-service`
- `cbs-deposit-service`
- `cbs-interest-service`
- `cbs-fee-service`
- `cbs-payment-service`
- `cbs-card-service`
- `cbs-fx-service`
- `cbs-notification-service`
- `cbs-reporting-service`

## Shared Audit Logging
`cbs-common` includes reusable audit helpers under `com.cbs.common.audit`: use `AuditLogHelper.success(...)` / `failure(...)` to build an `AuditEvent`, then log `AuditLogHelper.toStructuredFields(event)` for consistent structured audit entries across services.

## Standardized Error Handling
The system uses a centralized error handling strategy via `GlobalExceptionHandler` in `cbs-common`. 
- **ApiException**: Domain-specific exceptions that include a machine-readable `errorCode` and an `HttpStatus`.
- **Validation Errors**: Standardized handling of `MethodArgumentNotValidException` with field-level details.
- All error responses follow a consistent `ApiResponse` structure with `success`, `message`, `errorCode`, `timestamp`, and `data` fields.

## Docker (PostgreSQL Init)
PostgreSQL mounts initialization scripts from `db/init` and auto-creates service databases on first startup.

- First run: `docker compose up -d postgres`
- Re-run initialization when `postgres_data` already exists: `docker compose down -v` then `docker compose up -d postgres`

Scripts under `/docker-entrypoint-initdb.d` run only when Postgres initializes a fresh data directory.

## Quick Start (Makefile)
The easiest way to build and run the entire system:

```bash
# Build and start everything
make restart

# Just start existing build
make up

# Check status
make ps

# Follow logs
make logs

# Shut down
make down
```

## Docker (Full Stack)
The entire system can be started with a single command:

```bash
docker compose up -d
```

This starts:
- Infrastructure: PostgreSQL 16, Zookeeper, Kafka
- Discovery Server (Eureka): [http://localhost:8761](http://localhost:8761)
- Config Server: [http://localhost:8888](http://localhost:8888)
- API Gateway: [http://localhost:8080](http://localhost:8080)
- All 15 microservices (Auth, Customer, Account, Transaction, etc.)

## Local Development Startup Order
If running services manually:
1. Infrastructure: `docker compose up -d postgres zookeeper kafka`
2. Discovery server: `cbs-discovery-server` (port `8761`)
3. Config server: `cbs-config-server` (port `8888`)
4. Core services: auth, customer, account, ledger
5. Domain services: transaction, payment, loan, deposit, card, notification, interest, fee, fx, reporting
6. API Gateway: `cbs-api-gateway` (port `8080`)

Example run command from repository root:
- `mvn -pl <module-name> -am spring-boot:run`

## Build
```bash
mvn validate
```

## Real PostgreSQL Integration Tests (One Command)
Run all real (Docker-backed) service integration tests with:

```bash
make real-it-tests
```

Run infrastructure integration tests (gateway/config/discovery) with:

```bash
make infra-it-tests
```

Run both infrastructure + real PostgreSQL service integration tests with:

```bash
make all-it-tests
```

This command will:
- start a temporary PostgreSQL 16 container,
- create per-service test databases,
- run each `*PostgresIntegrationTest` class with real DB connections,
- stop and remove the container when finished.

Optional environment variables:
- `IT_DB_PORT` (default: `55432`)
- `IT_DB_USER` (default: `test`)
- `IT_DB_PASSWORD` (default: `test`)
- `IT_DB_CONTAINER_NAME` (default: `cbs-it-postgres`)
- `KEEP_IT_DB_CONTAINER=true` to keep container running after tests.
