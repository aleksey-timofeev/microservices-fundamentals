package com.alextim.resource.service.messaging;

import com.alextim.resource.configuration.RabbitMQRetryTemplateConfig;
import com.alextim.resource.configuration.RetryProperties;
import com.alextim.resource.exception.TransientPublishException;
import com.alextim.resource.factory.RetryTemplateFactory;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResourceEventPublisherRetryTest {
    private static final String BINDING_NAME = "resourceUploaded-out-0";

    @Test
    void publish_retriesWhenMessageHandlingExceptionWrapsRabbitConnectionFailure() {
        StreamBridge streamBridge = mock(StreamBridge.class);
        when(streamBridge.send(eq(BINDING_NAME), any(Message.class)))
                .thenThrow(new MessageHandlingException(
                        MessageBuilder.withPayload(17).build(),
                        new AmqpConnectException("RabbitMQ unavailable", new IOException("connection refused"))
                ))
                .thenAnswer(invocation -> acknowledge(invocation.getArgument(1)));
        var publisher = new ResourceEventPublisher(streamBridge, Duration.ofSeconds(1), retryTemplate());

        publisher.publish(17);

        verify(streamBridge, times(2)).send(eq(BINDING_NAME), any(Message.class));
    }

    @Test
    void publish_doesNotRetryPermanentMessageHandlingFailure() {
        StreamBridge streamBridge = mock(StreamBridge.class);
        when(streamBridge.send(eq(BINDING_NAME), any(Message.class)))
                .thenThrow(new MessageHandlingException(
                        MessageBuilder.withPayload(17).build(),
                        new IllegalArgumentException()
                ));
        var publisher = new ResourceEventPublisher(streamBridge, Duration.ofSeconds(1), retryTemplate());

        assertThatThrownBy(() -> publisher.publish(17))
                .isInstanceOf(MessageHandlingException.class);

        verify(streamBridge).send(eq(BINDING_NAME), any(Message.class));
    }

    @Test
    void publish_retriesTransientPublishExceptionAndStopsAtMaximumAttempts() {
        StreamBridge streamBridge = mock(StreamBridge.class);
        when(streamBridge.send(eq(BINDING_NAME), any(Message.class))).thenReturn(false);
        var publisher = new ResourceEventPublisher(streamBridge, Duration.ofSeconds(1), retryTemplate());

        assertThatThrownBy(() -> publisher.publish(17))
                .isInstanceOf(TransientPublishException.class);

        verify(streamBridge, times(3)).send(eq(BINDING_NAME), any(Message.class));
    }

    private boolean acknowledge(Message<?> message) {
        CorrelationData correlationData = message.getHeaders().get(
                AmqpHeaders.PUBLISH_CONFIRM_CORRELATION,
                CorrelationData.class
        );
        correlationData.getFuture().complete(new CorrelationData.Confirm(true, "ack"));
        return true;
    }

    private RetryTemplate retryTemplate() {
        var properties = new RetryProperties();
        properties.setInitialInterval(Duration.ofMillis(1));
        properties.setMaxInterval(Duration.ofMillis(1));

        var config = new RabbitMQRetryTemplateConfig();
        var policy = config.rabbitMQRetryPolicy(properties);
        return config.rabbitMQRetryTemplate(new RetryTemplateFactory(), properties, policy);
    }
}
