package br.com.greendrop.backend.exception.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400
    VALIDATION_FAILED("VALIDATION_FAILED", "error.validation_failed", HttpStatus.BAD_REQUEST),
    MISSING_TOKEN("MISSING_TOKEN", "error.missing_token", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("BAD_REQUEST", "error.bad_request", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID("PASSWORD_INVALID", "error.password_invalid", HttpStatus.BAD_REQUEST),


    // 401
    INVALID_TOKEN("INVALID_TOKEN", "error.invalid_token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "error.token_expired", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "error.invalid_credentials", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("UNAUTHORIZED", "error.unauthorized", HttpStatus.UNAUTHORIZED),

    // 404
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "error.resource_not_found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("USER_NOT_FOUND", "error.user_not_found", HttpStatus.NOT_FOUND),


    // 409
    USER_EMAIL_ALREADY_EXISTS("USER_EMAIL_ALREADY_EXISTS", "error.user_email_already_exists", HttpStatus.CONFLICT),
    CONFLICT("CONFLICT", "error.conflict", HttpStatus.CONFLICT),

    // 500
    INTERNAL_ERROR("INTERNAL_ERROR", "error.internal_error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String messageKey;
    private final HttpStatus status;

    ErrorCode(String code, String messageKey, HttpStatus status) {
        this.code = code;
        this.messageKey = messageKey;
        this.status = status;
    }
}
