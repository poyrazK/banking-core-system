# ADR-0001: Build and Run Strategy

- Date: 2026-02-18
- Status: Accepted

## Context
CBS is a multi-module Maven monorepo with multiple Spring Boot services. Teams need a consistent way to build all modules, build one module with dependencies, and run services locally in a predictable order.

## Decision
Use Maven reactor builds from repository root as the default strategy.

- Full build validation:
  - `mvn validate`
- Build/compile one module with required dependencies:
  - `mvn -pl <module-name> -am -DskipTests compile`
- Run one service from root:
  - `mvn -pl <module-name> -am spring-boot:run`

Use Docker Compose for local infra:

- `docker compose up -d postgres zookeeper kafka`

PostgreSQL databases are initialized via `db/init` mounted to `/docker-entrypoint-initdb.d`.

## Consequences
- Build and run commands are uniform across services.
- CI and local workflows can reuse identical module-scoped commands.
- New services can be added with minimal onboarding cost.
