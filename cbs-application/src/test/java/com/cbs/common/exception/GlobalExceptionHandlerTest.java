package com.cbs.common.exception;

import com.cbs.common.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApiException_returnsConfiguredStatusAndErrorCode() {
        ApiException exception = new ApiException("ALREADY_EXISTS", "User exists", HttpStatus.CONFLICT);

        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ALREADY_EXISTS", response.getBody().errorCode());
        assertEquals("User exists", response.getBody().message());
    }

    @Test
    void handleApiException_defaultsToBadRequest() {
        ApiException exception = new ApiException("ERROR", "Generic message");

        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().errorCode());
        assertEquals("Generic message", response.getBody().message());
        assertEquals(null, response.getBody().data());
    }

    @Test
    void handleGeneralException_returnsInternalServerError() {
        Exception exception = new RuntimeException("Unexpected crash");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneralException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().errorCode());
    }
}
