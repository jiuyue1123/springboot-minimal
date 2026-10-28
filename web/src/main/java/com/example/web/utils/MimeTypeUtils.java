package com.example.web.utils;

import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

/**
 * Detects MIME types from file content with Apache Tika.
 *
 * <p>Detection is content based, so renaming a file does not change the result. The optional
 * file name is only a hint for formats whose magic bytes are ambiguous. Input streams are read
 * but never closed; an input that cannot be read fails with {@link UncheckedIOException}.</p>
 *
 * @author nanak
 */
public final class MimeTypeUtils {

    private static final Tika TIKA = new Tika();

    private static final Set<String> ARCHIVE_MIME_TYPES = Set.of(
            "application/zip",
            "application/x-tar",
            "application/gzip",
            "application/x-bzip2",
            "application/x-7z-compressed",
            "application/x-rar-compressed");

    private MimeTypeUtils() {
    }

    /**
     * Detects the MIME type of the given content.
     *
     * @param content file content, must not be {@code null}
     * @return detected MIME type, {@code application/octet-stream} when unknown
     */
    public static String detect(byte[] content) {
        return TIKA.detect(content);
    }

    /**
     * Detects the MIME type from a stream. The caller remains responsible for closing it.
     *
     * @param input content stream
     * @return detected MIME type
     */
    public static String detect(InputStream input) {
        try {
            return TIKA.detect(input);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to detect MIME type", ex);
        }
    }

    /**
     * Detects the MIME type using the file name as an additional hint.
     *
     * @param filename original file name
     * @param content  file content
     * @return detected MIME type
     */
    public static String detect(String filename, byte[] content) {
        return TIKA.detect(content, filename);
    }

    /**
     * Checks whether the content matches the expected MIME type.
     *
     * @param content          file content
     * @param expectedMimeType exact type such as {@code application/pdf}, or a wildcard such as
     *                         {@code image/*}
     * @return {@code true} when the detected type matches
     */
    public static boolean is(byte[] content, String expectedMimeType) {
        return matches(detect(content), expectedMimeType);
    }

    /**
     * Compares two MIME types, ignoring case and supporting the {@code type/*} wildcard.
     *
     * @param detectedMimeType detected type
     * @param expectedMimeType expected type or wildcard
     * @return {@code true} when they match
     */
    public static boolean matches(String detectedMimeType, String expectedMimeType) {
        if (detectedMimeType == null || expectedMimeType == null) {
            return false;
        }
        String detected = detectedMimeType.toLowerCase(Locale.ROOT);
        String expected = expectedMimeType.toLowerCase(Locale.ROOT);
        if (expected.endsWith("/*")) {
            return detected.startsWith(expected.substring(0, expected.length() - 1));
        }
        return detected.equals(expected);
    }

    /**
     * Checks whether the content matches any of the expected MIME types, for example
     * {@code isAny(content, "image/jpeg", "image/png")}.
     *
     * @param content           file content
     * @param expectedMimeTypes exact types or wildcards
     * @return {@code true} when at least one type matches
     */
    public static boolean isAny(byte[] content, String... expectedMimeTypes) {
        return matchesAny(detect(content), expectedMimeTypes);
    }

    /**
     * Checks whether the content matches any of the expected MIME types held in a collection,
     * for example a list loaded from configuration.
     *
     * @param content           file content
     * @param expectedMimeTypes exact types or wildcards
     * @return {@code true} when at least one type matches
     */
    public static boolean isAny(byte[] content, Collection<String> expectedMimeTypes) {
        return matchesAny(detect(content), expectedMimeTypes);
    }

    /**
     * Checks whether the detected type matches any of the expected MIME types.
     *
     * @param detectedMimeType  detected type
     * @param expectedMimeTypes exact types or wildcards
     * @return {@code true} when at least one type matches
     */
    public static boolean matchesAny(String detectedMimeType, String... expectedMimeTypes) {
        return expectedMimeTypes != null
                && Arrays.stream(expectedMimeTypes).anyMatch(type -> matches(detectedMimeType, type));
    }

    /**
     * Checks whether the detected type matches any of the expected MIME types.
     *
     * @param detectedMimeType  detected type
     * @param expectedMimeTypes exact types or wildcards
     * @return {@code true} when at least one type matches
     */
    public static boolean matchesAny(String detectedMimeType, Collection<String> expectedMimeTypes) {
        return expectedMimeTypes != null
                && expectedMimeTypes.stream().anyMatch(type -> matches(detectedMimeType, type));
    }

    public static boolean isImage(byte[] content) {
        return is(content, "image/*");
    }

    public static boolean isAudio(byte[] content) {
        return is(content, "audio/*");
    }

    public static boolean isVideo(byte[] content) {
        return is(content, "video/*");
    }

    public static boolean isText(byte[] content) {
        return is(content, "text/*");
    }

    public static boolean isPdf(byte[] content) {
        return is(content, "application/pdf");
    }

    public static boolean isArchive(byte[] content) {
        return ARCHIVE_MIME_TYPES.contains(detect(content).toLowerCase(Locale.ROOT));
    }
}
