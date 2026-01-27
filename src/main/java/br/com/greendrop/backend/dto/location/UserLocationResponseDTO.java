package br.com.greendrop.backend.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Schema(description = "Response DTO for a user's location")
public record UserLocationResponseDTO(

        @Schema(description = "Latitude of the user", example = "-25.4242")
        @NotNull
        Double latitude,

        @Schema(description = "Longitude of the user", example = "-45.4584")
        @NotNull
        Double longitude,

        @Schema(description = "When this location was created", example = "2026-01-19T17:33:10.771143Z")
        Instant createdAt,

        @Schema(description = "When this location was last updated", example = "2026-01-19T17:33:10.771143Z")
        Instant updatedAt
) {}
