package com.subscription.auth.controller;

import com.subscription.auth.constants.ApiConstants;
import com.subscription.auth.dto.request.LoginRequest;
import com.subscription.auth.dto.request.RegisterRequest;
import com.subscription.auth.dto.response.ApiResponse;
import com.subscription.auth.dto.response.AuthResponse;
import com.subscription.auth.dto.response.ErrorResponse;
import com.subscription.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiConstants.AUTH_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = ApiConstants.AUTH_TAG, description = "Endpoints for user registration and login")
public class AuthController {

    private final AuthService authService;

    // =========================================================
    //  POST /api/auth/register
    // =========================================================

    @Operation(
            summary     = "Register a new user",
            description = "Creates a new user account and returns a JWT access token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description  = "User registered successfully",
                    content      = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description  = "Validation error",
                    content      = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description  = "Email already in use",
                    content      = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping(ApiConstants.REGISTER_ENDPOINT)
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register request received for email: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiConstants.USER_REGISTERED_SUCCESS, authResponse));
    }

    // =========================================================
    //  POST /api/auth/login
    // =========================================================

    @Operation(
            summary     = "User login",
            description = "Authenticates user credentials and returns a JWT access token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description  = "Login successful",
                    content      = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description  = "Validation error",
                    content      = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description  = "Invalid credentials",
                    content      = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping(ApiConstants.LOGIN_ENDPOINT)
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(ApiConstants.LOGIN_SUCCESS, authResponse));
    }
}
