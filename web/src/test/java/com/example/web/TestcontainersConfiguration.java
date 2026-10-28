package com.example.web;

import com.example.web.aop.annotation.DistributedLock;
import com.example.web.aop.annotation.Idempotent;
import com.example.web.aop.annotation.OperationLog;
import com.example.web.aop.annotation.RateLimit;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Testcontainers-backed infrastructure for integration tests.
 *
 * <p>Requires a running Docker daemon. The containers are Spring beans, so the test context
 * cache reuses them for every test class that shares the same context configuration.</p>
 *
 * @author nanak
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.4.11"))
                .withDatabaseName("springboot_minimal_test")
                .withUsername("test")
                .withPassword("test");
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:8.10.2"))
                .withExposedPorts(6379);
    }

    @Bean
    ResilienceProbe resilienceProbe() {
        return new ResilienceProbe();
    }

    /**
     * Exercises the AOP annotations against the containers.
     */
    public static class ResilienceProbe {

        private final AtomicInteger attempts = new AtomicInteger();

        @Idempotent(key = "#key", timeout = 30)
        public String idempotent(String key) {
            return "ok";
        }

        @RateLimit(key = "#key", limit = 2, window = 10)
        public String rateLimited(String key) {
            return "ok";
        }

        @DistributedLock(key = "#key", waitTime = 1)
        public String locked(String key) {
            return "ok";
        }

        @OperationLog("probe")
        public String logged() {
            return "ok";
        }

        @Async
        public CompletableFuture<String> asyncValue() {
            return CompletableFuture.completedFuture("async");
        }

        @Retryable(retryFor = IllegalStateException.class, maxAttempts = 3, backoff = @Backoff(delay = 10))
        public String retried() {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("transient");
            }
            return "ok";
        }
    }
}
