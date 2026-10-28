package com.example.web.config;

import com.example.web.async.MdcTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables asynchronous execution, scheduling and method retries.
 *
 * <p>Pool sizes and shutdown behaviour come from {@code spring.task.execution.*} and
 * {@code spring.task.scheduling.*}. The {@link TaskDecorator} bean is applied by Boot to the
 * auto-configured executor.</p>
 *
 * @author nanak
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
@EnableScheduling
@EnableRetry
public class ExecutionConfig {

    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }
}
