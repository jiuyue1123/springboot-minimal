package com.example.web.aop.annotation;

import com.example.common.exception.ErrorCode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Limits how many calls may resolve to the same key inside a fixed window.
 *
 * <p>The counter is kept in Redis and expires after {@link #window()}, so the limit applies
 * across application instances.</p>
 *
 * @author nanak
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * SpEL expression evaluated against the method arguments. When empty, the method signature
     * and argument hash are used.
     */
    String key() default "";

    /** Maximum number of calls allowed in one window. */
    int limit() default 10;

    /** Window length. */
    long window() default 60;

    /** Unit of {@link #window()}. */
    TimeUnit unit() default TimeUnit.SECONDS;

    /** Error code raised when the limit is exceeded. */
    ErrorCode errorCode() default ErrorCode.RATE_LIMIT_EXCEEDED;
}
