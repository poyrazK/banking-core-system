package com.cbs.transaction.integration;

import com.cbs.ledger.dto.PostPolicyEntryRequest;
import com.cbs.ledger.model.LedgerOperationType;
import com.cbs.ledger.service.LedgerPostingService;
import com.cbs.transaction.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class DirectLedgerPostingClient implements LedgerPostingClient {

    private final LedgerPostingService ledgerPostingService;

    public DirectLedgerPostingClient(LedgerPostingService ledgerPostingService) {
        this.ledgerPostingService = ledgerPostingService;
    }

    @Override
    public void postTransaction(Transaction transaction) {
        PostPolicyEntryRequest request = new PostPolicyEntryRequest(
                transaction.getReference(),
                transaction.getDescription(),
                transaction.getValueDate(),
                LedgerOperationType.valueOf(transaction.getType().name()),
                transaction.getAmount(),
                transaction.getAccountId().toString(),
                transaction.getCounterpartyAccountId() == null ? null
                        : transaction.getCounterpartyAccountId().toString());
        ledgerPostingService.postPolicyEntry(request);
    }
}
