package com.example.web.filter;

import com.example.common.constant.CommonConstants;
import com.example.common.utils.TraceIdUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the trace id from the {@code X-Trace-Id} request header, generating one when absent,
 * then exposes it through the MDC and echoes it back in the response headers.
 *
 * <p>The filter runs with the highest precedence so that responses produced by other
 * filters also carry the trace id. {@code X-Timestamp} holds the epoch milliseconds at
 * which the request entered the filter.</p>
 *
 * @author nanak
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader(CommonConstants.TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = TraceIdUtils.generate();
        }

        MDC.put(CommonConstants.MDC_TRACE_ID, traceId);
        response.setHeader(CommonConstants.TRACE_ID_HEADER, traceId);
        response.setHeader(CommonConstants.TIMESTAMP_HEADER, String.valueOf(System.currentTimeMillis()));

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CommonConstants.MDC_TRACE_ID);
        }
    }
}
