package com.alextim.apigateway.handler;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.autoconfigure.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {
    private static final String ERROR_CODE_KEY = "errorCode";
    private static final String ERROR_MESSAGE_KEY = "errorMessage";
    private static final String STATUS_ATTRIBUTE = "status";

    /**
     * Create a new {@code AbstractErrorWebExceptionHandler}.
     *
     * @param errorAttributes    the error attributes
     * @param resources          the resources configuration properties
     * @param applicationContext the application context
     */
    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                          WebProperties webProperties,
                                          ServerCodecConfigurer codecConfigurer,
                                          ApplicationContext applicationContext) {
        super(errorAttributes, webProperties.getResources(), applicationContext);

        setMessageWriters(codecConfigurer.getWriters());
        setMessageReaders(codecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(
                RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> attributes =
                getErrorAttributes(request, ErrorAttributeOptions.defaults());
        Throwable throwable = getError(request);

        HttpStatus status = resolveStatus(attributes, throwable);

        Map<String, String> body = Map.of(
                ERROR_MESSAGE_KEY, status.getReasonPhrase(),
                ERROR_CODE_KEY, String.valueOf(status.value()));

        return ServerResponse.status(status)
                .contentType(APPLICATION_JSON)
                .bodyValue(body);
    }

    private HttpStatus resolveStatus(Map<String, Object> attributes, Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause instanceof java.net.ConnectException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        Object value = attributes.get(STATUS_ATTRIBUTE);
        if (value instanceof Number number) {
            HttpStatus status = HttpStatus.resolve(number.intValue());
            if (status != null) {
                return status;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
