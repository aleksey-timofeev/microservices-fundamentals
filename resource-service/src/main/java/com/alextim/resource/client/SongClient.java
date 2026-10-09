package com.alextim.resource.client;

import com.alextim.resource.request.SongRequest;
import com.alextim.resource.response.IdResponse;
import com.alextim.resource.response.IdsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SongClient {

    private final RestClient songRestClient;

    public IdResponse saveSongMetadata(SongRequest request) {
        return songRestClient.post()
                .uri("/songs")
                .body(request)
                .retrieve()
                .body(IdResponse.class);
    }

    public IdsResponse deleteSongMetadata(List<Integer> resourceIds) {
        String ids = resourceIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return songRestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/songs")
                        .queryParam("id", ids)
                        .build())
                .retrieve()
                .body(IdsResponse.class);
    }
}
