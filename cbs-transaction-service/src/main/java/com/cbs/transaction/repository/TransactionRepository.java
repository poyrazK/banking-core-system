package com.cbs.transaction.repository;

import com.cbs.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByReference(String reference);

    List<Transaction> findByAccountIdOrderByIdDesc(Long accountId);

    List<Transaction> findByCustomerIdOrderByIdDesc(Long customerId);

    List<Transaction> findByAccountIdAndCustomerIdOrderByIdDesc(Long accountId, Long customerId);
}
