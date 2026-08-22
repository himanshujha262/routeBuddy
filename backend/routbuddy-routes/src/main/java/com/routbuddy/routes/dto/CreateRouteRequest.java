package com.routbuddy.routes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

public class CreateRouteRequest {

    @NotBlank(message = "Route name is required")
    private String name;

    @NotBlank(message = "Origin name is required")
    private String originName;

    @NotBlank(message = "Destination name is required")
    private String destinationName;

    @NotNull(message = "Origin latitude is required")
    private Double originLatitude;

    @NotNull(message = "Origin longitude is required")
    private Double originLongitude;

    @NotNull(message = "Destination latitude is required")
    private Double destinationLatitude;

    @NotNull(message = "Destination longitude is required")
    private Double destinationLongitude;

    @NotNull(message = "Base fare is required")
    @Positive(message = "Base fare must be positive")
    private Double baseFareInr;

    @NotNull(message = "Total distance is required")
    @Positive(message = "Total distance must be positive")
    private Double totalDistanceKm;

    @NotNull(message = "Estimated duration is required")
    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDurationMin;

    @Valid
    private List<CreateRouteStopRequest> stops = new ArrayList<>();

    public CreateRouteRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String originName;
        private String destinationName;
        private Double originLatitude;
        private Double originLongitude;
        private Double destinationLatitude;
        private Double destinationLongitude;
        private Double baseFareInr;
        private Double totalDistanceKm;
        private Integer estimatedDurationMin;
        private List<CreateRouteStopRequest> stops = new ArrayList<>();

        public Builder name(String name) { this.name = name; return this; }
        public Builder originName(String originName) { this.originName = originName; return this; }
        public Builder destinationName(String destinationName) { this.destinationName = destinationName; return this; }
        public Builder originLatitude(Double originLatitude) { this.originLatitude = originLatitude; return this; }
        public Builder originLongitude(Double originLongitude) { this.originLongitude = originLongitude; return this; }
        public Builder destinationLatitude(Double destinationLatitude) { this.destinationLatitude = destinationLatitude; return this; }
        public Builder destinationLongitude(Double destinationLongitude) { this.destinationLongitude = destinationLongitude; return this; }
        public Builder baseFareInr(Double baseFareInr) { this.baseFareInr = baseFareInr; return this; }
        public Builder totalDistanceKm(Double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; return this; }
        public Builder estimatedDurationMin(Integer estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; return this; }
        public Builder stops(List<CreateRouteStopRequest> stops) { this.stops = stops; return this; }

        public CreateRouteRequest build() {
            CreateRouteRequest req = new CreateRouteRequest();
            req.name = name;
            req.originName = originName;
            req.destinationName = destinationName;
            req.originLatitude = originLatitude;
            req.originLongitude = originLongitude;
            req.destinationLatitude = destinationLatitude;
            req.destinationLongitude = destinationLongitude;
            req.baseFareInr = baseFareInr;
            req.totalDistanceKm = totalDistanceKm;
            req.estimatedDurationMin = estimatedDurationMin;
            req.stops = stops != null ? stops : new ArrayList<>();
            return req;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOriginName() { return originName; }
    public void setOriginName(String originName) { this.originName = originName; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public Double getOriginLatitude() { return originLatitude; }
    public void setOriginLatitude(Double originLatitude) { this.originLatitude = originLatitude; }

    public Double getOriginLongitude() { return originLongitude; }
    public void setOriginLongitude(Double originLongitude) { this.originLongitude = originLongitude; }

    public Double getDestinationLatitude() { return destinationLatitude; }
    public void setDestinationLatitude(Double destinationLatitude) { this.destinationLatitude = destinationLatitude; }

    public Double getDestinationLongitude() { return destinationLongitude; }
    public void setDestinationLongitude(Double destinationLongitude) { this.destinationLongitude = destinationLongitude; }

    public Double getBaseFareInr() { return baseFareInr; }
    public void setBaseFareInr(Double baseFareInr) { this.baseFareInr = baseFareInr; }

    public Double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(Double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public Integer getEstimatedDurationMin() { return estimatedDurationMin; }
    public void setEstimatedDurationMin(Integer estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; }

    public List<CreateRouteStopRequest> getStops() { return stops; }
    public void setStops(List<CreateRouteStopRequest> stops) { this.stops = stops; }
}
