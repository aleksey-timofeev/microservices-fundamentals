package com.alextim.resourceprocessor.e2e;

import com.alextim.resourceprocessor.response.IdResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceUploadE2EIT {
    private static final String GATEWAY_BASE_URL = "http://localhost:8080";
    private static final String MP3_FIXTURE_PATH = "/mp3/valid-sample-with-required-tags.mp3";
    private static final String RESOURCE_PATH = "/resources";
    private static final String SONG_PATH_FORMAT = "/songs/%d";
    private static final String EXPECTED_TITLE = "Test Title";
    private static final String EXPECTED_ARTIST = "Test Artist";
    private static final String EXPECTED_ALBUM = "Test Album";
    private static final String EXPECTED_DURATION = "00:08";
    private static final String EXPECTED_YEAR = "2025";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration PROCESSING_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(250);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();
    private static final ParameterizedTypeReference<Map<String, Object>> SONG_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final RestClient GATEWAY_CLIENT = createGatewayClient();

    @Test
    void uploadMp3_shouldEventuallyExposeExtractedMetadataThroughGateway() throws Exception {
        Integer resourceId = uploadResource();
        Map<String, Object> song = awaitSongMetadata(resourceId);

        assertSongMetadata(song, resourceId);
    }

    private Integer uploadResource() throws Exception {
        ResponseEntity<IdResponse> uploadResponse = GATEWAY_CLIENT
                .post()
                .uri(RESOURCE_PATH)
                .contentType(MediaType.valueOf("audio/mpeg"))
                .body(loadMp3Fixture())
                .retrieve()
                .toEntity(IdResponse.class);
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        IdResponse uploadedResource = Objects.requireNonNull(
                uploadResponse.getBody(), "Resource upload response body is empty");
        assertThat(uploadedResource.getId()).isNotNull();
        return uploadedResource.getId();
    }

    private static void assertSongMetadata(Map<String, Object> song, Integer resourceId) {
        assertThat(song.get("id")).isEqualTo(resourceId);
        assertThat(song.get("name")).isEqualTo(EXPECTED_TITLE);
        assertThat(song.get("artist")).isEqualTo(EXPECTED_ARTIST);
        assertThat(song.get("album")).isEqualTo(EXPECTED_ALBUM);
        assertThat(song.get("duration")).isEqualTo(EXPECTED_DURATION);
        assertThat(song.get("year")).isEqualTo(EXPECTED_YEAR);
    }

    private Map<String, Object> awaitSongMetadata(Integer songId) throws Exception {
        String path = SONG_PATH_FORMAT.formatted(songId);
        long deadline = System.nanoTime() + PROCESSING_TIMEOUT.toNanos();
        HttpClientErrorException.NotFound lastNotFound = null;

        while (System.nanoTime() < deadline) {
            try {
                ResponseEntity<Map<String, Object>> response = GATEWAY_CLIENT
                        .get()
                        .uri(path)
                        .retrieve()
                        .toEntity(SONG_RESPONSE_TYPE);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                return Objects.requireNonNull(response.getBody(), "Song response body is empty");
            } catch (HttpClientErrorException.NotFound notFound) {
                lastNotFound = notFound;
            }
            if (System.nanoTime() < deadline) {
                Thread.sleep(POLL_INTERVAL.toMillis());
            }
        }

        throw new AssertionError("Song metadata was not available within " + PROCESSING_TIMEOUT
                + "; last response: " + lastNotFound.getResponseBodyAsString(), lastNotFound);
    }

    private static RestClient createGatewayClient() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(HTTP_CLIENT);
        requestFactory.setReadTimeout(REQUEST_TIMEOUT);
        return RestClient.builder()
                .baseUrl(GATEWAY_BASE_URL)
                .requestFactory(requestFactory)
                .build();
    }

    private static byte[] loadMp3Fixture() throws IOException {
        try (InputStream fixture = Objects.requireNonNull(
                ResourceUploadE2EIT.class.getResourceAsStream(MP3_FIXTURE_PATH),
                "MP3 fixture not found: " + MP3_FIXTURE_PATH)) {
            return fixture.readAllBytes();
        }
    }
}
