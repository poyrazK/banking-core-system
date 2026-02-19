package com.cbs.fx.controller;

import com.cbs.common.exception.ApiException;
import com.cbs.fx.dto.FxRateResponse;
import com.cbs.fx.exception.FxExceptionHandler;
import com.cbs.fx.model.FxRateStatus;
import com.cbs.fx.service.FxService;
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

@WebMvcTest(FxController.class)
@Import(FxExceptionHandler.class)
class FxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FxService fxService;

    @Test
    void createRate_returnsSuccessResponse() throws Exception {
        FxRateResponse response = new FxRateResponse(
                1L,
                "USD/TRY",
                "USD",
                "TRY",
                new BigDecimal("35.00000000"),
                new BigDecimal("10.0000"),
                new BigDecimal("12.0000"),
                FxRateStatus.ACTIVE
        );
        when(fxService.createRate(any())).thenReturn(response);

        String body = """
                {
                  "baseCurrency": "USD",
                  "quoteCurrency": "TRY",
                  "midRate": 35.00000000,
                  "buySpreadBps": 10.0000,
                  "sellSpreadBps": 12.0000
                }
                """;

        mockMvc.perform(post("/api/v1/fx/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("FX rate created"))
                .andExpect(jsonPath("$.data.currencyPair").value("USD/TRY"));
    }

    @Test
    void createRate_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
        String body = """
                {
                  "baseCurrency": "US",
                  "midRate": 35.00000000
                }
                """;

        mockMvc.perform(post("/api/v1/fx/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateRate_returnsBusinessErrorWhenRateMissing() throws Exception {
        when(fxService.updateRate(any(), any()))
                .thenThrow(new ApiException("FX_RATE_NOT_FOUND", "FX rate not found"));

        String body = """
                {
                  "midRate": 35.00000000,
                  "buySpreadBps": 10.0000,
                  "sellSpreadBps": 12.0000,
                  "status": "ACTIVE"
                }
                """;

        mockMvc.perform(patch("/api/v1/fx/rates/{currencyPair}", "USD%2FTRY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("FX rate not found"));
    }
}
