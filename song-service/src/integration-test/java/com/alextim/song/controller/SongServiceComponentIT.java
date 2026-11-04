package com.alextim.song.controller;

import com.alextim.configuration.TestcontainersConfiguration;
import com.alextim.song.request.SongRequest;
import com.alextim.song.response.IdResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.alextim.song.controller.SongServiceComponentIT.DISABLE_EUREKA_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = DISABLE_EUREKA_PROPERTY
)
@Import(TestcontainersConfiguration.class)
class SongServiceComponentIT {
    static final String DISABLE_EUREKA_PROPERTY = "eureka.client.enabled=false";

    private static final String LOCALHOST_BASE_URL = "http://localhost:";
    private static final String SONGS_PATH = "/songs";
    private static final String CONTENT_TYPE_HEADER = HttpHeaders.CONTENT_TYPE;
    private static final String JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;
    private static final int SUCCESS_STATUS = 200;
    private static final int CONFLICT_STATUS = 409;
    private static final int MIN_SONG_ID = 1;
    private static final int MAX_SONG_ID_EXCLUSIVE = Integer.MAX_VALUE;
    private static final int MIN_DURATION_SECONDS = 0;
    private static final int MAX_DURATION_SECONDS_EXCLUSIVE = 3600;
    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR_EXCLUSIVE = 2100;
    private static final String SONG_NAME_FORMAT = "Song %s";
    private static final String ARTIST_NAME_FORMAT = "Artist %s";
    private static final String ALBUM_NAME_FORMAT = "Album %s";
    private static final String DURATION_FORMAT = "%02d:%02d";
    private static final String ERROR_MESSAGE_KEY = "errorMessage";
    private static final String ERROR_CODE_KEY = "errorCode";
    private static final String CONFLICT_MESSAGE_FORMAT = "Metadata for resource ID=%d already exists";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${local.server.port}")
    private int port;

    @Test
    void songMetadata_shouldBePersistedAndDuplicateRequestShouldConflict() throws Exception {
        int songId = ThreadLocalRandom.current().nextInt(MIN_SONG_ID, MAX_SONG_ID_EXCLUSIVE);
        SongRequest songRequest = createSongRequest(songId);
        String requestBody = objectMapper.writeValueAsString(songRequest);

        HttpResponse<String> createResponse = sendPost(requestBody);
        assertThat(createResponse.statusCode()).isEqualTo(SUCCESS_STATUS);
        IdResponse createdSong = objectMapper.readValue(createResponse.body(), IdResponse.class);
        assertThat(createdSong.getId()).isEqualTo(songId);

        HttpResponse<String> duplicateResponse = sendPost(requestBody);
        assertThat(duplicateResponse.statusCode()).isEqualTo(CONFLICT_STATUS);
        Map<String, String> conflict = objectMapper.readValue(
                duplicateResponse.body(),
                new TypeReference<>() {});
        assertThat(conflict.get(ERROR_MESSAGE_KEY))
                .isEqualTo(CONFLICT_MESSAGE_FORMAT.formatted(songId));
        assertThat(conflict.get(ERROR_CODE_KEY)).isEqualTo(Integer.toString(CONFLICT_STATUS));
    }

    private HttpResponse<String> sendPost(String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri(SONGS_PATH))
                .header(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create(LOCALHOST_BASE_URL + port + path);
    }

    private static SongRequest createSongRequest(int songId) {
        SongRequest request = new SongRequest();
        String uniqueSuffix = UUID.randomUUID().toString();
        int durationSeconds = ThreadLocalRandom.current().nextInt(
                MIN_DURATION_SECONDS,
                MAX_DURATION_SECONDS_EXCLUSIVE);
        request.setId(songId);
        request.setName(SONG_NAME_FORMAT.formatted(uniqueSuffix));
        request.setArtist(ARTIST_NAME_FORMAT.formatted(uniqueSuffix));
        request.setAlbum(ALBUM_NAME_FORMAT.formatted(uniqueSuffix));
        request.setDuration(DURATION_FORMAT.formatted(durationSeconds / 60, durationSeconds % 60));
        request.setYear(Integer.toString(ThreadLocalRandom.current().nextInt(MIN_YEAR, MAX_YEAR_EXCLUSIVE)));
        return request;
    }
}
