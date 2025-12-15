package br.com.greendrop.backend.domain.service;

import br.com.greendrop.backend.domain.model.enums.RouteStatus;
import br.com.greendrop.backend.dto.route.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RouteService {
    RouteResponseDTO createRoute(RouteCreateDTO dto);
    RouteResponseDTO getRoute(UUID id);
    List<RouteResponseDTO> getRoutesForCollectorOnDate(UUID collectorId, LocalDate date);
    RouteStopDTO updateRouteStop(RouteStopUpdateDTO dto);
    void assignCollector(UUID routeId, UUID collectorId);
    RouteResponseDTO changeStatus(UUID routeId, RouteStatus newStatus);

    // admin helpers
    void addStopToRoute(UUID routeId, UUID collectionRequestId, Integer position);
    void removeStop(UUID stopId);
    void reorderStops(UUID routeId, List<UUID> stopIdsInOrder);
}
