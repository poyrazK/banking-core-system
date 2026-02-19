package com.cbs.account.repository;

import com.cbs.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByCustomerIdOrderByIdDesc(Long customerId);
}
