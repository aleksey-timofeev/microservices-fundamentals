package com.alextim.resourceprocessor.client;

import com.alextim.resourceprocessor.extractor.SongMetadata;
import com.alextim.resourceprocessor.response.IdResponse;
import com.alextim.resourceprocessor.service.mapper.SongMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class SongClient {
    private final RestClient songRestClient;
    private final SongMapper songMapper;
    private final RetryTemplate httpRetryTemplate;

    public IdResponse createSongMetadata(Integer resourceId, SongMetadata metadata) {
        var songRequest = songMapper.toRequest(resourceId, metadata);

        return httpRetryTemplate.execute(context -> songRestClient.post()
                    .uri("/songs")
                    .body(songRequest)
                    .retrieve()
                    .body(IdResponse.class));
    }
}
