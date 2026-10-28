package com.example.web.aop.aspect;

import com.example.common.exception.BusinessException;
import com.example.web.aop.SpelKeys;
import com.example.web.aop.annotation.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Applies {@link DistributedLock} with a Redisson lock.
 *
 * @author nanak
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 400)
public class DistributedLockAspect {

    private static final String PREFIX = "lock:";

    private final RedissonClient redissonClient;

    public DistributedLockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String key = SpelKeys.resolve(distributedLock.key(), (MethodSignature) joinPoint.getSignature(),
                joinPoint.getArgs(), PREFIX);
        RLock lock = redissonClient.getLock(key);

        boolean acquired;
        try {
            acquired = acquire(lock, distributedLock);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(distributedLock.errorCode());
        }
        if (!acquired) {
            throw new BusinessException(distributedLock.errorCode());
        }

        try {
            return joinPoint.proceed();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean acquire(RLock lock, DistributedLock annotation) throws InterruptedException {
        if (annotation.leaseTime() < 0) {
            return lock.tryLock(annotation.waitTime(), annotation.unit());
        }
        return lock.tryLock(annotation.waitTime(), annotation.leaseTime(), annotation.unit());
    }
}
