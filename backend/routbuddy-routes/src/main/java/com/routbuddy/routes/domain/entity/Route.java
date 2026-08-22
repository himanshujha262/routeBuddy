package com.routbuddy.routes.domain.entity;

import com.routbuddy.common.domain.entity.BaseEntity;
import jakarta.persistence.*;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routes", indexes = {
        @Index(name = "idx_routes_name", columnList = "name"),
        @Index(name = "idx_routes_is_active", columnList = "is_active")
})
public class Route extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "origin_name", nullable = false, length = 100)
    private String originName;

    @Column(name = "destination_name", nullable = false, length = 100)
    private String destinationName;

    @Column(name = "origin_geom", nullable = false)
    private Point originGeom;

    @Column(name = "destination_geom", nullable = false)
    private Point destinationGeom;

    @Column(name = "corridor_path")
    private LineString corridorPath;

    @Column(name = "base_fare_inr", nullable = false)
    private double baseFareInr;

    @Column(name = "total_distance_km", nullable = false)
    private double totalDistanceKm;

    @Column(name = "estimated_duration_min", nullable = false)
    private int estimatedDurationMin;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<RouteStop> stops = new ArrayList<>();

    public Route() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String originName;
        private String destinationName;
        private Point originGeom;
        private Point destinationGeom;
        private LineString corridorPath;
        private double baseFareInr;
        private double totalDistanceKm;
        private int estimatedDurationMin;
        private boolean active = true;
        private List<RouteStop> stops = new ArrayList<>();

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder originName(String originName) { this.originName = originName; return this; }
        public Builder destinationName(String destinationName) { this.destinationName = destinationName; return this; }
        public Builder originGeom(Point originGeom) { this.originGeom = originGeom; return this; }
        public Builder destinationGeom(Point destinationGeom) { this.destinationGeom = destinationGeom; return this; }
        public Builder corridorPath(LineString corridorPath) { this.corridorPath = corridorPath; return this; }
        public Builder baseFareInr(double baseFareInr) { this.baseFareInr = baseFareInr; return this; }
        public Builder totalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; return this; }
        public Builder estimatedDurationMin(int estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder stops(List<RouteStop> stops) { this.stops = stops != null ? stops : new ArrayList<>(); return this; }

        public Route build() {
            Route r = new Route();
            if (id != null) r.setId(id);
            r.setName(name);
            r.setOriginName(originName);
            r.setDestinationName(destinationName);
            r.setOriginGeom(originGeom);
            r.setDestinationGeom(destinationGeom);
            r.setCorridorPath(corridorPath);
            r.setBaseFareInr(baseFareInr);
            r.setTotalDistanceKm(totalDistanceKm);
            r.setEstimatedDurationMin(estimatedDurationMin);
            r.setActive(active);
            r.setStops(stops != null ? stops : new ArrayList<>());
            return r;
        }
    }

    public void addStop(RouteStop stop) {
        if (this.stops == null) {
            this.stops = new ArrayList<>();
        }
        stop.setRoute(this);
        this.stops.add(stop);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOriginName() { return originName; }
    public void setOriginName(String originName) { this.originName = originName; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public Point getOriginGeom() { return originGeom; }
    public void setOriginGeom(Point originGeom) { this.originGeom = originGeom; }

    public Point getDestinationGeom() { return destinationGeom; }
    public void setDestinationGeom(Point destinationGeom) { this.destinationGeom = destinationGeom; }

    public LineString getCorridorPath() { return corridorPath; }
    public void setCorridorPath(LineString corridorPath) { this.corridorPath = corridorPath; }

    public double getBaseFareInr() { return baseFareInr; }
    public void setBaseFareInr(double baseFareInr) { this.baseFareInr = baseFareInr; }

    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public int getEstimatedDurationMin() { return estimatedDurationMin; }
    public void setEstimatedDurationMin(int estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<RouteStop> getStops() { return stops; }
    public void setStops(List<RouteStop> stops) { this.stops = stops; }
}
