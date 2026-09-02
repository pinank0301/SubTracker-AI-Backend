package com.subscription.auth.service;

import com.subscription.auth.dto.request.LoginRequest;
import com.subscription.auth.dto.request.RegisterRequest;
import com.subscription.auth.dto.response.AuthResponse;
import com.subscription.auth.entity.User;
import com.subscription.auth.exception.DuplicateUserException;
import com.subscription.auth.exception.InvalidCredentialsException;
import com.subscription.auth.exception.UserNotFoundException;
import com.subscription.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final JwtService        jwtService;

    // =========================================================
    //  Register
    // =========================================================

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already in use: {}", request.getEmail());
            throw new DuplicateUserException(
                    "User with email '" + request.getEmail() + "' already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail());
        return AuthResponse.builder().token(token).build();
    }

    // =========================================================
    //  Login
    // =========================================================

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        log.info("Login successful for user: id={}, email={}", user.getId(), user.getEmail());

        return AuthResponse.builder().token(token).build();
    }
}
