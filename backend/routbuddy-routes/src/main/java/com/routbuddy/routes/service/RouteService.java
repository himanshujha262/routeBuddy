package com.routbuddy.routes.service;

import com.routbuddy.routes.domain.entity.Route;
import com.routbuddy.routes.dto.CreateRouteRequest;
import com.routbuddy.routes.dto.CreateRouteStopRequest;
import com.routbuddy.routes.dto.RouteDto;
import com.routbuddy.routes.dto.RouteStopDto;

import java.util.List;
import java.util.UUID;

public interface RouteService {
    RouteDto createRoute(CreateRouteRequest request);
    RouteStopDto addStopToRoute(UUID routeId, CreateRouteStopRequest request);
    List<RouteDto> getAllActiveRoutes();
    RouteDto getRouteById(UUID routeId);
    Route getRouteEntity(UUID routeId);
}
