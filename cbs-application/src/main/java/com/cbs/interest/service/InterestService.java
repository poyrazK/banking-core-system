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
import com.cbs.account.dto.AccountResponse;
import com.cbs.account.dto.BalanceUpdateRequest;
import com.cbs.account.service.AccountService;
import com.cbs.interest.model.AccrualStatus;
import com.cbs.ledger.dto.PostPolicyEntryRequest;
import com.cbs.ledger.model.LedgerOperationType;
import com.cbs.ledger.service.LedgerPostingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InterestService {

    private final InterestConfigRepository interestConfigRepository;
    private final InterestAccrualRepository interestAccrualRepository;
    private final AccountService accountService;
    private final LedgerPostingService ledgerPostingService;

    public InterestService(InterestConfigRepository interestConfigRepository,
            InterestAccrualRepository interestAccrualRepository,
            AccountService accountService,
            LedgerPostingService ledgerPostingService) {
        this.interestConfigRepository = interestConfigRepository;
        this.interestAccrualRepository = interestAccrualRepository;
        this.accountService = accountService;
        this.ledgerPostingService = ledgerPostingService;
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
                request.accrualFrequencyDays());

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
                config.getInterestBasis());

        InterestAccrual accrual = new InterestAccrual(
                request.accountId(),
                config.getProductCode(),
                request.principalAmount(),
                accruedAmount,
                request.accrualDate());

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

    @Transactional
    public int calculateDailyAccrualsForAllAccounts(LocalDate processDate) {
        int accruedCount = 0;
        List<InterestConfig> activeConfigs = interestConfigRepository.findAll().stream()
                .filter(config -> config.getStatus() == InterestStatus.ACTIVE)
                .toList();

        for (InterestConfig config : activeConfigs) {
            com.cbs.account.model.AccountType targetType;
            try {
                targetType = com.cbs.account.model.AccountType.valueOf(config.getProductCode());
            } catch (IllegalArgumentException e) {
                continue;
            }

            List<AccountResponse> eligibleAccounts = accountService.listAccountsByTypeAndStatus(
                    targetType, com.cbs.account.model.AccountStatus.ACTIVE);

            for (AccountResponse account : eligibleAccounts) {
                BigDecimal balance = account.balance();
                if (balance.compareTo(BigDecimal.ZERO) > 0 &&
                        !interestAccrualRepository.existsByAccountIdAndAccrualDate(account.id(), processDate)) {

                    BigDecimal accruedAmount = calculateAccruedAmount(
                            balance,
                            config.getAnnualRate(),
                            1,
                            config.getInterestBasis());

                    InterestAccrual accrual = new InterestAccrual(
                            account.id(),
                            config.getProductCode(),
                            balance,
                            accruedAmount,
                            processDate);

                    interestAccrualRepository.save(accrual);
                    accruedCount++;
                }
            }
        }
        return accruedCount;
    }

    @Transactional
    public int capitalizeMonthlyAccruals(LocalDate processDate) {
        int capitalizedCount = 0;
        List<InterestAccrual> pendingAccruals = interestAccrualRepository.findByStatus(AccrualStatus.ACCRUED);

        Map<Long, List<InterestAccrual>> accrualsByAccount = pendingAccruals.stream()
                .collect(Collectors.groupingBy(InterestAccrual::getAccountId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateSuffix = processDate.format(formatter);

        for (Map.Entry<Long, List<InterestAccrual>> entry : accrualsByAccount.entrySet()) {
            Long accountId = entry.getKey();
            List<InterestAccrual> accountAccruals = entry.getValue();

            BigDecimal totalAccrued = accountAccruals.stream()
                    .map(InterestAccrual::getAccruedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalAccrued.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            AccountResponse accountRes = accountService.getAccount(accountId);
            String accountCode = accountRes.accountNumber();

            accountService.creditBalance(accountId, new BalanceUpdateRequest(totalAccrued, accountRes.currency()));

            String reference = "INT-CAP-" + accountCode + "-" + dateSuffix;
            ledgerPostingService.postPolicyEntry(new PostPolicyEntryRequest(
                    reference,
                    "Monthly interest capitalization",
                    processDate,
                    LedgerOperationType.INTEREST,
                    totalAccrued,
                    accountCode,
                    null));

            accountAccruals.forEach(accrual -> {
                accrual.setStatus(AccrualStatus.CAPITALIZED);
                accrual.setCapitalizationDate(processDate);
                interestAccrualRepository.save(accrual);
            });
            capitalizedCount++;
        }
        return capitalizedCount;
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
