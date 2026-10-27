package com.alextim.resourceprocessor.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(HttpRetryProperties.class)
public class HttpRetryConfig {
    @Bean
    public RetryTemplate httpRetryTemplate(HttpRetryProperties properties) {
        var retryPolicy = new SimpleRetryPolicy(
                properties.getMaxAttempts(),
                Map.of(
                        ResourceAccessException.class, true,
                        HttpServerErrorException.class, true
                ),
                true
        );

        var backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(properties.getInitialInterval().toMillis());
        backOffPolicy.setMaxInterval(properties.getMaxInterval().toMillis());
        backOffPolicy.setMultiplier(properties.getMultiplier());

        var retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }
}
