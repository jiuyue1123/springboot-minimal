package com.example.common.result;

import com.example.common.exception.ErrorCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Unified API response envelope.
 *
 * <p>The message is always taken from {@link ErrorCode}; callers cannot provide a custom
 * message. The trace id and timestamp are returned as response headers, not in the body.</p>
 *
 * @param <T> payload type
 * @author nanak
 */
@Getter
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;
    private final String message;
    private final T data;

    protected Result(ErrorCode errorCode, T data) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS, data);
    }

    public static <T> Result<T> failure(ErrorCode errorCode) {
        return new Result<>(errorCode, null);
    }
}
