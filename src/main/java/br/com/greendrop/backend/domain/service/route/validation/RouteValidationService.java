package br.com.greendrop.backend.domain.service.route.validation;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.RouteStop;
import br.com.greendrop.backend.domain.model.enums.RouteStatus;
import br.com.greendrop.backend.domain.model.enums.RouteStopStatus;
import br.com.greendrop.backend.exception.global.ErrorCode;
import br.com.greendrop.backend.exception.generic.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class RouteValidationService {

    /**
     * Validate basic business preconditions for creating a route.
     */
    public void validateCreateDtoNonBusiness(String name, Object routeDate, List<?> stops) {
        if (routeDate == null) throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.create.date_required");
        if (name == null || name.isBlank()) throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.create.name_required");
        if (stops == null || stops.isEmpty()) throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.create.no_stops");
    }

    /**
     * Validate that route can be updated (not completed/cancelled).
     */
    public void validateRouteEditable(Route route) {
        if (route.getStatus() == RouteStatus.COMPLETED || route.getStatus() == RouteStatus.CANCELLED) {
            throw new BadRequestException(ErrorCode.ROUTE_CANNOT_UPDATE_FINISHED);
        }
    }

    /**
     * Validate we can start a route (collector set, has stops).
     */
    public void validateStartPossible(Route route, List<RouteStop> stops) {
        if (route.getCollectorId() == null) throw new BadRequestException(ErrorCode.ROUTE_START_COLLECTOR_REQUIRED);
        if (stops == null || stops.isEmpty()) throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.start.no_stops");
    }

    /**
     * Validate finishing condition: all stops must be DONE.
     */
    public void validateCompletePossible(List<RouteStop> stops) {
        boolean allDone = (stops == null) || stops.stream().allMatch(s -> s.getStatus() == RouteStopStatus.DONE);
        if (!allDone) throw new BadRequestException(ErrorCode.ROUTE_COMPLETE_NOT_ALL_DONE);
    }

    /**
     * Check that provided new stop order matches existing stops (IDs).
     */
    public void validateReorderMatches(List<RouteStop> existingStops, List<java.util.UUID> requestOrder) {
        if (requestOrder == null || requestOrder.isEmpty()) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.reorder.empty");
        }
        var existingIds = existingStops.stream().map(RouteStop::getId).toList();
        if (!existingIds.containsAll(requestOrder) || requestOrder.size() != existingIds.size()) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.reorder.mismatch");
        }
    }

    /**
     * Validate stop position for insert.
     */
    public void validateInsertPosition(int position, int size) {
        if (position < 0 || position > size) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "route.insert.invalid_position");
        }
    }
}