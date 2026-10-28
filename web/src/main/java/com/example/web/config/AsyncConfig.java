package com.example.web.config;

import com.example.web.async.AsyncExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

/**
 * Binds {@code @Async} to the Boot-managed executor and installs the custom exception handler.
 *
 * <p>Returning the executor explicitly avoids Spring's ambiguity when both the application
 * task executor and the task scheduler are visible as {@link TaskExecutor} beans.</p>
 *
 * @author nanak
 */
@Configuration(proxyBeanMethods = false)
public class AsyncConfig implements AsyncConfigurer {

    private final TaskExecutor taskExecutor;

    private final AsyncExceptionHandler asyncExceptionHandler;

    public AsyncConfig(@Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
                       AsyncExceptionHandler asyncExceptionHandler) {
        this.taskExecutor = taskExecutor;
        this.asyncExceptionHandler = asyncExceptionHandler;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return asyncExceptionHandler;
    }
}
