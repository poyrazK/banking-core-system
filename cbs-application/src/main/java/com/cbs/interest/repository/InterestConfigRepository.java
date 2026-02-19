package com.cbs.interest.repository;

import com.cbs.interest.model.InterestConfig;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestConfigRepository extends JpaRepository<InterestConfig, Long> {

    boolean existsByProductCode(String productCode);

    Optional<InterestConfig> findByProductCode(String productCode);
}
