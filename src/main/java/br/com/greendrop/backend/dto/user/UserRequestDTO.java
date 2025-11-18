package br.com.greendrop.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO used for user registration or authentication requests.
 * Contains only user-provided data (input layer).
 */
public record UserRequestDTO(

        @NotBlank(message = "Name cannot be blank")
        String name,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password cannot be blank")
        String password,

        String cep,        // Optional: ZIP/Postal code
        Double latitude,   // Optional: Geo coordinate
        Double longitude   // Optional: Geo coordinate
) {}
