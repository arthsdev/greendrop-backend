package br.com.greendrop.backend.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload to create or update the current user's location")
public record UserLocationRequestDTO(

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        @Schema(description = "Latitude of the user", example = "-25.4242")
        Double latitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        @Schema(description = "Longitude of the user", example = "-45.4584")
        Double longitude
) {}

