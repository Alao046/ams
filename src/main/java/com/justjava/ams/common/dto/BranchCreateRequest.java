package com.justjava.ams.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchCreateRequest {
	@NotNull(message = "Organization ID is required")
	private Long organizationId;

	@NotBlank(message = "Branch name is required")
	@Size(max = 255, message = "Branch name must not exceed 255 characters")
	private String name;

	@NotBlank(message = "Branch code is required")
	@Size(max = 255, message = "Branch code must not exceed 255 characters")
	private String code;

	private Boolean active = true;
}


