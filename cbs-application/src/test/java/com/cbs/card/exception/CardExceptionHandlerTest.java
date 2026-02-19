package com.cbs.card.exception;

import com.cbs.common.api.ApiResponse;
import com.cbs.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CardExceptionHandlerTest {

    @Test
    void handleApiExceptionReturnsErrorCodeInResponseBody() {
        CardExceptionHandler handler = new CardExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new ApiException("CARD_BLOCKED", "Card is blocked")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CARD_BLOCKED", response.getBody().errorCode());
        assertEquals("Card is blocked", response.getBody().message());
    }
}
