# ADR 0024: Interest Accrual and Capitalization Architecture

## Status
Proposed

## Context
The core banking system needs to calculate interest earned on customer accounts (like Savings or Term Deposits) and credit them on a defined schedule (e.g., monthly). Currently, `cbs-interest-service` provides APIs to calculate and store an `InterestAccrual` record, but this process is manual and requires the caller to provide the principal amount.

We need automated processes to:
1.  **Accrue**: Calculate and record the daily interest earned for all eligible accounts based on their End-Of-Day (EOD) balance.
2.  **Capitalize**: Aggregate uncapitalized daily accruals at the end of the month (or at maturity) and post the actual financial credit to the customer's account via the Ledger.

## Decision

### 1. Processing Model: Scheduled Jobs over Events
We will use Spring's `@Scheduled` background jobs running within the `cbs-application` modular monolith rather than an event-driven "EOD Balance Snapshot" emitted over Kafka.

*   **Reasoning**: In our current modular monolith architecture, direct service calls (e.g., `AccountService` fetching balances from the primary DB replica) are highly performant and simpler to implement and trace than eventually consistent event streams.
*   **Implementation**: `DailyInterestAccrualJob` and `MonthlyInterestCapitalizationJob` will execute on cron schedules.

### 2. Idempotency Strategy
Batch processing jobs must be idempotent to handle crashes or accidental re-runs without double-counting interest or double-posting to the ledger.

*   **Accrual Idempotency**: The `interest_accruals` table will use the combination of `account_id` and `accrual_date` as a unique business key. If the job runs twice for the same day, it will safely ignore or update existing records.
*   **Capitalization Idempotency**: The job will find all records where `status = ACCRUED` for a given month and `account_id`. Once the aggregate is posted to the Ledger, the records will be immediately updated to `CAPITALIZED`. The Ledger posting itself will use an idempotency key derived from the `account_id` and the `YearMonth` (e.g., `INT-CAP-1002-2026-02`).

### 3. Data Storage
*   `InterestAccrual` entity will route to the `cbs_interest` schema.
*   It will gain an `AccrualStatus` (`ACCRUED`, `CAPITALIZED`).
*   It will gain a `capitalizationDate` to track when the funds were actually moved.

## Consequences
*   **Positive**: Simple to implement, easy to reason about DB transactions, leverages existing synchronous service boundaries.
*   **Negative**: As the system scales to millions of accounts, a single `@Scheduled` job iterating over all accounts may exceed acceptable execution windows. At that scale, we will need to refactor to a distributed batch processing framework (like Spring Batch) with chunking and partitioning. For now, this approach is sufficient.
