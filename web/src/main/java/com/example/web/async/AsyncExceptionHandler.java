package com.example.web.async;

import com.example.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Handles exceptions that escape {@code @Async} methods.
 *
 * <p>Asynchronous methods run on another thread, so their exceptions never reach
 * {@code GlobalExceptionHandler}. Business failures are logged at WARN without a stack trace;
 * anything else is logged at ERROR with the full stack.</p>
 *
 * <p>This handler only receives failures from {@code void} async methods. Methods returning
 * {@code CompletableFuture} capture the exception in the future, so callers must handle it
 * with {@code exceptionally} or {@code whenComplete}.</p>
 *
 * @author nanak
 */
@Slf4j
@Component
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        String target = method.getDeclaringClass().getSimpleName() + '#' + method.getName();
        if (ex instanceof BusinessException businessException) {
            log.warn("async business exception method={} code={} message={}",
                    target,
                    businessException.getErrorCode().getCode(),
                    businessException.getErrorCode().getMessage());
            return;
        }
        log.error("async uncaught exception method={}", target, ex);
    }
}
