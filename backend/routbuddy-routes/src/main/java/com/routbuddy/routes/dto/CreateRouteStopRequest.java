package com.routbuddy.routes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateRouteStopRequest {

    @NotBlank(message = "Stop name is required")
    private String stopName;

    @NotNull(message = "Sequence order is required")
    @Min(value = 1, message = "Sequence order must be at least 1")
    private Integer sequenceOrder;

    @NotNull(message = "Stop latitude is required")
    private Double latitude;

    @NotNull(message = "Stop longitude is required")
    private Double longitude;

    private double distanceFromOriginKm = 0.0;
    private double stageFareInr = 0.0;
    private int geofenceRadiusMeters = 50;

    public CreateRouteStopRequest() {}

    public CreateRouteStopRequest(String stopName, Integer sequenceOrder, Double latitude, Double longitude, double distanceFromOriginKm, double stageFareInr, int geofenceRadiusMeters) {
        this.stopName = stopName;
        this.sequenceOrder = sequenceOrder;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceFromOriginKm = distanceFromOriginKm;
        this.stageFareInr = stageFareInr;
        this.geofenceRadiusMeters = geofenceRadiusMeters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String stopName;
        private Integer sequenceOrder;
        private Double latitude;
        private Double longitude;
        private double distanceFromOriginKm = 0.0;
        private double stageFareInr = 0.0;
        private int geofenceRadiusMeters = 50;

        public Builder stopName(String stopName) { this.stopName = stopName; return this; }
        public Builder sequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; return this; }
        public Builder latitude(Double latitude) { this.latitude = latitude; return this; }
        public Builder longitude(Double longitude) { this.longitude = longitude; return this; }
        public Builder distanceFromOriginKm(double distanceFromOriginKm) { this.distanceFromOriginKm = distanceFromOriginKm; return this; }
        public Builder stageFareInr(double stageFareInr) { this.stageFareInr = stageFareInr; return this; }
        public Builder geofenceRadiusMeters(int geofenceRadiusMeters) { this.geofenceRadiusMeters = geofenceRadiusMeters; return this; }

        public CreateRouteStopRequest build() {
            return new CreateRouteStopRequest(stopName, sequenceOrder, latitude, longitude, distanceFromOriginKm, stageFareInr, geofenceRadiusMeters);
        }
    }

    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public double getDistanceFromOriginKm() { return distanceFromOriginKm; }
    public void setDistanceFromOriginKm(double distanceFromOriginKm) { this.distanceFromOriginKm = distanceFromOriginKm; }

    public double getStageFareInr() { return stageFareInr; }
    public void setStageFareInr(double stageFareInr) { this.stageFareInr = stageFareInr; }

    public int getGeofenceRadiusMeters() { return geofenceRadiusMeters; }
    public void setGeofenceRadiusMeters(int geofenceRadiusMeters) { this.geofenceRadiusMeters = geofenceRadiusMeters; }
}
