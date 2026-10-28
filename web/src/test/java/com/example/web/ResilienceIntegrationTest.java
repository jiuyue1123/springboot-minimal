package com.example.web;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResilienceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestcontainersConfiguration.ResilienceProbe probe;

    @Test
    void idempotentRejectsDuplicateCalls() {
        String key = UUID.randomUUID().toString();

        assertThat(probe.idempotent(key)).isEqualTo("ok");
        assertThatThrownBy(() -> probe.idempotent(key))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_CONFLICT));
    }

    @Test
    void rateLimitRejectsCallsOverTheWindowLimit() {
        String key = UUID.randomUUID().toString();

        assertThat(probe.rateLimited(key)).isEqualTo("ok");
        assertThat(probe.rateLimited(key)).isEqualTo("ok");
        assertThatThrownBy(() -> probe.rateLimited(key))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED));
    }

    @Test
    void distributedLockExecutesTheMethod() {
        assertThat(probe.locked(UUID.randomUUID().toString())).isEqualTo("ok");
    }

    @Test
    void asyncAndRetryWork() throws Exception {
        assertThat(probe.asyncValue().get(5, TimeUnit.SECONDS)).isEqualTo("async");
        assertThat(probe.retried()).isEqualTo("ok");
    }

    @Test
    void operationLogDoesNotChangeTheResult() {
        assertThat(probe.logged()).isEqualTo("ok");
    }
}
