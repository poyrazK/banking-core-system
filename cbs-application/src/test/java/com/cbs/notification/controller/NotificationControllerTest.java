package com.cbs.notification.controller;

import com.cbs.common.exception.ApiException;
import com.cbs.notification.dto.NotificationResponse;
import com.cbs.notification.exception.NotificationExceptionHandler;
import com.cbs.notification.model.NotificationChannel;
import com.cbs.notification.model.NotificationStatus;
import com.cbs.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(NotificationController.class)
@Import(NotificationExceptionHandler.class)
class NotificationControllerTest {
    @MockBean
    private com.cbs.auth.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    void createNotification_returnsSuccessResponse() throws Exception {
        NotificationResponse response = new NotificationResponse(
                1L,
                10L,
                "user@example.com",
                NotificationChannel.EMAIL,
                NotificationStatus.CREATED,
                "Welcome",
                "Hello",
                "REF-1",
                null
        );
        when(notificationService.createNotification(any())).thenReturn(response);

        String body = """
                {
                  "customerId": 10,
                  "recipient": "user@example.com",
                  "channel": "EMAIL",
                  "subject": "Welcome",
                  "message": "Hello",
                  "reference": "REF-1"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification created"))
                .andExpect(jsonPath("$.data.reference").value("REF-1"));
    }

    @Test
    void createNotification_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
        String body = """
                {
                  "customerId": 10,
                  "recipient": "",
                  "channel": "EMAIL"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void cancelNotification_returnsBusinessErrorWhenAlreadySent() throws Exception {
        when(notificationService.cancelNotification(any(), any()))
                .thenThrow(new ApiException("NOTIFICATION_ALREADY_SENT", "Sent notification cannot be cancelled"));

        String body = objectMapper.writeValueAsString(new ReasonPayload("duplicate"));

        mockMvc.perform(patch("/api/v1/notifications/{notificationId}/cancel", 9)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Sent notification cannot be cancelled"));
    }

    private record ReasonPayload(String reason) {
    }
}
