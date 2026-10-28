package com.example.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables asynchronous execution, scheduling and method retries.
 *
 * <p>Pool sizes and shutdown behaviour come from {@code spring.task.execution.*} and
 * {@code spring.task.scheduling.*}.</p>
 *
 * @author nanak
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
@EnableScheduling
@EnableRetry
public class ExecutionConfig {
}
