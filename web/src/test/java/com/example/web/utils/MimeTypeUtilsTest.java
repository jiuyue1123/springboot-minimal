package com.example.web.utils;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MimeTypeUtilsTest {

    private static final byte[] PNG = {
            (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A
    };

    private static final byte[] PDF = "%PDF-1.7 sample".getBytes(StandardCharsets.US_ASCII);

    @Test
    void detectsCommonTypesFromContent() {
        assertThat(MimeTypeUtils.detect(PNG)).isEqualTo("image/png");
        assertThat(MimeTypeUtils.detect(PDF)).isEqualTo("application/pdf");
        assertThat(MimeTypeUtils.detect("hello world".getBytes(StandardCharsets.UTF_8)))
                .isEqualTo("text/plain");
    }

    @Test
    void matchesExactAndWildcardTypes() {
        assertThat(MimeTypeUtils.is(PNG, "image/*")).isTrue();
        assertThat(MimeTypeUtils.is(PNG, "application/pdf")).isFalse();
        assertThat(MimeTypeUtils.matches("IMAGE/PNG", "image/png")).isTrue();
    }

    @Test
    void convenienceChecks() {
        assertThat(MimeTypeUtils.isImage(PNG)).isTrue();
        assertThat(MimeTypeUtils.isPdf(PDF)).isTrue();
        assertThat(MimeTypeUtils.isText("plain".getBytes(StandardCharsets.UTF_8))).isTrue();
        assertThat(MimeTypeUtils.isArchive(PNG)).isFalse();
    }

    @Test
    void unknownContentFallsBackToOctetStream() {
        assertThat(MimeTypeUtils.detect(new byte[0])).isEqualTo("application/octet-stream");
        assertThat(MimeTypeUtils.is(new byte[0], "image/*")).isFalse();
    }

    @Test
    void acceptsAnyOfSeveralTypes() {
        assertThat(MimeTypeUtils.isAny(PNG, "image/jpeg", "image/png")).isTrue();
        assertThat(MimeTypeUtils.isAny(PNG, "image/jpeg", "application/pdf")).isFalse();
        assertThat(MimeTypeUtils.isAny(PNG, List.of("image/jpeg", "image/*"))).isTrue();
        assertThat(MimeTypeUtils.matchesAny("image/png", "IMAGE/JPEG", "IMAGE/PNG")).isTrue();
    }

    @Test
    void filenameActsAsAHint() {
        assertThat(MimeTypeUtils.detect("picture.png", PNG)).isEqualTo("image/png");
    }
}
