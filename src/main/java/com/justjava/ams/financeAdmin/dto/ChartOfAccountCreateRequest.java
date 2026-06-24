package com.justjava.ams.financeAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChartOfAccountCreateRequest {
    @NotBlank(message = "Account code is required")
    @Size(max = 255, message = "Account code must not exceed 255 characters")
    private String accountCode;

    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    private String accountName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Account type is required")
    private String accountType;

    @NotBlank(message = "Account subtype is required")
    private String accountSubtype;

    @NotBlank(message = "Normal balance is required")
    private String normalBalance;
}
