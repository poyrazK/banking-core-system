# ADR-0009: Deposit Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Term/demand deposit products require their own maturity, accrual, and breakage workflow.

## Decision
`cbs-deposit-service` owns deposit account lifecycle and related status transitions.

- Module: `cbs-deposit-service`
- Port: `8086`
- Base API: `/api/v1/deposits/**`
- Data store: PostgreSQL database `cbs_deposit`
- Core capabilities: create deposit, accrue interest, mature, close, break

Build and run:

- `mvn -pl cbs-deposit-service -am -DskipTests compile`
- `mvn -pl cbs-deposit-service -am spring-boot:run`

## Consequences
- Deposit rules are separated from loan/account rules.
- Product maturity logic can evolve independently.
