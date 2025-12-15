package br.com.greendrop.backend.mapper.route;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.RouteStop;
import br.com.greendrop.backend.dto.route.RouteResponseDTO;
import br.com.greendrop.backend.dto.route.RouteStopDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        List<RouteStopDTO> sortedStops = route.getStops() == null ? List.of() :
                route.getStops().stream()
                        .sorted(Comparator.comparingInt(RouteStop::getStopOrder))
                        .map(delegate::toStopResponse)
                        .collect(Collectors.toList());

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
        return delegate.toStopResponse(stop);
    }

    @Override
    public List<RouteStopDTO> toStopResponseList(List<RouteStop> stops) {
        return delegate.toStopResponseList(stops);
    }
}
