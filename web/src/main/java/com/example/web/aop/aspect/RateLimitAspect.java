package com.example.web.aop.aspect;

import com.example.common.exception.BusinessException;
import com.example.web.aop.SpelKeys;
import com.example.web.aop.annotation.RateLimit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Applies {@link RateLimit} with a Redis fixed-window counter.
 *
 * @author nanak
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 300)
public class RateLimitAspect {

    private static final String PREFIX = "rate-limit:";

    private static final RedisScript<Long> COUNTER_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimitAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = SpelKeys.resolve(rateLimit.key(), (MethodSignature) joinPoint.getSignature(),
                joinPoint.getArgs(), PREFIX);
        long windowMillis = rateLimit.unit().toMillis(rateLimit.window());

        Long current = redisTemplate.execute(COUNTER_SCRIPT, List.of(key), String.valueOf(windowMillis));
        if (current != null && current > rateLimit.limit()) {
            throw new BusinessException(rateLimit.errorCode());
        }
        return joinPoint.proceed();
    }
}
