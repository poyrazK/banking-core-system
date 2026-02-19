package com.cbs.auth.controller;

import com.cbs.auth.dto.AuthResponse;
import com.cbs.auth.exception.AuthExceptionHandler;
import com.cbs.auth.service.AuthService;
import com.cbs.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthExceptionHandler.class)
class AuthControllerTest {
        @MockBean
        private com.cbs.auth.service.JwtService jwtService;

        @MockBean
        private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AuthService authService;

        @Test
        void register_returnsSuccessResponse() throws Exception {
                when(authService.register(any())).thenReturn(AuthResponse.bearer("token-1"));

                String body = """
                                {
                                  "username": "john",
                                  "password": "Password123",
                                  "role": "CUSTOMER"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered"))
                                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
        }

        @Test
        void register_returnsBadRequestWhenPayloadInvalid() throws Exception {
                String body = """
                                {
                                  "username": "ab",
                                  "password": "123"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void login_returnsBusinessErrorWhenUserMissing() throws Exception {
                when(authService.login(any())).thenThrow(new ApiException("AUTH_NOT_FOUND", "User not found"));

                String body = """
                                {
                                  "username": "john",
                                  "password": "Password123"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("User not found"));
        }
}
