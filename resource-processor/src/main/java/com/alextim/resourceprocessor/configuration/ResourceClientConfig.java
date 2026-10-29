package com.alextim.resourceprocessor.configuration;

import com.alextim.resourceprocessor.factory.RestClientFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ResourceApiClientProperties.class)
public class ResourceClientConfig {
    @Bean
    public RestClient resourceRestClient(
            RestClientFactory restClientFactory,
            ResourceApiClientProperties properties
    ) {
        return restClientFactory.create(
                properties.getBaseUrl(),
                properties.getTimeouts()
        );
    }
}
