# ADR-0015: Notification Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Outbound communication (email/SMS/push/in-app) should be decoupled from domain services.

## Decision
`cbs-notification-service` owns notification records and delivery status transitions.

- Module: `cbs-notification-service`
- Port: `8091`
- Base API: `/api/v1/notifications/**`
- Data store: PostgreSQL database `cbs_notification`
- Core capabilities: create notification, list/query, mark sent/failed/cancelled

Build and run:

- `mvn -pl cbs-notification-service -am -DskipTests compile`
- `mvn -pl cbs-notification-service -am spring-boot:run`

## Consequences
- Notification retries and outcomes can be managed centrally.
- Business services remain focused on domain operations.
