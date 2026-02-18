# ADR-0008: Loan Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Loan lifecycle has distinct decision and servicing rules that should not be mixed with generic account operations.

## Decision
`cbs-loan-service` owns loan origination and servicing state transitions.

- Module: `cbs-loan-service`
- Port: `8085`
- Base API: `/api/v1/loans/**`
- Data store: PostgreSQL database `cbs_loan`
- Core capabilities: create/apply loan, approve/reject, disburse, repay, close

Build and run:

- `mvn -pl cbs-loan-service -am -DskipTests compile`
- `mvn -pl cbs-loan-service -am spring-boot:run`

## Consequences
- Loan-specific validations remain isolated.
- Product teams can evolve loan behavior independently.
