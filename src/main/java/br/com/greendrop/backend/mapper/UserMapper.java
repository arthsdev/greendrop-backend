package br.com.greendrop.backend.mapper;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Handles conversion between User entities and DTOs using MapStruct.
 * - Converts incoming request data to User entities.
 * - Converts persisted User entities to response DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    //  Entity → Response DTO

    /**
     * Converts a User entity into a UserResponseDTO.
     * Explicitly maps all fields for type safety and removes warnings.
     */
    @Mapping(target = "id", expression = "java(user.getId() != null ? user.getId().toString() : null)")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "cep", source = "cep")
    @Mapping(target = "latitude", source = "latitude")
    @Mapping(target = "longitude", source = "longitude")
    @Mapping(target = "points", source = "points")
    UserResponseDTO toResponse(User user);


    //Request DTO → Entity


    /**
     * Converts a UserRequestDTO into a User entity.
     * - ID is ignored (generated automatically).
     * - Role is set later in AuthService.
     * - Password is encoded manually before saving.
     * - Points use Lombok's @Builder.Default (handled automatically).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "cep", source = "cep")
    @Mapping(target = "latitude", source = "latitude")
    @Mapping(target = "longitude", source = "longitude")
    @Mapping(target = "points", ignore = true)
    User toEntity(UserRequestDTO dto);
}
