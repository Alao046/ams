package com.justjava.ams.financeAdmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.justjava.ams.common.entity.Organization;

import java.time.LocalDateTime;

@Entity
@Table(name = "module_controls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModuleType moduleType;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean allowUserConfiguration = false;

    @Column(nullable = false)
    private Boolean requiresApproval = false;

    @Column(nullable = false)
    private Boolean enableAuditTrail = true;

    @Column(nullable = false)
    private Boolean enableNotifications = true;

    @Column(nullable = false)
    private Integer maxTransactionAmountLimit = Integer.MAX_VALUE;

    @Column
    private String configurationJson;

    @Column
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ModuleType {
        GENERAL_LEDGER,
        ACCOUNTS_PAYABLE,
        ACCOUNTS_RECEIVABLE,
        INVENTORY,
        FIXED_ASSETS,
        PAYROLL,
        BUDGETING,
        REPORTING,
        BANKING,
        PAYMENTS,
        APPROVALS,
        AUDIT
    }
}
