# ADR-0006: Transaction Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Monetary movements require a dedicated transactional history service independent of account master data.

## Decision
`cbs-transaction-service` owns transaction records and reversal workflow.

- Module: `cbs-transaction-service`
- Port: `8084`
- Base API: `/api/v1/transactions/**`
- Data store: PostgreSQL database `cbs_transaction`
- Core capabilities: create transaction, query/list transactions, reverse transaction

Build and run:

- `mvn -pl cbs-transaction-service -am -DskipTests compile`
- `mvn -pl cbs-transaction-service -am spring-boot:run`

## Consequences
- Transaction audit trail is centralized.
- Reversal logic remains consistent across channels.
