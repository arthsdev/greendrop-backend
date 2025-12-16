package br.com.greendrop.backend.mapper.route;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.RouteStop;
import br.com.greendrop.backend.dto.route.RouteResponseDTO;
import br.com.greendrop.backend.dto.route.RouteStopDTO;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * RouteMapper — MapStruct interface for Route and RouteStop.
 */
@Mapper(componentModel = "spring")
@DecoratedWith(RouteMapperDecorator.class)
public interface RouteMapper {

    @Mapping(target = "status", expression = "java(route.getStatus() != null ? route.getStatus().name() : null)")
    RouteResponseDTO toResponse(Route route);

    @Mapping(target = "status", expression = "java(stop.getStatus() != null ? stop.getStatus().name() : null)")
    RouteStopDTO toStopResponse(RouteStop stop);

    List<RouteStopDTO> toStopResponseList(List<RouteStop> stops);
}