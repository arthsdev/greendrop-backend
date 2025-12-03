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
    ROUTE_START_COLLECTOR_REQUIRED("ROUTE_START_COLLECTOR_REQUIRED", "route.start.collector_required", HttpStatus.BAD_REQUEST),
    ROUTE_COMPLETE_NOT_ALL_DONE("ROUTE_COMPLETE_NOT_ALL_DONE", "route.complete.not_all_done", HttpStatus.BAD_REQUEST),
    ROUTE_INVALID_STOP_ORDER("ROUTE_INVALID_STOP_ORDER", "route.invalid_stop_order", HttpStatus.BAD_REQUEST),
    ROUTE_CANNOT_UPDATE_FINISHED("ROUTE_CANNOT_UPDATE_FINISHED", "route.cannot_update_finished", HttpStatus.BAD_REQUEST),
    ROUTE_ALREADY_STARTED("ROUTE_ALREADY_STARTED", "route.already_started", HttpStatus.BAD_REQUEST),
    ROUTE_ALREADY_FINISHED("ROUTE_ALREADY_FINISHED", "route.already_finished", HttpStatus.BAD_REQUEST),


    // 401
    INVALID_TOKEN("INVALID_TOKEN", "error.invalid_token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "error.token_expired", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "error.invalid_credentials", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("UNAUTHORIZED", "error.unauthorized", HttpStatus.UNAUTHORIZED),
    USER_INACTIVE("USER_INACTIVE", "error.user_inactive", HttpStatus.UNAUTHORIZED),
    INVALID_SECRET_KEY("INVALID_SECRET_KEY", "error.invalid_secret_key", HttpStatus.BAD_REQUEST),

    // 404
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "error.resource_not_found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("USER_NOT_FOUND", "error.user_not_found", HttpStatus.NOT_FOUND),
    ROUTE_NOT_FOUND("ROUTE_NOT_FOUND", "route.not_found", HttpStatus.NOT_FOUND),
    ROUTE_STOP_NOT_FOUND("ROUTE_STOP_NOT_FOUND", "route.stop.not_found", HttpStatus.NOT_FOUND),


    // 409
    USER_EMAIL_ALREADY_EXISTS("USER_EMAIL_ALREADY_EXISTS", "error.user_email_already_exists", HttpStatus.CONFLICT),
    CONFLICT("CONFLICT", "error.conflict", HttpStatus.CONFLICT),

    // 429
    USER_TOO_MANY_ATTEMPTS("USER_TOO_MANY_ATTEMPTS", "error.user_too_many_attempts", HttpStatus.TOO_MANY_REQUESTS),


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
