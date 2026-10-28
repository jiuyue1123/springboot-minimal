package com.example.web.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson configuration for distributed locks.
 *
 * <p>Reuses the standard {@code spring.data.redis.*} settings instead of a separate Redisson
 * configuration block.</p>
 *
 * @author nanak
 */
@Configuration(proxyBeanMethods = false)
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisConnectionDetails connectionDetails,
                                         RedisProperties redisProperties) {
        RedisConnectionDetails.Standalone standalone = connectionDetails.getStandalone();
        if (standalone == null) {
            throw new IllegalStateException("RedissonConfig only supports standalone Redis");
        }

        Config config = new Config();
        SingleServerConfig server = config.useSingleServer()
                .setAddress("redis://" + standalone.getHost() + ":" + standalone.getPort())
                .setDatabase(standalone.getDatabase());

        if (StringUtils.hasText(connectionDetails.getUsername())) {
            server.setUsername(connectionDetails.getUsername());
        }
        if (StringUtils.hasText(connectionDetails.getPassword())) {
            server.setPassword(connectionDetails.getPassword());
        }
        if (redisProperties.getTimeout() != null) {
            server.setTimeout((int) redisProperties.getTimeout().toMillis());
        }

        return Redisson.create(config);
    }
}
