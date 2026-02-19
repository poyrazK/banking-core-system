package com.cbs.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
                @Size(min = 1, max = 100) @Pattern(regexp = "^[\\p{L} '-]+$") String firstName,
                @Size(min = 1, max = 100) @Pattern(regexp = "^[\\p{L} '-]+$") String lastName,
                @Email @Size(max = 255) String email,
                @Size(max = 30) @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phoneNumber) {
}
