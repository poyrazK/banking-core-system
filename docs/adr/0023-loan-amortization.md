# ADR-0023: Loan Amortization Schedule Implementation

## Status
Accepted

## Context
The existing Loan service allowed disbursement and repayment but did not provide customers with a clear payment plan (installment breakdown). This lacks transparency regarding interest vs. principal allocation and does not align with core banking standards.

## Decision
We will implement an automated Loan Amortization Engine.

### Core Components
1. **AmortizationCalculator**: A stateless utility for calculating installment breakdowns.
   - Supports **Annuity** (standard fixed payment).
   - Supports **Flat Rate** (simple interest).
   - Supports **Reducing Balance** (fixed principal).
2. **LoanScheduleEntry**: A persistent entity to store the calculated installment plan per loan.
3. **Automated Generation**: The schedule is automatically generated and persisted when a loan is **disbursed**.
4. **API Visibility**: A new endpoint `GET /api/v1/loans/{loanId}/schedule` provides full visibility into the installment plan and total interest/payments.

### Calculation Policy
- All calculations use `BigDecimal` with 2-decimal precision.
- **Rounding Strategy**: `RoundingMode.HALF_UP`.
- **Last Installment Adjustment**: The final installment amount is adjusted to absorb any rounding remainders, ensuring the loan balance reaches exactly zero.

## Alternatives Considered
- **On-the-fly computation**: Rejected because audit trails require fixed historical schedules that don't change if business rules evolve after disbursement.
- **Storing schedule on the Loan entity**: Rejected due to JSON bloat and difficulty in querying specific installments (e.g., "all due payments today").
- **External amortization library**: Rejected to keep the core banking math transparent, manageable, and free of external dependency risks.

## Consequences
- **Positive**: Improved transparency for customers.
- **Positive**: Consistent interest accrual calculation across the system.
- **Neutral**: Increased storage requirement due to installment records (e.g., 360 rows for a 30-year mortgage).
- **Negative**: The disbursement operation is now slightly heavier as it involves batch insertion of schedule rows.
- **Transactional Atomicity**: Disbursement status update and schedule persistence are tied to a single transactional boundary. Failure in generation triggers a rollback of the disbursement.
- **Idempotency**: Retrying disbursement is safe as `LoanScheduleRepository.deleteByLoanId` performs a bulk deletion of existing rows before recreation, preventing duplicate installments on retry.

## Verification
- Unit tests verify math accuracy for all three amortization types.
- Mocked service tests ensure persistence happens during disbursement.
- Integration tests (`LoanServicePostgresIntegrationTest`) validate end-to-end persistence of amortization schedules against a real Postgres instance, covering disbursement, schedule creation, storage, and retrieval. This integration test ensures the full data flow and DB constraints are exercised to strengthen the verification story.
