package com.subscription.auth.controller;

import com.subscription.auth.constants.ApiConstants;
import com.subscription.auth.dto.response.ApiResponse;
import com.subscription.auth.dto.response.ErrorResponse;
import com.subscription.auth.dto.response.UserResponse;
import com.subscription.auth.exception.UnauthorizedException;
import com.subscription.auth.service.UserService;
import com.subscription.auth.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiConstants.USERS_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = ApiConstants.USERS_TAG, description = "Endpoints for authenticated user operations")
public class UserController {

    private final UserService userService;

    // =========================================================
    //  GET /api/users/me
    // =========================================================

    @Operation(
            summary     = "Get current user profile",
            description = "Returns the profile of the authenticated user. Requires a valid JWT Bearer token.",
            security    = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description  = "User profile retrieved successfully",
                    content      = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description  = "Unauthorized - missing or invalid Bearer token",
                    content      = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping(ApiConstants.ME_ENDPOINT)
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {

        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> {
                    log.warn("GET /api/users/me called without authenticated user in SecurityContext");
                    return new UnauthorizedException("User is not authenticated");
                });

        log.info("Fetching profile for authenticated user: {}", email);
        UserResponse userResponse = userService.getUserByEmail(email);

        return ResponseEntity.ok(ApiResponse.success(ApiConstants.USER_FETCHED_SUCCESS, userResponse));
    }
}
