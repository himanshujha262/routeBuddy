package com.routbuddy.routes.dto;

import com.routbuddy.routes.domain.entity.RouteStop;

import java.util.UUID;

public class RouteStopDto {
    private UUID id;
    private String stopName;
    private int sequenceOrder;
    private double latitude;
    private double longitude;
    private double distanceFromOriginKm;
    private double stageFareInr;
    private int geofenceRadiusMeters;

    public RouteStopDto() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String stopName;
        private int sequenceOrder;
        private double latitude;
        private double longitude;
        private double distanceFromOriginKm;
        private double stageFareInr;
        private int geofenceRadiusMeters;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder stopName(String stopName) { this.stopName = stopName; return this; }
        public Builder sequenceOrder(int sequenceOrder) { this.sequenceOrder = sequenceOrder; return this; }
        public Builder latitude(double latitude) { this.latitude = latitude; return this; }
        public Builder longitude(double longitude) { this.longitude = longitude; return this; }
        public Builder distanceFromOriginKm(double distanceFromOriginKm) { this.distanceFromOriginKm = distanceFromOriginKm; return this; }
        public Builder stageFareInr(double stageFareInr) { this.stageFareInr = stageFareInr; return this; }
        public Builder geofenceRadiusMeters(int geofenceRadiusMeters) { this.geofenceRadiusMeters = geofenceRadiusMeters; return this; }

        public RouteStopDto build() {
            RouteStopDto dto = new RouteStopDto();
            dto.id = id;
            dto.stopName = stopName;
            dto.sequenceOrder = sequenceOrder;
            dto.latitude = latitude;
            dto.longitude = longitude;
            dto.distanceFromOriginKm = distanceFromOriginKm;
            dto.stageFareInr = stageFareInr;
            dto.geofenceRadiusMeters = geofenceRadiusMeters;
            return dto;
        }
    }

    public static RouteStopDto fromEntity(RouteStop stop) {
        return RouteStopDto.builder()
                .id(stop.getId())
                .stopName(stop.getStopName())
                .sequenceOrder(stop.getSequenceOrder())
                .latitude(stop.getStopGeom() != null ? stop.getStopGeom().getY() : 0.0)
                .longitude(stop.getStopGeom() != null ? stop.getStopGeom().getX() : 0.0)
                .distanceFromOriginKm(stop.getDistanceFromOriginKm())
                .stageFareInr(stop.getStageFareInr())
                .geofenceRadiusMeters(stop.getGeofenceRadiusMeters())
                .build();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }
    public int getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(int sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getDistanceFromOriginKm() { return distanceFromOriginKm; }
    public void setDistanceFromOriginKm(double distanceFromOriginKm) { this.distanceFromOriginKm = distanceFromOriginKm; }
    public double getStageFareInr() { return stageFareInr; }
    public void setStageFareInr(double stageFareInr) { this.stageFareInr = stageFareInr; }
    public int getGeofenceRadiusMeters() { return geofenceRadiusMeters; }
    public void setGeofenceRadiusMeters(int geofenceRadiusMeters) { this.geofenceRadiusMeters = geofenceRadiusMeters; }
}
