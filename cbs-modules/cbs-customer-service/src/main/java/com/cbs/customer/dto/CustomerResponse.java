package com.cbs.customer.dto;

import com.cbs.customer.model.Customer;
import com.cbs.customer.model.KycStatus;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        KycStatus kycStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long version) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getKycStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getVersion());
    }
}
