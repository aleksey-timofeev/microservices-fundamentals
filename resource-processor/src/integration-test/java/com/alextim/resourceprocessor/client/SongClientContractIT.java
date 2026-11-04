package com.alextim.resourceprocessor.client;

import com.alextim.resourceprocessor.configuration.TimeoutProperties;
import com.alextim.resourceprocessor.extractor.SongMetadata;
import com.alextim.resourceprocessor.factory.RestClientFactory;
import com.alextim.resourceprocessor.response.IdResponse;
import com.alextim.resourceprocessor.service.mapper.SongMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

class SongClientContractIT {
    private static final String STUB_HOST = "localhost";
    private static final String BASE_URL_FORMAT = "http://%s:%d";
    private static final String SONGS_PATH = "/songs";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";
    private static final int EPHEMERAL_PORT = 0;
    private static final int SERVER_BACKLOG = 0;
    private static final int SINGLE_RETRY_ATTEMPT = 1;
    private static final int MAX_DURATION_SECONDS = 179;
    private static final String DURATION_FORMAT = "%02d:%02d";
    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String ARTIST_FIELD = "artist";
    private static final String ALBUM_FIELD = "album";
    private static final String DURATION_FIELD = "duration";
    private static final String YEAR_FIELD = "year";
    private static final Integer SONG_ID = 42;

    private static final String TITLE = "Contract Test Song";
    private static final String ARTIST = "Contract Test Artist";
    private static final String ALBUM = "Contract Test Album";
    private static final String RAW_DURATION = Integer.toString(MAX_DURATION_SECONDS);
    private static final String FORMATTED_DURATION = DURATION_FORMAT.formatted(
            MAX_DURATION_SECONDS / 60,
            MAX_DURATION_SECONDS % 60);
    private static final String YEAR = "2001";

    private static final String RESPONSE_BODY_FORMAT = "{\"id\":%d}";
    private static final String RESPONSE_CONTENT_TYPE = APPLICATION_JSON_CONTENT_TYPE;
    private static final String RESPONSE_BODY = RESPONSE_BODY_FORMAT.formatted(SONG_ID);
    private static final AtomicReference<RequestCapture> RECEIVED_REQUEST = new AtomicReference<>();

    private static HttpServer server;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeAll
    static void startSongServiceStub() throws IOException {
        server = HttpServer.create(new InetSocketAddress(STUB_HOST, EPHEMERAL_PORT), SERVER_BACKLOG);
        server.createContext(SONGS_PATH, SongClientContractIT::respondWithSongId);
        server.start();
    }

    @AfterAll
    static void stopSongServiceStub() {
        if (server != null) {
            server.stop(0);
        }
    }

    @BeforeEach
    void resetCapturedRequest() {
        RECEIVED_REQUEST.set(null);
    }

    @Test
    void createSongMetadata_shouldMatchSongServiceContract() throws Exception {
        SongClient songClient = createSongClient();

        IdResponse response = songClient.createSongMetadata(
                SONG_ID,
                new SongMetadata(TITLE, ARTIST, ALBUM, RAW_DURATION, YEAR));

        assertThat(response).isEqualTo(IdResponse
                .builder()
                .id(SONG_ID)
                .build());

        RequestCapture request = RECEIVED_REQUEST.get();
        assertThat(request).isNotNull();
        assertThat(request.method()).isEqualTo(POST.name());
        assertThat(request.path()).isEqualTo(SONGS_PATH);
        assertThat(request.contentType()).startsWith(APPLICATION_JSON_CONTENT_TYPE);

        JsonNode body = OBJECT_MAPPER.readTree(request.body());
        assertThat(body.get(ID_FIELD).intValue()).isEqualTo(SONG_ID);
        assertThat(body.get(NAME_FIELD).textValue()).isEqualTo(TITLE);
        assertThat(body.get(ARTIST_FIELD).textValue()).isEqualTo(ARTIST);
        assertThat(body.get(ALBUM_FIELD).textValue()).isEqualTo(ALBUM);
        assertThat(body.get(DURATION_FIELD).textValue()).isEqualTo(FORMATTED_DURATION);
        assertThat(body.get(YEAR_FIELD).textValue()).isEqualTo(YEAR);
    }

    private static SongClient createSongClient() {
        String baseUrl = BASE_URL_FORMAT.formatted(STUB_HOST, server.getAddress().getPort());

        RestClient restClient = new RestClientFactory().create(baseUrl, new TimeoutProperties());
        SongMapper songMapper = Mappers.getMapper(SongMapper.class);
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(SINGLE_RETRY_ATTEMPT));

        return new SongClient(restClient, songMapper, retryTemplate);
    }

    private static void respondWithSongId(HttpExchange exchange) throws IOException {
        try (exchange) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            RECEIVED_REQUEST.set(new RequestCapture(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestHeaders().getFirst(CONTENT_TYPE_HEADER),
                    body));
            byte[] response = RESPONSE_BODY.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set(CONTENT_TYPE_HEADER, RESPONSE_CONTENT_TYPE);
            exchange.sendResponseHeaders(OK.value(), response.length);
            exchange.getResponseBody().write(response);
        }
    }

    private record RequestCapture(String method, String path, String contentType, String body) {
    }
}
