package br.com.greendrop.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload for updating user data.
 * Only contains fields that can be updated by the user.
 */
@Schema(description = "User update request payload.")
public record UserUpdateDTO(

        @Schema(description = "Updated full name of the user.", example = "Fabiano Augusto")
        @Size(min = 2, max = 100, message = "{user.name.size}")
        String name,

        @Schema(description = "Updated email of the user.", example = "fabiano@example.com")
        @Email(message = "{user.email.invalid}")
        String email,

        @Schema(description = "Updated postal code.", example = "37500-001")
        @Pattern(
                regexp = "\\d{5}-\\d{3}",
                message = "{user.cep.invalid}"
        )
        String cep,

        @Schema(description = "Updated latitude.", example = "-23.55052")
        Double latitude,

        @Schema(description = "Updated longitude.", example = "-46.633308")
        Double longitude
) {}
