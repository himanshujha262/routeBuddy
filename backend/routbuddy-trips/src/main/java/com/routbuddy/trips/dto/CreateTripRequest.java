package com.routbuddy.trips.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public class CreateTripRequest {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    @NotNull(message = "Route ID is required")
    private UUID routeId;

    @NotNull(message = "Scheduled departure is required")
    private Instant scheduledDeparture;

    @NotNull(message = "Total seats is required")
    @Positive(message = "Total seats must be positive")
    private Integer totalSeats;

    @NotNull(message = "Fare per seat is required")
    @Positive(message = "Fare per seat must be positive")
    private Double farePerSeatInr;

    public CreateTripRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID vehicleId;
        private UUID routeId;
        private Instant scheduledDeparture;
        private Integer totalSeats;
        private Double farePerSeatInr;

        public Builder vehicleId(UUID vehicleId) { this.vehicleId = vehicleId; return this; }
        public Builder routeId(UUID routeId) { this.routeId = routeId; return this; }
        public Builder scheduledDeparture(Instant scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; return this; }
        public Builder totalSeats(Integer totalSeats) { this.totalSeats = totalSeats; return this; }
        public Builder farePerSeatInr(Double farePerSeatInr) { this.farePerSeatInr = farePerSeatInr; return this; }

        public CreateTripRequest build() {
            CreateTripRequest req = new CreateTripRequest();
            req.vehicleId = vehicleId;
            req.routeId = routeId;
            req.scheduledDeparture = scheduledDeparture;
            req.totalSeats = totalSeats;
            req.farePerSeatInr = farePerSeatInr;
            return req;
        }
    }

    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }

    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }

    public Instant getScheduledDeparture() { return scheduledDeparture; }
    public void setScheduledDeparture(Instant scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public Double getFarePerSeatInr() { return farePerSeatInr; }
    public void setFarePerSeatInr(Double farePerSeatInr) { this.farePerSeatInr = farePerSeatInr; }
}
