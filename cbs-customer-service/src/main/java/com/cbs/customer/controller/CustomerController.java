package com.cbs.customer.controller;

import com.cbs.common.api.ApiResponse;
import com.cbs.customer.dto.CreateCustomerRequest;
import com.cbs.customer.dto.CustomerResponse;
import com.cbs.customer.dto.UpdateKycStatusRequest;
import com.cbs.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse customerResponse = customerService.createCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("Customer created", customerResponse));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable("customerId") Long customerId) {
        CustomerResponse customerResponse = customerService.getCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer found", customerResponse));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> searchCustomers(@RequestParam(value = "query", required = false) String query) {
        List<CustomerResponse> customerResponses = customerService.searchCustomers(query);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved", customerResponses));
    }

    @PatchMapping("/{customerId}/kyc-status")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateKycStatus(
            @PathVariable("customerId") Long customerId,
            @Valid @RequestBody UpdateKycStatusRequest request
    ) {
        CustomerResponse customerResponse = customerService.updateKycStatus(customerId, request);
        return ResponseEntity.ok(ApiResponse.success("KYC status updated", customerResponse));
    }
}
