package com.cbs.payment.integration;

import com.cbs.ledger.dto.PostPolicyEntryRequest;
import com.cbs.ledger.model.LedgerOperationType;
import com.cbs.ledger.service.LedgerPostingService;
import com.cbs.payment.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class DirectPaymentLedgerPostingClient implements LedgerPostingClient {

    private final LedgerPostingService ledgerPostingService;
    private final boolean postingEnabled;

    public DirectPaymentLedgerPostingClient(LedgerPostingService ledgerPostingService,
            @org.springframework.beans.factory.annotation.Value("${ledger.posting.enabled:true}") boolean postingEnabled) {
        this.ledgerPostingService = ledgerPostingService;
        this.postingEnabled = postingEnabled;
    }

    @Override
    public void postPayment(Payment payment) {
        if (!postingEnabled) {
            return;
        }
        PostPolicyEntryRequest request = new PostPolicyEntryRequest(
                payment.getReference(),
                payment.getDescription(),
                payment.getValueDate(),
                LedgerOperationType.PAYMENT,
                payment.getAmount(),
                payment.getSourceAccountId().toString(),
                payment.getDestinationAccountId() == null ? null : payment.getDestinationAccountId().toString());
        ledgerPostingService.postPolicyEntry(request);
    }
}
