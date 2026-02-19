package com.cbs.fee.controller;

import com.cbs.common.exception.ApiException;
import com.cbs.fee.dto.FeeConfigResponse;
import com.cbs.fee.exception.FeeExceptionHandler;
import com.cbs.fee.model.FeeStatus;
import com.cbs.fee.model.FeeType;
import com.cbs.fee.service.FeeService;
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
@WebMvcTest(FeeController.class)
@Import(FeeExceptionHandler.class)
class FeeControllerTest {
    @MockBean
    private com.cbs.auth.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeeService feeService;

    @Test
    void createConfig_returnsSuccessResponse() throws Exception {
        FeeConfigResponse response = new FeeConfigResponse(
                1L,
                "TRN-FEE",
                "Transfer Fee",
                FeeType.TRANSFER,
                BigDecimal.valueOf(1.00),
                BigDecimal.valueOf(0.50),
                FeeStatus.ACTIVE
        );
        when(feeService.createConfig(any())).thenReturn(response);

        String body = """
                {
                  "feeCode": "TRN-FEE",
                  "name": "Transfer Fee",
                  "feeType": "TRANSFER",
                  "fixedAmount": 1.00,
                  "percentageRate": 0.50
                }
                """;

        mockMvc.perform(post("/api/v1/fees/configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Fee config created"))
                .andExpect(jsonPath("$.data.feeCode").value("TRN-FEE"));
    }

    @Test
    void createConfig_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
        String body = """
                {
                  "feeCode": "",
                  "fixedAmount": 1.00
                }
                """;

        mockMvc.perform(post("/api/v1/fees/configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateConfig_returnsBusinessErrorWhenMissing() throws Exception {
        when(feeService.updateConfig(any(), any()))
                .thenThrow(new ApiException("FEE_CONFIG_NOT_FOUND", "Fee config not found"));

        String body = """
                {
                  "name": "Transfer Fee",
                  "feeType": "TRANSFER",
                  "fixedAmount": 1.00,
                  "percentageRate": 0.50,
                  "status": "ACTIVE"
                }
                """;

        mockMvc.perform(patch("/api/v1/fees/configs/{feeCode}", "TRN-FEE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Fee config not found"));
    }
}
