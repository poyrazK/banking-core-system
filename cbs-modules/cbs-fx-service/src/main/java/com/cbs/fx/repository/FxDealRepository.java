package com.cbs.fx.repository;

import com.cbs.fx.model.FxDeal;
import com.cbs.fx.model.FxDealStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FxDealRepository extends JpaRepository<FxDeal, Long> {

    boolean existsByReference(String reference);

    List<FxDeal> findByCustomerIdOrderByIdDesc(Long customerId);

    List<FxDeal> findByStatusOrderByIdDesc(FxDealStatus status);

    List<FxDeal> findByCustomerIdAndStatusOrderByIdDesc(Long customerId, FxDealStatus status);
}
