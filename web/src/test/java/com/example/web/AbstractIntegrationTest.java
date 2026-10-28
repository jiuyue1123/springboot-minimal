package com.example.web;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests.
 *
 * <p>Starts MySQL and Redis through Testcontainers, activates the {@code test} profile and
 * prepares MockMvc. Subclasses inherit the same configuration, so they share one cached
 * application context. A running Docker daemon is required.</p>
 *
 * @author nanak
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {
}
