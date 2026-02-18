package com.cbs.ledger.exception;

import com.cbs.common.api.ApiResponse;
import com.cbs.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LedgerExceptionHandlerTest {

    @Test
    void handleApiExceptionReturnsErrorCodeInResponseBody() {
        LedgerExceptionHandler handler = new LedgerExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new ApiException("LEDGER_UNBALANCED_ENTRY", "Debit and credit totals must be equal")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("LEDGER_UNBALANCED_ENTRY", response.getBody().errorCode());
        assertEquals("Debit and credit totals must be equal", response.getBody().message());
    }
}
