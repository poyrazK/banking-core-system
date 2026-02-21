# ADR-0022: Scheduled Payments and Standing Orders

## Status
Accepted

## Context
Standard retail and corporate banking requires the ability for customers to set up recurring payments (standing orders) or schedule a one-off payment for a future date. The existing `PaymentService` only supports immediate, one-shot payment initiations. We need a robust mechanism to manage the lifecycle of these schedules and ensure reliable execution when they become due.

## Decision
We will implement a Dedicated Scheduling Domain within the Payment module.

### Core Components
1. **ScheduledPayment Entity**: A persistence model to track the schedule configuration (frequency, start date, end date) and execution state (next run date, execution count, failure count).
2. **Frequency Logic**: Support for `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`, and `YEARLY` intervals.
3. **Execution Engine**: A background task (Spring `@Scheduled`) that identifies `ACTIVE` scheduled payments where `nextExecutionDate <= today`.
4. **Integration**: Each scheduled execution will trigger a standard payment initiation via the existing `PaymentService.createPayment()`. This ensures that all ledger posting, balance checks, and security validations are consistently applied.

### Lifecycle Management
- **Status Transitions**: ACTIVE <-> PAUSED, ACTIVE/PAUSED -> CANCELLED, ACTIVE -> COMPLETED (when end date is reached).
- **Auto-Pause Policy**: To prevent repeated failures (e.g., due to insufficient funds), the system will automatically transition a schedule to `PAUSED` after 3 consecutive execution failures.
- **Reference Tracking**: Each scheduled payment has a unique reference. Executed payments will use a derived reference (`{SCH-REF}-{count}`) for traceability.

## Consequences
- **Positive**: Customers gain essential banking functionality.
- **Positive**: High reliability through auto-pause and detailed failure tracking.
- **Positive**: Reuse of existing payment logic ensures consistent data integrity across the ledger.
- **Neutral**: Adds a background processing load to the application (configurable via cron).
- **Negative**: Increases the complexity of the Payment domain with new entity relationships.

## Verification
- Unit tests for frequency calculation and state management.
- Integration tests for the execution flow.
- Manual verification via REST endpoints for lifecycle control.
