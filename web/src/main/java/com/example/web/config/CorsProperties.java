package com.example.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS settings bound from {@code app.cors.*}.
 *
 * <p>Origins are expressed as patterns, so {@code *} works together with credentials. In
 * production provide the list through {@code CORS_ALLOWED_ORIGINS} (comma separated) instead
 * of publishing the local development origin.</p>
 *
 * @author nanak
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /** Whether CORS handling is registered at all. */
    private boolean enabled = true;

    /** Origin patterns allowed to call the API. */
    private List<String> allowedOriginPatterns = new ArrayList<>();

    /** HTTP methods allowed for cross-origin requests. */
    private List<String> allowedMethods = new ArrayList<>(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    /** Request headers allowed for cross-origin requests. */
    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

    /** Response headers readable by the browser. */
    private List<String> exposedHeaders = new ArrayList<>(List.of("X-Trace-Id", "X-Timestamp"));

    /** Whether cookies and authorization headers may be sent. */
    private boolean allowCredentials = true;

    /** How long the browser may cache the preflight result, in seconds. */
    private long maxAge = 3600;
}
