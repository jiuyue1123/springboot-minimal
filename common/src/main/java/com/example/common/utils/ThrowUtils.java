package com.example.common.utils;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;

/**
 * Utility methods for throwing exceptions.
 *
 * @author nanak
 */
public final class ThrowUtils {

    private ThrowUtils() {
    }

    /**
     * Throws the given exception when the condition is {@code true}.
     *
     * @param condition condition to check
     * @param exception exception to throw
     */
    public static void throwIf(boolean condition, RuntimeException exception) {
        if (condition) {
            throw exception;
        }
    }

    /**
     * Throws a {@link BusinessException} carrying the given error code when the condition is
     * {@code true}.
     *
     * @param condition condition to check
     * @param errorCode error code carried by the exception
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BusinessException(errorCode);
        }
    }
}
