package com.cbs.card.controller;

import com.cbs.card.dto.CardResponse;
import com.cbs.card.exception.CardExceptionHandler;
import com.cbs.card.model.CardStatus;
import com.cbs.card.model.CardType;
import com.cbs.card.service.CardService;
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
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CardController.class)
@Import(CardExceptionHandler.class)
class CardControllerTest {
    @MockBean
    private com.cbs.auth.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;


        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CardService cardService;

        @Test
        void createCard_returnsSuccessResponse() throws Exception {
                CardResponse response = new CardResponse(
                                1L,
                                10L,
                                20L,
                                "************1111",
                                "TOKEN-1",
                                CardType.DEBIT,
                                CardStatus.NEW,
                                BigDecimal.valueOf(1000),
                                BigDecimal.valueOf(10000),
                                LocalDate.of(2028, 1, 1),
                                null);
                when(cardService.createCard(any())).thenReturn(response);

                String body = """
                                {
                                  "customerId": 10,
                                  "accountId": 20,
                                  "cardNumber": "4111111111111111",
                                  "token": "TOKEN-1",
                                  "cardType": "DEBIT",
                                  "dailyLimit": 1000.00,
                                  "monthlyLimit": 10000.00,
                                  "expiryDate": "2028-01-01"
                                }
                                """;

                mockMvc.perform(post("/api/v1/cards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Card created"))
                                .andExpect(jsonPath("$.data.token").value("TOKEN-1"));
        }

        @Test
        void createCard_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
                String body = """
                                {
                                  "customerId": 10,
                                  "cardNumber": "",
                                  "cardType": "DEBIT"
                                }
                                """;

                mockMvc.perform(post("/api/v1/cards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void freezeCard_returnsBusinessErrorWhenNotActive() throws Exception {
                when(cardService.freezeCard(any(), any()))
                                .thenThrow(new ApiException("CARD_NOT_ACTIVE", "Only active cards can be frozen"));

                String body = objectMapper.writeValueAsString(new ReasonPayload("risk"));

                mockMvc.perform(patch("/api/v1/cards/{cardId}/freeze", 9)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Only active cards can be frozen"));
        }

        private record ReasonPayload(String reason) {
        }
}
