package com.cbs.ledger.service;

import com.cbs.common.exception.ApiException;
import com.cbs.ledger.dto.AccountResponse;
import com.cbs.ledger.dto.CreateAccountRequest;
import com.cbs.ledger.model.LedgerAccount;
import com.cbs.ledger.repository.LedgerAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LedgerAccountService {

    private final LedgerAccountRepository ledgerAccountRepository;

    public LedgerAccountService(LedgerAccountRepository ledgerAccountRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        String code = normalizeCode(request.code());
        if (ledgerAccountRepository.existsByCode(code)) {
            throw new ApiException("LEDGER_ACCOUNT_EXISTS", "Account code already exists");
        }

        LedgerAccount account = new LedgerAccount(code, request.name().trim(), request.type());
        return AccountResponse.fromEntity(ledgerAccountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts() {
        return ledgerAccountRepository.findAllByOrderByCodeAsc()
                .stream()
                .map(AccountResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public LedgerAccount getAccountByCode(String accountCode) {
        String code = normalizeCode(accountCode);
        return ledgerAccountRepository.findByCode(code)
                .orElseThrow(() -> new ApiException("LEDGER_ACCOUNT_NOT_FOUND", "Account not found: " + code));
    }

    @Transactional(readOnly = true)
    public LedgerAccount getActiveAccountByCode(String accountCode) {
        LedgerAccount account = getAccountByCode(accountCode);
        if (!account.isActive()) {
            throw new ApiException("LEDGER_ACCOUNT_INACTIVE", "Account is inactive: " + account.getCode());
        }
        return account;
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}
