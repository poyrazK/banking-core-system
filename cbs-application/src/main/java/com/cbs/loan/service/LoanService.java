package com.cbs.loan.service;

import com.cbs.common.exception.ApiException;
import com.cbs.loan.dto.CreateLoanRequest;
import com.cbs.loan.dto.LoanDecisionRequest;
import com.cbs.loan.dto.LoanRepaymentRequest;
import com.cbs.loan.dto.LoanResponse;
import com.cbs.loan.dto.LoanScheduleResponse;
import com.cbs.loan.model.Loan;
import com.cbs.loan.model.LoanScheduleEntry;
import com.cbs.loan.model.LoanStatus;
import com.cbs.loan.repository.LoanRepository;
import com.cbs.loan.repository.LoanScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanScheduleRepository loanScheduleRepository;

    public LoanService(LoanRepository loanRepository, LoanScheduleRepository loanScheduleRepository) {
        this.loanRepository = loanRepository;
        this.loanScheduleRepository = loanScheduleRepository;
    }

    @Transactional
    public LoanResponse createLoan(CreateLoanRequest request) {
        if (request.startDate().isAfter(request.maturityDate())) {
            throw new ApiException("LOAN_INVALID_DATE_RANGE", "Start date must be before or equal to maturity date");
        }

        if (request.termMonths() <= 0) {
            throw new ApiException("LOAN_INVALID_TERM", "Term months must be greater than zero");
        }

        String loanNumber = normalizeLoanNumber(request.loanNumber());
        if (loanRepository.existsByLoanNumber(loanNumber)) {
            throw new ApiException("LOAN_NUMBER_EXISTS", "Loan number already exists");
        }

        Loan loan = new Loan(
                request.customerId(),
                request.accountId(),
                loanNumber,
                request.loanType(),
                request.principalAmount(),
                request.annualInterestRate(),
                request.termMonths(),
                request.startDate(),
                request.maturityDate(),
                request.amortizationType());

        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoan(Long loanId) {
        return LoanResponse.from(findLoan(loanId));
    }

    @Transactional(readOnly = true)
    public LoanScheduleResponse getSchedule(Long loanId) {
        Loan loan = findLoan(loanId);
        List<LoanScheduleEntry> entries = loanScheduleRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId);

        if (entries.isEmpty()) {
            throw new ApiException("LOAN_SCHEDULE_NOT_FOUND", "Amortization schedule not found for loan ID: " + loanId);
        }

        return LoanScheduleResponse.from(loan, entries);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> listLoans(Long customerId, LoanStatus status) {
        List<Loan> loans;

        if (customerId != null && status != null) {
            loans = loanRepository.findByCustomerIdAndStatusOrderByIdDesc(customerId, status);
        } else if (customerId != null) {
            loans = loanRepository.findByCustomerIdOrderByIdDesc(customerId);
        } else if (status != null) {
            loans = loanRepository.findByStatusOrderByIdDesc(status);
        } else {
            loans = loanRepository.findAll().stream()
                    .sorted(Comparator.comparing(Loan::getId).reversed())
                    .toList();
        }

        return loans.stream().map(LoanResponse::from).toList();
    }

    @Transactional
    public LoanResponse approveLoan(Long loanId) {
        Loan loan = findLoan(loanId);
        if (loan.getStatus() != LoanStatus.APPLIED) {
            throw new ApiException("LOAN_NOT_APPLIED", "Only applied loans can be approved");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setDecisionReason(null);
        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse rejectLoan(Long loanId, LoanDecisionRequest request) {
        Loan loan = findLoan(loanId);
        if (loan.getStatus() != LoanStatus.APPLIED && loan.getStatus() != LoanStatus.APPROVED) {
            throw new ApiException("LOAN_NOT_REJECTABLE", "Loan cannot be rejected in current status");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setDecisionReason(request.reason().trim());
        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse disburseLoan(Long loanId) {
        Loan loan = findLoan(loanId);
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new ApiException("LOAN_NOT_APPROVED", "Only approved loans can be disbursed");
        }

        loan.setStatus(LoanStatus.DISBURSED);
        loan.setOutstandingAmount(loan.getPrincipalAmount());
        loan.setDecisionReason(null);
        Loan savedLoan = loanRepository.save(loan);

        // Generate and save amortization schedule
        loanScheduleRepository.deleteByLoanId(loanId);
        List<LoanScheduleEntry> schedule = AmortizationCalculator.generateSchedule(
                savedLoan.getId(),
                savedLoan.getPrincipalAmount(),
                savedLoan.getAnnualInterestRate(),
                savedLoan.getTermMonths(),
                savedLoan.getStartDate(),
                savedLoan.getAmortizationType());
        loanScheduleRepository.saveAll(schedule);

        return LoanResponse.from(savedLoan);
    }

    @Transactional
    public LoanResponse repayLoan(Long loanId, LoanRepaymentRequest request) {
        Loan loan = findLoan(loanId);
        if (loan.getStatus() != LoanStatus.DISBURSED) {
            throw new ApiException("LOAN_NOT_DISBURSED", "Repayments are allowed only for disbursed loans");
        }

        BigDecimal amount = request.amount();
        if (loan.getOutstandingAmount().compareTo(amount) < 0) {
            throw new ApiException("LOAN_OVERPAYMENT", "Repayment amount exceeds outstanding balance");
        }

        BigDecimal remaining = loan.getOutstandingAmount().subtract(amount);
        loan.setOutstandingAmount(remaining);
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.CLOSED);
        }

        return LoanResponse.from(loanRepository.save(loan));
    }

    private Loan findLoan(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new ApiException("LOAN_NOT_FOUND", "Loan not found"));
    }

    private String normalizeLoanNumber(String loanNumber) {
        return loanNumber.trim().toUpperCase();
    }
}
