package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.model.User;
import br.com.greendrop.backend.domain.service.UserService;
import br.com.greendrop.backend.dto.user.PasswordUpdateDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import br.com.greendrop.backend.dto.user.UserUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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
                    @ApiResponse(responseCode = "403", description = "Access denied")
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
                                                      "longitude": -45.4584",
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
    public UserResponseDTO getMe(@AuthenticationPrincipal User user) {
        return userService.getLogged(user.getId());
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
    public UserResponseDTO updateMe(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserUpdateDTO dto
    ) {
        return userService.updateUserProfile(user.getId(), dto);
    }

    // ===============================================================
    // UPDATE PASSWORD
    // ===============================================================
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER','COLLECTOR','ADMIN')")
    @Operation(
            summary = "Update password for logged user",
            description = "Changes the password of the currently authenticated user. Password must be at least 8 characters, with uppercase, lowercase, number, and special character.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Password updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid old password or new password does not meet criteria"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "429", description = "Too many password change attempts, try later")
            }
    )
    public void updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PasswordUpdateDTO dto
    ) {
        userService.updatePassword(user.getId(), dto.oldPassword(), dto.newPassword());
        // TODO: Implement rate limiting for password update attempts
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
