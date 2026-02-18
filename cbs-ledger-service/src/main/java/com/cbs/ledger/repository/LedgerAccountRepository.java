package com.cbs.ledger.repository;

import com.cbs.ledger.model.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, Long> {
    boolean existsByCode(String code);
    Optional<LedgerAccount> findByCode(String code);
    List<LedgerAccount> findAllByOrderByCodeAsc();
}
