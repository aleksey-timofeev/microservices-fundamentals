package com.alextim.resource.configuration;

import com.alextim.resource.factory.RetryTemplateFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@Configuration
public class SongRetryTemplateConfig {
    @Bean
    public RetryTemplate songRetryTemplate(
            RetryTemplateFactory retryTemplateFactory,
            RetryProperties retryProperties,
            @Qualifier("songRetryPolicy") RetryPolicy retryPolicy
    ) {
        return retryTemplateFactory.create(
                retryProperties,
                retryPolicy);
    }

    @Bean
    public RetryPolicy songRetryPolicy(RetryProperties properties) {
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
