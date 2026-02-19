package com.cbs.fx.repository;

import com.cbs.fx.model.FxRate;
import com.cbs.fx.model.FxRateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FxRateRepository extends JpaRepository<FxRate, Long> {

    boolean existsByCurrencyPair(String currencyPair);

    Optional<FxRate> findByCurrencyPair(String currencyPair);

    List<FxRate> findByStatusOrderByIdDesc(FxRateStatus status);
}
