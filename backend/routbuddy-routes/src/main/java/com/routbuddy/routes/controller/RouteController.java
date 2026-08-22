package com.routbuddy.routes.controller;

import com.routbuddy.common.dto.ApiResponse;
import com.routbuddy.routes.dto.CreateRouteRequest;
import com.routbuddy.routes.dto.CreateRouteStopRequest;
import com.routbuddy.routes.dto.RouteDto;
import com.routbuddy.routes.dto.RouteStopDto;
import com.routbuddy.routes.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routes")
@Tag(name = "Route & Stop Management", description = "Endpoints for route definition, corridor paths, stops, and pricing")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Create a new transit route with stops", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RouteDto>> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        RouteDto route = routeService.createRoute(request);
        return new ResponseEntity<>(ApiResponse.ok("Route created successfully", route), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all active transit routes")
    public ResponseEntity<ApiResponse<List<RouteDto>>> getAllActiveRoutes() {
        List<RouteDto> routes = routeService.getAllActiveRoutes();
        return ResponseEntity.ok(ApiResponse.ok(routes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get route details and sequential stops by ID")
    public ResponseEntity<ApiResponse<RouteDto>> getRouteById(@PathVariable UUID id) {
        RouteDto route = routeService.getRouteById(id);
        return ResponseEntity.ok(ApiResponse.ok(route));
    }

    @PostMapping("/{id}/stops")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Add a new stop to an existing route", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RouteStopDto>> addStop(
            @PathVariable UUID id,
            @Valid @RequestBody CreateRouteStopRequest request) {
        RouteStopDto stop = routeService.addStopToRoute(id, request);
        return new ResponseEntity<>(ApiResponse.ok("Stop added successfully", stop), HttpStatus.CREATED);
    }
}
