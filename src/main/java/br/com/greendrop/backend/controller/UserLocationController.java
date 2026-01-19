package br.com.greendrop.backend.controller;

import br.com.greendrop.backend.domain.service.location.UserLocationService;
import br.com.greendrop.backend.dto.location.UserLocationRequestDTO;
import br.com.greendrop.backend.dto.location.UserLocationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/location")
@Tag(
        name = "Location",
        description = "Endpoints related to the authenticated user's location"
)
public class UserLocationController {

    private final UserLocationService userLocationService;

    // ===============================================================
// CREATE OR UPDATE LOCATION
// ===============================================================
    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Create or update current user location",
            description = "Creates or updates the authenticated user's current location"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Location created or updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{ \"latitude\": -25.4242, \"longitude\": -45.4584, " +
                                            "\"createdAt\": \"2026-01-19T17:33:10.771143Z\", " +
                                            "\"updatedAt\": \"2026-01-19T17:33:10.771143Z\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or malformed JSON",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Latitude",
                                            value = "{ \"code\": \"USER_LOCATION_INVALID_LATITUDE\", " +
                                                    "\"message\": \"Latitude inválida\" }"
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Longitude",
                                            value = "{ \"code\": \"USER_LOCATION_INVALID_LONGITUDE\", " +
                                                    "\"message\": \"Longitude inválida\" }"
                                    ),
                                    @ExampleObject(
                                            name = "Malformed JSON",
                                            value = "{ \"code\": \"MALFORMED_JSON\", " +
                                                    "\"message\": \"Malformed JSON or missing request body\" }"
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<UserLocationResponseDTO> upsertMyLocation(
            @Valid @RequestBody UserLocationRequestDTO dto
    ) {
        UserLocationResponseDTO response =
                userLocationService.upsertMyLocation(dto);

        return ResponseEntity.ok(response);
    }

    // ===============================================================
    // GET CURRENT USER LOCATION
    // ===============================================================
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get current user location",
            description = "Returns the authenticated user's current location, if it exists"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Location found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{ \"latitude\": -25.4242, \"longitude\": -45.4584, " +
                                            "\"createdAt\": \"2026-01-19T17:33:10.771143Z\", " +
                                            "\"updatedAt\": \"2026-01-19T17:33:10.771143Z\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "User has no location registered",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed request",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Malformed JSON",
                                    value = "{ \"code\": \"MALFORMED_JSON\", " +
                                            "\"message\": \"Malformed JSON or missing request body\" }"
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<UserLocationResponseDTO> getMyLocation() {
        return userLocationService.getMyLocation()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

}
