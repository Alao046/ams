package com.justjava.ams.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchCreateRequest {
	private Long organizationId;
	private String name;
	private String code;
	private Boolean active = true;
}


