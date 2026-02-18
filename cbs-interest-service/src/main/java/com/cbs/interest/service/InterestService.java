package com.cbs.interest.service;

import com.cbs.common.exception.ApiException;
import com.cbs.interest.dto.CreateInterestConfigRequest;
import com.cbs.interest.dto.InterestAccrualResponse;
import com.cbs.interest.dto.InterestConfigResponse;
import com.cbs.interest.dto.RunAccrualRequest;
import com.cbs.interest.dto.UpdateInterestConfigRequest;
import com.cbs.interest.model.InterestAccrual;
import com.cbs.interest.model.InterestBasis;
import com.cbs.interest.model.InterestConfig;
import com.cbs.interest.model.InterestStatus;
import com.cbs.interest.repository.InterestAccrualRepository;
import com.cbs.interest.repository.InterestConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class InterestService {

    private final InterestConfigRepository interestConfigRepository;
    private final InterestAccrualRepository interestAccrualRepository;

    public InterestService(InterestConfigRepository interestConfigRepository,
                           InterestAccrualRepository interestAccrualRepository) {
        this.interestConfigRepository = interestConfigRepository;
        this.interestAccrualRepository = interestAccrualRepository;
    }

    @Transactional
    public InterestConfigResponse createConfig(CreateInterestConfigRequest request) {
        if (request.accrualFrequencyDays() <= 0) {
            throw new ApiException("INTEREST_INVALID_FREQUENCY", "Accrual frequency must be greater than zero");
        }

        String productCode = normalizeProductCode(request.productCode());
        if (interestConfigRepository.existsByProductCode(productCode)) {
            throw new ApiException("INTEREST_CONFIG_EXISTS", "Interest config already exists for product code");
        }

        InterestConfig config = new InterestConfig(
                productCode,
                request.annualRate(),
                request.interestBasis(),
                request.accrualFrequencyDays()
        );

        return InterestConfigResponse.from(interestConfigRepository.save(config));
    }

    @Transactional
    public InterestConfigResponse updateConfig(String productCode, UpdateInterestConfigRequest request) {
        if (request.accrualFrequencyDays() <= 0) {
            throw new ApiException("INTEREST_INVALID_FREQUENCY", "Accrual frequency must be greater than zero");
        }

        InterestConfig config = findConfig(productCode);
        config.setAnnualRate(request.annualRate());
        config.setInterestBasis(request.interestBasis());
        config.setAccrualFrequencyDays(request.accrualFrequencyDays());
        config.setStatus(request.status());

        return InterestConfigResponse.from(interestConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public InterestConfigResponse getConfig(String productCode) {
        return InterestConfigResponse.from(findConfig(productCode));
    }

    @Transactional(readOnly = true)
    public List<InterestConfigResponse> listConfigs(InterestStatus status) {
        return interestConfigRepository.findAll().stream()
                .filter(config -> status == null || config.getStatus() == status)
                .sorted(Comparator.comparing(InterestConfig::getId).reversed())
                .map(InterestConfigResponse::from)
                .toList();
    }

    @Transactional
    public InterestAccrualResponse runAccrual(RunAccrualRequest request) {
        InterestConfig config = findConfig(request.productCode());
        if (config.getStatus() != InterestStatus.ACTIVE) {
            throw new ApiException("INTEREST_CONFIG_INACTIVE", "Interest config is not active");
        }

        BigDecimal accruedAmount = calculateAccruedAmount(
                request.principalAmount(),
                config.getAnnualRate(),
                config.getAccrualFrequencyDays(),
                config.getInterestBasis()
        );

        InterestAccrual accrual = new InterestAccrual(
                request.accountId(),
                config.getProductCode(),
                request.principalAmount(),
                accruedAmount,
                request.accrualDate()
        );

        return InterestAccrualResponse.from(interestAccrualRepository.save(accrual));
    }

    @Transactional(readOnly = true)
    public List<InterestAccrualResponse> listAccruals(Long accountId, String productCode) {
        List<InterestAccrual> accruals;

        if (accountId != null && productCode != null) {
            String normalized = normalizeProductCode(productCode);
            accruals = interestAccrualRepository.findAll().stream()
                    .filter(item -> item.getAccountId().equals(accountId))
                    .filter(item -> item.getProductCode().equals(normalized))
                    .sorted(Comparator.comparing(InterestAccrual::getId).reversed())
                    .toList();
        } else if (accountId != null) {
            accruals = interestAccrualRepository.findByAccountIdOrderByIdDesc(accountId);
        } else if (productCode != null) {
            accruals = interestAccrualRepository.findByProductCodeOrderByIdDesc(normalizeProductCode(productCode));
        } else {
            accruals = interestAccrualRepository.findAll().stream()
                    .sorted(Comparator.comparing(InterestAccrual::getId).reversed())
                    .toList();
        }

        return accruals.stream().map(InterestAccrualResponse::from).toList();
    }

    private InterestConfig findConfig(String productCode) {
        String normalized = normalizeProductCode(productCode);
        return interestConfigRepository.findByProductCode(normalized)
                .orElseThrow(() -> new ApiException("INTEREST_CONFIG_NOT_FOUND", "Interest config not found"));
    }

    private BigDecimal calculateAccruedAmount(BigDecimal principal,
                                              BigDecimal annualRate,
                                              Integer frequencyDays,
                                              InterestBasis basis) {
        BigDecimal ratePerPeriod = annualRate
                .divide(BigDecimal.valueOf(100), 12, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(frequencyDays))
                .divide(BigDecimal.valueOf(365), 12, RoundingMode.HALF_UP);

        BigDecimal base = basis == InterestBasis.SIMPLE
                ? principal
                : principal.add(principal.multiply(ratePerPeriod));

        return base.multiply(ratePerPeriod).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeProductCode(String productCode) {
        return productCode.trim().toUpperCase();
    }
}
