package br.com.greendrop.backend.exception.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ===============================
    // 400 — Bad Request
    // ===============================
    VALIDATION_FAILED("VALIDATION_FAILED", "error.validation_failed", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("BAD_REQUEST", "error.bad_request", HttpStatus.BAD_REQUEST),
    MISSING_TOKEN("MISSING_TOKEN", "error.missing_token", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID("PASSWORD_INVALID", "error.password_invalid", HttpStatus.BAD_REQUEST),
    INVALID_SECRET_KEY("INVALID_SECRET_KEY", "error.invalid_secret_key", HttpStatus.BAD_REQUEST),
    PRODUCT_ALREADY_ASSIGNED_TO_ROUTE("PRODUCT_ALREADY_ASSIGNED_TO_ROUTE",
            "error.product_already_assigned_to_route", HttpStatus.BAD_REQUEST),
    PRODUCT_ALREADY_CLAIMED("PRODUCT_ALREADY_CLAIMED", "error.product_already_claimed", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_AVAILABLE_FOR_CLAIM("PRODUCT_NOT_AVAILABLE_FOR_CLAIM",
            "error.product_not_available_for_claim", HttpStatus.BAD_REQUEST),


    // Route domain (400)
    ROUTE_START_COLLECTOR_REQUIRED("ROUTE_START_COLLECTOR_REQUIRED", "route.start.collector_required", HttpStatus.BAD_REQUEST),
    ROUTE_COMPLETE_NOT_ALL_DONE("ROUTE_COMPLETE_NOT_ALL_DONE", "route.complete.not_all_done", HttpStatus.BAD_REQUEST),
    ROUTE_INVALID_STOP_ORDER("ROUTE_INVALID_STOP_ORDER", "route.invalid_stop_order", HttpStatus.BAD_REQUEST),
    ROUTE_CANNOT_UPDATE_FINISHED("ROUTE_CANNOT_UPDATE_FINISHED", "route.cannot_update_finished", HttpStatus.BAD_REQUEST),
    ROUTE_ALREADY_STARTED("ROUTE_ALREADY_STARTED", "route.already_started", HttpStatus.BAD_REQUEST),
    ROUTE_ALREADY_FINISHED("ROUTE_ALREADY_FINISHED", "route.already_finished", HttpStatus.BAD_REQUEST),

    // Product domain (400)
    PRODUCT_IMAGE_INVALID("PRODUCT_IMAGE_INVALID", "product.image.invalid", HttpStatus.BAD_REQUEST),
    PRODUCT_CANNOT_DELETE("PRODUCT_CANNOT_DELETE", "product.cannot_delete", HttpStatus.BAD_REQUEST),
    PRODUCT_CANNOT_UPDATE("PRODUCT_CANNOT_UPDATE", "product.cannot_update", HttpStatus.BAD_REQUEST),

    // ===============================
    // 401 — Unauthorized
    // ===============================
    UNAUTHORIZED("UNAUTHORIZED", "error.unauthorized", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("INVALID_TOKEN", "error.invalid_token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "error.token_expired", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "error.invalid_credentials", HttpStatus.UNAUTHORIZED),

    // ===============================
    // 403 — Forbidden
    // ===============================
    FORBIDDEN("FORBIDDEN", "error.forbidden", HttpStatus.FORBIDDEN),
    USER_INACTIVE("USER_INACTIVE", "error.user_inactive", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_ACTION("UNAUTHORIZED_ACTION", "error.unauthorized_action", HttpStatus.FORBIDDEN),
    INVALID_CATEGORY_FOR_ROLE("INVALID_CATEGORY_FOR_ROLE", "invalid.category.for.role", HttpStatus.FORBIDDEN),

    // Domain-specific forbidden
    PRODUCT_FORBIDDEN("PRODUCT_FORBIDDEN", "product.forbidden", HttpStatus.FORBIDDEN),

    // ===============================
    // 404 — Not Found
    // ===============================
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "error.resource_not_found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("USER_NOT_FOUND", "error.user_not_found", HttpStatus.NOT_FOUND),
    ROUTE_NOT_FOUND("ROUTE_NOT_FOUND", "route.not_found", HttpStatus.NOT_FOUND),
    ROUTE_STOP_NOT_FOUND("ROUTE_STOP_NOT_FOUND", "route.stop.not_found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "product.not_found", HttpStatus.NOT_FOUND),

    // ===============================
    // 409 — Conflict
    // ===============================
    USER_EMAIL_ALREADY_EXISTS("USER_EMAIL_ALREADY_EXISTS", "error.user_email_already_exists", HttpStatus.CONFLICT),
    CONFLICT("CONFLICT", "error.conflict", HttpStatus.CONFLICT),

    // ===============================
    // 429 — Too Many Requests
    // ===============================
    USER_TOO_MANY_ATTEMPTS("USER_TOO_MANY_ATTEMPTS", "error.user_too_many_attempts", HttpStatus.TOO_MANY_REQUESTS),

    // ===============================
    // 500 — Internal Server Error
    // ===============================
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
