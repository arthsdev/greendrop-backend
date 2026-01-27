package br.com.greendrop.backend.mapper.location;

import br.com.greendrop.backend.domain.model.UserLocation;
import br.com.greendrop.backend.dto.location.UserLocationResponseDTO;
import org.mapstruct.Mapper;

/**
 * Converts UserLocation entities into DTOs.
 * Uses MapStruct to generate mapping code at compile time.
 * Only maps to response DTOs; no reverse mapping needed.
 */
@Mapper(componentModel = "spring")
public interface UserLocationMapper {

    /**
     * Convert UserLocation entity to a response DTO.
     *
     * @param entity the UserLocation entity
     * @return a simple DTO with coordinates and timestamps
     */
    UserLocationResponseDTO toResponse(UserLocation entity);
}
