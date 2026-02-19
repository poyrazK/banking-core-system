package com.cbs.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReverseTransactionRequest(
        @NotBlank @Size(max = 255) String reason
) {
}
