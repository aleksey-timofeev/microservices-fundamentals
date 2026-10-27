package com.alextim.resourceprocessor.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ResourceClient {
    private final RestClient resourceRestClient;
    private final RetryTemplate httpRetryTemplate;

    public byte[] getResource(Integer id) {
        return httpRetryTemplate.execute(context -> resourceRestClient.get()
                    .uri("/resources/{id}", id)
                    .retrieve()
                    .body(byte[].class));
    }
}
