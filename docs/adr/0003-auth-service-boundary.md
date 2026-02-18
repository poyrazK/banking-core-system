# ADR-0003: Auth Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
CBS requires centralized authentication and token issuance for downstream services.

## Decision
`cbs-auth-service` owns user registration/login and JWT issuance.

- Module: `cbs-auth-service`
- Port: `8081`
- Base API: `/api/v1/auth/**`
- Data store: PostgreSQL database `cbs_auth`
- Security model: stateless JWT-based authentication

Build and run:

- `mvn -pl cbs-auth-service -am -DskipTests compile`
- `mvn -pl cbs-auth-service -am spring-boot:run`

## Consequences
- Auth concerns are isolated from business domains.
- Other services can remain focused on domain logic.
