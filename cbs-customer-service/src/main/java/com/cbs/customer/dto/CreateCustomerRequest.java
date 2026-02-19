package com.cbs.customer.dto;

import com.cbs.customer.model.KycStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
                @NotBlank @Size(min = 1, max = 100) @Pattern(regexp = "^[\\p{L} '-]+$") String firstName,
                @NotBlank @Size(min = 1, max = 100) @Pattern(regexp = "^[\\p{L} '-]+$") String lastName,
                @NotBlank @Email @Size(max = 255) String email,
                @NotBlank @Size(max = 30) @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phoneNumber,
                KycStatus kycStatus) {
}
