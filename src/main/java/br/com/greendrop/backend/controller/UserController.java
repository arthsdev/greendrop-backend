package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.service.UserService;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(
        name = "Users",
        description = "Endpoints for user registration, profile management, and administrative operations."
)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ---------------------------------------------------------------------
    //  Create User (Public)
    // ---------------------------------------------------------------------
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. Public endpoint – no authentication required.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User successfully created.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponseDTO.class),
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
                                                      "points": 0
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation error."),
                    @ApiResponse(responseCode = "409", description = "Email already registered.")
            }
    )
    public UserResponseDTO register(@Valid @RequestBody UserRequestDTO dto) {
        return userService.create(dto);
    }

    // ---------------------------------------------------------------------
    //  List All Users (ADMIN)
    // ---------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all users (ADMIN)",
            description = "Returns a paginated list of registered users. Restricted to ADMIN users.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated list returned.",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)),
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
                                                        "points": 0
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Forbidden – insufficient permissions.")
            }
    )
    public Page<UserResponseDTO> listAll(Pageable pageable) {
        return userService.findAll(pageable);
    }

    // ---------------------------------------------------------------------
    //  Find User By ID (Authenticated)
    // ---------------------------------------------------------------------
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'COLLECTOR', 'ADMIN')")
    @Operation(
            summary = "Get user by ID",
            description = "Returns the details of a user based on ID. Requires authentication.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User found.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponseDTO.class),
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
                                                      "points": 0
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Forbidden."),
                    @ApiResponse(responseCode = "404", description = "User not found.")
            }
    )
    public UserResponseDTO findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    // ---------------------------------------------------------------------
    //  Delete User (ADMIN)
    // ---------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete user by ID (ADMIN)",
            description = "Removes a user from the system. Only ADMIN can delete users.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully."),
                    @ApiResponse(responseCode = "403", description = "Forbidden – insufficient permissions."),
                    @ApiResponse(responseCode = "404", description = "User not found.")
            }
    )
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
