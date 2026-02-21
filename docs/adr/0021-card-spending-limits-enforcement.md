# ADR-0021: Card Spending Limits Enforcement

- Date: 2026-02-21
- Status: Accepted

## Context
The `Card` entity in the Core Banking System includes `dailyLimit` and `monthlyLimit` fields intended to restrict spending for security and risk management. However, these limits were previously ignored during transaction processing, allowing cards to spend indefinitely as long as the underlying account had funds. There was a requirement to implement a robust, real-time enforcement mechanism for these limits.

## Decision
We decided to implement a dedicated spending tracking and validation system integrated into the transaction flow.

Key components of the implementation:
1.  **Dedicated Tracking**: Created a `CardSpendingRecord` entity to track individual spending events. This allows for clean aggregation without overloading the main `Transaction` table with card-specific metadata.
2.  **Aggregation Strategy**: Used JPQL queries in `CardSpendingRepository` to calculate `sumDailySpending` and `sumMonthlySpending` for a specific card using optimized database aggregation.
3.  **Real-time Validation**: Implemented `CardSpendingService.validateAndRecordSpending()`, which:
    - Fetches the current card limits.
    - Aggregates existing spending for the current day and month.
    - Rejects the transaction (throwing an `ApiException`) if the requested amount would exceed either limit.
    - Records the spending event atomically upon successful validation.
4.  **Transaction Integration**: Modified `TransactionService.createTransaction()` to accept an optional `cardId`. If present, the service invokes the `CardSpendingService` before finalizing the ledger entries.
5.  **User Transparency**: Added a `GET /api/v1/cards/{cardId}/spending` endpoint to retrieve a detailed `SpendingLimitResponse` showing limits, spent amounts, and remaining balances.

## Consequences
- **Security**: Strictly enforces card-level risk controls, protecting both the bank and the customer from unauthorized or excessive spending.
- **Performance**: Introduces a small overhead for card-based transactions due to the aggregation queries. This is mitigated by using a lean `CardSpendingRecord` table and indexing.
- **Maintainability**: The logic is encapsulated within `CardSpendingService`, keeping the `TransactionService` focused on core movement of funds.
- **Reporting**: Provides clear visibility into spending patterns through the new status endpoint.
- **Atomic Operations**: All spending checks and records are performed within the database transaction boundary of the financial transaction, ensuring consistency.
