package br.com.greendrop.backend.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiError", description = "Standard API error response")
public class ApiError {

    @Schema(example = "400")
    private final int status;

    @Schema(example = "VALIDATION_FAILED")
    private final String code;

    @Schema(example = "Validation failed")
    private final String message;

    @Schema(example = "/auth/register")
    private final String path;

    @Schema(example = "2025-01-01T12:00:00")
    private final LocalDateTime timestamp;

    @Schema(
            description = "Field validation errors",
            example = "{ \"email\": \"Email is invalid\" }"
    )
    private final Map<String, String> errors;
}
