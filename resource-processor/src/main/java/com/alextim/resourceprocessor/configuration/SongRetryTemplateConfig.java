package com.alextim.resourceprocessor.configuration;

import com.alextim.resourceprocessor.factory.RetryTemplateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class SongRetryTemplateConfig {
    @Bean
    public RetryTemplate songRetryTemplate(
            RetryTemplateFactory retryTemplateFactory,
            RetryProperties retryProperties,
            RetryPolicy retryPolicy
    ) {
        return retryTemplateFactory.create(
                retryProperties,
                retryPolicy);
    }
}
