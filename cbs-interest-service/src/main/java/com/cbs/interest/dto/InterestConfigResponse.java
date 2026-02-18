package com.cbs.interest.dto;

import com.cbs.interest.model.InterestBasis;
import com.cbs.interest.model.InterestConfig;
import com.cbs.interest.model.InterestStatus;

import java.math.BigDecimal;

public record InterestConfigResponse(
        Long id,
        String productCode,
        BigDecimal annualRate,
        InterestBasis interestBasis,
        Integer accrualFrequencyDays,
        InterestStatus status
) {
    public static InterestConfigResponse from(InterestConfig config) {
        return new InterestConfigResponse(
                config.getId(),
                config.getProductCode(),
                config.getAnnualRate(),
                config.getInterestBasis(),
                config.getAccrualFrequencyDays(),
                config.getStatus()
        );
    }
}
