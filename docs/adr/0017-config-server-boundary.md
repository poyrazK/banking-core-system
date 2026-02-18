# ADR-0017: Config Server Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Centralized configuration management is required for multi-service deployments.

## Decision
`cbs-config-server` is the central configuration provider for CBS services.

- Module: `cbs-config-server`
- Port: `8888`
- Responsibility: serve centralized Spring Cloud configuration
- Discovery: registered as Eureka client
- Current backend profile: native/classpath config

Build and run:

- `mvn -pl cbs-config-server -am -DskipTests compile`
- `mvn -pl cbs-config-server -am spring-boot:run`

## Consequences
- Configuration can be managed in one place.
- Service-specific environment differences can be externalized.
