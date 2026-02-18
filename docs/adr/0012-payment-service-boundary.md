# ADR-0012: Payment Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Payment instruction lifecycle has distinct operational states from generic transactions.

## Decision
`cbs-payment-service` owns payment initiation and status transitions.

- Module: `cbs-payment-service`
- Port: `8089`
- Base API: `/api/v1/payments/**`
- Data store: PostgreSQL database `cbs_payment`
- Core capabilities: create payment, list/query payments, complete/fail/cancel

Build and run:

- `mvn -pl cbs-payment-service -am -DskipTests compile`
- `mvn -pl cbs-payment-service -am spring-boot:run`

## Consequences
- Payment workflow states are explicit and traceable.
- Integrations can map external payment outcomes to internal statuses.
