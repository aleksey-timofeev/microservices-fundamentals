package com.alextim.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "testcontainers.postgres")
public record PostgresContainerProperties(
        String image,
        String database,
        String username,
        String password
) {}