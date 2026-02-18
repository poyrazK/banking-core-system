# ADR-0010: Interest Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Interest configuration and accrual calculations are cross-product concerns requiring consistent formulas.

## Decision
`cbs-interest-service` owns rate configuration and accrual event calculation records.

- Module: `cbs-interest-service`
- Port: `8087`
- Base API: `/api/v1/interests/**`
- Data store: PostgreSQL database `cbs_interest`
- Core capabilities: configure product rates, update status, run accrual, query accrual history

Build and run:

- `mvn -pl cbs-interest-service -am -DskipTests compile`
- `mvn -pl cbs-interest-service -am spring-boot:run`

## Consequences
- Interest logic is reusable and consistent.
- Product services can consume accrual outcomes without embedding formulas.
