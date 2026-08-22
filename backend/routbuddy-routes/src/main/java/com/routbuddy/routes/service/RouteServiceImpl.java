package com.routbuddy.routes.service;

import com.routbuddy.common.exception.ResourceNotFoundException;
import com.routbuddy.common.util.GeoUtils;
import com.routbuddy.routes.domain.entity.Route;
import com.routbuddy.routes.domain.entity.RouteStop;
import com.routbuddy.routes.dto.CreateRouteRequest;
import com.routbuddy.routes.dto.CreateRouteStopRequest;
import com.routbuddy.routes.dto.RouteDto;
import com.routbuddy.routes.dto.RouteStopDto;
import com.routbuddy.routes.repository.RouteRepository;
import com.routbuddy.routes.repository.RouteStopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RouteServiceImpl implements RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteServiceImpl.class);

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;

    public RouteServiceImpl(RouteRepository routeRepository, RouteStopRepository routeStopRepository) {
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
    }

    @Override
    @Transactional
    public RouteDto createRoute(CreateRouteRequest request) {
        Route route = Route.builder()
                .name(request.getName().trim())
                .originName(request.getOriginName().trim())
                .destinationName(request.getDestinationName().trim())
                .originGeom(GeoUtils.createPoint(request.getOriginLongitude(), request.getOriginLatitude()))
                .destinationGeom(GeoUtils.createPoint(request.getDestinationLongitude(), request.getDestinationLatitude()))
                .baseFareInr(request.getBaseFareInr())
                .totalDistanceKm(request.getTotalDistanceKm())
                .estimatedDurationMin(request.getEstimatedDurationMin())
                .active(true)
                .stops(new ArrayList<>())
                .build();

        if (request.getStops() != null && !request.getStops().isEmpty()) {
            for (CreateRouteStopRequest stopReq : request.getStops()) {
                RouteStop stop = RouteStop.builder()
                        .route(route)
                        .stopName(stopReq.getStopName().trim())
                        .sequenceOrder(stopReq.getSequenceOrder())
                        .stopGeom(GeoUtils.createPoint(stopReq.getLongitude(), stopReq.getLatitude()))
                        .distanceFromOriginKm(stopReq.getDistanceFromOriginKm())
                        .stageFareInr(stopReq.getStageFareInr())
                        .geofenceRadiusMeters(stopReq.getGeofenceRadiusMeters())
                        .build();
                route.addStop(stop);
            }
        }

        Route saved = routeRepository.save(route);
        log.info("Created route with id: {}, name: {} and {} stops", saved.getId(), saved.getName(), saved.getStops().size());
        return RouteDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public RouteStopDto addStopToRoute(UUID routeId, CreateRouteStopRequest request) {
        Route route = getRouteEntity(routeId);

        RouteStop stop = RouteStop.builder()
                .route(route)
                .stopName(request.getStopName().trim())
                .sequenceOrder(request.getSequenceOrder())
                .stopGeom(GeoUtils.createPoint(request.getLongitude(), request.getLatitude()))
                .distanceFromOriginKm(request.getDistanceFromOriginKm())
                .stageFareInr(request.getStageFareInr())
                .geofenceRadiusMeters(request.getGeofenceRadiusMeters())
                .build();

        RouteStop saved = routeStopRepository.save(stop);
        log.info("Added stop {} sequence {} to route {}", saved.getStopName(), saved.getSequenceOrder(), routeId);
        return RouteStopDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteDto> getAllActiveRoutes() {
        return routeRepository.findByActiveTrue().stream()
                .map(RouteDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RouteDto getRouteById(UUID routeId) {
        Route route = getRouteEntity(routeId);
        return RouteDto.fromEntity(route);
    }

    @Override
    @Transactional(readOnly = true)
    public Route getRouteEntity(UUID routeId) {
        return routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));
    }
}
