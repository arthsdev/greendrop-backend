package br.com.greendrop.backend.domain.service.route.rules;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.RouteStop;
import br.com.greendrop.backend.domain.model.enums.RouteStatus;
import br.com.greendrop.backend.domain.model.enums.RouteStopStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RouteBusinessRulesService {

    /**
     * Apply transition when a stop was updated (e.g. route PLANNED -> IN_PROGRESS, or COMPLETED).
     * Returns true if route changed.
     */
    public boolean applyRouteStateTransitionsOnStopUpdate(Route route, List<RouteStop> stops) {
        boolean changed = false;

        if (route.getStatus() == RouteStatus.PLANNED) {
            route.setStatus(RouteStatus.IN_PROGRESS);
            changed = true;
        }

        boolean allDone = stops.stream().allMatch(s -> s.getStatus() == RouteStopStatus.DONE);
        if (allDone && route.getStatus() != RouteStatus.COMPLETED) {
            route.setStatus(RouteStatus.COMPLETED);
            changed = true;
        }
        return changed;
    }

    /**
     * Build a RouteStop (factory) ready to persist.
     */
    public RouteStop buildNewStop(Route route, java.util.UUID collectionRequestId, int order) {
        return RouteStop.builder()
                .id(java.util.UUID.randomUUID())
                .route(route)
                .collectionRequestId(collectionRequestId)
                .stopOrder(order)
                .status(RouteStopStatus.PENDING)
                .build();
    }

    /**
     * Update stop completion metadata consistently.
     */
    public void markStopDone(RouteStop stop) {
        stop.setStatus(RouteStopStatus.DONE);
        stop.setCompletedAt(Instant.now());
        stop.setFailureReason(null);
        stop.setFailureType(null);
    }

    public void markStopFailed(RouteStop stop, String failureType, String failureReason, String notes) {
        stop.setStatus(RouteStopStatus.FAILED);
        stop.setFailureType(failureType);
        stop.setFailureReason(failureReason);
        stop.setNotes(notes);
        stop.setCompletedAt(Instant.now());
    }

    public void markStopSkipped(RouteStop stop, String notes) {
        stop.setStatus(RouteStopStatus.SKIPPED);
        stop.setNotes(notes);
        stop.setCompletedAt(Instant.now());
    }
}