package com.routbuddy.trips.dto;

import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.trips.domain.entity.Trip;

import java.time.Instant;
import java.util.UUID;

public class TripDto {
    private UUID id;
    private UUID driverId;
    private UUID vehicleId;
    private UUID routeId;
    private Instant scheduledDeparture;
    private Instant actualDeparture;
    private Instant actualArrival;
    private TripStatus status;
    private int totalSeats;
    private int availableSeats;
    private double farePerSeatInr;
    private Double liveLatitude;
    private Double liveLongitude;
    private Double liveHeading;
    private Double liveSpeedKmph;
    private Instant lastTelemetryPingAt;
    private int currentStopSequence;

    public TripDto() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID driverId;
        private UUID vehicleId;
        private UUID routeId;
        private Instant scheduledDeparture;
        private Instant actualDeparture;
        private Instant actualArrival;
        private TripStatus status;
        private int totalSeats;
        private int availableSeats;
        private double farePerSeatInr;
        private Double liveLatitude;
        private Double liveLongitude;
        private Double liveHeading;
        private Double liveSpeedKmph;
        private Instant lastTelemetryPingAt;
        private int currentStopSequence;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder driverId(UUID driverId) { this.driverId = driverId; return this; }
        public Builder vehicleId(UUID vehicleId) { this.vehicleId = vehicleId; return this; }
        public Builder routeId(UUID routeId) { this.routeId = routeId; return this; }
        public Builder scheduledDeparture(Instant scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; return this; }
        public Builder actualDeparture(Instant actualDeparture) { this.actualDeparture = actualDeparture; return this; }
        public Builder actualArrival(Instant actualArrival) { this.actualArrival = actualArrival; return this; }
        public Builder status(TripStatus status) { this.status = status; return this; }
        public Builder totalSeats(int totalSeats) { this.totalSeats = totalSeats; return this; }
        public Builder availableSeats(int availableSeats) { this.availableSeats = availableSeats; return this; }
        public Builder farePerSeatInr(double farePerSeatInr) { this.farePerSeatInr = farePerSeatInr; return this; }
        public Builder liveLatitude(Double liveLatitude) { this.liveLatitude = liveLatitude; return this; }
        public Builder liveLongitude(Double liveLongitude) { this.liveLongitude = liveLongitude; return this; }
        public Builder liveHeading(Double liveHeading) { this.liveHeading = liveHeading; return this; }
        public Builder liveSpeedKmph(Double liveSpeedKmph) { this.liveSpeedKmph = liveSpeedKmph; return this; }
        public Builder lastTelemetryPingAt(Instant lastTelemetryPingAt) { this.lastTelemetryPingAt = lastTelemetryPingAt; return this; }
        public Builder currentStopSequence(int currentStopSequence) { this.currentStopSequence = currentStopSequence; return this; }

        public TripDto build() {
            TripDto dto = new TripDto();
            dto.id = id;
            dto.driverId = driverId;
            dto.vehicleId = vehicleId;
            dto.routeId = routeId;
            dto.scheduledDeparture = scheduledDeparture;
            dto.actualDeparture = actualDeparture;
            dto.actualArrival = actualArrival;
            dto.status = status;
            dto.totalSeats = totalSeats;
            dto.availableSeats = availableSeats;
            dto.farePerSeatInr = farePerSeatInr;
            dto.liveLatitude = liveLatitude;
            dto.liveLongitude = liveLongitude;
            dto.liveHeading = liveHeading;
            dto.liveSpeedKmph = liveSpeedKmph;
            dto.lastTelemetryPingAt = lastTelemetryPingAt;
            dto.currentStopSequence = currentStopSequence;
            return dto;
        }
    }

    public static TripDto fromEntity(Trip trip) {
        return TripDto.builder()
                .id(trip.getId())
                .driverId(trip.getDriverId())
                .vehicleId(trip.getVehicleId())
                .routeId(trip.getRouteId())
                .scheduledDeparture(trip.getScheduledDeparture())
                .actualDeparture(trip.getActualDeparture())
                .actualArrival(trip.getActualArrival())
                .status(trip.getStatus())
                .totalSeats(trip.getTotalSeats())
                .availableSeats(trip.getAvailableSeats())
                .farePerSeatInr(trip.getFarePerSeatInr())
                .liveLatitude(trip.getLiveLatitude())
                .liveLongitude(trip.getLiveLongitude())
                .liveHeading(trip.getLiveHeading())
                .liveSpeedKmph(trip.getLiveSpeedKmph())
                .lastTelemetryPingAt(trip.getLastTelemetryPingAt())
                .currentStopSequence(trip.getCurrentStopSequence())
                .build();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }
    public Instant getScheduledDeparture() { return scheduledDeparture; }
    public void setScheduledDeparture(Instant scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }
    public Instant getActualDeparture() { return actualDeparture; }
    public void setActualDeparture(Instant actualDeparture) { this.actualDeparture = actualDeparture; }
    public Instant getActualArrival() { return actualArrival; }
    public void setActualArrival(Instant actualArrival) { this.actualArrival = actualArrival; }
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public double getFarePerSeatInr() { return farePerSeatInr; }
    public void setFarePerSeatInr(double farePerSeatInr) { this.farePerSeatInr = farePerSeatInr; }
    public Double getLiveLatitude() { return liveLatitude; }
    public void setLiveLatitude(Double liveLatitude) { this.liveLatitude = liveLatitude; }
    public Double getLiveLongitude() { return liveLongitude; }
    public void setLiveLongitude(Double liveLongitude) { this.liveLongitude = liveLongitude; }
    public Double getLiveHeading() { return liveHeading; }
    public void setLiveHeading(Double liveHeading) { this.liveHeading = liveHeading; }
    public Double getLiveSpeedKmph() { return liveSpeedKmph; }
    public void setLiveSpeedKmph(Double liveSpeedKmph) { this.liveSpeedKmph = liveSpeedKmph; }
    public Instant getLastTelemetryPingAt() { return lastTelemetryPingAt; }
    public void setLastTelemetryPingAt(Instant lastTelemetryPingAt) { this.lastTelemetryPingAt = lastTelemetryPingAt; }
    public int getCurrentStopSequence() { return currentStopSequence; }
    public void setCurrentStopSequence(int currentStopSequence) { this.currentStopSequence = currentStopSequence; }
}
