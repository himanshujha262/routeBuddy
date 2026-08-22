package com.routbuddy.auth.controller;

import com.routbuddy.auth.dto.*;
import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.auth.service.AuthService;
import com.routbuddy.auth.service.RefreshTokenService;
import com.routbuddy.common.dto.ApiResponse;
import com.routbuddy.users.dto.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication & RBAC", description = "Endpoints for registration, login, phone OTP, refresh tokens, and profile")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user (Passenger, Driver, Commuter)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.ok("User registered successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with phone/email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to mobile number")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        String message = authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(message, null));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify phone OTP and authenticate/register")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified successfully", response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Rotate and refresh expired JWT using Refresh Token")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse response = refreshTokenService.rotateRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out and revoke refresh token")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserProfileDto profile = authService.getCurrentUser(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @GetMapping("/test-rbac/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Test RBAC guard for Admin", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<String>> testAdminRbac(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.ok("Admin RBAC verified for user: " + userPrincipal.getUsername(), "Access Granted"));
    }

    @GetMapping("/test-rbac/driver")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Test RBAC guard for Driver", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<String>> testDriverRbac(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.ok("Driver RBAC verified for user: " + userPrincipal.getUsername(), "Access Granted"));
    }

    @GetMapping("/test-rbac/passenger")
    @PreAuthorize("hasRole('PASSENGER') or hasRole('COMMUTER')")
    @Operation(summary = "Test RBAC guard for Passenger/Commuter", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<String>> testPassengerRbac(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.ok("Passenger RBAC verified for user: " + userPrincipal.getUsername(), "Access Granted"));
    }
}
