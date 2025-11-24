package br.com.greendrop.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for updating a user's password.
 * Requires the current password for verification and the new password.
 */
@Schema(description = "Password update request payload.")
public record PasswordUpdateDTO(

        @Schema(description = "Current password of the user.", example = "OldPass123!")
        @NotBlank(message = "Current password cannot be blank")
        String oldPassword,

        @Schema(description = "New password to set.", example = "NewStrongPass456!")
        @NotBlank(message = "New password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String newPassword
) {}
