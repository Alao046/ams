package com.justjava.ams.accountant;

import com.justjava.ams.accountant.dto.GeneralLedgerDTO;
import com.justjava.ams.accountant.dto.ChartOfAccountsDTO;
import com.justjava.ams.accountant.dto.JournalLineCreateRequest;
import com.justjava.ams.accountant.dto.JournalLineDTO;
import com.justjava.ams.accountant.dto.JournalLineUpdateRequest;
import com.justjava.ams.accountant.dto.JournalPostRequest;
import com.justjava.ams.accountant.dto.JournalSubmitRequest;
import com.justjava.ams.accountant.dto.ManualJournalCreateRequest;
import com.justjava.ams.accountant.dto.ManualJournalDTO;
import com.justjava.ams.accountant.service.GeneralLedgerService;
import com.justjava.ams.accountant.service.ChartOfAccountsService;
import com.justjava.ams.accountant.service.ManualJournalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/accountant")
@RequiredArgsConstructor
public class AccountantApiController {
    private final ManualJournalService manualJournalService;
    private final GeneralLedgerService generalLedgerService;
    private final ChartOfAccountsService chartOfAccountsService;

    @GetMapping("/manual-journals/org/{organizationId}")
    public List<ManualJournalDTO> getManualJournals(@PathVariable Long organizationId) {
        return manualJournalService.getJournalsByOrganization(organizationId);
    }

    @PostMapping("/manual-journals/org/{organizationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ManualJournalDTO createManualJournal(
            @PathVariable Long organizationId,
            @Valid @RequestBody ManualJournalCreateRequest request,
            Principal principal) {
        return manualJournalService.createManualJournal(organizationId, toDTO(request), getUserName(principal));
    }

    @GetMapping("/manual-journals/{journalId}")
    public ManualJournalDTO getManualJournal(@PathVariable Long journalId) {
        return manualJournalService.getJournal(journalId);
    }

    @PostMapping("/manual-journals/{journalId}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    public JournalLineDTO addJournalLine(
            @PathVariable Long journalId,
            @Valid @RequestBody JournalLineCreateRequest request) {
        return manualJournalService.addJournalLine(journalId, toDTO(request));
    }

    @PutMapping("/manual-journals/lines/{lineId}")
    public JournalLineDTO updateJournalLine(
            @PathVariable Long lineId,
            @Valid @RequestBody JournalLineUpdateRequest request) {
        return manualJournalService.updateJournalLine(lineId, toDTO(request));
    }

    @DeleteMapping("/manual-journals/lines/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJournalLine(@PathVariable Long lineId) {
        manualJournalService.deleteJournalLine(lineId);
    }

    @PatchMapping("/manual-journals/{journalId}/submit")
    public ManualJournalDTO submitManualJournal(
            @PathVariable Long journalId,
            @Valid @RequestBody(required = false) JournalSubmitRequest request,
            Principal principal) {
        JournalSubmitRequest submitRequest = request != null ? request : new JournalSubmitRequest();
        if (submitRequest.getSubmittedBy() == null || submitRequest.getSubmittedBy().trim().isEmpty()) {
            submitRequest.setSubmittedBy(getUserName(principal));
        }
        return manualJournalService.submitJournal(journalId, submitRequest);
    }

    @PatchMapping("/manual-journals/{journalId}/post")
    public ManualJournalDTO postManualJournal(
            @PathVariable Long journalId,
            @Valid @RequestBody(required = false) JournalPostRequest request,
            Principal principal) {
        String postedBy = principal != null ? principal.getName() : null;
        if ((postedBy == null || postedBy.trim().isEmpty())
                && request != null
                && request.getPostedBy() != null
                && !request.getPostedBy().trim().isEmpty()) {
            postedBy = request.getPostedBy();
        }
        return manualJournalService.postJournal(journalId, postedBy);
    }

    @GetMapping("/general-ledger/journal/{journalId}")
    public List<GeneralLedgerDTO> getGeneralLedgerEntriesForJournal(@PathVariable Long journalId) {
        return generalLedgerService.getEntriesByJournalId(journalId);
    }

    @GetMapping("/chart-of-accounts/org/{organizationId}")
    public List<ChartOfAccountsDTO> getChartOfAccounts(@PathVariable Long organizationId) {
        return chartOfAccountsService.getAccountsByOrganization(organizationId);
    }

    private ManualJournalDTO toDTO(ManualJournalCreateRequest request) {
        return ManualJournalDTO.builder()
                .branchId(request.getBranchId())
                .description(request.getDescription())
                .journalDate(request.getJournalDate())
                .build();
    }

    private JournalLineDTO toDTO(JournalLineCreateRequest request) {
        return JournalLineDTO.builder()
                .chartOfAccountId(request.getChartOfAccountId())
                .debitAmount(request.getDebitAmount())
                .creditAmount(request.getCreditAmount())
                .departmentCode(request.getDepartmentCode())
                .projectCode(request.getProjectCode())
                .branchCode(request.getBranchCode())
                .narration(request.getNarration())
                .lineSequence(request.getLineSequence())
                .build();
    }

    private JournalLineDTO toDTO(JournalLineUpdateRequest request) {
        return JournalLineDTO.builder()
                .chartOfAccountId(request.getChartOfAccountId())
                .debitAmount(request.getDebitAmount())
                .creditAmount(request.getCreditAmount())
                .departmentCode(request.getDepartmentCode())
                .projectCode(request.getProjectCode())
                .branchCode(request.getBranchCode())
                .narration(request.getNarration())
                .lineSequence(request.getLineSequence())
                .build();
    }

    private String getUserName(Principal principal) {
        return principal != null ? principal.getName() : "system";
    }
}
