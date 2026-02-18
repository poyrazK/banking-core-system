package com.cbs.fee.service;

import com.cbs.common.exception.ApiException;
import com.cbs.fee.dto.ChargeFeeRequest;
import com.cbs.fee.dto.CreateFeeConfigRequest;
import com.cbs.fee.dto.FeeChargeResponse;
import com.cbs.fee.dto.FeeConfigResponse;
import com.cbs.fee.dto.UpdateFeeConfigRequest;
import com.cbs.fee.model.FeeCharge;
import com.cbs.fee.model.FeeConfig;
import com.cbs.fee.model.FeeStatus;
import com.cbs.fee.repository.FeeChargeRepository;
import com.cbs.fee.repository.FeeConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class FeeService {

    private final FeeConfigRepository feeConfigRepository;
    private final FeeChargeRepository feeChargeRepository;

    public FeeService(FeeConfigRepository feeConfigRepository,
                      FeeChargeRepository feeChargeRepository) {
        this.feeConfigRepository = feeConfigRepository;
        this.feeChargeRepository = feeChargeRepository;
    }

    @Transactional
    public FeeConfigResponse createConfig(CreateFeeConfigRequest request) {
        String feeCode = normalizeFeeCode(request.feeCode());
        if (feeConfigRepository.existsByFeeCode(feeCode)) {
            throw new ApiException("FEE_CONFIG_EXISTS", "Fee config already exists for fee code");
        }

        FeeConfig config = new FeeConfig(
                feeCode,
                request.name().trim(),
                request.feeType(),
                request.fixedAmount(),
                request.percentageRate()
        );

        return FeeConfigResponse.from(feeConfigRepository.save(config));
    }

    @Transactional
    public FeeConfigResponse updateConfig(String feeCode, UpdateFeeConfigRequest request) {
        FeeConfig config = findConfig(feeCode);
        config.setName(request.name().trim());
        config.setFeeType(request.feeType());
        config.setFixedAmount(request.fixedAmount());
        config.setPercentageRate(request.percentageRate());
        config.setStatus(request.status());

        return FeeConfigResponse.from(feeConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public FeeConfigResponse getConfig(String feeCode) {
        return FeeConfigResponse.from(findConfig(feeCode));
    }

    @Transactional(readOnly = true)
    public List<FeeConfigResponse> listConfigs(FeeStatus status) {
        return feeConfigRepository.findAll().stream()
                .filter(config -> status == null || config.getStatus() == status)
                .sorted(Comparator.comparing(FeeConfig::getId).reversed())
                .map(FeeConfigResponse::from)
                .toList();
    }

    @Transactional
    public FeeChargeResponse chargeFee(ChargeFeeRequest request) {
        FeeConfig config = findConfig(request.feeCode());
        if (config.getStatus() != FeeStatus.ACTIVE) {
            throw new ApiException("FEE_CONFIG_INACTIVE", "Fee config is not active");
        }

        BigDecimal feeAmount = calculateFee(config.getFixedAmount(), config.getPercentageRate(), request.baseAmount());
        FeeCharge charge = new FeeCharge(
                request.accountId(),
                config.getFeeCode(),
                request.baseAmount(),
                feeAmount,
                request.currency().trim().toUpperCase()
        );

        return FeeChargeResponse.from(feeChargeRepository.save(charge));
    }

    @Transactional(readOnly = true)
    public List<FeeChargeResponse> listCharges(Long accountId, String feeCode) {
        List<FeeCharge> charges;

        if (accountId != null && feeCode != null) {
            String normalized = normalizeFeeCode(feeCode);
            charges = feeChargeRepository.findAll().stream()
                    .filter(charge -> charge.getAccountId().equals(accountId))
                    .filter(charge -> charge.getFeeCode().equals(normalized))
                    .sorted(Comparator.comparing(FeeCharge::getId).reversed())
                    .toList();
        } else if (accountId != null) {
            charges = feeChargeRepository.findByAccountIdOrderByIdDesc(accountId);
        } else if (feeCode != null) {
            charges = feeChargeRepository.findByFeeCodeOrderByIdDesc(normalizeFeeCode(feeCode));
        } else {
            charges = feeChargeRepository.findAll().stream()
                    .sorted(Comparator.comparing(FeeCharge::getId).reversed())
                    .toList();
        }

        return charges.stream().map(FeeChargeResponse::from).toList();
    }

    private FeeConfig findConfig(String feeCode) {
        String normalized = normalizeFeeCode(feeCode);
        return feeConfigRepository.findByFeeCode(normalized)
                .orElseThrow(() -> new ApiException("FEE_CONFIG_NOT_FOUND", "Fee config not found"));
    }

    private BigDecimal calculateFee(BigDecimal fixedAmount, BigDecimal percentageRate, BigDecimal baseAmount) {
        BigDecimal variable = baseAmount
                .multiply(percentageRate)
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);

        return fixedAmount.add(variable).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeFeeCode(String feeCode) {
        return feeCode.trim().toUpperCase();
    }
}
