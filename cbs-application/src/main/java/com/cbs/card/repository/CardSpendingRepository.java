package com.cbs.card.repository;

import com.cbs.card.model.CardSpendingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CardSpendingRepository extends JpaRepository<CardSpendingRecord, Long> {

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM CardSpendingRecord r "
            + "WHERE r.cardId = :cardId AND r.spendingDate = :date")
    BigDecimal sumDailySpending(@Param("cardId") Long cardId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM CardSpendingRecord r "
            + "WHERE r.cardId = :cardId "
            + "AND r.spendingDate BETWEEN :startOfMonth AND :endOfMonth")
    BigDecimal sumMonthlySpending(@Param("cardId") Long cardId,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth);
}
