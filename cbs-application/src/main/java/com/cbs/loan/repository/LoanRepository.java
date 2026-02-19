package com.cbs.loan.repository;

import com.cbs.loan.model.Loan;
import com.cbs.loan.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByLoanNumber(String loanNumber);

    List<Loan> findByCustomerIdOrderByIdDesc(Long customerId);

    List<Loan> findByStatusOrderByIdDesc(LoanStatus status);

    List<Loan> findByCustomerIdAndStatusOrderByIdDesc(Long customerId, LoanStatus status);
}
