package com.cbs.customer.dto;

import com.cbs.customer.model.KycStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateKycStatusRequest(@NotNull KycStatus kycStatus) {
}
