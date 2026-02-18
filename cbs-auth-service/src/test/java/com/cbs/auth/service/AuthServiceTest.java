package com.cbs.auth.service;

import com.cbs.auth.dto.AuthResponse;
import com.cbs.auth.dto.LoginRequest;
import com.cbs.auth.dto.RegisterRequest;
import com.cbs.auth.model.Role;
import com.cbs.auth.model.User;
import com.cbs.auth.repository.UserRepository;
import com.cbs.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void register_returnsBearerTokenForNewUser() {
        RegisterRequest request = new RegisterRequest("john", "Password123", Role.CUSTOMER);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("token-1");

        AuthResponse response = authService.register(request);

        assertEquals("token-1", response.accessToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void register_throwsWhenUsernameExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.register(new RegisterRequest("john", "Password123", Role.CUSTOMER))
        );

        assertEquals("AUTH_USER_EXISTS", exception.getErrorCode());
    }

    @Test
    void login_authenticatesAndReturnsToken() {
        User user = new User("john", "encoded", Role.CUSTOMER);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token-2");

        AuthResponse response = authService.login(new LoginRequest("john", "Password123"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("token-2", response.accessToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void login_throwsWhenUserMissingAfterAuthentication() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.login(new LoginRequest("john", "Password123"))
        );

        assertEquals("AUTH_NOT_FOUND", exception.getErrorCode());
    }
}
