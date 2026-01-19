package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.service.UserService;
import br.com.greendrop.backend.domain.service.location.UserLocationService;
import br.com.greendrop.backend.dto.location.UserLocationRequestDTO;
import br.com.greendrop.backend.dto.location.UserLocationResponseDTO;
import br.com.greendrop.backend.dto.user.PasswordUpdateDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import br.com.greendrop.backend.dto.user.UserUpdateDTO;
import br.com.greendrop.backend.infrastructure.security.service.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "User management endpoints")
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final UserLocationService userLocationService;


    // ===============================================================
    // LIST ALL USERS (ADMIN)
    // ===============================================================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all users (ADMIN)",
            description = "Returns paginated list of all users for administrators",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of users returned",
                            content = @Content(schema = @Schema(implementation = UserResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    [
                                                      {
                                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                                        "name": "Fabiano Augusto",
                                                        "email": "fabiano@example.com",
                                                        "role": "USER",
                                                        "cep": "37500-000",
                                                        "latitude": -22.4242,
                                                        "longitude": -45.4584,
                                                        "points": 100
                                                      }
                                                    ]
                                                    """
                                    ))),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "204", description = "Location not found")
            }
    )
    public Page<UserResponseDTO> listAll(Pageable pageable) {
        return userService.findAll(pageable);
    }

    // ===============================================================
    // GET USER BY ID (ADMIN)
    // ===============================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Find user by ID (ADMIN)",
            description = "Returns user details by UUID. Admin only.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found",
                            content = @Content(schema = @Schema(implementation = UserResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "id": "123e4567-e89b-12d3-a456-426614174000",
                                                      "name": "Fabiano Augusto",
                                                      "email": "fabiano@example.com",
                                                      "role": "USER",
                                                      "cep": "37500-000",
                                                      "latitude": -22.4242,
                                                      "longitude": -45.4584,
                                                      "points": 100
                                                    }
                                                    """
                                    ))),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public UserResponseDTO findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    // ===============================================================
    // GET LOGGED USER PROFILE
    // ===============================================================
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','COLLECTOR','ADMIN')")
    @Operation(
            summary = "Get logged user profile",
            description = "Returns the profile of the currently authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User profile returned",
                            content = @Content(schema = @Schema(implementation = UserResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "id": "123e4567-e89b-12d3-a456-426614174000",
                                                      "name": "Fabiano Augusto",
                                                      "email": "fabiano@example.com",
                                                      "role": "USER",
                                                      "cep": "37500-000",
                                                      "latitude": -22.4242,
                                                      "longitude": -45.4584,
                                                      "points": 100
                                                    }
                                                    """
                                    ))),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public UserResponseDTO getMe() {
        return userService.getLoggedUser();
    }

    // ===============================================================
    // UPDATE LOGGED USER PROFILE
    // ===============================================================
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER','COLLECTOR','ADMIN')")
    @Operation(
            summary = "Update logged user profile",
            description = "Updates the currently authenticated user's profile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User profile updated",
                            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public UserResponseDTO updateMe(@Valid @RequestBody UserUpdateDTO dto) {
        return userService.updateUserProfile(dto);
    }

    // ===============================================================
    // UPDATE PASSWORD
    // ===============================================================
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER','COLLECTOR','ADMIN')")
    @Operation(
            summary = "Update password for logged user",
            description = "Changes the password of the currently authenticated user. Password must meet security criteria.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Password updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid old password or weak new password"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "429", description = "Too many password change attempts")
            }
    )
    public void updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(dto.oldPassword(), dto.newPassword());
    }

    // ===============================================================
    // DELETE USER (ADMIN)
    // ===============================================================
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete user (ADMIN)",
            description = "Deletes a user by UUID. Admin only.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}