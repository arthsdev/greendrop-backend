package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.service.AuthService;
import br.com.greendrop.backend.dto.auth.AuthRequestDTO;
import br.com.greendrop.backend.dto.auth.AuthResponseDTO;
import br.com.greendrop.backend.dto.auth.RefreshTokenRequestDTO;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations:
 * - User registration
 * - Login
 * - Refresh token
 * - Logout
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and token management endpoints")
public class AuthController {

    private final AuthService authService;

    // ---------------------------
    // REGISTER
    // ---------------------------
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns access + refresh tokens. "
                    + "Password must be at least 8 characters, with uppercase, lowercase, number, and special character.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully registered",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                      "user": {
                                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                                        "name": "Fabiano Augusto",
                                                        "email": "fabiano@example.com",
                                                        "role": "USER",
                                                        "cep": "37500-000",
                                                        "latitude": -22.4242,
                                                        "longitude": -45.4584,
                                                        "points": 0
                                                      }
                                                    }
                                                    """
                                    )
                            )),
                    @ApiResponse(responseCode = "409", description = "Email already exists"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    // ---------------------------
    // LOGIN
    // ---------------------------
    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates user and returns access + refresh tokens",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponseDTO.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "429", description = "Too many login attempts")
            }
    )
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto.email(), dto.password()));
    }

    // ---------------------------
    // REFRESH TOKEN
    // ---------------------------
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token. "
                    + "The old refresh token is invalidated and a new one is issued.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "New access token issued",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Missing or invalid token")
            }
    )
    public ResponseEntity<String> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authService.refreshAccessToken(request.refreshToken()));
    }

    // ---------------------------
    // LOGOUT
    // ---------------------------
    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Revokes and blacklists the provided refresh token. Idempotent operation.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User logged out successfully"),
                    @ApiResponse(responseCode = "400", description = "Missing or invalid token")
            }
    )
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
