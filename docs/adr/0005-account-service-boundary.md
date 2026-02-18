# ADR-0005: Account Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Deposit/current account lifecycle and balances need isolated control rules.

## Decision
`cbs-account-service` owns account records, statuses, and balance updates.

- Module: `cbs-account-service`
- Port: `8083`
- Base API: `/api/v1/accounts/**`
- Data store: PostgreSQL database `cbs_account`
- Core capabilities: create account, list/query accounts, credit/debit, status update

Build and run:

- `mvn -pl cbs-account-service -am -DskipTests compile`
- `mvn -pl cbs-account-service -am spring-boot:run`

## Consequences
- Account state transitions are enforced in one place.
- Other services can orchestrate account changes via API contracts.
