# ADR-0004: Customer Service Boundary

- Date: 2026-02-18
- Status: Accepted

## Context
Customer identity and KYC lifecycle should be owned by a dedicated bounded context.

## Decision
`cbs-customer-service` owns customer master data and KYC status management.

- Module: `cbs-customer-service`
- Port: `8082`
- Base API: `/api/v1/customers/**`
- Data store: PostgreSQL database `cbs_customer`
- Core capabilities: create customer, query/search customer, update KYC status

Build and run:

- `mvn -pl cbs-customer-service -am -DskipTests compile`
- `mvn -pl cbs-customer-service -am spring-boot:run`

## Consequences
- KYC and customer profile changes are centralized.
- Account and product services reference customer IDs without duplicating profiles.
