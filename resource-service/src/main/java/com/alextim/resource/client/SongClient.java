package com.alextim.resource.client;

import com.alextim.resource.response.IdsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SongClient {
    private final RestClient songRestClient;
    private final RetryTemplate songRetryTemplate;

    public IdsResponse deleteSongMetadata(List<Integer> resourceIds) {
        String ids = resourceIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return songRetryTemplate.execute(context -> songRestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/songs")
                        .queryParam("id", ids)
                        .build())
                .retrieve()
                .body(IdsResponse.class));
    }
}
