package com.example.web.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Translates exceptions raised by the web layer into the unified response body.
 *
 * <p>The response message is always the one defined by the error code, and the HTTP status
 * is derived from that same error code. Exception details are written to the log instead of
 * the response.</p>
 *
 * @author nanak
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ResponseEntity<Result<Void>> respond(ErrorCode errorCode) {
        return ResponseEntity.status(statusOf(errorCode)).body(Result.failure(errorCode));
    }

    private static HttpStatus statusOf(ErrorCode errorCode) {
        return switch (errorCode) {
            case SUCCESS -> HttpStatus.OK;
            case INTERNAL_SERVER_ERROR, OPERATION_FAILED, DATABASE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case INVALID_REQUEST, INVALID_PARAMETER, MISSING_PARAMETER -> HttpStatus.BAD_REQUEST;
            case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
            case UNSUPPORTED_MEDIA_TYPE -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            case REQUEST_TOO_LARGE -> HttpStatus.PAYLOAD_TOO_LARGE;
            case UNAUTHENTICATED, INVALID_CREDENTIALS, TOKEN_EXPIRED, TOKEN_INVALID -> HttpStatus.UNAUTHORIZED;
            case ACCESS_DENIED, OPERATION_NOT_PERMITTED -> HttpStatus.FORBIDDEN;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RESOURCE_ALREADY_EXISTS, RESOURCE_CONFLICT, INVALID_STATE -> HttpStatus.CONFLICT;
            case RESOURCE_GONE -> HttpStatus.GONE;
            case OPERATION_TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case DEPENDENCY_FAILURE, EXTERNAL_SERVICE_ERROR -> HttpStatus.BAD_GATEWAY;
            case RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
        };
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException ex) {
        return respond(ex.getErrorCode());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException ex) {
        log.warn("Request binding failed: {}", formatErrors(ex.getBindingResult().getAllErrors()));
        return respond(ErrorCode.INVALID_PARAMETER);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("Constraint violation: {}", details);
        return respond(ErrorCode.INVALID_PARAMETER);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Result<Void>> handleInvalidParameter(Exception ex) {
        log.warn("Invalid request parameter: {}", ex.getMessage());
        return respond(ErrorCode.INVALID_PARAMETER);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request body: {}", ex.getMessage());
        return respond(ErrorCode.INVALID_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        log.debug("No resource found: {}", ex.getMessage());
        return respond(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMessage());
        return respond(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported media type: {}", ex.getMessage());
        return respond(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("Request body too large: {}", ex.getMessage());
        return respond(ErrorCode.REQUEST_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return respond(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String formatErrors(List<ObjectError> errors) {
        return errors.stream()
                .map(this::formatError)
                .collect(Collectors.joining("; "));
    }

    private String formatError(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getField() + ": " + fieldError.getDefaultMessage();
        }
        return error.getDefaultMessage();
    }
}
