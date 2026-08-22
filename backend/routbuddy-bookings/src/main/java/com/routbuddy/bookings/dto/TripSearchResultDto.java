package com.routbuddy.bookings.dto;

import com.routbuddy.common.domain.enums.TripStatus;
import com.routbuddy.common.domain.enums.VehicleType;

import java.time.Instant;
import java.util.UUID;

public class TripSearchResultDto {

    private UUID tripId;
    private UUID routeId;
    private String routeName;
    private UUID driverId;
    private String vehiclePlateNumber;
    private VehicleType vehicleType;
    private Instant scheduledDeparture;
    private TripStatus status;
    private int totalSeats;
    private int availableSeats;
    private double farePerSeatInr;
    private Double liveLatitude;
    private Double liveLongitude;
    private Double liveSpeedKmph;
    private Integer currentStopSequence;

    public TripSearchResultDto() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID tripId;
        private UUID routeId;
        private String routeName;
        private UUID driverId;
        private String vehiclePlateNumber;
        private VehicleType vehicleType;
        private Instant scheduledDeparture;
        private TripStatus status;
        private int totalSeats;
        private int availableSeats;
        private double farePerSeatInr;
        private Double liveLatitude;
        private Double liveLongitude;
        private Double liveSpeedKmph;
        private Integer currentStopSequence;

        public Builder tripId(UUID tripId) { this.tripId = tripId; return this; }
        public Builder routeId(UUID routeId) { this.routeId = routeId; return this; }
        public Builder routeName(String routeName) { this.routeName = routeName; return this; }
        public Builder driverId(UUID driverId) { this.driverId = driverId; return this; }
        public Builder vehiclePlateNumber(String vehiclePlateNumber) { this.vehiclePlateNumber = vehiclePlateNumber; return this; }
        public Builder vehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; return this; }
        public Builder scheduledDeparture(Instant scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; return this; }
        public Builder status(TripStatus status) { this.status = status; return this; }
        public Builder totalSeats(int totalSeats) { this.totalSeats = totalSeats; return this; }
        public Builder availableSeats(int availableSeats) { this.availableSeats = availableSeats; return this; }
        public Builder farePerSeatInr(double farePerSeatInr) { this.farePerSeatInr = farePerSeatInr; return this; }
        public Builder liveLatitude(Double liveLatitude) { this.liveLatitude = liveLatitude; return this; }
        public Builder liveLongitude(Double liveLongitude) { this.liveLongitude = liveLongitude; return this; }
        public Builder liveSpeedKmph(Double liveSpeedKmph) { this.liveSpeedKmph = liveSpeedKmph; return this; }
        public Builder currentStopSequence(Integer currentStopSequence) { this.currentStopSequence = currentStopSequence; return this; }

        public TripSearchResultDto build() {
            TripSearchResultDto dto = new TripSearchResultDto();
            dto.tripId = tripId;
            dto.routeId = routeId;
            dto.routeName = routeName;
            dto.driverId = driverId;
            dto.vehiclePlateNumber = vehiclePlateNumber;
            dto.vehicleType = vehicleType;
            dto.scheduledDeparture = scheduledDeparture;
            dto.status = status;
            dto.totalSeats = totalSeats;
            dto.availableSeats = availableSeats;
            dto.farePerSeatInr = farePerSeatInr;
            dto.liveLatitude = liveLatitude;
            dto.liveLongitude = liveLongitude;
            dto.liveSpeedKmph = liveSpeedKmph;
            dto.currentStopSequence = currentStopSequence;
            return dto;
        }
    }

    public UUID getTripId() { return tripId; }
    public void setTripId(UUID tripId) { this.tripId = tripId; }
    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    public String getVehiclePlateNumber() { return vehiclePlateNumber; }
    public void setVehiclePlateNumber(String vehiclePlateNumber) { this.vehiclePlateNumber = vehiclePlateNumber; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public Instant getScheduledDeparture() { return scheduledDeparture; }
    public void setScheduledDeparture(Instant scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }
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
    public Double getLiveSpeedKmph() { return liveSpeedKmph; }
    public void setLiveSpeedKmph(Double liveSpeedKmph) { this.liveSpeedKmph = liveSpeedKmph; }
    public Integer getCurrentStopSequence() { return currentStopSequence; }
    public void setCurrentStopSequence(Integer currentStopSequence) { this.currentStopSequence = currentStopSequence; }
}
