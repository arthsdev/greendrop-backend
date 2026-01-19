package br.com.greendrop.backend.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "Response with the user's current location")
public record UserLocationResponseDTO(

        @Schema(description = "Latitude of the user", example = "-25.4242")
        @NotNull
        Double latitude,

        @Schema(description = "Longitude of the user", example = "-45.4584")
        @NotNull
        Double longitude,

        @Schema(description = "Timestamp of creation", example = "2026-01-19T17:33:10.771143Z")
        Instant createdAt,

        @Schema(description = "Timestamp of last update", example = "2026-01-19T17:33:10.771143Z")
        Instant updatedAt
) {
}

