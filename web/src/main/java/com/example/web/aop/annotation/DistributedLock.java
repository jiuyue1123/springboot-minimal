package com.example.web.aop.annotation;

import com.example.common.exception.ErrorCode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Serializes calls that resolve to the same key across application instances with a Redisson
 * lock.
 *
 * <p>When {@link #leaseTime()} is negative the Redisson watchdog keeps renewing the lock until
 * the method returns. A positive lease time releases the lock automatically after that period,
 * even if the owner dies.</p>
 *
 * @author nanak
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /** SpEL expression evaluated against the method arguments, for example {@code "#orderId"}. */
    String key();

    /** How long to wait for the lock; zero fails immediately when it is held elsewhere. */
    long waitTime() default 0;

    /** Lease time, or a negative value to rely on the watchdog. */
    long leaseTime() default -1;

    /** Unit of {@link #waitTime()} and {@link #leaseTime()}. */
    TimeUnit unit() default TimeUnit.SECONDS;

    /** Error code raised when the lock cannot be acquired. */
    ErrorCode errorCode() default ErrorCode.OPERATION_NOT_PERMITTED;
}
