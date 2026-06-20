package com.justjava.ams.financeAdmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.justjava.ams.common.entity.Organization;

import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    @Column(nullable = false)
    private Integer fiscalYearStartMonth;

    @Column(nullable = false)
    private Integer fiscalYearEndMonth;

    @Column(nullable = false)
    private Integer numberOfQuarters = 4;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountingMethod accountingMethod = AccountingMethod.ACCRUAL;

    @Column(nullable = false)
    private Boolean multiCurrencyEnabled = false;

    @Column(nullable = false)
    private String baseCurrency = "NGN";

    @Column(nullable = false)
    private Boolean allowNegativeInventory = false;

    @Column(nullable = false)
    private Boolean requireApprovalForTransactions = true;

    @Column(nullable = false)
    private Integer approvalHierarchyLevels = 2;

    @Column
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum AccountingMethod {
        ACCRUAL,
        CASH,
        HYBRID
    }
}
