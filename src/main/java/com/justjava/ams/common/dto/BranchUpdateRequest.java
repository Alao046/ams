package com.justjava.ams.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BranchUpdateRequest {
    @NotBlank(message = "Branch name is required")
    @Size(max = 255, message = "Branch name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Branch code is required")
    @Size(max = 255, message = "Branch code must not exceed 255 characters")
    private String code;

    private Boolean active;
}
