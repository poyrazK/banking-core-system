package com.cbs.payment.controller;

import com.cbs.common.exception.ApiException;
import com.cbs.payment.dto.PaymentResponse;
import com.cbs.payment.exception.PaymentExceptionHandler;
import com.cbs.payment.model.PaymentMethod;
import com.cbs.payment.model.PaymentStatus;
import com.cbs.payment.service.PaymentService;
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

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PaymentController.class)
@Import(PaymentExceptionHandler.class)
class PaymentControllerTest {
    @MockBean
    private com.cbs.auth.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void createPayment_returnsSuccessResponse() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L,
                10L,
                20L,
                30L,
                BigDecimal.valueOf(200.00),
                "TRY",
                PaymentMethod.BANK_TRANSFER,
                PaymentStatus.INITIATED,
                "PAY-100",
                "Invoice",
                LocalDate.of(2026, 2, 18),
                null
        );
        when(paymentService.createPayment(any())).thenReturn(response);

        String body = """
                {
                  "customerId": 10,
                  "sourceAccountId": 20,
                  "destinationAccountId": 30,
                  "amount": 200.00,
                  "currency": "TRY",
                  "method": "BANK_TRANSFER",
                  "reference": "PAY-100",
                  "description": "Invoice",
                  "valueDate": "2026-02-18"
                }
                """;

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment created"))
                .andExpect(jsonPath("$.data.reference").value("PAY-100"));
    }

    @Test
    void createPayment_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
        String body = """
                {
                  "customerId": 10,
                  "amount": 200.00,
                  "currency": "TRY",
                  "reference": "PAY-100",
                  "description": "Invoice"
                }
                """;

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void cancelPayment_returnsBusinessErrorWhenAlreadyCompleted() throws Exception {
        when(paymentService.cancelPayment(any(), any()))
                .thenThrow(new ApiException("PAYMENT_ALREADY_COMPLETED", "Completed payment cannot be changed"));

        String body = objectMapper.writeValueAsString(new StatusPayload("not needed"));

        mockMvc.perform(patch("/api/v1/payments/{paymentId}/cancel", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.errorCode").value("PAYMENT_ALREADY_COMPLETED"))
                .andExpect(jsonPath("$.message").value("Completed payment cannot be changed"));
    }

    @Test
    void retryPosting_returnsSuccessResponse() throws Exception {
        PaymentResponse response = new PaymentResponse(
                5L,
                10L,
                20L,
                30L,
                BigDecimal.valueOf(200.00),
                "TRY",
                PaymentMethod.BANK_TRANSFER,
                PaymentStatus.COMPLETED,
                "PAY-200",
                "Invoice",
                LocalDate.of(2026, 2, 18),
                null
        );
        when(paymentService.retryPosting(5L)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/payments/{paymentId}/retry-posting", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment posting retried"))
                .andExpect(jsonPath("$.data.reference").value("PAY-200"));
    }

    private record StatusPayload(String reason) {
    }
}
