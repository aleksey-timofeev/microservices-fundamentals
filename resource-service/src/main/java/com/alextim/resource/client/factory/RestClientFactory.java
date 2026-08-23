package com.alextim.resource.client.factory;

import com.alextim.resource.configuration.TimeoutProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RestClientFactory {
    public RestClient create(String baseUrl, TimeoutProperties timeouts) {
        var factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(timeouts.getConnectTimeout());
        factory.setReadTimeout(timeouts.getReadTimeout() * 1000);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
    }
}