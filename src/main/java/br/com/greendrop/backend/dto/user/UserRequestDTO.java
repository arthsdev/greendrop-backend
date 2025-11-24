package br.com.greendrop.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for user registration. Contains only client-provided fields.
 */
@Schema(description = "User creation request payload.")
public record UserRequestDTO(

        @Schema(description = "Full name of the user.", example = "Fabiano Augusto")
        @NotBlank(message = "Name cannot be blank")
        String name,

        @Schema(description = "User email.", example = "fabiano@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "User password.", example = "StrongPass123!")
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @Schema(description = "Postal code (optional).", example = "37500-001")
        String cep,

        @Schema(description = "Latitude for geolocation (optional).", example = "-23.55052")
        Double latitude,

        @Schema(description = "Longitude for geolocation (optional).", example = "-46.633308")
        Double longitude
) {}