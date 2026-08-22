package com.routbuddy.trips.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateTelemetryRequest {

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double heading;
    private Double speedKmph;
    private Integer currentStopSequence;

    public UpdateTelemetryRequest() {}

    public UpdateTelemetryRequest(Double latitude, Double longitude, Double heading, Double speedKmph, Integer currentStopSequence) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.heading = heading;
        this.speedKmph = speedKmph;
        this.currentStopSequence = currentStopSequence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Double latitude;
        private Double longitude;
        private Double heading;
        private Double speedKmph;
        private Integer currentStopSequence;

        public Builder latitude(Double latitude) { this.latitude = latitude; return this; }
        public Builder longitude(Double longitude) { this.longitude = longitude; return this; }
        public Builder heading(Double heading) { this.heading = heading; return this; }
        public Builder speedKmph(Double speedKmph) { this.speedKmph = speedKmph; return this; }
        public Builder currentStopSequence(Integer currentStopSequence) { this.currentStopSequence = currentStopSequence; return this; }

        public UpdateTelemetryRequest build() {
            return new UpdateTelemetryRequest(latitude, longitude, heading, speedKmph, currentStopSequence);
        }
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }

    public Double getSpeedKmph() { return speedKmph; }
    public void setSpeedKmph(Double speedKmph) { this.speedKmph = speedKmph; }

    public Integer getCurrentStopSequence() { return currentStopSequence; }
    public void setCurrentStopSequence(Integer currentStopSequence) { this.currentStopSequence = currentStopSequence; }
}
