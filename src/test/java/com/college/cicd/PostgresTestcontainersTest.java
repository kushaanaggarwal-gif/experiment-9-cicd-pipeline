package com.college.cicd;

import com.college.cicd.model.PostgresContainerConfig;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Infrastructure Tests: Testcontainers PostgreSQL")
public class PostgresTestcontainersTest {

    private static final String POSTGRES_IMAGE = "postgres:15-alpine";

    private boolean isDockerRunning() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Test
    @DisplayName("Testcontainers 1: Verify PostgreSQL Container Configuration Specification")
    void testPostgreSqlContainerSpecification() {
        PostgresContainerConfig config = new PostgresContainerConfig();
        assertNotNull(config, "PostgresContainerConfig should be initialized");
        assertEquals("postgres:15-alpine", config.getDockerImage(), "Container image specification should be postgres:15-alpine");
        assertEquals("testdb", config.getDatabaseName(), "Database name must be testdb");
        assertEquals(5432, config.getContainerPort(), "PostgreSQL default exposed port must be 5432");
    }

    @Test
    @DisplayName("Testcontainers 2: Execute Real Container Lifecycle (Requires Docker Daemon)")
    void testRealContainerLifecycle() {
        boolean dockerActive = isDockerRunning();

        // Dynamically evaluate Docker availability:
        // If Docker is not running, skip test per specification and report reason.
        Assumptions.assumeTrue(dockerActive,
            "Docker engine is not running on this host. Skipping live Testcontainers spin-up per experiment specification.");

        // If Docker is running, launch container and verify live database connectivity
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName("testdb")
                .withUsername("postgres")
                .withPassword("secret")) {

            postgres.start();
            assertTrue(postgres.isRunning(), "Real PostgreSQL container must be running inside Docker engine");
            assertNotNull(postgres.getJdbcUrl(), "JDBC URL should be provided by running container");
            postgres.stop();
        }
    }
}
