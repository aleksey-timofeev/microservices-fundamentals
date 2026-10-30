package com.alextim.resource.configuration;

import com.alextim.resource.factory.RetryTemplateFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RetryProperties.class)
public class RetryConfig {
    @Bean
    public RetryTemplateFactory retryTemplateFactory() {
        return new RetryTemplateFactory();
    }
}
