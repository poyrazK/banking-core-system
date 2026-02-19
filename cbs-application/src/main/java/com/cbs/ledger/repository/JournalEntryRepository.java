package com.cbs.ledger.repository;

import com.cbs.ledger.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    boolean existsByReference(String reference);

    long countByValueDateBetween(LocalDate fromDate, LocalDate toDate);
}
