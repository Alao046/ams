package com.justjava.ams.cfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrialBalanceLineDTO {

    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private String normalBalance;
    private BigDecimal debitBalance;
    private BigDecimal creditBalance;
}
