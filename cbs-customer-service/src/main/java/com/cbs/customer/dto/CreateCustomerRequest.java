package com.cbs.customer.dto;

import com.cbs.customer.model.KycStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 30) String phoneNumber,
        KycStatus kycStatus
) {
}
