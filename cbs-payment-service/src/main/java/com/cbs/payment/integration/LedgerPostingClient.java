package com.cbs.payment.integration;

import com.cbs.payment.model.Payment;

public interface LedgerPostingClient {

    void postPayment(Payment payment);
}
