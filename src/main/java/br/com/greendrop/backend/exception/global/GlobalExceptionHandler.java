package br.com.greendrop.backend.exception.global;

import br.com.greendrop.backend.exception.model.ApiError;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // =====================================================================
    // ➤ Handle @Valid validation exceptions (DTO validation errors)
    // =====================================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     ServletWebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String message = messageSource.getMessage(error, Locale.getDefault());
            fieldErrors.put(error.getField(), message);
        });

        ApiError apiError = ApiError.builder()
                .status(ErrorCode.VALIDATION_FAILED.getStatus())
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message(getMessage(ErrorCode.VALIDATION_FAILED))
                .path(request.getRequest().getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();

        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    // =====================================================================
    // ➤ Handle custom domain exceptions (BaseException)
    // =====================================================================
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiError> handleBaseException(BaseException ex,
                                                        ServletWebRequest request) {

        ErrorCode code = ex.getErrorCode();

        ApiError apiError = ApiError.builder()
                .status(code.getStatus())
                .code(code.getCode())
                .message(getMessage(code)) // resolved through messages.properties
                .path(request.getRequest().getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(code.getStatus()).body(apiError);
    }

    // =====================================================================
    // ➤ Handle any unhandled exception (fallback)
    // =====================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex,
                                                  ServletWebRequest request) {

        ex.printStackTrace(); // register the error in logs

        ApiError apiError = ApiError.builder()
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .code(ErrorCode.INTERNAL_ERROR.getCode())
                .message(getMessage(ErrorCode.INTERNAL_ERROR))
                .path(request.getRequest().getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    // =====================================================================
    // ➤ Helper method for message resolution
    // =====================================================================
    private String getMessage(ErrorCode errorCode) {
        return messageSource.getMessage(
                errorCode.getMessageKey(),
                null,
                Locale.getDefault()
        );
    }
}
