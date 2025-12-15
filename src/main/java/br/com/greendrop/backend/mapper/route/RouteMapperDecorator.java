package br.com.greendrop.backend.mapper.route;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.RouteStop;
import br.com.greendrop.backend.dto.route.RouteResponseDTO;
import br.com.greendrop.backend.dto.route.RouteStopDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Decorator that extends the default MapStruct-generated implementation.
 *
 * This class is used for applying custom mapping logic such as:
 * - Sorting RouteStop elements
 * - Applying business-specific transformations
 * - Enriching DTOs beyond simple field mapping
 *
 * The delegate object is the original MapStruct mapper implementation.
 */
public class RouteMapperDecorator implements RouteMapper {

    private final RouteMapper delegate;

    public RouteMapperDecorator(RouteMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public RouteResponseDTO toResponse(Route route) {
        // First map the basic fields using MapStruct
        RouteResponseDTO base = delegate.toResponse(route);

        // Sorts the stops by their stopOrder and maps each using the delegate
        List<RouteStopDTO> sortedStops = route.getStops() == null ? List.of() :
                route.getStops().stream()
                        .sorted(Comparator.comparingInt(RouteStop::getStopOrder))
                        .map(delegate::toStopResponse)
                        .collect(Collectors.toList());

        // Creates a new immutable DTO using the record constructor
        return new RouteResponseDTO(
                base.id(),
                base.name(),
                base.routeDate(),
                base.collectorId(),
                base.status(),
                base.expectedStartTime(),
                base.expectedEndTime(),
                sortedStops
        );
    }

    @Override
    public RouteStopDTO toStopResponse(RouteStop stop) {
        // No custom logic here yet — simply delegate
        return delegate.toStopResponse(stop);
    }

    @Override
    public List<RouteStopDTO> toStopResponseList(List<RouteStop> stops) {
        // Delegate the list mapping
        return delegate.toStopResponseList(stops);
    }
}
