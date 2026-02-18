package com.cbs.transaction.integration;

import com.cbs.transaction.model.Transaction;

public interface LedgerPostingClient {

    void postTransaction(Transaction transaction);
}
