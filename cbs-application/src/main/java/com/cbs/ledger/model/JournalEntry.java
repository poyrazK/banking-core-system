package com.cbs.ledger.model;

import com.cbs.common.model.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(
        name = "journal_entries",
        uniqueConstraints = @UniqueConstraint(name = "uk_journal_entries_reference", columnNames = "reference")
)
public class JournalEntry extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String reference;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<JournalEntryLine> lines = new ArrayList<>();

    public JournalEntry() {
    }

    public JournalEntry(String reference, String description, LocalDate valueDate) {
        this.reference = reference;
        this.description = description;
        this.valueDate = valueDate;
    }

    public void addLine(JournalEntryLine line) {
        lines.add(line);
        line.setJournalEntry(this);
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public List<JournalEntryLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
}
