# ADR-0014: FX Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Currency conversion requires rate management, quote pricing, and deal booking with consistent spread logic.

## Decision
`cbs-fx-service` owns FX rates, quotes, and FX deal lifecycle.

- Module: `cbs-fx-service`
- Port: `8093`
- Base API: `/api/v1/fx/**`
- Data store: PostgreSQL database `cbs_fx`
- Core capabilities: create/update rates, generate quotes, book/cancel deals

Build and run:

- `mvn -pl cbs-fx-service -am -DskipTests compile`
- `mvn -pl cbs-fx-service -am spring-boot:run`

## Consequences
- Pricing logic is consistent across channels.
- FX audit trail is separated from generic transactions.
