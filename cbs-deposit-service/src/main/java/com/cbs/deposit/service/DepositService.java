package com.cbs.deposit.service;

import com.cbs.common.exception.ApiException;
import com.cbs.deposit.dto.AccrueInterestRequest;
import com.cbs.deposit.dto.CreateDepositRequest;
import com.cbs.deposit.dto.DepositResponse;
import com.cbs.deposit.dto.DepositStatusReasonRequest;
import com.cbs.deposit.model.DepositAccount;
import com.cbs.deposit.model.DepositStatus;
import com.cbs.deposit.repository.DepositAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class DepositService {

    private final DepositAccountRepository depositAccountRepository;

    public DepositService(DepositAccountRepository depositAccountRepository) {
        this.depositAccountRepository = depositAccountRepository;
    }

    @Transactional
    public DepositResponse createDeposit(CreateDepositRequest request) {
        if (request.termDays() <= 0) {
            throw new ApiException("DEPOSIT_INVALID_TERM", "Term days must be greater than zero");
        }

        if (request.openingDate().isAfter(request.maturityDate())) {
            throw new ApiException("DEPOSIT_INVALID_DATE_RANGE", "Opening date must be before or equal to maturity date");
        }

        String depositNumber = normalizeDepositNumber(request.depositNumber());
        if (depositAccountRepository.existsByDepositNumber(depositNumber)) {
            throw new ApiException("DEPOSIT_NUMBER_EXISTS", "Deposit number already exists");
        }

        DepositAccount account = new DepositAccount(
                request.customerId(),
                request.settlementAccountId(),
                depositNumber,
                request.productType(),
                request.principalAmount(),
                request.annualInterestRate(),
                request.termDays(),
                request.openingDate(),
                request.maturityDate()
        );

        return DepositResponse.from(depositAccountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public DepositResponse getDeposit(Long depositId) {
        return DepositResponse.from(findDeposit(depositId));
    }

    @Transactional(readOnly = true)
    public List<DepositResponse> listDeposits(Long customerId, DepositStatus status) {
        List<DepositAccount> accounts;

        if (customerId != null && status != null) {
            accounts = depositAccountRepository.findByCustomerIdAndStatusOrderByIdDesc(customerId, status);
        } else if (customerId != null) {
            accounts = depositAccountRepository.findByCustomerIdOrderByIdDesc(customerId);
        } else if (status != null) {
            accounts = depositAccountRepository.findByStatusOrderByIdDesc(status);
        } else {
            accounts = depositAccountRepository.findAll().stream()
                    .sorted(Comparator.comparing(DepositAccount::getId).reversed())
                    .toList();
        }

        return accounts.stream().map(DepositResponse::from).toList();
    }

    @Transactional
    public DepositResponse accrueInterest(Long depositId, AccrueInterestRequest request) {
        DepositAccount account = findDeposit(depositId);
        ensureOpenStatus(account);

        BigDecimal updatedAmount = account.getCurrentAmount().add(request.amount());
        account.setCurrentAmount(updatedAmount);

        return DepositResponse.from(depositAccountRepository.save(account));
    }

    @Transactional
    public DepositResponse matureDeposit(Long depositId) {
        DepositAccount account = findDeposit(depositId);
        ensureOpenStatus(account);

        account.setStatus(DepositStatus.MATURED);
        account.setStatusReason(null);
        return DepositResponse.from(depositAccountRepository.save(account));
    }

    @Transactional
    public DepositResponse closeDeposit(Long depositId) {
        DepositAccount account = findDeposit(depositId);
        if (account.getStatus() != DepositStatus.MATURED) {
            throw new ApiException("DEPOSIT_NOT_MATURED", "Only matured deposit can be closed");
        }

        account.setStatus(DepositStatus.CLOSED);
        account.setStatusReason(null);
        return DepositResponse.from(depositAccountRepository.save(account));
    }

    @Transactional
    public DepositResponse breakDeposit(Long depositId, DepositStatusReasonRequest request) {
        DepositAccount account = findDeposit(depositId);
        if (account.getStatus() == DepositStatus.CLOSED) {
            throw new ApiException("DEPOSIT_ALREADY_CLOSED", "Closed deposit cannot be broken");
        }

        if (account.getStatus() == DepositStatus.BROKEN) {
            throw new ApiException("DEPOSIT_ALREADY_BROKEN", "Deposit is already broken");
        }

        account.setStatus(DepositStatus.BROKEN);
        account.setStatusReason(request.reason().trim());

        return DepositResponse.from(depositAccountRepository.save(account));
    }

    private DepositAccount findDeposit(Long depositId) {
        return depositAccountRepository.findById(depositId)
                .orElseThrow(() -> new ApiException("DEPOSIT_NOT_FOUND", "Deposit account not found"));
    }

    private void ensureOpenStatus(DepositAccount account) {
        if (account.getStatus() != DepositStatus.OPEN) {
            throw new ApiException("DEPOSIT_NOT_OPEN", "Operation is allowed only for open deposits");
        }
    }

    private String normalizeDepositNumber(String depositNumber) {
        return depositNumber.trim().toUpperCase();
    }
}
