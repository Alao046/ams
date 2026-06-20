package com.justjava.ams.accountant.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChartOfAccountsDTO {

    private Long id;
    private Long organizationId;
    private String accountCode;
    private String accountName;
    private String description;
    private String accountType;
    private String accountSubtype;
    private String normalBalance;
    private BigDecimal balance;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

