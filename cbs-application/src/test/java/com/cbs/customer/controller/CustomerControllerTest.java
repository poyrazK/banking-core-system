package com.cbs.customer.controller;

import com.cbs.common.exception.ApiException;
import com.cbs.customer.dto.CustomerResponse;
import com.cbs.common.exception.GlobalExceptionHandler;
import com.cbs.customer.model.KycStatus;
import com.cbs.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {
    @MockBean
    private com.cbs.auth.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;


  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CustomerService customerService;

  @Test
  void createCustomer_returnsSuccessResponse() throws Exception {
    when(customerService.createCustomer(any())).thenReturn(new CustomerResponse(
        1L,
        "Jane",
        "Doe",
        "jane@cbs.com",
        "+905550101",
        KycStatus.PENDING,
        null,
        null,
        0L));

    String body = """
        {
          "firstName": "Jane",
          "lastName": "Doe",
          "email": "jane@cbs.com",
          "phoneNumber": "+905550101"
        }
        """;

    mockMvc.perform(post("/api/v1/customers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Customer created"))
        .andExpect(jsonPath("$.data.email").value("jane@cbs.com"));
  }

  @Test
  void createCustomer_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
    String body = """
        {
          "firstName": "",
          "lastName": "Doe",
          "email": "not-an-email",
          "phoneNumber": ""
        }
        """;

    mockMvc.perform(post("/api/v1/customers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void updateKycStatus_returnsBusinessErrorWhenCustomerMissing() throws Exception {
    when(customerService.updateKycStatus(any(), any()))
        .thenThrow(new ApiException("CUSTOMER_NOT_FOUND", "Customer not found"));

    String body = """
        {
          "kycStatus": "VERIFIED"
        }
        """;

    mockMvc.perform(patch("/api/v1/customers/{customerId}/kyc-status", 99)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Customer not found"));
  }

  @Test
  void updateCustomer_returnsSuccessResponse() throws Exception {
    when(customerService.updateCustomer(any(), any())).thenReturn(new CustomerResponse(
        1L,
        "Janet",
        "Doe",
        "janet@cbs.com",
        "+90-555-0101",
        KycStatus.PENDING,
        null,
        null,
        0L));

    String body = """
        {
          "firstName": "Janet",
          "email": "janet@cbs.com"
        }
        """;

    mockMvc.perform(put("/api/v1/customers/{customerId}", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Customer updated"))
        .andExpect(jsonPath("$.data.firstName").value("Janet"))
        .andExpect(jsonPath("$.data.email").value("janet@cbs.com"));
  }
}