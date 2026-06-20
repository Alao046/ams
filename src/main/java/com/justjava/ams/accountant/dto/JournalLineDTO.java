package com.justjava.ams.accountant.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalLineDTO {

	private Long id;
	private Long manualJournalId;
	private Long chartOfAccountId;
	private String accountCode;
	private String accountName;
	private BigDecimal debitAmount;
	private BigDecimal creditAmount;
	private String departmentCode;
	private String projectCode;
	private String branchCode;
	private String narration;
	private Integer lineSequence;
	private LocalDateTime createdAt;
}


