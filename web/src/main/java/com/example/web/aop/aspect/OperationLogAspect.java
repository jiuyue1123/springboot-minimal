package com.example.web.aop.aspect;

import com.example.web.aop.annotation.OperationLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Applies {@link OperationLog} by logging the operation outcome and duration.
 *
 * @author nanak
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private static final int MAX_ARGS_LENGTH = 500;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        if (operationLog.logArgs() && log.isDebugEnabled()) {
            log.debug("operation start description={} method={} args={}",
                    operationLog.value(), method, abbreviate(Arrays.toString(joinPoint.getArgs())));
        }

        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            log.info("operation={} method={} duration={}ms result=success",
                    operationLog.value(), method, elapsedMillis(start));
            return result;
        } catch (Throwable ex) {
            log.warn("operation={} method={} duration={}ms result=failure error={}",
                    operationLog.value(), method, elapsedMillis(start), ex.getMessage());
            throw ex;
        }
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String abbreviate(String value) {
        return value.length() <= MAX_ARGS_LENGTH ? value : value.substring(0, MAX_ARGS_LENGTH) + "...";
    }
}
