package com.alextim.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@EnableConfigurationProperties(PostgresContainerProperties.class)
public class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSQLContainer(PostgresContainerProperties properties) {
        return new PostgreSQLContainer(DockerImageName.parse(properties.image()))
                .withDatabaseName(properties.database())
                .withUsername(properties.username())
                .withPassword(properties.password());
    }
}
