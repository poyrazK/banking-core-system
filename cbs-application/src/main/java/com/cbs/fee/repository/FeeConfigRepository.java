package com.cbs.fee.repository;

import com.cbs.fee.model.FeeConfig;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeeConfigRepository extends JpaRepository<FeeConfig, Long> {

    boolean existsByFeeCode(String feeCode);

    Optional<FeeConfig> findByFeeCode(String feeCode);
}
