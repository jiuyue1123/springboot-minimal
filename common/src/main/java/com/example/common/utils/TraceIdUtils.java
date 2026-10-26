package com.example.common.utils;

import java.util.UUID;

/**
 * Utility methods for trace ids.
 *
 * @author nanak
 */
public final class TraceIdUtils {

    private TraceIdUtils() {
    }

    /**
     * Generates a new trace id.
     *
     * <p>The id is a random UUID in its 32-character hexadecimal form, without hyphens.</p>
     *
     * @return newly generated trace id
     */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
