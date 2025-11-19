package br.com.greendrop.backend.dto.user;

import br.com.greendrop.backend.domain.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Sanitized user data returned to the client. Never exposes sensitive fields.
 */
@Schema(description = "User response payload.")
public record UserResponseDTO(

        @Schema(description = "User ID.", example = "c12fa458-5523-4cd3-b805-aa88c2ed921a")
        String id,

        @Schema(description = "Full name of the user.", example = "Fabiano Augusto")
        String name,

        @Schema(description = "User email.", example = "fabiano@example.com")
        String email,

        @Schema(description = "User role.", example = "USER")
        Role role,

        @Schema(description = "Postal code.", example = "37500-001")
        String cep,

        @Schema(description = "Latitude.", example = "-23.55052")
        Double latitude,

        @Schema(description = "Longitude.", example = "-46.633308")
        Double longitude,

        @Schema(description = "User eco-points.", example = "120")
        Integer points
) {}
