package com.cbs.deposit.repository;

import com.cbs.deposit.model.DepositAccount;
import com.cbs.deposit.model.DepositStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositAccountRepository extends JpaRepository<DepositAccount, Long> {

    boolean existsByDepositNumber(String depositNumber);

    List<DepositAccount> findByCustomerIdOrderByIdDesc(Long customerId);

    List<DepositAccount> findByStatusOrderByIdDesc(DepositStatus status);

    List<DepositAccount> findByCustomerIdAndStatusOrderByIdDesc(Long customerId, DepositStatus status);
}
