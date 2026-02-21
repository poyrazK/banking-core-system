package com.cbs.loan.controller;

import com.cbs.common.exception.ApiException;
import com.cbs.loan.dto.LoanResponse;
import com.cbs.loan.exception.LoanExceptionHandler;
import com.cbs.loan.model.AmortizationType;
import com.cbs.loan.model.LoanStatus;
import com.cbs.loan.model.LoanType;
import com.cbs.loan.service.LoanService;
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
@WebMvcTest(LoanController.class)
@Import(LoanExceptionHandler.class)
class LoanControllerTest {
        @MockBean
        private com.cbs.auth.service.JwtService jwtService;

        @MockBean
        private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private LoanService loanService;

        @Test
        void createLoan_returnsSuccessResponse() throws Exception {
                LoanResponse response = new LoanResponse(
                                1L,
                                10L,
                                20L,
                                "LOAN-100",
                                LoanType.PERSONAL,
                                LoanStatus.APPLIED,
                                BigDecimal.valueOf(10000),
                                BigDecimal.ZERO,
                                BigDecimal.valueOf(12.5),
                                12,
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2027, 2, 1),
                                null,
                                AmortizationType.ANNUITY);
                when(loanService.createLoan(any())).thenReturn(response);

                String body = """
                                {
                                  "customerId": 10,
                                  "accountId": 20,
                                  "loanNumber": "LOAN-100",
                                  "loanType": "PERSONAL",
                                  "principalAmount": 10000.00,
                                  "annualInterestRate": 12.50,
                                  "termMonths": 12,
                                  "startDate": "2026-02-01",
                                  "maturityDate": "2027-02-01",
                                  "amortizationType": "ANNUITY"
                                }
                                """;

                mockMvc.perform(post("/api/v1/loans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Loan created"))
                                .andExpect(jsonPath("$.data.loanNumber").value("LOAN-100"));
        }

        @Test
        void createLoan_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
                String body = """
                                {
                                  "customerId": 10,
                                  "loanType": "PERSONAL",
                                  "principalAmount": 10000.00
                                }
                                """;

                mockMvc.perform(post("/api/v1/loans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void repayLoan_returnsBusinessErrorWhenNotDisbursed() throws Exception {
                when(loanService.repayLoan(any(), any()))
                                .thenThrow(new ApiException("LOAN_NOT_DISBURSED",
                                                "Repayments are allowed only for disbursed loans"));

                String body = objectMapper.writeValueAsString(new RepaymentPayload(BigDecimal.valueOf(100)));

                mockMvc.perform(patch("/api/v1/loans/{loanId}/repay", 99)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Repayments are allowed only for disbursed loans"));
        }

        private record RepaymentPayload(BigDecimal amount) {
        }
}
