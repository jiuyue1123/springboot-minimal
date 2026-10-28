package com.example.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void cachesAndReadsValuesThroughRedis() {
        Cache cache = cacheManager.getCache("integration");
        assertThat(cache).isNotNull();

        cache.put("key", "value");

        assertThat(cache.get("key", String.class)).isEqualTo("value");
        cache.clear();
    }
}
