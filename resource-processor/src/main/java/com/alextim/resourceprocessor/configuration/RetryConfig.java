package com.alextim.resourceprocessor.configuration;

import com.alextim.resourceprocessor.factory.RetryTemplateFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(RetryProperties.class)
public class RetryConfig {
    @Bean
    public RetryTemplateFactory retryTemplateFactory() {
        return new RetryTemplateFactory();
    }

    @Bean
    public RetryPolicy retryPolicy(RetryProperties properties) {
        return new SimpleRetryPolicy(
                properties.getMaxAttempts(),
                Map.of(
                        ResourceAccessException.class, true,
                        HttpServerErrorException.class, true
                ),
                true
        );
    }
}
