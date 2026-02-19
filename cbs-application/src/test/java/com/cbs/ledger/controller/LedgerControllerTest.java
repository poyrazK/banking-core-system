package com.cbs.ledger.controller;

import com.cbs.ledger.dto.AccountResponse;
import com.cbs.ledger.dto.PostJournalEntryResponse;
import com.cbs.ledger.dto.ReconciliationResponse;
import com.cbs.ledger.exception.LedgerExceptionHandler;
import com.cbs.ledger.model.AccountType;
import com.cbs.ledger.service.LedgerAccountService;
import com.cbs.ledger.service.LedgerPostingService;
import com.cbs.ledger.service.LedgerQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(LedgerController.class)
@Import(LedgerExceptionHandler.class)
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LedgerAccountService ledgerAccountService;

    @MockBean
    private LedgerPostingService ledgerPostingService;

    @MockBean
    private LedgerQueryService ledgerQueryService;

    @Test
    void createAccount_returnsSuccessResponse() throws Exception {
        when(ledgerAccountService.createAccount(any())).thenReturn(new AccountResponse(
                1L,
                "1000",
                "Cash",
                AccountType.ASSET,
                true
        ));

        String body = """
                {
                  "code": "1000",
                  "name": "Cash",
                  "type": "ASSET"
                }
                """;

        mockMvc.perform(post("/api/v1/ledger/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account created"))
                .andExpect(jsonPath("$.data.code").value("1000"));
    }

    @Test
    void createAccount_returnsBadRequestWhenPayloadInvalid() throws Exception {
        String body = """
                {
                  "code": "",
                  "name": "",
                  "type": null
                }
                """;

        mockMvc.perform(post("/api/v1/ledger/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void postPolicyEntry_returnsSuccessResponse() throws Exception {
        when(ledgerPostingService.postPolicyEntry(any())).thenReturn(new PostJournalEntryResponse(
                1L,
                "PAY-100",
                new BigDecimal("200.0000"),
                new BigDecimal("200.0000")
        ));

        String body = """
                {
                  "reference": "PAY-100",
                  "description": "Bill payment",
                  "valueDate": "2026-02-18",
                  "operationType": "PAYMENT",
                  "amount": 200.00,
                  "accountCode": "1000"
                }
                """;

        mockMvc.perform(post("/api/v1/ledger/entries/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Policy journal entry posted"))
                .andExpect(jsonPath("$.data.reference").value("PAY-100"));
    }

    @Test
    void reconcile_returnsSummary() throws Exception {
        when(ledgerQueryService.reconcile(any(), any())).thenReturn(new ReconciliationResponse(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28),
                new BigDecimal("500.0000"),
                new BigDecimal("500.0000"),
                true,
                7L
        ));

        mockMvc.perform(get("/api/v1/ledger/reconciliation")
                        .param("fromDate", "2026-02-01")
                        .param("toDate", "2026-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balanced").value(true))
                .andExpect(jsonPath("$.data.entryCount").value(7));
    }
}