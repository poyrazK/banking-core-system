package com.cbs.fee.repository;

import com.cbs.fee.model.FeeCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeChargeRepository extends JpaRepository<FeeCharge, Long> {

    List<FeeCharge> findByAccountIdOrderByIdDesc(Long accountId);

    List<FeeCharge> findByFeeCodeOrderByIdDesc(String feeCode);
}
