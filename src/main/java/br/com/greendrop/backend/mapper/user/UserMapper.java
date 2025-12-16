package br.com.greendrop.backend.mapper.user;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import br.com.greendrop.backend.dto.user.UserUpdateDTO;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct interface responsible for converting
 * between User entities and their DTOs.
 */
@Mapper(componentModel = "spring")
@DecoratedWith(UserMapperDecorator.class)
public interface UserMapper {

    // -----------------------------
    // Entity → Response DTO
    // -----------------------------
    @Mapping(target = "id", expression = "java(user.getId() != null ? user.getId().toString() : null)")
    UserResponseDTO toResponse(User user);

    // -----------------------------
    // Request DTO → Entity
    // -----------------------------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "points", ignore = true)
    User toEntity(UserRequestDTO dto);

    // -----------------------------
    // Update existing Entity
    // -----------------------------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "points", ignore = true)
    void updateEntity(UserUpdateDTO dto, @MappingTarget User user);
}