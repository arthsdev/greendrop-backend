package br.com.greendrop.backend.mapper.route;

import br.com.greendrop.backend.domain.model.Route;
import br.com.greendrop.backend.domain.model.RouteStop;
import br.com.greendrop.backend.dto.route.RouteResponseDTO;
import br.com.greendrop.backend.dto.route.RouteStopDTO;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct interface responsible for converting Route entities
 * and their children (RouteStop) into DTOs.
 *
 * The complex logic (sorting, nested conversions, business-specific formatting)
 * is delegated to RouteMapperDecorator.
 */
@Mapper(componentModel = "spring")
@DecoratedWith(RouteMapperDecorator.class)
public interface RouteMapper {

    /**
     * Converts a Route entity into its full response DTO.
     * Complex fields (like sorted stops) are handled by the decorator.
     */
    RouteResponseDTO toResponse(Route route);

    /**
     * Converts a RouteStop entity into its response DTO.
     */
    RouteStopDTO toStopResponse(RouteStop stop);

    /**
     * Converts a list of RouteStop entities into a list of DTOs.
     */
    List<RouteStopDTO> toStopResponseList(List<RouteStop> stops);
}
