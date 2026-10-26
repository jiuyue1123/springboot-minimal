package com.example.common.exception;

import java.io.Serial;

/**
 * Exception raised when a business rule is violated.
 *
 * <p>Both the result code and the message are supplied by {@link ErrorCode}; the exception
 * itself carries no additional error semantics.</p>
 *
 * @author nanak
 */
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
