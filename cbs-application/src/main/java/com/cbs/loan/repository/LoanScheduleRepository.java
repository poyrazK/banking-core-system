package com.cbs.loan.repository;

import com.cbs.loan.model.LoanScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanScheduleRepository extends JpaRepository<LoanScheduleEntry, Long> {

    List<LoanScheduleEntry> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);

    void deleteByLoanId(Long loanId);
}
