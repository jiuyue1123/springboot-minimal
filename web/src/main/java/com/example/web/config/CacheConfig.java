package com.example.web.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Spring Cache configuration backed by Redis.
 *
 * <p>TTL, null handling and the key prefix come from {@code spring.cache.redis.*}. This class
 * only replaces the default JDK value serializer with the typed JSON serializer shared with
 * {@code RedisTemplate}.</p>
 *
 * @author nanak
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheSerializerCustomizer(
            GenericJackson2JsonRedisSerializer valueSerializer) {
        return builder -> builder.cacheDefaults(builder.cacheDefaults()
                .serializeKeysWith(SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer)));
    }
}
