package com.cbs.account.controller;

import com.cbs.account.dto.AccountResponse;
import com.cbs.account.exception.AccountExceptionHandler;
import com.cbs.account.model.AccountStatus;
import com.cbs.account.model.AccountType;
import com.cbs.account.model.Currency;
import com.cbs.account.service.AccountService;
import com.cbs.common.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AccountController.class)
@Import(AccountExceptionHandler.class)
class AccountControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AccountService accountService;

        @Test
        void createAccount_returnsSuccessResponse() throws Exception {
                AccountResponse response = new AccountResponse(
                                1L,
                                10L,
                                "TR001",
                                AccountType.SAVINGS,
                                Currency.TRY,
                                AccountStatus.ACTIVE,
                                BigDecimal.valueOf(100));
                when(accountService.createAccount(any())).thenReturn(response);

                String body = """
                                {
                                  "customerId": 10,
                                  "accountNumber": "TR001",
                                  "accountType": "SAVINGS",
                                  "currency": "TRY",
                                  "initialBalance": 100.00
                                }
                                """;

                mockMvc.perform(post("/api/v1/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Account created"))
                                .andExpect(jsonPath("$.data.accountNumber").value("TR001"));
        }

        @Test
        void createAccount_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
                String body = """
                                {
                                  "accountNumber": "",
                                  "accountType": "SAVINGS"
                                }
                                """;

                mockMvc.perform(post("/api/v1/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void debitBalance_returnsBusinessErrorWhenInsufficient() throws Exception {
                when(accountService.debitBalance(any(), any()))
                                .thenThrow(new ApiException("INSUFFICIENT_BALANCE", "Insufficient balance"));

                String body = objectMapper.writeValueAsString(new AmountPayload(BigDecimal.valueOf(100)));

                mockMvc.perform(patch("/api/v1/accounts/{accountId}/debit", 99)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Insufficient balance"));
        }

        private record AmountPayload(BigDecimal amount) {
        }
}
