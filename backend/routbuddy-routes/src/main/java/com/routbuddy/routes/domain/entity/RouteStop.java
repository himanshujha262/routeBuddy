package com.routbuddy.routes.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.routbuddy.common.domain.entity.BaseEntity;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "route_stops", indexes = {
        @Index(name = "idx_route_stops_route_id", columnList = "route_id"),
        @Index(name = "idx_route_stops_sequence", columnList = "route_id, sequence_order")
})
public class RouteStop extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @JsonIgnore
    private Route route;

    @Column(name = "stop_name", nullable = false, length = 100)
    private String stopName;

    @Column(name = "sequence_order", nullable = false)
    private int sequenceOrder;

    @Column(name = "stop_geom", nullable = false)
    private Point stopGeom;

    @Column(name = "distance_from_origin_km", nullable = false)
    private double distanceFromOriginKm;

    @Column(name = "stage_fare_inr", nullable = false)
    private double stageFareInr;

    @Column(name = "geofence_radius_meters", nullable = false)
    private int geofenceRadiusMeters = 50;

    public RouteStop() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Route route;
        private String stopName;
        private int sequenceOrder;
        private Point stopGeom;
        private double distanceFromOriginKm;
        private double stageFareInr;
        private int geofenceRadiusMeters = 50;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder route(Route route) { this.route = route; return this; }
        public Builder stopName(String stopName) { this.stopName = stopName; return this; }
        public Builder sequenceOrder(int sequenceOrder) { this.sequenceOrder = sequenceOrder; return this; }
        public Builder stopGeom(Point stopGeom) { this.stopGeom = stopGeom; return this; }
        public Builder distanceFromOriginKm(double distanceFromOriginKm) { this.distanceFromOriginKm = distanceFromOriginKm; return this; }
        public Builder stageFareInr(double stageFareInr) { this.stageFareInr = stageFareInr; return this; }
        public Builder geofenceRadiusMeters(int geofenceRadiusMeters) { this.geofenceRadiusMeters = geofenceRadiusMeters; return this; }

        public RouteStop build() {
            RouteStop rs = new RouteStop();
            if (id != null) rs.setId(id);
            rs.setRoute(route);
            rs.setStopName(stopName);
            rs.setSequenceOrder(sequenceOrder);
            rs.setStopGeom(stopGeom);
            rs.setDistanceFromOriginKm(distanceFromOriginKm);
            rs.setStageFareInr(stageFareInr);
            rs.setGeofenceRadiusMeters(geofenceRadiusMeters);
            return rs;
        }
    }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }

    public int getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(int sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public Point getStopGeom() { return stopGeom; }
    public void setStopGeom(Point stopGeom) { this.stopGeom = stopGeom; }

    public double getDistanceFromOriginKm() { return distanceFromOriginKm; }
    public void setDistanceFromOriginKm(double distanceFromOriginKm) { this.distanceFromOriginKm = distanceFromOriginKm; }

    public double getStageFareInr() { return stageFareInr; }
    public void setStageFareInr(double stageFareInr) { this.stageFareInr = stageFareInr; }

    public int getGeofenceRadiusMeters() { return geofenceRadiusMeters; }
    public void setGeofenceRadiusMeters(int geofenceRadiusMeters) { this.geofenceRadiusMeters = geofenceRadiusMeters; }
}
