package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.service.AuthService;
import br.com.greendrop.backend.dto.auth.AuthRequestDTO;
import br.com.greendrop.backend.dto.auth.AuthResponseDTO;
import br.com.greendrop.backend.dto.auth.AuthTokens;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and token lifecycle management")
public class AuthController {

    private final AuthService authService;
    private static final String REFRESH_COOKIE = "refreshToken";
    private static final String COOKIE_PATH = "/api/auth";

    // ========================================================================
    // REGISTER
    // ========================================================================
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a user account, returns access token and sets refresh token in a secure HttpOnly cookie",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = AuthResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                                              "expiresIn": 3600,
                                              "user": {
                                                "id": "uuid",
                                                "name": "Fabiano",
                                                "email": "fabiano@example.com",
                                                "role": "USER"
                                              }
                                            }
                                            """)
                            )),
                    @ApiResponse(responseCode = "409", description = "Email already exists"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UserRequestDTO dto) {
        AuthTokens tokens = authService.register(dto);
        ResponseCookie cookie = buildRefreshCookie(tokens.refreshToken());
        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponseDTO(tokens.accessToken(), tokens.expiresIn(), tokens.user()));
    }

    // ========================================================================
    // LOGIN
    // ========================================================================
    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Validates email and password, returns access token and sets refresh token in HttpOnly cookie",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                            content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "429", description = "Too many login attempts")
            }
    )
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO dto) {
        AuthTokens tokens = authService.login(dto.email(), dto.password());
        ResponseCookie cookie = buildRefreshCookie(tokens.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponseDTO(tokens.accessToken(), tokens.expiresIn(), tokens.user()));
    }

    // ========================================================================
    // REFRESH TOKEN
    // ========================================================================
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Validates refresh token in HttpOnly cookie, rotates it and returns new access token with new refresh cookie",
            responses = {
                    @ApiResponse(responseCode = "200", description = "New access token issued",
                            content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Missing refresh token cookie"),
                    @ApiResponse(responseCode = "401", description = "Invalid refresh token")
            }
    )
    public ResponseEntity<AuthResponseDTO> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshTokenCookie
    ) {
        if (refreshTokenCookie == null) return ResponseEntity.badRequest().build();
        AuthTokens tokens = authService.refresh(refreshTokenCookie);
        ResponseCookie cookie = buildRefreshCookie(tokens.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponseDTO(tokens.accessToken(), tokens.expiresIn(), tokens.user()));
    }

    // ========================================================================
    // LOGOUT
    // ========================================================================
    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Invalidates the refresh token and clears the HttpOnly cookie",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Logged out successfully")
            }
    )
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshTokenCookie
    ) {
        if (refreshTokenCookie != null) {
            authService.logout(refreshTokenCookie);
        }
        ResponseCookie clear = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clear.toString())
                .build();
    }

    // ========================================================================
    // PRIVATE HELPER - BUILD COOKIE
    // ========================================================================
    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(COOKIE_PATH)
                .maxAge(7 * 24 * 3600) // 7 days
                .build();
    }
}
