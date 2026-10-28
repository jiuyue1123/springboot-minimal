package com.example.web.aop.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Writes one operation log entry per call: description, method, duration and outcome.
 *
 * <p>Arguments are only logged at DEBUG level and only when {@link #logArgs()} is enabled, so
 * sensitive payloads are not written by default.</p>
 *
 * @author nanak
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** Human-readable description of the operation. */
    String value() default "";

    /** Whether to log the method arguments at DEBUG level. */
    boolean logArgs() default false;
}
