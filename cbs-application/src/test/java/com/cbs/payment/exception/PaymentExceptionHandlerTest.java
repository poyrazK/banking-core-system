package com.cbs.payment.exception;

import com.cbs.common.api.ApiResponse;
import com.cbs.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentExceptionHandlerTest {

    @Test
    void handleApiExceptionReturnsErrorCodeInResponseBody() {
        PaymentExceptionHandler handler = new PaymentExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new ApiException("PAYMENT_NOT_FOUND", "Payment not found")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PAYMENT_NOT_FOUND", response.getBody().errorCode());
        assertEquals("Payment not found", response.getBody().message());
    }
}
