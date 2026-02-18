package com.cbs.ledger.repository;

import com.cbs.ledger.model.EntryType;
import com.cbs.ledger.model.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    @Query("""
            select coalesce(sum(line.amount), 0)
            from JournalEntryLine line
            where line.account.code = :accountCode and line.entryType = :entryType
            """)
    BigDecimal sumAmountByAccountCodeAndEntryType(@Param("accountCode") String accountCode,
                                                  @Param("entryType") EntryType entryType);

    @Query("""
            select coalesce(sum(line.amount), 0)
            from JournalEntryLine line
            where line.entryType = :entryType
              and line.journalEntry.valueDate between :fromDate and :toDate
            """)
    BigDecimal sumAmountByEntryTypeAndDateRange(@Param("entryType") EntryType entryType,
                                                @Param("fromDate") LocalDate fromDate,
                                                @Param("toDate") LocalDate toDate);
}
