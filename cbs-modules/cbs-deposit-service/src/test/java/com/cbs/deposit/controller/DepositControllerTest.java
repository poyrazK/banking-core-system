package com.cbs.deposit.controller;

import com.cbs.common.exception.ApiException;
import com.cbs.deposit.dto.DepositResponse;
import com.cbs.deposit.exception.DepositExceptionHandler;
import com.cbs.deposit.model.DepositProductType;
import com.cbs.deposit.model.DepositStatus;
import com.cbs.deposit.service.DepositService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepositController.class)
@Import(DepositExceptionHandler.class)
class DepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepositService depositService;

    @Test
    void createDeposit_returnsSuccessResponse() throws Exception {
        DepositResponse response = new DepositResponse(
                1L,
                10L,
                20L,
                "DEP-100",
                DepositProductType.TERM,
                DepositStatus.OPEN,
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(18.0),
                90,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 5, 2),
                null
        );
        when(depositService.createDeposit(any())).thenReturn(response);

        String body = """
                {
                  "customerId": 10,
                  "settlementAccountId": 20,
                  "depositNumber": "DEP-100",
                  "productType": "TERM",
                  "principalAmount": 20000.00,
                  "annualInterestRate": 18.00,
                  "termDays": 90,
                  "openingDate": "2026-02-01",
                  "maturityDate": "2026-05-02"
                }
                """;

        mockMvc.perform(post("/api/v1/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deposit account created"))
                .andExpect(jsonPath("$.data.depositNumber").value("DEP-100"));
    }

    @Test
    void createDeposit_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
        String body = """
                {
                  "customerId": 10,
                  "productType": "TERM",
                  "principalAmount": 20000.00
                }
                """;

        mockMvc.perform(post("/api/v1/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void closeDeposit_returnsBusinessErrorWhenNotMatured() throws Exception {
        when(depositService.closeDeposit(any()))
                .thenThrow(new ApiException("DEPOSIT_NOT_MATURED", "Only matured deposit can be closed"));

        mockMvc.perform(patch("/api/v1/deposits/{depositId}/close", 99))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only matured deposit can be closed"));
    }

    @Test
    void breakDeposit_returnsBusinessErrorWhenAlreadyClosed() throws Exception {
        when(depositService.breakDeposit(any(), any()))
                .thenThrow(new ApiException("DEPOSIT_ALREADY_CLOSED", "Closed deposit cannot be broken"));

        String body = objectMapper.writeValueAsString(new ReasonPayload("urgent"));

        mockMvc.perform(patch("/api/v1/deposits/{depositId}/break", 100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Closed deposit cannot be broken"));
    }

    private record ReasonPayload(String reason) {
    }
}
