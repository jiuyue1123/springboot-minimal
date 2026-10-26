package com.example.common.constant;

/**
 * Common constants shared across modules.
 *
 * @author nanak
 */
public final class CommonConstants {

    /** MDC key of the trace id. */
    public static final String MDC_TRACE_ID = "traceId";

    /** Request and response header carrying the trace id. */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** Response header carrying the epoch-millisecond timestamp. */
    public static final String TIMESTAMP_HEADER = "X-Timestamp";

    private CommonConstants() {
    }
}
