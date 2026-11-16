package com.alextim.apigateway.handler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.ConnectException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

class GlobalErrorWebExceptionHandlerTest {
    private static final int INVALID_HTTP_STATUS = 999;
    private static final String ERROR_CODE_PATH = "$.errorCode";
    private static final String ERROR_MESSAGE_PATH = "$.errorMessage";
    private static final String STATUS_ATTRIBUTE = "status";

    @Test
    void handle_usesHttpStatusFromErrorAttributes() {
        ErrorAttributes errorAttributes = errorAttributes(NOT_FOUND.value(), new RuntimeException("not found"));
        expectErrorResponse(errorAttributes, NOT_FOUND.value(), NOT_FOUND.getReasonPhrase());
    }

    @Test
    void handle_returnsServiceUnavailableWhenCauseIsConnectionFailure() {
        ErrorAttributes errorAttributes = errorAttributes(
                SERVICE_UNAVAILABLE.value(),
                new RuntimeException("gateway failure", new ConnectException("connection refused"))
        );

        expectErrorResponse(errorAttributes, SERVICE_UNAVAILABLE.value(), SERVICE_UNAVAILABLE.getReasonPhrase());
    }

    @Test
    void handle_fallsBackToInternalServerErrorWhenStatusIsMissingOrInvalid() {
        ErrorAttributes errorAttributes = mock(ErrorAttributes.class);
        when(errorAttributes.getErrorAttributes(any(ServerRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(Map.of(STATUS_ATTRIBUTE, INVALID_HTTP_STATUS));
        when(errorAttributes.getError(any(ServerRequest.class))).thenReturn(new RuntimeException("failure"));

        expectErrorResponse(errorAttributes, INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    private void expectErrorResponse(ErrorAttributes errorAttributes, int status, String message) {
        client(errorAttributes).get()
                .uri("/")
                .exchange()
                .expectStatus().isEqualTo(status)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath(ERROR_CODE_PATH).isEqualTo(String.valueOf(status))
                .jsonPath(ERROR_MESSAGE_PATH).isEqualTo(message);
    }

    private WebTestClient client(ErrorAttributes errorAttributes) {
        TestableGlobalErrorWebExceptionHandler handler =
                new TestableGlobalErrorWebExceptionHandler(errorAttributes);
        return WebTestClient.bindToRouterFunction(handler.routingFunction(errorAttributes)).build();
    }

    private ErrorAttributes errorAttributes(int status, Throwable error) {
        ErrorAttributes errorAttributes = mock(ErrorAttributes.class);
        when(errorAttributes.getErrorAttributes(any(ServerRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(Map.of(STATUS_ATTRIBUTE, status));
        when(errorAttributes.getError(any(ServerRequest.class))).thenReturn(error);
        return errorAttributes;
    }

    private static class TestableGlobalErrorWebExceptionHandler extends GlobalErrorWebExceptionHandler {
        private TestableGlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes) {
            super(errorAttributes, new WebProperties(), ServerCodecConfigurer.create(),
                    new AnnotationConfigApplicationContext());
        }

        private RouterFunction<ServerResponse> routingFunction(ErrorAttributes errorAttributes) {
            return getRoutingFunction(errorAttributes);
        }
    }
}
