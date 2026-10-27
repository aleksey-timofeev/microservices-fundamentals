package com.alextim.resourceprocessor.client;

import com.alextim.resourceprocessor.configuration.HttpRetryConfig;
import com.alextim.resourceprocessor.configuration.HttpRetryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ResourceClientRetryTest {
    private static final int RESOURCE_ID = 17;
    private static final String RESOURCE_URL = "http://resource-service";
    private static final String GET_RESOURCE_URL =
            String.format("%s/resources/%d", RESOURCE_URL, RESOURCE_ID);

    private MockRestServiceServer server;
    private ResourceClient resourceClient;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl(RESOURCE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        resourceClient = new ResourceClient(builder.build(), retryTemplate());
    }

    @Test
    void getResource_retriesServerFailureThenSucceeds() {
        server.expect(requestTo(GET_RESOURCE_URL)).andRespond(withServerError());
        server.expect(requestTo(GET_RESOURCE_URL))
                .andRespond(withSuccess(new byte[]{1, 2, 3}, MediaType.APPLICATION_OCTET_STREAM));

        assertThat(resourceClient.getResource(RESOURCE_ID)).containsExactly(1, 2, 3);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 404})
    void getResource_doesNotRetryPermanentClientErrors(int status) {
        server.expect(requestTo(GET_RESOURCE_URL))
                .andRespond(withStatus(HttpStatus.valueOf(status)));

        assertThatThrownBy(() -> resourceClient.getResource(RESOURCE_ID))
                .isInstanceOf(HttpClientErrorException.class);
        server.verify();
    }

    @Test
    void getResource_failsAfterMaximumAttempts() {
        server.expect(requestTo(GET_RESOURCE_URL)).andRespond(withServerError());
        server.expect(requestTo(GET_RESOURCE_URL)).andRespond(withServerError());
        server.expect(requestTo(GET_RESOURCE_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> resourceClient.getResource(RESOURCE_ID))
                .isInstanceOf(HttpServerErrorException.class);
        server.verify();
    }

    private RetryTemplate retryTemplate() {
        var properties = new HttpRetryProperties();

        properties.setMaxAttempts(3);
        properties.setInitialInterval(Duration.ZERO);
        properties.setMaxInterval(Duration.ZERO);
        return new HttpRetryConfig().httpRetryTemplate(properties);
    }
}
