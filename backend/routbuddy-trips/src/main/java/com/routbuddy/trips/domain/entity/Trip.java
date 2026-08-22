package com.routbuddy.trips.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import com.routbuddy.common.domain.enums.TripStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trips_driver_id", columnList = "driver_id"),
        @Index(name = "idx_trips_route_id", columnList = "route_id"),
        @Index(name = "idx_trips_status", columnList = "status"),
        @Index(name = "idx_trips_scheduled_departure", columnList = "scheduled_departure")
})
public class Trip extends BaseEntity {

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    @Column(name = "scheduled_departure", nullable = false)
    private Instant scheduledDeparture;

    @Column(name = "actual_departure")
    private Instant actualDeparture;

    @Column(name = "actual_arrival")
    private Instant actualArrival;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TripStatus status = TripStatus.SCHEDULED;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @Column(name = "fare_per_seat_inr", nullable = false)
    private double farePerSeatInr;

    @Column(name = "live_latitude")
    private Double liveLatitude;

    @Column(name = "live_longitude")
    private Double liveLongitude;

    @Column(name = "live_heading")
    private Double liveHeading;

    @Column(name = "live_speed_kmph")
    private Double liveSpeedKmph;

    @Column(name = "last_telemetry_ping_at")
    private Instant lastTelemetryPingAt;

    @Column(name = "current_stop_sequence")
    private int currentStopSequence = 0;

    public Trip() {}

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
        private TripStatus status = TripStatus.SCHEDULED;
        private int totalSeats;
        private int availableSeats;
        private double farePerSeatInr;
        private Double liveLatitude;
        private Double liveLongitude;
        private Double liveHeading;
        private Double liveSpeedKmph;
        private Instant lastTelemetryPingAt;
        private int currentStopSequence = 0;

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

        public Trip build() {
            Trip t = new Trip();
            if (id != null) t.setId(id);
            t.setDriverId(driverId);
            t.setVehicleId(vehicleId);
            t.setRouteId(routeId);
            t.setScheduledDeparture(scheduledDeparture);
            t.setActualDeparture(actualDeparture);
            t.setActualArrival(actualArrival);
            t.setStatus(status != null ? status : TripStatus.SCHEDULED);
            t.setTotalSeats(totalSeats);
            t.setAvailableSeats(availableSeats);
            t.setFarePerSeatInr(farePerSeatInr);
            t.setLiveLatitude(liveLatitude);
            t.setLiveLongitude(liveLongitude);
            t.setLiveHeading(liveHeading);
            t.setLiveSpeedKmph(liveSpeedKmph);
            t.setLastTelemetryPingAt(lastTelemetryPingAt);
            t.setCurrentStopSequence(currentStopSequence);
            return t;
        }
    }

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
