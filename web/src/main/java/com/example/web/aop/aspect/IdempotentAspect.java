package com.example.web.aop.aspect;

import com.example.common.exception.BusinessException;
import com.example.web.aop.SpelKeys;
import com.example.web.aop.annotation.Idempotent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Applies {@link Idempotent} by reserving a Redis key before the method runs.
 *
 * @author nanak
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
public class IdempotentAspect {

    private static final String PREFIX = "idempotent:";

    private final StringRedisTemplate redisTemplate;

    public IdempotentAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String key = SpelKeys.resolve(idempotent.key(), (MethodSignature) joinPoint.getSignature(),
                joinPoint.getArgs(), PREFIX);

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", idempotent.timeout(), idempotent.unit());
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BusinessException(idempotent.errorCode());
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            redisTemplate.delete(key);
            throw ex;
        }
    }
}
