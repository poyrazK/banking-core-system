# 19. Cross-Currency Transaction Validation

Date: 2026-02-19

## Status

Accepted

## Context

The system previously allowed transactions to be processed without strict validation of the currency against the user's account currency. This could lead to data corruption where a transaction in `USD` is recorded against an account denominated in `TRY` without any exchange rate application or conversion, effectively treating the units as 1:1.

We needed a mechanism to prevent such inconsistencies at the entry point of the transaction processing pipeline.

## Decision

We have decided to implement strict **Cross-Currency Validation** at both the `cbs-account-service` and `cbs-transaction-service` layers.

1. **Account Service Validation**:
   - The `AccountService` `creditBalance` and `debitBalance` methods now explicitly check if the transaction currency matches the account's base currency.
   - If a mismatch is detected, a `400 Bad Request` with `CURRENCY_MISMATCH` error code is thrown.
   - To support backward compatibility with clients that do not yet send currency information (if any), requests with `null` currency are currently accepted (though discouraged for future use).

2. **Transaction Service Pre-Check**:
   - The `TransactionService` now queries the `AccountService` via a new internal endpoint (`GET /api/v1/accounts/{id}/currency`) to retrieve the account's currency *before* persisting any transaction data.
   - If the transaction request's currency does not match the account's currency, the transaction creation is rejected immediately.

3. **API Changes**:
   - `BalanceUpdateRequest` DTO in `cbs-account-service` has been updated to include an optional `Currency` field.
   - `AccountController` exposes a new lightweight endpoint for currency lookup to minimize payload overhead during inter-service communication.

## Consequences

### Positive
- **Data Integrity**: Eliminates the risk of recording transactions in the wrong currency, ensuring ledger accuracy.
- **Fail-Fast**: Transactions fail immediately at the initiation stage rather than causing downstream reconciliation issues.
- **Foundation for FX**: This validation layer is a prerequisite for the future implementation of the `cbs-fx-service`, which will handle legitimate cross-currency transfers via conversion.

### Negative
- **Latency**: Creating a transaction now requires an additional synchronous HTTP call to the `AccountService` to fetch the currency, slightly increasing the latency of the `createTransaction` operation.
- **Coupling**: The `TransactionService` has a stronger dependency on the `AccountService` availability.

## Compliance

- This change respects the single responsibility principle by keeping currency conversion logic out of the core transaction flow (deferred to a future FX service) while enforcing validation rules strictly.
