package com.routbuddy.locations.controller;

import com.routbuddy.auth.security.UserPrincipal;
import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.common.exception.ResourceNotFoundException;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.repository.DriverProfileRepository;
import com.routbuddy.locations.dto.DriverLocationPing;
import com.routbuddy.locations.service.LocationTrackingService;
import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationWebSocketController {

    private final LocationTrackingService locationTrackingService;
    private final TripRepository tripRepository;
    private final DriverProfileRepository driverProfileRepository;

    @MessageMapping("/trips/{tripId}/location")
    public void handleTripLocationPing(
            @DestinationVariable UUID tripId,
            @Payload DriverLocationPing ping,
            Principal principal) {

        log.debug("Received WebSocket location ping for trip {}: lat={}, lng={}",
                tripId, ping.getLatitude(), ping.getLongitude());

        // 1. Fetch trip and validate existence
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        // 2. Validate trip active status
        if (trip.getStatus() != TripStatus.PUBLISHED &&
            trip.getStatus() != TripStatus.BOARDING &&
            trip.getStatus() != TripStatus.IN_TRANSIT) {
            log.warn("Rejected location ping for inactive trip {} in state {}", tripId, trip.getStatus());
            throw new AccessDeniedException("Cannot publish location for inactive trip: " + trip.getStatus());
        }

        // 3. Driver Authorization: verify caller is the assigned driver
        if (principal != null) {
            UUID authenticatedUserId = extractUserId(principal);
            if (authenticatedUserId != null) {
                // Find driver profile for authenticated user
                Optional<DriverProfile> driverOpt = driverProfileRepository.findByUserId(authenticatedUserId);
                if (driverOpt.isEmpty() || !driverOpt.get().getId().equals(trip.getDriverId())) {
                    log.warn("Unauthorized location ping attempt by user {} on trip {} assigned to driver {}",
                            authenticatedUserId, tripId, trip.getDriverId());
                    throw new AccessDeniedException("Only the assigned driver can publish locations for this active trip");
                }
            }
        }

        // 4. Populate verified attributes
        ping.setTripId(trip.getId());
        ping.setDriverId(trip.getDriverId());

        // 5. Process ping (Redis GEO, Redis Hash, and WebSocket broadcasts)
        locationTrackingService.processLocationPing(ping);
    }

    @MessageMapping("/telemetry")
    public void handleDirectTelemetry(
            @Payload DriverLocationPing ping,
            Principal principal) {
        if (ping.getTripId() != null) {
            handleTripLocationPing(ping.getTripId(), ping, principal);
        }
    }

    private UUID extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof UserPrincipal up) {
                return up.getId();
            }
        }
        try {
            return UUID.fromString(principal.getName());
        } catch (Exception e) {
            return null;
        }
    }
}
