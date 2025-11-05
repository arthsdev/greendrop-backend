package br.com.greendrop.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO for receiving user registration or update requests
public record UserRequestDTO(

        @NotBlank(message = "Name is mandatory")
        String name,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password is mandatory")
        @Size(min = 6, message = "Password must have at least 6 characters")
        String password,

        String cep,

        @NotBlank(message = "Role is mandatory")
        String role // USER or COLLECTOR
) {}
