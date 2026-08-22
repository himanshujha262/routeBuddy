package com.routbuddy.trips.controller;

import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.common.dto.ApiResponse;
import com.routbuddy.trips.dto.CreateTripRequest;
import com.routbuddy.trips.dto.TripDto;
import com.routbuddy.trips.dto.UpdateTelemetryRequest;
import com.routbuddy.trips.service.TripService;
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
@RequestMapping("/api/v1/trips")
@Tag(name = "Trip Lifecycle & Management", description = "Endpoints for scheduling, publishing, boarding, transitioning, and tracking trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Schedule/Create a new trip for current driver", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripDto>> createTrip(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateTripRequest request) {
        TripDto trip = tripService.createTrip(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.ok("Trip scheduled successfully", trip), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Publish trip to make it visible and bookable", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripDto>> publishTrip(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        TripDto trip = tripService.publishTrip(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Trip published successfully", trip));
    }

    @PutMapping("/{id}/start-boarding")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver arrives at origin and opens passenger boarding", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripDto>> startBoarding(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        TripDto trip = tripService.startBoarding(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Trip boarding started", trip));
    }

    @PutMapping("/{id}/start-trip")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver departs from origin (trip transitions to IN_TRANSIT)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripDto>> startTrip(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        TripDto trip = tripService.startTrip(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Trip departed and is in transit", trip));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver completes the trip at final destination stop", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripDto>> completeTrip(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        TripDto trip = tripService.completeTrip(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Trip completed successfully", trip));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    @Operation(summary = "Cancel trip before completion", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripDto>> cancelTrip(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        TripDto trip = tripService.cancelTrip(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Trip cancelled", trip));
    }

    @PutMapping("/{id}/telemetry")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Update live GPS telemetry and current stop sequence", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripDto>> updateTelemetry(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTelemetryRequest request) {
        TripDto trip = tripService.updateTelemetry(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Telemetry updated", trip));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trip details by ID")
    public ResponseEntity<ApiResponse<TripDto>> getTripById(@PathVariable UUID id) {
        TripDto trip = tripService.getTripById(id);
        return ResponseEntity.ok(ApiResponse.ok(trip));
    }

    @GetMapping("/route/{routeId}")
    @Operation(summary = "Get available and bookable trips for a route")
    public ResponseEntity<ApiResponse<List<TripDto>>> getAvailableTripsForRoute(@PathVariable UUID routeId) {
        List<TripDto> trips = tripService.getAvailableTripsForRoute(routeId);
        return ResponseEntity.ok(ApiResponse.ok(trips));
    }

    @GetMapping("/driver/active")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Get current active trip for logged-in driver", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripDto>> getActiveTripForDriver(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        TripDto trip = tripService.getActiveTripForDriver(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(trip));
    }
}
