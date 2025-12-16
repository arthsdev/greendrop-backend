package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.service.AuthService;
import br.com.greendrop.backend.dto.auth.AuthRequestDTO;
import br.com.greendrop.backend.dto.auth.AuthResponseDTO;
import br.com.greendrop.backend.dto.auth.AuthTokens;
import br.com.greendrop.backend.dto.user.UserRequestDTO;
import br.com.greendrop.backend.dto.user.UserResponseDTO;
import br.com.greendrop.backend.exception.global.ErrorCode;
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
            description = "Creates a new user account. No tokens are issued during registration; the user must log in afterwards.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = UserResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                        {
                                          "id": "c12fa458-5523-4cd3-b805-aa88c2ed921a",
                                          "name": "Fabiano Augusto",
                                          "email": "fabiano@example.com",
                                          "role": "USER",
                                          "cep": "37500-001",
                                          "latitude": -23.55052,
                                          "longitude": -46.633308,
                                          "points": 120
                                        }
                                        """)
                            )),
                    @ApiResponse(responseCode = "409", description = "Email already exists",
                            content = @Content(schema = @Schema(implementation = ErrorCode.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(schema = @Schema(implementation = ErrorCode.class)))
            }
    )
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO user = authService.register(dto);
        return ResponseEntity.status(201).body(user);
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