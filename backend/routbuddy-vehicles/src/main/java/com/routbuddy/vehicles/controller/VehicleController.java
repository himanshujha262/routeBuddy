package com.routbuddy.vehicles.controller;

import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.common.dto.ApiResponse;
import com.routbuddy.vehicles.dto.RegisterVehicleRequest;
import com.routbuddy.vehicles.dto.VehicleDto;
import com.routbuddy.vehicles.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicle Management", description = "Endpoints for vehicle registration, verification, capacity, and fleet listing")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    @Operation(summary = "Register a new vehicle under current driver", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<VehicleDto>> registerVehicle(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RegisterVehicleRequest request) {
        VehicleDto vehicle = vehicleService.registerVehicle(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.ok("Vehicle registered successfully. Pending admin approval.", vehicle), HttpStatus.CREATED);
    }

    @GetMapping("/my-vehicles")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    @Operation(summary = "List all vehicles registered by current driver", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<VehicleDto>>> getMyVehicles(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<VehicleDto> vehicles = vehicleService.getVehiclesByDriver(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(vehicles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle details by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<VehicleDto>> getVehicleById(@PathVariable UUID id) {
        VehicleDto vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.ok(vehicle));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Approve registered vehicle", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<VehicleDto>> approveVehicle(@PathVariable UUID id) {
        VehicleDto vehicle = vehicleService.approveVehicle(id);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle approved successfully", vehicle));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Reject registered vehicle", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<VehicleDto>> rejectVehicle(@PathVariable UUID id) {
        VehicleDto vehicle = vehicleService.rejectVehicle(id);
        return ResponseEntity.ok(ApiResponse.ok("Vehicle rejected", vehicle));
    }
}
