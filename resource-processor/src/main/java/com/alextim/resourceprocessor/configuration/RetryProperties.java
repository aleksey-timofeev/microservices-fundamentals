package com.alextim.resourceprocessor.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "resource-service.http-retry")
public class RetryProperties {
    private int maxAttempts = 3;
    private Duration initialInterval = Duration.ofMillis(200);
    private Duration maxInterval = Duration.ofSeconds(1);
    private double multiplier = 2.0;
}
