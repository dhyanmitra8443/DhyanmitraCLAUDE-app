package com.lms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Smoke test: verifies the full Spring context (security config, JWT beans,
 * Flyway migration) loads successfully against a real, throwaway Postgres
 * container - not the dev docker-compose database.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class LmsApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {
        // Passing means: Flyway applied V1__initial_schema.sql cleanly, and
        // every bean (SecurityConfig, JwtService, JwtAuthenticationFilter,
        // CorsConfig, AsyncConfig, OpenApiConfig) wired up without errors.
    }
}
