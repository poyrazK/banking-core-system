package com.cbs.interest.exception;

import com.cbs.common.api.ApiResponse;
import com.cbs.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InterestExceptionHandlerTest {

    @Test
    void handleApiExceptionReturnsErrorCodeInResponseBody() {
        InterestExceptionHandler handler = new InterestExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new ApiException("INTEREST_RULE_NOT_FOUND", "Interest rule not found")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTEREST_RULE_NOT_FOUND", response.getBody().errorCode());
        assertEquals("Interest rule not found", response.getBody().message());
    }
}
