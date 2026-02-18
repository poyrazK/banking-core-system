# ADR-0011: Fee Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Fee configuration and charge computation should be managed independently from transaction posting logic.

## Decision
`cbs-fee-service` owns fee catalogs and fee charge calculations.

- Module: `cbs-fee-service`
- Port: `8092`
- Base API: `/api/v1/fees/**`
- Data store: PostgreSQL database `cbs_fee`
- Core capabilities: create/update fee config, charge fee by base amount, query fee charges

Build and run:

- `mvn -pl cbs-fee-service -am -DskipTests compile`
- `mvn -pl cbs-fee-service -am spring-boot:run`

## Consequences
- Fee logic is centralized and auditable.
- New fee types can be introduced without touching core transaction flows.
