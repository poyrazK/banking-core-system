package com.cbs.deposit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepositStatusReasonRequest(
        @NotBlank @Size(max = 255) String reason
) {
}
