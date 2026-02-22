package com.cbs.interest.repository;

import com.cbs.interest.model.InterestAccrual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestAccrualRepository extends JpaRepository<InterestAccrual, Long> {

    List<InterestAccrual> findByAccountIdOrderByIdDesc(Long accountId);

    List<InterestAccrual> findByProductCodeOrderByIdDesc(String productCode);

    boolean existsByAccountIdAndAccrualDate(Long accountId, java.time.LocalDate accrualDate);

    List<InterestAccrual> findByStatus(com.cbs.interest.model.AccrualStatus status);
}
