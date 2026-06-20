package com.justjava.ams.accountant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_lines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "manualJournal")
@ToString(exclude = "manualJournal")
public class JournalLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "manual_journal_id", nullable = false)
	private ManualJournal manualJournal;

	@ManyToOne
	@JoinColumn(name = "chart_of_account_id", nullable = false)
	private ChartOfAccounts chartOfAccounts;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal debitAmount = BigDecimal.ZERO;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal creditAmount = BigDecimal.ZERO;

	@Column(length = 50)
	private String departmentCode;

	@Column(length = 50)
	private String projectCode;

	@Column(length = 50)
	private String branchCode;

	@Column(columnDefinition = "TEXT")
	private String narration;

	@Column
	private Integer lineSequence;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@PrePersist
	@PreUpdate
	protected void validate() {
		if (debitAmount == null) debitAmount = BigDecimal.ZERO;
		if (creditAmount == null) creditAmount = BigDecimal.ZERO;
		if (debitAmount.compareTo(BigDecimal.ZERO) > 0 && creditAmount.compareTo(BigDecimal.ZERO) > 0) {
			throw new IllegalArgumentException("Journal line cannot have both debit and credit amounts");
		}
	}
}


