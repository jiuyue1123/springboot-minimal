package com.example.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Writes an access log pair per request: a start line when it enters and an end line with the
 * response status and duration when it completes.
 *
 * <p>When DEBUG logging is enabled, a details line additionally records the query string,
 * request parameters and request headers. Values of sensitive parameters and headers are
 * masked and every value is truncated, so credentials never reach the log. The request body is
 * not logged because it may be large, binary or sensitive.</p>
 *
 * <p>Runs right after {@link TraceIdFilter}, so the MDC trace id is included in every line by
 * the logging pattern. Health checks and the favicon are skipped to keep the log focused on
 * API traffic.</p>
 *
 * <p>{@code X-Forwarded-For} and {@code X-Real-IP} are trusted only when the application runs
 * behind a proxy that sets them.</p>
 *
 * @author nanak
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AccessLogFilter extends OncePerRequestFilter {

    private static final String UNKNOWN = "unknown";

    private static final String MASK = "******";

    private static final int MAX_VALUE_LENGTH = 200;

    private static final Set<String> SENSITIVE_HEADERS =
            Set.of("authorization", "proxy-authorization", "cookie", "set-cookie", "x-api-key");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String method = request.getMethod();
        String path = request.getRequestURI();
        log.info("access start method={} path={} client={}", method, path, resolveClientIp(request));

        if (log.isDebugEnabled()) {
            log.debug("access details method={} path={} query={} params={} headers={}",
                    method,
                    path,
                    describeQueryString(request.getQueryString()),
                    describeParameters(request),
                    describeHeaders(request));
        }

        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.info("access end method={} path={} status={} duration={}ms",
                    method, path, response.getStatus(), durationMs);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/favicon.ico") || path.equals("/actuator") || path.startsWith("/actuator/");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null ? UNKNOWN : remoteAddr;
    }

    private String describeQueryString(String queryString) {
        if (!StringUtils.hasText(queryString)) {
            return "-";
        }
        return Arrays.stream(queryString.split("&"))
                .map(this::maskQueryParameter)
                .collect(Collectors.joining("&"));
    }

    private String maskQueryParameter(String parameter) {
        int separator = parameter.indexOf('=');
        if (separator < 0) {
            return isSensitive(parameter) ? MASK : parameter;
        }
        String name = parameter.substring(0, separator);
        String value = parameter.substring(separator + 1);
        return name + '=' + (isSensitive(name) ? MASK : abbreviate(value));
    }

    private String describeParameters(HttpServletRequest request) {
        Map<String, String[]> parameters = request.getParameterMap();
        if (parameters.isEmpty()) {
            return "-";
        }
        return parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "="
                        + (isSensitive(entry.getKey()) ? MASK : abbreviate(String.join(",", entry.getValue()))))
                .collect(Collectors.joining(", "));
    }

    private String describeHeaders(HttpServletRequest request) {
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return "-";
        }
        StringBuilder builder = new StringBuilder();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(name).append('=')
                    .append(isSensitive(name) ? MASK : abbreviate(request.getHeader(name)));
        }
        return builder.isEmpty() ? "-" : builder.toString();
    }

    private boolean isSensitive(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return SENSITIVE_HEADERS.contains(lower)
                || lower.contains("password")
                || lower.contains("passwd")
                || lower.contains("secret")
                || lower.contains("token")
                || lower.contains("credential");
    }

    private String abbreviate(String value) {
        if (value == null) {
            return "-";
        }
        return value.length() <= MAX_VALUE_LENGTH ? value : value.substring(0, MAX_VALUE_LENGTH) + "...";
    }
}
