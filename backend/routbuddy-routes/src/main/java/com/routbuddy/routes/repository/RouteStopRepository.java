package com.routbuddy.routes.repository;

import com.routbuddy.routes.domain.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {
    List<RouteStop> findByRouteIdOrderBySequenceOrderAsc(UUID routeId);
}
