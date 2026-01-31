package br.com.greendrop.backend.mapper.route;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.RouteStop;
import br.com.greendrop.backend.dto.route.RouteResponseDTO;
import br.com.greendrop.backend.dto.route.RouteStopDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Decorates RouteMapper to apply presentation-specific logic.
 *
 * Responsibilities:
 * - Sort route stops by stopOrder before returning the response
 * - Map stops only if they are already loaded (no lazy loading here)
 * - Never trigger database access or fix fetch problems (N+1 is handled in the repository)
 *
 * This decorator assumes that the service layer is responsible for
 * fetching routes with the correct relationships eagerly loaded.
 */
@Component
public abstract class RouteMapperDecorator implements RouteMapper {

    protected RouteMapper delegate;

    @Autowired
    public void setDelegate(RouteMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public RouteResponseDTO toResponse(Route route) {
        RouteResponseDTO base = delegate.toResponse(route);

        List<RouteStopDTO> stops = mapStopsSafely(route);

        return new RouteResponseDTO(
                base.id(),
                base.name(),
                base.routeDate(),
                base.collectorId(),
                base.status(),
                base.expectedStartTime(),
                base.expectedEndTime(),
                stops
        );
    }

    /**
     * Maps route stops only if they are present.
     * This method must not trigger lazy loading or database queries.
     */
    private List<RouteStopDTO> mapStopsSafely(Route route) {
        if (route.getStops() == null || route.getStops().isEmpty()) {
            return List.of();
        }

        return route.getStops().stream()
                .sorted(Comparator.comparingInt(RouteStop::getStopOrder))
                .map(delegate::toStopResponse)
                .toList();
    }
}
