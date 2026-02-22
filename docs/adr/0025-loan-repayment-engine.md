# ADR-0025: Scheduled Loan Repayment Engine

## Status
Accepted

## Context
The Loan module currently handles loan disbursements and records an `AmortizationSchedule` with exact installment amounts, principal, and interest breakdowns. However, the collection of these installments from the customer's account is either a manual process or non-existent in the automated flow. We need a reliable mechanism to automatically process loan repayments on their due dates, deducting the payment from the customer account, and properly posting to the ledger.

## Decision
We will implement an automated **Scheduled Loan Repayment Engine**.

### Core Components
1. **DailyLoanRepaymentJob**: A Spring `@Scheduled` background job that runs daily (e.g., at 01:00 AM) to find and process all due installments.
2. **LoanRepaymentService**: A robust service layer handling the business logic of a repayment:
   - Queries `LoanScheduleEntry` records where `paid = false` and `dueDate <= current_date`.
   - Iterates over each due entry and attempts to collect the `totalPayment`.
   - Validates if the customer's linked `accountId` has sufficient balance.
   - Debits the customer's account.
   - Reduces the `outstandingAmount` of the `Loan`.
   - Updates the `LoanScheduleEntry` to `paid = true`.
   - If the loan balance reaches exactly zero, the loan is marked as `CLOSED`.

### Ledger Integration
To meet accounting standards, repayment cannot be a simple balance deduction. We must issue a split journal entry:
- **Debit**: Customer's Current/Savings Account (for the full `totalPayment`).
- **Credit**: Loan Asset Account (for the `principalAmount` portion, reducing the bank's asset).
- **Credit**: Interest Income Account (for the `interestAmount` portion, realizing the bank's profit).
We will use the existing `LedgerPostingService.postEntry` which supports multi-leg transactions.

### Handling Insufficient Funds
To keep the initial implementation predictable:
- **Partial Payments**: Are **NOT** supported in this phase. If an account has \$90 and the installment is \$100, the payment is fully skipped.
- **Retry Mechanism**: The engine will simply pick up the skipped entry the next day (since the query looks for `dueDate <= current_date`). The installment remains `paid = false`.
- **Late Fees**: Deferred to a future implementation.

## Consequences
- **Positive**: Automates cash collection without human intervention.
- **Positive**: Directly ties the amortized interest to real financial postings in the ledger, keeping accounting automatically in sync with operations.
- **Negative**: Missing partial payments might lower the immediate collection rate, but significantly simplifies race conditions and balance handling.
- **Idempotency**: The engine queries by `paid = false`. Once marked true, the row is excluded from future runs, avoiding double charging.

## Verification
- Unit and Integration tests will verify that a scheduled job deducts the correct balance, splits the Ledger entries perfectly, and closes the loan when fully paid.
