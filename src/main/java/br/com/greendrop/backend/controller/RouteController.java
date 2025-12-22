package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.model.enums.RouteStatus;
import br.com.greendrop.backend.domain.service.route.RouteService;
import br.com.greendrop.backend.dto.route.RouteCreateDTO;
import br.com.greendrop.backend.dto.route.RouteResponseDTO;
import br.com.greendrop.backend.dto.route.RouteStopDTO;
import br.com.greendrop.backend.dto.route.RouteStopUpdateDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Routes", description = "Endpoints for Routes management")
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    // ----------------------
    // CREATE ROUTE
    // ----------------------

    @Operation(summary = "Create a new route", description = "Creates a collection route. ADMIN only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Route created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid route data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - only admins can create routes", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<RouteResponseDTO> create(@RequestBody RouteCreateDTO dto) {
        var created = routeService.createRoute(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ----------------------
    // GET BY ID
    // ----------------------

    @Operation(summary = "Get a route by ID", description = "Admin can read any route. Collector can read only routes assigned to them.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - not your route", content = @Content),
            @ApiResponse(responseCode = "404", description = "Route not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponseDTO> get(@PathVariable UUID id) {
        return ResponseEntity.ok(routeService.getRoute(id));
    }

    // ----------------------
    // GET BY COLLECTOR + DATE
    // ----------------------

    @Operation(
            summary = "Get all routes assigned to a collector on a specific date",
            description = """
                    Admin: may read any collector's routes.
                    Collector: may only read their own.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routes returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - not your routes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Collector not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/collector/{collectorId}")
    public ResponseEntity<List<RouteResponseDTO>> getByCollectorAndDate(
            @PathVariable UUID collectorId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(routeService.getRoutesForCollectorOnDate(collectorId, date));
    }

    // ----------------------
    // UPDATE STOP
    // ----------------------

    @Operation(
            summary = "Update a route stop status",
            description = "Used by collectors (owner) or admin to mark a stop as done, failed, or skipped."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route stop updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid stop update data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed to update this stop", content = @Content),
            @ApiResponse(responseCode = "404", description = "Stop or route not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Stop already completed or locked", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PatchMapping("/stops")
    public ResponseEntity<RouteStopDTO> updateStop(@RequestBody RouteStopUpdateDTO dto) {
        return ResponseEntity.ok(routeService.updateRouteStop(dto));
    }

    // ----------------------
    // ASSIGN COLLECTOR
    // ----------------------

    @Operation(
            summary = "Assign a collector to a route",
            description = "Admin only. Used to bind a collector to an existing route."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Collector assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid collector or route ID", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin only", content = @Content),
            @ApiResponse(responseCode = "404", description = "Route or collector not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Route already assigned", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PatchMapping("/{id}/assign/{collectorId}")
    public ResponseEntity<Void> assignCollector(
            @PathVariable UUID id,
            @PathVariable UUID collectorId
    ) {
        routeService.assignCollector(id, collectorId);
        return ResponseEntity.noContent().build();
    }

    // ----------------------
    // CHANGE ROUTE STATUS
    // ----------------------

    @Operation(
            summary = "Change route status",
            description = "Admins or collectors (depending on business rules) can update the route status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - not allowed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Route not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Status transition not allowed", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<RouteResponseDTO> changeStatus(
            @PathVariable UUID id,
            @RequestParam RouteStatus status
    ) {
        return ResponseEntity.ok(routeService.changeStatus(id, status));
    }
}