package com.justjava.ams.cfo.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrialBalanceDTO {

    private Long id;
    private Long organizationId;
    private LocalDate reportDate;
    private String accountCode;
    private String accountName;
    private BigDecimal debitBalance;
    private BigDecimal creditBalance;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

