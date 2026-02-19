package com.cbs.transaction.controller;

import com.cbs.common.exception.ApiException;
import com.cbs.transaction.dto.TransactionResponse;
import com.cbs.transaction.exception.TransactionExceptionHandler;
import com.cbs.transaction.model.TransactionStatus;
import com.cbs.transaction.model.TransactionType;
import com.cbs.transaction.service.TransactionService;
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

@WebMvcTest(TransactionController.class)
@Import(TransactionExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void createTransaction_returnsSuccessResponse() throws Exception {
        TransactionResponse response = new TransactionResponse(
                1L,
                10L,
                20L,
                30L,
                TransactionType.TRANSFER,
                TransactionStatus.POSTED,
                BigDecimal.valueOf(100.00),
                "TRY",
                "Payment",
                "REF-100",
                LocalDate.of(2026, 2, 18),
                null,
                null
        );
        when(transactionService.createTransaction(any())).thenReturn(response);

        String body = """
                {
                  "customerId": 10,
                  "accountId": 20,
                  "counterpartyAccountId": 30,
                  "type": "TRANSFER",
                  "amount": 100.00,
                  "currency": "TRY",
                  "description": "Payment",
                  "reference": "REF-100",
                  "valueDate": "2026-02-18"
                }
                """;

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transaction created"))
                .andExpect(jsonPath("$.data.reference").value("REF-100"));
    }

    @Test
    void createTransaction_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
        String body = """
                {
                  "customerId": 10,
                  "type": "TRANSFER",
                  "amount": 100.00,
                  "currency": "TRY",
                  "description": "Payment",
                  "reference": "REF-100"
                }
                """;

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void reverseTransaction_returnsBusinessErrorWhenAlreadyReversed() throws Exception {
        when(transactionService.reverseTransaction(any(), any()))
                .thenThrow(new ApiException("TRANSACTION_ALREADY_REVERSED", "Transaction is already reversed"));

        String body = objectMapper.writeValueAsString(new ReversalPayload("duplicate"));

        mockMvc.perform(patch("/api/v1/transactions/{transactionId}/reverse", 101)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.errorCode").value("TRANSACTION_ALREADY_REVERSED"))
                .andExpect(jsonPath("$.message").value("Transaction is already reversed"));
    }

            @Test
            void retryPosting_returnsSuccessResponse() throws Exception {
                TransactionResponse response = new TransactionResponse(
                        2L,
                        10L,
                        20L,
                        30L,
                        TransactionType.TRANSFER,
                        TransactionStatus.POSTED,
                        BigDecimal.valueOf(100.00),
                        "TRY",
                        "Payment",
                        "REF-200",
                        LocalDate.of(2026, 2, 18),
                        null,
                        null
                );
                when(transactionService.retryPosting(2L)).thenReturn(response);

                mockMvc.perform(patch("/api/v1/transactions/{transactionId}/retry-posting", 2))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.message").value("Transaction posting retried"))
                        .andExpect(jsonPath("$.data.reference").value("REF-200"));
            }

    private record ReversalPayload(String reason) {
    }
}
