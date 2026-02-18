# ADR-0016: Reporting Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Reporting workflows need independent request/status tracking and output metadata.

## Decision
`cbs-reporting-service` owns report request lifecycle and generated report metadata.

- Module: `cbs-reporting-service`
- Port: `8094`
- Base API: `/api/v1/reports/**`
- Data store: PostgreSQL database `cbs_reporting`
- Core capabilities: create report request, list/query reports, update/cancel report status

Build and run:

- `mvn -pl cbs-reporting-service -am -DskipTests compile`
- `mvn -pl cbs-reporting-service -am spring-boot:run`

## Consequences
- Reporting orchestration remains decoupled from transaction-processing services.
- Report lifecycle is auditable and queryable.
