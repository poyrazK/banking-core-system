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

## Consequences
- **Positive**: Improved transparency for customers.
- **Positive**: Consistent interest accrual calculation across the system.
- **Neutral**: Increased storage requirement due to installment records (e.g., 360 rows for a 30-year mortgage).
- **Negative**: The disbursement operation is now slightly heavier as it involves batch insertion of schedule rows.

## Verification
- Unit tests verify math accuracy for all three amortization types.
- Mocked service tests ensure persistence happens during disbursement.
