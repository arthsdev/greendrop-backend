package br.com.greendrop.backend.mapper;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for converting between User entities and DTOs.
 * MapStruct generates the implementation automatically at build time.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Converts a UserRequestDTO to a User entity.
     * The password will be handled (encoded) by the service layer before saving.
     */
    User toEntity(UserRequestDTO dto);

    /**
     * Converts a User entity to a UserResponseDTO.
     * The password is intentionally ignored.
     */
    UserResponseDTO toResponse(User user);
}
