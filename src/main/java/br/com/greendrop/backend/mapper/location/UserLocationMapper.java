package br.com.greendrop.backend.mapper.location;

import br.com.greendrop.backend.domain.model.UserLocation;
import br.com.greendrop.backend.dto.location.UserLocationResponseDTO;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting {@link UserLocation} entities
 * to {@link UserLocationResponseDTO} DTOs.
 * Uses MapStruct for compile-time mapping generation.
 *
 * Note: All validation and business rules are handled in the {@link UserLocation}
 * entity itself. This mapper is only responsible for mapping to the response DTO.
 */
@Mapper(componentModel = "spring")
public interface UserLocationMapper {

    /**
     * Converts a {@link UserLocation} entity to a {@link UserLocationResponseDTO}.
     *
     * @param entity the user location entity
     * @return the corresponding response DTO
     */
    UserLocationResponseDTO toResponse(UserLocation entity);
}
