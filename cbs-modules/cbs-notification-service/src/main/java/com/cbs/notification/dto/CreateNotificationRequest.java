package com.cbs.notification.dto;

import com.cbs.notification.model.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull Long customerId,
        @NotBlank @Size(max = 128) String recipient,
        @NotNull NotificationChannel channel,
        @NotBlank @Size(max = 100) String subject,
        @NotBlank @Size(max = 2000) String message,
        @NotBlank @Size(max = 64) String reference
) {
}
