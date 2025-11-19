package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.service.AuthService;
import br.com.greendrop.backend.dto.auth.AuthRequestDTO;
import br.com.greendrop.backend.dto.auth.AuthResponseDTO;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication, login, and token management.")
public class AuthController {

    private final AuthService authService;

    // ============================================================
    // REGISTER
    // ============================================================
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns access token + user details.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User registered successfully",
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
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "message": "Email already exists"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    // ============================================================
    // LOGIN
    // ============================================================
    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Validates user credentials and returns access token + user details.",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Login Request Example",
                                    value = """
                                            {
                                              "email": "user@example.com",
                                              "password": "123456"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login successful",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "status": 401,
                                                      "message": "Invalid email or password"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto.email(), dto.password()));
    }

    // ============================================================
    // REFRESH TOKEN
    // ============================================================
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Returns a new access token using a valid refresh token.",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    name = "Refresh Token Example",
                                    value = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Token refreshed successfully",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                                                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid or expired refresh token",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "status": 401,
                                                      "message": "Refresh token invalid or expired"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<String> refresh(@RequestBody String refreshToken) {
        return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
    }

    // ============================================================
    // LOGOUT
    // ============================================================
    @PostMapping("/logout/{userId}")
    @Operation(
            summary = "Logout user",
            description = "Deletes the user's refresh token from Redis and invalidates the session.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User logged out successfully")
            }
    )
    public ResponseEntity<Void> logout(@PathVariable String userId) {
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }
}
