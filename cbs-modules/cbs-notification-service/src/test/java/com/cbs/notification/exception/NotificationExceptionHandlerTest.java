package com.cbs.notification.exception;

import com.cbs.common.api.ApiResponse;
import com.cbs.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationExceptionHandlerTest {

    @Test
    void handleApiExceptionReturnsErrorCodeInResponseBody() {
        NotificationExceptionHandler handler = new NotificationExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new ApiException("NOTIFICATION_CHANNEL_UNAVAILABLE", "Notification channel unavailable")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("NOTIFICATION_CHANNEL_UNAVAILABLE", response.getBody().errorCode());
        assertEquals("Notification channel unavailable", response.getBody().message());
    }
}
