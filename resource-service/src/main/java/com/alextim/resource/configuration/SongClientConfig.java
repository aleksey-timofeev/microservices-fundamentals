package com.alextim.resource.configuration;

import com.alextim.resource.client.factory.RestClientFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SongApiClientProperties.class)
public class SongClientConfig {
    @Bean
    public RestClientFactory restClientFactory() {
        return new RestClientFactory();
    }

    @Bean
    public RestClient songRestClient(
            RestClientFactory restClientFactory,
            SongApiClientProperties properties
    ) {
        return restClientFactory.create(
                properties.getBaseUrl(),
                properties.getTimeouts()
        );
    }
}
