package com.cbs.account.exception;

import com.cbs.common.api.ApiResponse;
import com.cbs.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountExceptionHandlerTest {

    @Test
    void handleApiExceptionReturnsErrorCodeInResponseBody() {
        AccountExceptionHandler handler = new AccountExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new ApiException("ACCOUNT_NOT_FOUND", "Account not found")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCOUNT_NOT_FOUND", response.getBody().errorCode());
        assertEquals("Account not found", response.getBody().message());
    }
}
