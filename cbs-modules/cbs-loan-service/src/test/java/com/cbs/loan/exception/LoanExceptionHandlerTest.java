package com.cbs.loan.exception;

import com.cbs.common.api.ApiResponse;
import com.cbs.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoanExceptionHandlerTest {

    @Test
    void handleApiExceptionReturnsErrorCodeInResponseBody() {
        LoanExceptionHandler handler = new LoanExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new ApiException("LOAN_NOT_FOUND", "Loan not found")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("LOAN_NOT_FOUND", response.getBody().errorCode());
        assertEquals("Loan not found", response.getBody().message());
    }
}
