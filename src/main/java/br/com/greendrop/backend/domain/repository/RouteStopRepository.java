package br.com.greendrop.backend.domain.repository;

import br.com.greendrop.backend.domain.model.RouteStop;
import br.com.greendrop.backend.domain.model.enums.RouteStopStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing {@link RouteStop} entities.
 * <p>
 * Provides methods to perform CRUD operations and custom queries
 * on stops associated with a route.
 * </p>
 */
public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {

    /**
     * Retrieves all RouteStop entities associated with a given route ID,
     * ordered by their stop order in ascending sequence.
     *
     * @param routeId the ID of the route
     * @return a list of RouteStop entities sorted by stop order
     */
    List<RouteStop> findByRoute_IdOrderByStopOrderAsc(UUID routeId);

    /**
     * Retrieves all RouteStop entities associated with a given route ID
     * and having a specific status.
     *
     * @param routeId the ID of the route
     * @param status  the status of the stops to filter
     * @return a list of RouteStop entities matching the route ID and status
     */
    List<RouteStop> findByRoute_IdAndStatus(UUID routeId, RouteStopStatus status);

}