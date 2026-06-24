package com.justjava.ams.accountant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class JournalLineUpdateRequest {
    @NotNull(message = "Chart of account is required")
    private Long chartOfAccountId;

    @DecimalMin(value = "0.00", message = "Debit amount cannot be negative")
    private BigDecimal debitAmount;

    @DecimalMin(value = "0.00", message = "Credit amount cannot be negative")
    private BigDecimal creditAmount;

    @Size(max = 50, message = "Department code must not exceed 50 characters")
    private String departmentCode;

    @Size(max = 50, message = "Project code must not exceed 50 characters")
    private String projectCode;

    @Size(max = 50, message = "Branch code must not exceed 50 characters")
    private String branchCode;

    @Size(max = 5000, message = "Narration must not exceed 5000 characters")
    private String narration;

    private Integer lineSequence;
}
