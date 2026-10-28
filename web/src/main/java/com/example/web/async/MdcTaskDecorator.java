package com.example.web.async;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * Propagates the submitting thread's MDC context to asynchronous workers.
 *
 * <p>Spring Boot applies a {@link TaskDecorator} bean to the auto-configured
 * {@code TaskExecutor}, so {@code @Async} methods keep the request trace id in their log lines.
 * The context is cleared afterwards to avoid leaking between pooled tasks.</p>
 *
 * @author nanak
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                } else {
                    MDC.clear();
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
