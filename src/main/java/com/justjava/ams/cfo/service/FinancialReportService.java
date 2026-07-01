package com.justjava.ams.cfo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.justjava.ams.accountant.entity.ChartOfAccounts;
import com.justjava.ams.accountant.entity.GeneralLedger;
import com.justjava.ams.accountant.repository.GeneralLedgerRepository;
import com.justjava.ams.auditor.service.AuditLogService;
import com.justjava.ams.auditor.service.SecurityEventService;
import com.justjava.ams.cfo.dto.*;
import com.justjava.ams.cfo.entity.FinancialReport;
import com.justjava.ams.cfo.repository.FinancialReportRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.repository.OrganizationRepository;
import com.justjava.ams.financeAdmin.service.ModuleControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialReportService {

    private final FinancialReportRepository financialReportRepository;
    private final OrganizationRepository organizationRepository;
    private final GeneralLedgerRepository generalLedgerRepository;
    private final AuditLogService auditLogService;
    private final SecurityEventService securityEventService;
    private final ObjectMapper objectMapper;
    private final ModuleControlService moduleControlService;

    public FinancialReportDTO createReport(Long organizationId, FinancialReportDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Organization not found"));

        FinancialReport report = FinancialReport.builder()
                .organization(organization)
                .reportType(FinancialReport.ReportType.valueOf(dto.getReportType()))
                .reportName(dto.getReportName())
                .reportDate(dto.getReportDate())
                .fromDate(dto.getFromDate())
                .toDate(dto.getToDate())
                .generatedBy(dto.getGeneratedBy())
                .reportContent(dto.getReportContent())
                .notes(dto.getNotes())
                .build();

        return mapToDTO(financialReportRepository.save(report));
    }

    public FinancialReportDTO getReport(Long reportId) {
        FinancialReport report = financialReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Report not found"));
        return mapToDTO(report);
    }

    public List<FinancialReportDTO> getReportsByType(Long organizationId, String reportType) {
        // validate organization
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Organization not found"));

        try {
            FinancialReport.ReportType rt = FinancialReport.ReportType.valueOf(reportType);
            return financialReportRepository.findByOrganizationIdAndReportType(organizationId, rt)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid report type");
        }
    }

    public List<FinancialReportDTO> getReportsByDateRange(Long organizationId, LocalDate fromDate, LocalDate toDate) {
        return financialReportRepository.findByOrganizationIdAndReportDateBetween(organizationId, fromDate, toDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FinancialReportDTO approveReport(Long reportId, String approvedBy) {
        FinancialReport report = financialReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Report not found"));
        report.setStatus(FinancialReport.ReportStatus.APPROVED);
        report.setApprovedBy(approvedBy);
        report.setApprovedDate(LocalDate.now());
        return mapToDTO(financialReportRepository.save(report));
    }

    // Step 9 implementation: new methods for generating financial reports

    /**
     * Generate a financial report based on the request and optionally persist it.
     */
    public FinancialReportSummaryResponse generateReport(Long organizationId, FinancialReportGenerateRequest request, String generatedBy) {
        // Validate organization
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        // Validate report type
        if (request.getReportType() == null || request.getReportType().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report type is required");
        }

        String reportType = request.getReportType().trim().toUpperCase();
        if (!reportType.equals("INCOME_STATEMENT") && !reportType.equals("BALANCE_SHEET")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report type must be INCOME_STATEMENT or BALANCE_SHEET");
        }

        // Validate dates
        if (request.getFromDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From date is required");
        }
        if (request.getToDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "To date is required");
        }
        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From date must not be after to date");
        }

        // Default persist to true if not specified
        boolean persist = request.getPersist() != null ? request.getPersist() : true;

        // Use passed generatedBy or fallback to request
        String finalGeneratedBy = generatedBy != null ? generatedBy : (request.getGeneratedBy() != null ? request.getGeneratedBy() : "system");

        // Generate appropriate report
        if (reportType.equals("INCOME_STATEMENT")) {
            return generateIncomeStatement(organizationId, request.getFromDate(), request.getToDate(), finalGeneratedBy, persist);
        } else {
            return generateBalanceSheet(organizationId, request.getFromDate(), request.getToDate(), finalGeneratedBy, persist);
        }
    }

    /**
     * Generate an Income Statement from posted GL entries.
     */
    public FinancialReportSummaryResponse generateIncomeStatement(Long organizationId, LocalDate fromDate, LocalDate toDate, String generatedBy, boolean persist) {
        // Validate organization
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        // Validate dates
        if (fromDate == null || toDate == null || fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid date range is required");
        }

        // Check module controls if available
        if (moduleControlService != null) {
            try {
                moduleControlService.requireModuleEnabled(organizationId, com.justjava.ams.financeAdmin.entity.ModuleControl.ModuleType.REPORTING);
            } catch (ResponseStatusException ex) {
                // Module control check failed
                throw ex;
            }
        }

        // Fetch posted GL entries for the period
        List<GeneralLedger> postedEntries = generalLedgerRepository.findPostedEntriesByOrganizationAndDateRange(organizationId, fromDate, toDate);

        // Calculate revenue and expenses
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        List<FinancialReportLineDTO> lines = new ArrayList<>();

        // Group by account and calculate totals
        Map<Long, List<GeneralLedger>> groupedByAccount = postedEntries.stream()
                .collect(Collectors.groupingBy(gl -> gl.getAccount().getId()));

        for (List<GeneralLedger> glEntries : groupedByAccount.values()) {
            ChartOfAccounts account = glEntries.get(0).getAccount();

            if (account.getAccountType() == ChartOfAccounts.AccountType.REVENUE) {
                // Revenue: credits - debits
                BigDecimal credits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.CREDIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal debits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.DEBIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal revenueAmount = credits.subtract(debits);
                if (revenueAmount.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(FinancialReportLineDTO.builder()
                            .section("REVENUE")
                            .accountId(account.getId())
                            .accountCode(account.getAccountCode())
                            .accountName(account.getAccountName())
                            .amount(revenueAmount)
                            .build());
                    totalRevenue = totalRevenue.add(revenueAmount);
                }
            } else if (account.getAccountType() == ChartOfAccounts.AccountType.EXPENSE) {
                // Expenses: debits - credits
                BigDecimal debits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.DEBIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal credits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.CREDIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal expenseAmount = debits.subtract(credits);
                if (expenseAmount.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(FinancialReportLineDTO.builder()
                            .section("EXPENSE")
                            .accountId(account.getId())
                            .accountCode(account.getAccountCode())
                            .accountName(account.getAccountName())
                            .amount(expenseAmount)
                            .build());
                    totalExpenses = totalExpenses.add(expenseAmount);
                }
            }
        }

        BigDecimal netIncome = totalRevenue.subtract(totalExpenses);

        // Build the response
        FinancialReportSummaryResponse response = FinancialReportSummaryResponse.builder()
                .organizationId(organizationId)
                .reportType("INCOME_STATEMENT")
                .reportName("Income Statement from " + fromDate + " to " + toDate)
                .fromDate(fromDate)
                .toDate(toDate)
                .reportDate(LocalDate.now())
                .status("DRAFT")
                .lines(lines)
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netIncome(netIncome)
                .generatedAt(java.time.LocalDateTime.now())
                .build();

        // Persist if requested
        if (persist) {
            try {
                String reportContent = writeReportContent(response);
                FinancialReport report = FinancialReport.builder()
                        .organization(organizationRepository.findById(organizationId).get())
                        .reportType(FinancialReport.ReportType.INCOME_STATEMENT)
                        .reportName(response.getReportName())
                        .reportDate(LocalDate.now())
                        .fromDate(fromDate)
                        .toDate(toDate)
                        .status(FinancialReport.ReportStatus.DRAFT)
                        .reportContent(reportContent)
                        .generatedBy(generatedBy)
                        .build();

                FinancialReport saved = financialReportRepository.save(report);
                response.setId(saved.getId());

                // Create audit log
                try {
                    auditLogService.log(organizationId, "FinancialReport", saved.getId(), "CREATE",
                            null, response.getReportName(),
                            "Income Statement generated for " + fromDate + " to " + toDate);
                } catch (Exception ex) {
                    // Log audit failures but don't block report generation
                }

                // Create security event
                try {
                    securityEventService.logEvent(organizationId, "REPORT_GENERATION", "LOW",
                            "Income Statement Generated",
                            "Income Statement report generated for period " + fromDate + " to " + toDate,
                            null, null);
                } catch (Exception ex) {
                    // Log security event failures but don't block report generation
                }
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to serialize and persist report: " + ex.getMessage(), ex);
            }
        }

        return response;
    }

    /**
     * Generate a Balance Sheet from posted GL entries.
     */
    public FinancialReportSummaryResponse generateBalanceSheet(Long organizationId, LocalDate fromDate, LocalDate toDate, String generatedBy, boolean persist) {
        // Validate organization
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        // Validate dates
        if (fromDate == null || toDate == null || fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid date range is required");
        }

        // Check module controls if available
        if (moduleControlService != null) {
            try {
                moduleControlService.requireModuleEnabled(organizationId, com.justjava.ams.financeAdmin.entity.ModuleControl.ModuleType.REPORTING);
            } catch (ResponseStatusException ex) {
                throw ex;
            }
        }

        // Fetch posted GL entries up to toDate
        List<GeneralLedger> postedEntries = generalLedgerRepository.findPostedEntriesByOrganizationAsOf(organizationId, toDate);

        // Calculate balance sheet components
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;
        List<FinancialReportLineDTO> lines = new ArrayList<>();

        // Group by account
        Map<Long, List<GeneralLedger>> groupedByAccount = postedEntries.stream()
                .collect(Collectors.groupingBy(gl -> gl.getAccount().getId()));

        for (List<GeneralLedger> glEntries : groupedByAccount.values()) {
            ChartOfAccounts account = glEntries.get(0).getAccount();

            if (account.getAccountType() == ChartOfAccounts.AccountType.ASSET) {
                // Assets: debits - credits
                BigDecimal debits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.DEBIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal credits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.CREDIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal assetAmount = debits.subtract(credits);
                if (assetAmount.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(FinancialReportLineDTO.builder()
                            .section("ASSET")
                            .accountId(account.getId())
                            .accountCode(account.getAccountCode())
                            .accountName(account.getAccountName())
                            .amount(assetAmount)
                            .build());
                    totalAssets = totalAssets.add(assetAmount);
                }
            } else if (account.getAccountType() == ChartOfAccounts.AccountType.LIABILITY) {
                // Liabilities: credits - debits
                BigDecimal credits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.CREDIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal debits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.DEBIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal liabilityAmount = credits.subtract(debits);
                if (liabilityAmount.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(FinancialReportLineDTO.builder()
                            .section("LIABILITY")
                            .accountId(account.getId())
                            .accountCode(account.getAccountCode())
                            .accountName(account.getAccountName())
                            .amount(liabilityAmount)
                            .build());
                    totalLiabilities = totalLiabilities.add(liabilityAmount);
                }
            } else if (account.getAccountType() == ChartOfAccounts.AccountType.EQUITY) {
                // Equity: credits - debits
                BigDecimal credits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.CREDIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal debits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.DEBIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal equityAmount = credits.subtract(debits);
                if (equityAmount.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(FinancialReportLineDTO.builder()
                            .section("EQUITY")
                            .accountId(account.getId())
                            .accountCode(account.getAccountCode())
                            .accountName(account.getAccountName())
                            .amount(equityAmount)
                            .build());
                    totalEquity = totalEquity.add(equityAmount);
                }
            }
        }

        // Calculate net income for the selected period
        List<GeneralLedger> periodEntries = generalLedgerRepository.findPostedEntriesByOrganizationAndDateRange(organizationId, fromDate, toDate);
        BigDecimal periodNetIncome = calculatePeriodNetIncome(periodEntries);

        // Calculate variance: Assets - (Liabilities + Equity + NetIncome)
        BigDecimal balanceSheetVariance = totalAssets.subtract(totalLiabilities).subtract(totalEquity).subtract(periodNetIncome);

        // Build the response
        FinancialReportSummaryResponse response = FinancialReportSummaryResponse.builder()
                .organizationId(organizationId)
                .reportType("BALANCE_SHEET")
                .reportName("Balance Sheet as of " + toDate)
                .fromDate(fromDate)
                .toDate(toDate)
                .reportDate(LocalDate.now())
                .status("DRAFT")
                .lines(lines)
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .totalEquity(totalEquity)
                .netIncome(periodNetIncome)
                .balanceSheetVariance(balanceSheetVariance)
                .generatedAt(java.time.LocalDateTime.now())
                .build();

        // Persist if requested
        if (persist) {
            try {
                String reportContent = writeReportContent(response);
                FinancialReport report = FinancialReport.builder()
                        .organization(organizationRepository.findById(organizationId).get())
                        .reportType(FinancialReport.ReportType.BALANCE_SHEET)
                        .reportName(response.getReportName())
                        .reportDate(LocalDate.now())
                        .fromDate(fromDate)
                        .toDate(toDate)
                        .status(FinancialReport.ReportStatus.DRAFT)
                        .reportContent(reportContent)
                        .generatedBy(generatedBy)
                        .build();

                FinancialReport saved = financialReportRepository.save(report);
                response.setId(saved.getId());

                // Create audit log
                try {
                    auditLogService.log(organizationId, "FinancialReport", saved.getId(), "CREATE",
                            null, response.getReportName(),
                            "Balance Sheet generated as of " + toDate);
                } catch (Exception ex) {
                    // Log audit failures but don't block report generation
                }

                // Create security event
                try {
                    securityEventService.logEvent(organizationId, "REPORT_GENERATION", "LOW",
                            "Balance Sheet Generated",
                            "Balance Sheet report generated as of " + toDate,
                            null, null);
                } catch (Exception ex) {
                    // Log security event failures but don't block report generation
                }
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to serialize and persist report: " + ex.getMessage(), ex);
            }
        }

        return response;
    }

    /**
     * Get a report summary by report ID.
     */
    public FinancialReportSummaryResponse getReportSummary(Long reportId) {
        FinancialReport report = financialReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        try {
            JsonNode content = objectMapper.readTree(report.getReportContent());
            List<FinancialReportLineDTO> lines = new ArrayList<>();
            JsonNode lineNodes = content.get("lines");
            if (lineNodes != null && lineNodes.isArray()) {
                for (JsonNode line : lineNodes) {
                    lines.add(FinancialReportLineDTO.builder()
                            .section(text(line, "section"))
                            .accountId(line.hasNonNull("accountId") ? line.get("accountId").asLong() : null)
                            .accountCode(text(line, "accountCode"))
                            .accountName(text(line, "accountName"))
                            .amount(decimal(line, "amount"))
                            .build());
                }
            }
            return FinancialReportSummaryResponse.builder()
                    .id(report.getId())
                    .organizationId(report.getOrganization().getId())
                    .reportType(report.getReportType().name())
                    .reportName(report.getReportName())
                    .fromDate(report.getFromDate())
                    .toDate(report.getToDate())
                    .reportDate(report.getReportDate())
                    .status(report.getStatus() != null ? report.getStatus().name() : "DRAFT")
                    .lines(lines)
                    .totalRevenue(decimal(content, "totalRevenue"))
                    .totalExpenses(decimal(content, "totalExpenses"))
                    .netIncome(decimal(content, "netIncome"))
                    .totalAssets(decimal(content, "totalAssets"))
                    .totalLiabilities(decimal(content, "totalLiabilities"))
                    .totalEquity(decimal(content, "totalEquity"))
                    .balanceSheetVariance(decimal(content, "balanceSheetVariance"))
                    .reportContent(report.getReportContent())
                    .generatedAt(report.getCreatedAt())
                    .build();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to deserialize report content");
        }
    }

    /**
     * Get reports for an organization with optional filtering.
     */
    public List<FinancialReportDTO> getReports(Long organizationId, String reportType, LocalDate fromDate, LocalDate toDate) {
        // Validate organization
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        List<FinancialReport> reports = financialReportRepository.findByOrganizationIdOrderByReportDateDesc(organizationId);

        // Filter by type if provided
        if (reportType != null && !reportType.trim().isEmpty()) {
            try {
                FinancialReport.ReportType rt = FinancialReport.ReportType.valueOf(reportType.toUpperCase());
                reports = reports.stream()
                        .filter(r -> r.getReportType() == rt)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid report type");
            }
        }

        // Filter by date range if provided
        if (fromDate != null || toDate != null) {
            if ((fromDate == null) != (toDate == null)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both from date and to date are required");
            }
            if (fromDate != null && fromDate.isAfter(toDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From date must not be after to date");
            }

            final LocalDate finalFromDate = fromDate;
            final LocalDate finalToDate = toDate;
            reports = reports.stream()
                    .filter(r -> !r.getReportDate().isBefore(finalFromDate) && !r.getReportDate().isAfter(finalToDate))
                    .collect(Collectors.toList());
        }

        return reports.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to calculate net income for a period.
     */
    private BigDecimal calculatePeriodNetIncome(List<GeneralLedger> periodEntries) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        Map<Long, List<GeneralLedger>> groupedByAccount = periodEntries.stream()
                .collect(Collectors.groupingBy(gl -> gl.getAccount().getId()));

        for (List<GeneralLedger> glEntries : groupedByAccount.values()) {
            ChartOfAccounts account = glEntries.get(0).getAccount();

            if (account.getAccountType() == ChartOfAccounts.AccountType.REVENUE) {
                BigDecimal credits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.CREDIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal debits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.DEBIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                totalRevenue = totalRevenue.add(credits.subtract(debits));
            } else if (account.getAccountType() == ChartOfAccounts.AccountType.EXPENSE) {
                BigDecimal debits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.DEBIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal credits = glEntries.stream()
                        .filter(gl -> gl.getDebitCredit() == GeneralLedger.DebitCredit.CREDIT)
                        .map(GeneralLedger::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                totalExpenses = totalExpenses.add(debits.subtract(credits));
            }
        }

        return totalRevenue.subtract(totalExpenses);
    }

    private String writeReportContent(FinancialReportSummaryResponse response) throws com.fasterxml.jackson.core.JsonProcessingException {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("id", response.getId());
        content.put("organizationId", response.getOrganizationId());
        content.put("reportType", response.getReportType());
        content.put("reportName", response.getReportName());
        content.put("fromDate", response.getFromDate() != null ? response.getFromDate().toString() : null);
        content.put("toDate", response.getToDate() != null ? response.getToDate().toString() : null);
        content.put("reportDate", response.getReportDate() != null ? response.getReportDate().toString() : null);
        content.put("status", response.getStatus());
        content.put("lines", response.getLines());
        content.put("totalRevenue", response.getTotalRevenue());
        content.put("totalExpenses", response.getTotalExpenses());
        content.put("netIncome", response.getNetIncome());
        content.put("totalAssets", response.getTotalAssets());
        content.put("totalLiabilities", response.getTotalLiabilities());
        content.put("totalEquity", response.getTotalEquity());
        content.put("balanceSheetVariance", response.getBalanceSheetVariance());
        content.put("generatedAt", response.getGeneratedAt() != null ? response.getGeneratedAt().toString() : null);
        return objectMapper.writeValueAsString(content);
    }

    private BigDecimal decimal(JsonNode node, String fieldName) {
        return node != null && node.hasNonNull(fieldName) ? node.get(fieldName).decimalValue() : BigDecimal.ZERO;
    }

    private String text(JsonNode node, String fieldName) {
        return node != null && node.hasNonNull(fieldName) ? node.get(fieldName).asText() : null;
    }

    private FinancialReportDTO mapToDTO(FinancialReport report) {
        return FinancialReportDTO.builder()
                .id(report.getId())
                .organizationId(report.getOrganization().getId())
                .reportType(report.getReportType().toString())
                .reportName(report.getReportName())
                .reportDate(report.getReportDate())
                .fromDate(report.getFromDate())
                .toDate(report.getToDate())
                .status(report.getStatus() != null ? report.getStatus().toString() : "DRAFT")
                .reportContent(report.getReportContent())
                .generatedBy(report.getGeneratedBy())
                .approvedDate(report.getApprovedDate())
                .approvedBy(report.getApprovedBy())
                .notes(report.getNotes())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
