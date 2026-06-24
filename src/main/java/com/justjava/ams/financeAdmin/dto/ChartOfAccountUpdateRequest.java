package com.justjava.ams.financeAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChartOfAccountUpdateRequest {
    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    private String accountName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String accountType;

    private String accountSubtype;

    private String normalBalance;

    private Boolean active;
}
