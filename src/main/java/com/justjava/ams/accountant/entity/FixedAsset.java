package com.justjava.ams.accountant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.justjava.ams.common.entity.Organization;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fixed_assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chart_account_id", nullable = false)
    private ChartOfAccounts chartAccount;

    @Column(unique = true, nullable = false)
    private String assetCode;

    @Column(nullable = false)
    private String assetName;

    @Column
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AssetCategory category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal originalCost;

    @Column(nullable = false)
    private LocalDate acquisitionDate;

    @Column
    private LocalDate disposalDate;

    @Column(precision = 19, scale = 2)
    private BigDecimal depreciation = BigDecimal.ZERO;

    @Column(precision = 3, scale = 2)
    private BigDecimal depreciationRate = BigDecimal.ZERO;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DepreciationMethod depreciationMethod;

    @Column(nullable = false)
    private Boolean active = true;

    @Column
    private String location;

    @Column
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum AssetCategory {
        PROPERTY,
        EQUIPMENT,
        VEHICLES,
        FURNITURE,
        TECHNOLOGY,
        OTHER
    }

    public enum DepreciationMethod {
        STRAIGHT_LINE,
        DECLINING_BALANCE,
        SUM_OF_YEARS,
        UNITS_OF_PRODUCTION
    }
}

