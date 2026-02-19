package com.cbs.interest.controller;

import com.cbs.common.exception.ApiException;
import com.cbs.interest.dto.InterestConfigResponse;
import com.cbs.interest.exception.InterestExceptionHandler;
import com.cbs.interest.model.InterestBasis;
import com.cbs.interest.model.InterestStatus;
import com.cbs.interest.service.InterestService;
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
@WebMvcTest(InterestController.class)
@Import(InterestExceptionHandler.class)
class InterestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private InterestService interestService;

        @Test
        void createConfig_returnsSuccessResponse() throws Exception {
                InterestConfigResponse response = new InterestConfigResponse(
                                1L,
                                "SAV-01",
                                BigDecimal.valueOf(12.50),
                                InterestBasis.SIMPLE,
                                30,
                                InterestStatus.ACTIVE);
                when(interestService.createConfig(any())).thenReturn(response);

                String body = """
                                {
                                  "productCode": "SAV-01",
                                  "annualRate": 12.50,
                                  "interestBasis": "SIMPLE",
                                  "accrualFrequencyDays": 30
                                }
                                """;

                mockMvc.perform(post("/api/v1/interests/configs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Interest config created"))
                                .andExpect(jsonPath("$.data.productCode").value("SAV-01"));
        }

        @Test
        void createConfig_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
                String body = """
                                {
                                  "productCode": "",
                                  "annualRate": 12.50
                                }
                                """;

                mockMvc.perform(post("/api/v1/interests/configs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void updateConfig_returnsBusinessErrorWhenConfigMissing() throws Exception {
                when(interestService.updateConfig(any(), any()))
                                .thenThrow(new ApiException("INTEREST_CONFIG_NOT_FOUND", "Interest config not found"));

                String body = """
                                {
                                  "annualRate": 13.00,
                                  "interestBasis": "SIMPLE",
                                  "accrualFrequencyDays": 30,
                                  "status": "ACTIVE"
                                }
                                """;

                mockMvc.perform(patch("/api/v1/interests/configs/{productCode}", "SAV-01")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Interest config not found"));
        }
}
