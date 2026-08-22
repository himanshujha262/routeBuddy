package com.routbuddy.routes.repository;

import com.routbuddy.routes.domain.entity.Route;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {

    List<Route> findByActiveTrue();

    @Query(value = """
        SELECT r.* FROM routes r
        WHERE r.is_active = true
        AND (
            ST_DWithin(r.origin_geom, :originPoint, :radiusMeters, false)
            OR ST_DWithin(r.destination_geom, :destPoint, :radiusMeters, false)
            OR (r.corridor_path IS NOT NULL AND ST_DWithin(r.corridor_path, :originPoint, :radiusMeters, false))
        )
        ORDER BY ST_Distance(r.origin_geom, :originPoint) ASC
    """, nativeQuery = true)
    List<Route> findRoutesNearPoints(
            @Param("originPoint") Point originPoint,
            @Param("destPoint") Point destPoint,
            @Param("radiusMeters") double radiusMeters);

    @Query(value = """
        SELECT r.* FROM routes r
        WHERE r.is_active = true
        AND EXISTS (
            SELECT 1 FROM route_stops rs1
            WHERE rs1.route_id = r.id AND ST_DWithin(rs1.stop_geom, :originPoint, :radiusMeters, false)
        )
        AND EXISTS (
            SELECT 1 FROM route_stops rs2
            WHERE rs2.route_id = r.id AND ST_DWithin(rs2.stop_geom, :destPoint, :radiusMeters, false)
        )
    """, nativeQuery = true)
    List<Route> findDirectCorridorRoutes(
            @Param("originPoint") Point originPoint,
            @Param("destPoint") Point destPoint,
            @Param("radiusMeters") double radiusMeters);
}
