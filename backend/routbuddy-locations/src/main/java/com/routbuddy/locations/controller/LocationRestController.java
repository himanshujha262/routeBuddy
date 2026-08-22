package com.routbuddy.locations.controller;

import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.common.domain.dto.ApiResponse;
import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.common.exception.ResourceNotFoundException;
import com.routbuddy.common.exception.UnauthorizedException;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.repository.DriverProfileRepository;
import com.routbuddy.locations.dto.DriverLocationPing;
import com.routbuddy.locations.dto.TripOccupancyUpdate;
import com.routbuddy.locations.service.LocationTrackingService;
import com.routbuddy.locations.service.TripOccupancyBroadcaster;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.repository.TripRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationRestController {

    private final LocationTrackingService locationTrackingService;
    private final TripOccupancyBroadcaster tripOccupancyBroadcaster;
    private final TripRepository tripRepository;
    private final DriverProfileRepository driverProfileRepository;

    @GetMapping("/trips/{tripId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTripLiveState(@PathVariable UUID tripId) {
        DriverLocationPing telemetry = locationTrackingService.getLatestTripTelemetry(tripId);
        TripOccupancyUpdate occupancy = tripOccupancyBroadcaster.getLatestTripOccupancy(tripId);

        Map<String, Object> state = new HashMap<>();
        state.put("tripId", tripId);
        state.put("telemetry", telemetry);
        state.put("occupancy", occupancy);

        return ResponseEntity.ok(ApiResponse.success(state));
    }

    @GetMapping("/trips/{tripId}/telemetry")
    public ResponseEntity<ApiResponse<DriverLocationPing>> getTripTelemetry(@PathVariable UUID tripId) {
        DriverLocationPing telemetry = locationTrackingService.getLatestTripTelemetry(tripId);
        if (telemetry == null) {
            return ResponseEntity.ok(ApiResponse.success("No active telemetry found for trip", null));
        }
        return ResponseEntity.ok(ApiResponse.success(telemetry));
    }

    @GetMapping("/trips/{tripId}/occupancy")
    public ResponseEntity<ApiResponse<TripOccupancyUpdate>> getTripOccupancy(@PathVariable UUID tripId) {
        TripOccupancyUpdate occupancy = tripOccupancyBroadcaster.getLatestTripOccupancy(tripId);
        if (occupancy == null) {
            return ResponseEntity.ok(ApiResponse.success("No occupancy data found for trip", null));
        }
        return ResponseEntity.ok(ApiResponse.success(occupancy));
    }

    @PostMapping("/trips/{tripId}/telemetry")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<DriverLocationPing>> postTelemetryFallback(
            @PathVariable UUID tripId,
            @Valid @RequestBody DriverLocationPing ping,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        if (trip.getStatus() != TripStatus.PUBLISHED &&
            trip.getStatus() != TripStatus.BOARDING &&
            trip.getStatus() != TripStatus.IN_TRANSIT) {
            throw new BadRequestException("Cannot post telemetry for inactive trip: " + trip.getStatus());
        }

        // Validate driver ownership
        if (userPrincipal != null) {
            Optional<DriverProfile> driverOpt = driverProfileRepository.findByUserId(userPrincipal.getId());
            if (driverOpt.isEmpty() || !driverOpt.get().getId().equals(trip.getDriverId())) {
                throw new UnauthorizedException("You are not the assigned driver for this trip");
            }
        }

        ping.setTripId(trip.getId());
        ping.setDriverId(trip.getDriverId());

        locationTrackingService.processLocationPing(ping);

        return ResponseEntity.ok(ApiResponse.success("Telemetry updated successfully", ping));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<GeoResults<RedisGeoCommands.GeoLocation<String>>>> getNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                locationTrackingService.findNearbyDrivers(latitude, longitude, radiusKm);

        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
