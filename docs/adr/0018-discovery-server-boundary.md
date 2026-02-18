# ADR-0018: Discovery Server Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Service registration and discovery are required for dynamic routing and service-to-service resolution.

## Decision
`cbs-discovery-server` is the Eureka service registry for CBS.

- Module: `cbs-discovery-server`
- Port: `8761`
- Responsibility: service registry and discovery endpoint
- Operation mode: standalone registry (`register-with-eureka: false`, `fetch-registry: false`)

Build and run:

- `mvn -pl cbs-discovery-server -am -DskipTests compile`
- `mvn -pl cbs-discovery-server -am spring-boot:run`

## Consequences
- Gateway and services can discover instances by service ID.
- Horizontal scaling is simpler with registry-based routing.
