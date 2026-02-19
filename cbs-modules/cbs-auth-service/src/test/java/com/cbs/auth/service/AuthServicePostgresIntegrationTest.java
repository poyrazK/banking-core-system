package com.cbs.auth.service;

import com.cbs.auth.dto.AuthResponse;
import com.cbs.auth.dto.LoginRequest;
import com.cbs.auth.dto.RegisterRequest;
import com.cbs.auth.model.Role;
import com.cbs.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55444/cbs_auth_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerPersistsUserInPostgres() {
        AuthResponse response = authService.register(new RegisterRequest("john", "Password123", Role.CUSTOMER));

        assertEquals("Bearer", response.tokenType());
        assertTrue(userRepository.existsByUsername("john"));
    }

    @Test
    void loginReturnsTokenForPersistedUser() {
        authService.register(new RegisterRequest("alice", "Password123", Role.ADMIN));

        AuthResponse response = authService.login(new LoginRequest("alice", "Password123"));

        assertEquals("Bearer", response.tokenType());
        assertTrue(response.accessToken() != null && !response.accessToken().isBlank());
    }
}