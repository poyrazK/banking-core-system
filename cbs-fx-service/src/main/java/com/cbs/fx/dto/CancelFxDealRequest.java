package com.cbs.fx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelFxDealRequest(
        @NotBlank @Size(max = 255) String reason
) {
}
