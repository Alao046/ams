package com.justjava.ams.mvp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MvpBackendAcceptanceCriteriaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fifteenHourMvpBackendFlowWorksThroughApis() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        LocalDate journalDate = LocalDate.now();

        long organizationId = postJson(
                "/api/organizations",
                Map.of(
                        "name", "MVP Acceptance Org " + suffix,
                        "registrationNumber", "MVP-REG-" + suffix,
                        "taxId", "MVP-TAX-" + suffix,
                        "active", true),
                financeAdmin(),
                status().isCreated())
                .get("id").asLong();

        long branchId = postJson(
                "/api/branches",
                Map.of(
                        "organizationId", organizationId,
                        "name", "Main Branch " + suffix,
                        "code", "MB-" + suffix,
                        "active", true),
                financeAdmin(),
                status().isCreated())
                .get("id").asLong();

        long debitAccountId = postJson(
                "/api/financeAdmin/chartOfAccounts/org/" + organizationId,
                Map.of(
                        "accountCode", "101-" + suffix,
                        "accountName", "Cash " + suffix,
                        "accountType", "ASSET",
                        "accountSubtype", "CURRENT_ASSET",
                        "normalBalance", "DEBIT"),
                financeAdmin(),
                status().isCreated())
                .get("id").asLong();

        long creditAccountId = postJson(
                "/api/financeAdmin/chartOfAccounts/org/" + organizationId,
                Map.of(
                        "accountCode", "401-" + suffix,
                        "accountName", "Revenue " + suffix,
                        "accountType", "REVENUE",
                        "accountSubtype", "REVENUE",
                        "normalBalance", "CREDIT"),
                financeAdmin(),
                status().isCreated())
                .get("id").asLong();

        postJson(
                "/api/financeAdmin/fiscalPeriods/org/" + organizationId,
                Map.of(
                        "year", journalDate.getYear(),
                        "quarter", quarterFor(journalDate),
                        "startDate", journalDate.minusDays(1).toString(),
                        "endDate", journalDate.plusDays(1).toString()),
                financeAdmin(),
                status().isCreated());

        long journalId = postJson(
                "/api/accountant/manual-journals/org/" + organizationId,
                Map.of(
                        "branchId", branchId,
                        "description", "MVP acceptance journal " + suffix,
                        "journalDate", journalDate.toString()),
                accountant(),
                status().isCreated())
                .get("id").asLong();

        postJson(
                "/api/accountant/manual-journals/" + journalId + "/lines",
                Map.of(
                        "chartOfAccountId", debitAccountId,
                        "debitAmount", new BigDecimal("100.00"),
                        "creditAmount", BigDecimal.ZERO,
                        "narration", "Debit line",
                        "lineSequence", 1),
                accountant(),
                status().isCreated());

        postJson(
                "/api/accountant/manual-journals/" + journalId + "/lines",
                Map.of(
                        "chartOfAccountId", creditAccountId,
                        "debitAmount", BigDecimal.ZERO,
                        "creditAmount", new BigDecimal("100.00"),
                        "narration", "Credit line",
                        "lineSequence", 2),
                accountant(),
                status().isCreated());

        JsonNode submittedJournal = patchJson(
                "/api/accountant/manual-journals/" + journalId + "/submit",
                Map.of("submittedBy", "acceptance-accountant"),
                accountant(),
                status().isOk());
        assertThat(submittedJournal.get("status").asText()).isEqualTo("SUBMITTED");

        JsonNode cfoOrganizations = getJson(
                "/api/organizations",
                cfo(),
                status().isOk());
        assertThat(arrayContains(cfoOrganizations, organization ->
                organization.get("id").asLong() == organizationId)).isTrue();

        JsonNode pendingJournals = getJson(
                "/api/cfo/manual-journals/org/" + organizationId + "/pending",
                cfo(),
                status().isOk());
        assertThat(arrayContains(pendingJournals, journal ->
                journal.get("journalId").asLong() == journalId)).isTrue();

        JsonNode cfoJournalDetail = getJson(
                "/api/cfo/manual-journals/" + journalId,
                cfo(),
                status().isOk());
        assertThat(cfoJournalDetail.get("journalLines").size()).isEqualTo(2);

        JsonNode approvedJournal = patchJson(
                "/api/cfo/manual-journals/" + journalId + "/approve",
                Map.of("approvedBy", "acceptance-cfo", "approvalNote", "Approved for MVP acceptance"),
                cfo(),
                status().isOk());
        assertThat(approvedJournal.get("status").asText()).isEqualTo("APPROVED");

        JsonNode postedJournal = patchJson(
                "/api/accountant/manual-journals/" + journalId + "/post",
                Map.of("postedBy", "acceptance-accountant"),
                accountant(),
                status().isOk());
        assertThat(postedJournal.get("status").asText()).isEqualTo("POSTED");

        JsonNode glRows = getJson(
                "/api/accountant/general-ledger/journal/" + journalId,
                accountant(),
                status().isOk());
        assertThat(glRows.size()).isEqualTo(2);
        assertThat(arrayContains(glRows, row -> "DEBIT".equals(row.get("debitCredit").asText()))).isTrue();
        assertThat(arrayContains(glRows, row -> "CREDIT".equals(row.get("debitCredit").asText()))).isTrue();

        JsonNode auditLogs = getJson(
                "/api/auditor/audit-logs/org/" + organizationId,
                auditor(),
                status().isOk());

        JsonNode auditorOrganizations = getJson(
                "/api/organizations",
                auditor(),
                status().isOk());
        assertThat(arrayContains(auditorOrganizations, organization ->
                organization.get("id").asLong() == organizationId)).isTrue();

        JsonNode approvalAuditLogs = getJson(
                "/api/auditor/audit-logs/org/" + organizationId + "?entityType=ManualJournal&action=APPROVE",
                auditor(),
                status().isOk());
        assertThat(arrayContains(approvalAuditLogs, log ->
                "ManualJournal".equals(log.get("entityType").asText())
                        && "APPROVE".equals(log.get("action").asText()))).isTrue();

        assertAuditEvent(auditLogs, "Organization", "CREATE");
        assertAuditEvent(auditLogs, "Branch", "CREATE");
        assertAuditEvent(auditLogs, "ChartOfAccounts", "CREATE");
        assertAuditEvent(auditLogs, "FiscalPeriod", "CREATE");
        assertAuditEvent(auditLogs, "ManualJournal", "CREATE");
        assertAuditEvent(auditLogs, "ManualJournal", "SUBMIT");
        assertAuditEvent(auditLogs, "ManualJournal", "APPROVE");
        assertAuditEvent(auditLogs, "ManualJournal", "POST");
        assertAuditEvent(auditLogs, "GeneralLedger", "POST");
    }

    private JsonNode postJson(String path, Object body, RequestPostProcessor user, ResultMatcher statusMatcher) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post(path)
                .with(user)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(statusMatcher)
                .andReturn()
                .getResponse()
                .getContentAsString());
    }

    private JsonNode patchJson(String path, Object body, RequestPostProcessor user, ResultMatcher statusMatcher) throws Exception {
        return objectMapper.readTree(mockMvc.perform(patch(path)
                .with(user)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(statusMatcher)
                .andReturn()
                .getResponse()
                .getContentAsString());
    }

    private JsonNode getJson(String path, RequestPostProcessor user, ResultMatcher statusMatcher) throws Exception {
        return objectMapper.readTree(mockMvc.perform(get(path)
                .with(user)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(statusMatcher)
                .andReturn()
                .getResponse()
                .getContentAsString());
    }

    private RequestPostProcessor financeAdmin() {
        return oidcUser("/financeAdmin", "acceptance-finance-admin");
    }

    private RequestPostProcessor accountant() {
        return oidcUser("/accountant", "acceptance-accountant");
    }

    private RequestPostProcessor cfo() {
        return oidcUser("/cfo", "acceptance-cfo");
    }

    private RequestPostProcessor auditor() {
        return oidcUser("/auditor", "acceptance-auditor");
    }

    private RequestPostProcessor oidcUser(String group, String username) {
        return oidcLogin()
                .idToken(token -> token
                        .subject(username)
                        .claim("groups", List.of(group))
                        .claim("preferred_username", username));
    }

    private int quarterFor(LocalDate date) {
        return ((date.getMonthValue() - 1) / 3) + 1;
    }

    private void assertAuditEvent(JsonNode auditLogs, String entityType, String action) {
        assertThat(arrayContains(auditLogs, log ->
                entityType.equals(log.get("entityType").asText())
                        && action.equals(log.get("action").asText()))).isTrue();
    }

    private boolean arrayContains(JsonNode array, java.util.function.Predicate<JsonNode> predicate) {
        return StreamSupport.stream(array.spliterator(), false).anyMatch(predicate);
    }
}
