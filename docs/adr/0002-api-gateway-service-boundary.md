# ADR-0002: API Gateway Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Client applications need a single ingress for CBS APIs instead of direct calls to each service.

## Decision
`cbs-api-gateway` is the API ingress and reverse-proxy boundary.

- Module: `cbs-api-gateway`
- Port: `8080`
- Base responsibility: route `/api/v1/**` paths to backing services
- Service discovery: Eureka client enabled
- Routing model: explicit route entries plus discovery locator

Build and run:

- `mvn -pl cbs-api-gateway -am -DskipTests compile`
- `mvn -pl cbs-api-gateway -am spring-boot:run`

## Consequences
- External clients target one endpoint.
- Service topology can evolve with minimal client impact.
