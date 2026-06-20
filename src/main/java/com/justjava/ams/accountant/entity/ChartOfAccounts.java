package com.justjava.ams.accountant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.justjava.ams.common.entity.Organization;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "chart_of_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organization_id", "account_code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChartOfAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String accountCode;

    @Column(nullable = false)
    private String accountName;

    @Column
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountSubtype accountSubtype;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DebitCredit normalBalance;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "account")
    private Set<GeneralLedger> generalLedgerEntries;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum AccountType {
        ASSET,
        LIABILITY,
        EQUITY,
        REVENUE,
        EXPENSE
    }

    public enum AccountSubtype {
        CURRENT_ASSET,
        FIXED_ASSET,
        CURRENT_LIABILITY,
        LONG_TERM_LIABILITY,
        RETAINED_EARNINGS,
        REVENUE,
        COST_OF_GOODS_SOLD,
        OPERATING_EXPENSE,
        OTHER_INCOME,
        OTHER_EXPENSE
    }

    public enum DebitCredit {
        DEBIT,
        CREDIT
    }
}

