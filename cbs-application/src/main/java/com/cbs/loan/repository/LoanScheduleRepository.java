package com.cbs.loan.repository;

import com.cbs.loan.model.LoanScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LoanScheduleRepository extends JpaRepository<LoanScheduleEntry, Long> {

    List<LoanScheduleEntry> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);

    @Modifying
    @Transactional
    @Query("DELETE FROM LoanScheduleEntry se WHERE se.loanId = :loanId")
    void deleteByLoanId(@Param("loanId") Long loanId);
}
