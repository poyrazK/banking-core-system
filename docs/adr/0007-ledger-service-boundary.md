# ADR-0007: Ledger Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Double-entry accounting and reconciliation must be isolated from customer-facing account operations.

## Decision
`cbs-ledger-service` owns GL accounts, journal posting, balances, and reconciliation queries.

- Module: `cbs-ledger-service`
- Port: `8088`
- Base API: `/api/v1/ledger/**`
- Data store: PostgreSQL database `cbs_ledger`
- Core capabilities: create GL account, post balanced journal entries, retrieve balances, reconcile date ranges

Build and run:

- `mvn -pl cbs-ledger-service -am -DskipTests compile`
- `mvn -pl cbs-ledger-service -am spring-boot:run`

## Consequences
- Accounting integrity rules are enforced centrally.
- Financial reporting can rely on a single ledger source.
