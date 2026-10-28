package com.example.web;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers-backed infrastructure for integration tests.
 *
 * <p>Requires a running Docker daemon. The containers are Spring beans, so the test context
 * cache reuses them for every test class that shares the same context configuration.</p>
 *
 * @author nanak
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.4.11"))
                .withDatabaseName("springboot_minimal_test")
                .withUsername("test")
                .withPassword("test");
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:8.10.2"))
                .withExposedPorts(6379);
    }
}
