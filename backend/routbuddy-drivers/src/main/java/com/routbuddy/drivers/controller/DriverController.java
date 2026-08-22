package com.routbuddy.drivers.controller;

import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.common.dto.ApiResponse;
import com.routbuddy.drivers.dto.DriverProfileDto;
import com.routbuddy.drivers.dto.RegisterDriverRequest;
import com.routbuddy.drivers.dto.UpdateKycRequest;
import com.routbuddy.drivers.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
@Tag(name = "Driver Management", description = "Endpoints for driver onboarding, KYC review, online status, and vehicle assignment")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register/onboard current user as a Driver", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<DriverProfileDto>> registerDriver(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RegisterDriverRequest request) {
        DriverProfileDto profile = driverService.registerDriver(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.ok("Driver registration submitted successfully", profile), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    @Operation(summary = "Get current authenticated driver profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<DriverProfileDto>> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        DriverProfileDto profile = driverService.getDriverProfileByUserId(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/me/status")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Toggle driver online/offline status (KYC must be APPROVED)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<DriverProfileDto>> updateStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("online") boolean online) {
        DriverProfileDto profile = driverService.updateOnlineStatus(userPrincipal.getId(), online);
        return ResponseEntity.ok(ApiResponse.ok("Driver online status updated to " + (online ? "ONLINE" : "OFFLINE"), profile));
    }

    @PutMapping("/me/vehicle/{vehicleId}")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Assign active vehicle to driver", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<DriverProfileDto>> assignVehicle(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID vehicleId) {
        DriverProfileDto profile = driverService.assignVehicle(userPrincipal.getId(), vehicleId);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle assigned successfully", profile));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get driver profile by Driver ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<DriverProfileDto>> getDriverById(@PathVariable UUID id) {
        DriverProfileDto profile = driverService.getDriverProfile(id);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/{id}/kyc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Approve or Reject Driver KYC verification", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<DriverProfileDto>> updateKyc(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateKycRequest request) {
        DriverProfileDto profile = driverService.updateKycStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Driver KYC status updated to " + request.getStatus(), profile));
    }
}
