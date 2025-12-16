package br.com.greendrop.backend.exception.global;

import br.com.greendrop.backend.exception.model.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // =====================================================================
    //  400 — DTO Validation Errors (@Valid)
    // =====================================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     ServletWebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String message = messageSource.getMessage(
                    error,
                    LocaleContextHolder.getLocale()
            );
            fieldErrors.put(error.getField(), message);
        });

        ApiError apiError = buildError(
                ErrorCode.VALIDATION_FAILED,
                request
        ).toBuilder()
                .errors(fieldErrors)
                .build();

        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    // =====================================================================
    //  401 — Authentication failure (Spring Security)
    // =====================================================================
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex,
                                                         ServletWebRequest request) {

        log.warn("Authentication failed: {}", ex.getMessage());

        ApiError apiError = buildError(
                ErrorCode.UNAUTHORIZED,
                request
        );

        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    // =====================================================================
    //  403 — Access denied (Roles / Permissions)
    // =====================================================================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex,
                                                       ServletWebRequest request) {

        log.warn("Access denied: {}", ex.getMessage());

        ApiError apiError = buildError(
                ErrorCode.FORBIDDEN,
                request
        );

        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    // =====================================================================
    //  4xx / 5xx — Domain exceptions (BaseException)
    // =====================================================================
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiError> handleBaseException(BaseException ex,
                                                        ServletWebRequest request) {

        ErrorCode errorCode = ex.getErrorCode();

        log.warn("Domain exception [{}]: {}", errorCode.getCode(), ex.getMessage());

        ApiError apiError = buildError(
                errorCode,
                request
        );

        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    // =====================================================================
    //  500 — Unhandled / unexpected exception
    // =====================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex,
                                                  ServletWebRequest request) {

        log.error("Unhandled exception", ex);

        ApiError apiError = buildError(
                ErrorCode.INTERNAL_ERROR,
                request
        );

        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    private ApiError buildError(ErrorCode errorCode, ServletWebRequest request) {
        return ApiError.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(resolveMessage(errorCode))
                .path(request.getRequest().getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String resolveMessage(ErrorCode errorCode) {
        return messageSource.getMessage(
                errorCode.getMessageKey(),
                null,
                errorCode.getCode(), // fallback
                LocaleContextHolder.getLocale()
        );
    }

}
