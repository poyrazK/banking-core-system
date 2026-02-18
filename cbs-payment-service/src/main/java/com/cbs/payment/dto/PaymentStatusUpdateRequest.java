package com.cbs.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentStatusUpdateRequest(
        @NotBlank @Size(max = 255) String reason
) {
}
