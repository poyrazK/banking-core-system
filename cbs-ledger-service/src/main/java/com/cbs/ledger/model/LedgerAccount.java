package com.cbs.ledger.model;

import com.cbs.common.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "ledger_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uk_ledger_accounts_code", columnNames = "code")
)
public class LedgerAccount extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AccountType type;

    @Column(nullable = false)
    private boolean active = true;

    public LedgerAccount() {
    }

    public LedgerAccount(String code, String name, AccountType type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public AccountType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }
}
