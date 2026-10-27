package com.alextim.resourceprocessor.client;

import com.alextim.resourceprocessor.configuration.HttpRetryConfig;
import com.alextim.resourceprocessor.configuration.HttpRetryProperties;
import com.alextim.resourceprocessor.extractor.SongMetadata;
import com.alextim.resourceprocessor.response.IdResponse;
import com.alextim.resourceprocessor.service.mapper.SongMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SongClientRetryTest {
    private static final int RESOURCE_ID = 17;
    private static final String SONG_METADATA_URL = "http://song-service";
    private static final String CREATE_SONG_METADATA_URL =
            String.format("%s/songs", SONG_METADATA_URL);

    private static final String TITLE = "Test Song";
    private static final String ARTIST = "Test Artist";
    private static final String ALBUM = "Test Album";
    private static final String DURATION = "7.810635089874268";
    private static final String YEAR = "2023";

    private SongMapper mapper = Mappers.getMapper(SongMapper.class);

    @Test
    void createSongMetadata_retriesServerFailureThenSucceeds() {
        var builder = RestClient.builder().baseUrl(SONG_METADATA_URL);
        var server = MockRestServiceServer.bindTo(builder).build();

        var songClient = new SongClient(builder.build(), mapper, retryTemplate());

        server.expect(requestTo(CREATE_SONG_METADATA_URL))
                .andExpect(method(POST))
                .andRespond(withServerError());
        server.expect(requestTo(CREATE_SONG_METADATA_URL))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"id\":23}", MediaType.APPLICATION_JSON));

        assertThat(songClient.createSongMetadata(RESOURCE_ID, createSongMetadata()))
                .isEqualTo(IdResponse.builder().id(23).build());
        server.verify();
    }

    private static SongMetadata createSongMetadata() {
        return new SongMetadata(TITLE, ARTIST, ALBUM, DURATION, YEAR);
    }

    private RetryTemplate retryTemplate() {
        var properties = new HttpRetryProperties();

        properties.setMaxAttempts(3);
        properties.setInitialInterval(Duration.ZERO);
        properties.setMaxInterval(Duration.ZERO);
        return new HttpRetryConfig().httpRetryTemplate(properties);
    }
}
