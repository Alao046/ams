package com.justjava.ams.financeAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleControlDefaultsResponse {

    private Long organizationId;
    private Integer createdCount;
    private Integer existingCount;
    private List<ModuleControlDTO> modules;
}
