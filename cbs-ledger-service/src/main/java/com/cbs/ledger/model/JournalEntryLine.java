package com.cbs.ledger.model;

import com.cbs.common.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "journal_entry_lines")
public class JournalEntryLine extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private LedgerAccount account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    public JournalEntryLine() {
    }

    public JournalEntryLine(LedgerAccount account, EntryType entryType, BigDecimal amount) {
        this.account = account;
        this.entryType = entryType;
        this.amount = amount;
    }

    void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public Long getId() {
        return id;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public LedgerAccount getAccount() {
        return account;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
