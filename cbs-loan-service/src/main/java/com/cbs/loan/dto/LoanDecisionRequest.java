package com.cbs.loan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoanDecisionRequest(
        @NotBlank @Size(max = 255) String reason
) {
}
