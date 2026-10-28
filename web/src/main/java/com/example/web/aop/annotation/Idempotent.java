package com.example.web.aop.annotation;

import com.example.common.exception.ErrorCode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Rejects duplicate calls that resolve to the same idempotency key.
 *
 * <p>The key is stored in Redis for {@link #timeout()} and released when the method throws, so
 * a failed call can be retried. Successful calls keep the key until it expires, which protects
 * against duplicate submissions.</p>
 *
 * @author nanak
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * SpEL expression evaluated against the method arguments, for example
     * {@code "#request.orderNo"}. When empty, the method signature and argument hash are used.
     */
    String key() default "";

    /** How long the key is kept after a successful call. */
    long timeout() default 10;

    /** Unit of {@link #timeout()}. */
    TimeUnit unit() default TimeUnit.SECONDS;

    /** Error code raised for a duplicate call. */
    ErrorCode errorCode() default ErrorCode.RESOURCE_CONFLICT;
}
