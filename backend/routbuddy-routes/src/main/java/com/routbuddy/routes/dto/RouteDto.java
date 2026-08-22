package com.routbuddy.routes.dto;

import com.routbuddy.routes.domain.entity.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RouteDto {
    private UUID id;
    private String name;
    private String originName;
    private String destinationName;
    private double originLatitude;
    private double originLongitude;
    private double destinationLatitude;
    private double destinationLongitude;
    private double baseFareInr;
    private double totalDistanceKm;
    private int estimatedDurationMin;
    private boolean active;
    private List<RouteStopDto> stops = new ArrayList<>();

    public RouteDto() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String originName;
        private String destinationName;
        private double originLatitude;
        private double originLongitude;
        private double destinationLatitude;
        private double destinationLongitude;
        private double baseFareInr;
        private double totalDistanceKm;
        private int estimatedDurationMin;
        private boolean active;
        private List<RouteStopDto> stops = new ArrayList<>();

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder originName(String originName) { this.originName = originName; return this; }
        public Builder destinationName(String destinationName) { this.destinationName = destinationName; return this; }
        public Builder originLatitude(double originLatitude) { this.originLatitude = originLatitude; return this; }
        public Builder originLongitude(double originLongitude) { this.originLongitude = originLongitude; return this; }
        public Builder destinationLatitude(double destinationLatitude) { this.destinationLatitude = destinationLatitude; return this; }
        public Builder destinationLongitude(double destinationLongitude) { this.destinationLongitude = destinationLongitude; return this; }
        public Builder baseFareInr(double baseFareInr) { this.baseFareInr = baseFareInr; return this; }
        public Builder totalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; return this; }
        public Builder estimatedDurationMin(int estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder stops(List<RouteStopDto> stops) { this.stops = stops; return this; }

        public RouteDto build() {
            RouteDto dto = new RouteDto();
            dto.id = id;
            dto.name = name;
            dto.originName = originName;
            dto.destinationName = destinationName;
            dto.originLatitude = originLatitude;
            dto.originLongitude = originLongitude;
            dto.destinationLatitude = destinationLatitude;
            dto.destinationLongitude = destinationLongitude;
            dto.baseFareInr = baseFareInr;
            dto.totalDistanceKm = totalDistanceKm;
            dto.estimatedDurationMin = estimatedDurationMin;
            dto.active = active;
            dto.stops = stops != null ? stops : new ArrayList<>();
            return dto;
        }
    }

    public static RouteDto fromEntity(Route route) {
        List<RouteStopDto> stopDtos = route.getStops() != null
                ? route.getStops().stream().map(RouteStopDto::fromEntity).collect(Collectors.toList())
                : new ArrayList<>();

        return RouteDto.builder()
                .id(route.getId())
                .name(route.getName())
                .originName(route.getOriginName())
                .destinationName(route.getDestinationName())
                .originLatitude(route.getOriginGeom() != null ? route.getOriginGeom().getY() : 0.0)
                .originLongitude(route.getOriginGeom() != null ? route.getOriginGeom().getX() : 0.0)
                .destinationLatitude(route.getDestinationGeom() != null ? route.getDestinationGeom().getY() : 0.0)
                .destinationLongitude(route.getDestinationGeom() != null ? route.getDestinationGeom().getX() : 0.0)
                .baseFareInr(route.getBaseFareInr())
                .totalDistanceKm(route.getTotalDistanceKm())
                .estimatedDurationMin(route.getEstimatedDurationMin())
                .active(route.isActive())
                .stops(stopDtos)
                .build();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOriginName() { return originName; }
    public void setOriginName(String originName) { this.originName = originName; }
    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }
    public double getOriginLatitude() { return originLatitude; }
    public void setOriginLatitude(double originLatitude) { this.originLatitude = originLatitude; }
    public double getOriginLongitude() { return originLongitude; }
    public void setOriginLongitude(double originLongitude) { this.originLongitude = originLongitude; }
    public double getDestinationLatitude() { return destinationLatitude; }
    public void setDestinationLatitude(double destinationLatitude) { this.destinationLatitude = destinationLatitude; }
    public double getDestinationLongitude() { return destinationLongitude; }
    public void setDestinationLongitude(double destinationLongitude) { this.destinationLongitude = destinationLongitude; }
    public double getBaseFareInr() { return baseFareInr; }
    public void setBaseFareInr(double baseFareInr) { this.baseFareInr = baseFareInr; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
    public int getEstimatedDurationMin() { return estimatedDurationMin; }
    public void setEstimatedDurationMin(int estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<RouteStopDto> getStops() { return stops; }
    public void setStops(List<RouteStopDto> stops) { this.stops = stops; }
}
