# ADR-0013: Card Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Card issuance and lifecycle controls (activate/freeze/block/close) are separate from account servicing.

## Decision
`cbs-card-service` owns card master records, statuses, and spend limits.

- Module: `cbs-card-service`
- Port: `8090`
- Base API: `/api/v1/cards/**`
- Data store: PostgreSQL database `cbs_card`
- Core capabilities: create card, lifecycle transitions, limit updates, list/query cards

Build and run:

- `mvn -pl cbs-card-service -am -DskipTests compile`
- `mvn -pl cbs-card-service -am spring-boot:run`

## Consequences
- Card risk and lifecycle policies are centralized.
- Card channel changes do not impact account service internals.
