package com.cbs.auth.service;

import com.cbs.auth.dto.AuthResponse;
import com.cbs.auth.dto.LoginRequest;
import com.cbs.auth.dto.RegisterRequest;
import com.cbs.auth.model.User;
import com.cbs.auth.repository.UserRepository;
import com.cbs.common.exception.ApiException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException("AUTH_USER_EXISTS", "Username is already in use");
        }

        User user = new User(request.username(), passwordEncoder.encode(request.password()), request.role());
        User savedUser = userRepository.save(user);
        return AuthResponse.bearer(jwtService.generateToken(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException("AUTH_NOT_FOUND", "User not found"));

        return AuthResponse.bearer(jwtService.generateToken(user));
    }
}
