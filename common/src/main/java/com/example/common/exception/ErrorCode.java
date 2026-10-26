package com.example.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Error codes for the common module (A=1).
 *
 * <p>Code format {@code A BB CC}: {@code A} is the module (1 digit), {@code BB} is the error
 * category (2 digits) and {@code CC} is the specific error (01-99). {@code A=1} denotes the
 * common module; other modules use 2, 3 and so on. Success is always {@code 00000}.</p>
 *
 * @author nanak
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 0xxxx - Success

    SUCCESS("00000", "Success"),

    // 101xx - General errors
    INTERNAL_SERVER_ERROR("10101", "Internal server error"),
    SERVICE_UNAVAILABLE("10102", "Service unavailable"),
    OPERATION_FAILED("10103", "Operation failed"),

    // 102xx - Request and parameter errors
    INVALID_REQUEST("10201", "Invalid request"),
    INVALID_PARAMETER("10202", "Invalid parameter"),
    MISSING_PARAMETER("10203", "Required parameter is missing"),
    METHOD_NOT_ALLOWED("10204", "HTTP method not allowed"),
    UNSUPPORTED_MEDIA_TYPE("10205", "Unsupported media type"),
    REQUEST_TOO_LARGE("10206", "Request body is too large"),

    // 103xx - Authentication and authorization errors
    UNAUTHENTICATED("10301", "Authentication required"),
    INVALID_CREDENTIALS("10302", "Invalid credentials"),
    TOKEN_EXPIRED("10303", "Authentication token has expired"),
    TOKEN_INVALID("10304", "Invalid authentication token"),
    ACCESS_DENIED("10305", "Access denied"),

    // 104xx - Resource errors
    RESOURCE_NOT_FOUND("10401", "Resource not found"),
    RESOURCE_ALREADY_EXISTS("10402", "Resource already exists"),
    RESOURCE_CONFLICT("10403", "Resource conflict"),
    RESOURCE_GONE("10404", "Resource is no longer available"),

    // 105xx - State and operation errors
    INVALID_STATE("10501", "Invalid resource state"),
    OPERATION_NOT_PERMITTED("10502", "Operation is not permitted"),
    OPERATION_TIMEOUT("10503", "Operation timed out"),

    // 106xx - Dependency and infrastructure errors
    DEPENDENCY_FAILURE("10601", "Dependency failure"),
    DATABASE_ERROR("10602", "Database operation failed"),
    EXTERNAL_SERVICE_ERROR("10603", "External service error"),
    RATE_LIMIT_EXCEEDED("10604", "Rate limit exceeded");

    private final String code;
    private final String message;
}
