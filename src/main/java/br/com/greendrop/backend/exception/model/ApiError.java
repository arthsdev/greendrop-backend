package br.com.greendrop.backend.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    // Validation errors → field → message
    private final Map<String, String> errors;
}
