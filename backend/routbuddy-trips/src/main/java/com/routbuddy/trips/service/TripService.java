package com.routbuddy.trips.service;

import com.routbuddy.trips.domain.entity.Trip;
import com.routbuddy.trips.dto.CreateTripRequest;
import com.routbuddy.trips.dto.TripDto;
import com.routbuddy.trips.dto.UpdateTelemetryRequest;

import java.util.List;
import java.util.UUID;

public interface TripService {
    TripDto createTrip(UUID userId, CreateTripRequest request);
    TripDto publishTrip(UUID userId, UUID tripId);
    TripDto startBoarding(UUID userId, UUID tripId);
    TripDto startTrip(UUID userId, UUID tripId);
    TripDto completeTrip(UUID userId, UUID tripId);
    TripDto cancelTrip(UUID userId, UUID tripId);
    TripDto updateTelemetry(UUID userId, UUID tripId, UpdateTelemetryRequest request);
    TripDto getTripById(UUID tripId);
    Trip getTripEntity(UUID tripId);
    List<TripDto> getAvailableTripsForRoute(UUID routeId);
    TripDto getActiveTripForDriver(UUID userId);
}
