package com.cbs.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotificationStatusReasonRequest(
        @NotBlank @Size(max = 255) String reason
) {
}
