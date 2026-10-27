package com.alextim.resource.service.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResourceEventPublisherTest {
    private static final String BINDING_NAME = "resourceUploaded-out-0";

    @Test
    void publish_failsWhenMessageChannelRejectsMessage() {
        StreamBridge streamBridge = mock(StreamBridge.class);
        when(streamBridge.send(eq(BINDING_NAME), any(Message.class))).thenReturn(false);
        var publisher = new ResourceEventPublisher(streamBridge, Duration.ofMillis(10));

        assertThatThrownBy(() -> publisher.publish(17))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not accepted");
    }

    @Test
    void publish_waitsForBrokerConfirmation() throws Exception {
        StreamBridge streamBridge = mock(StreamBridge.class);
        when(streamBridge.send(eq(BINDING_NAME), any(Message.class))).thenAnswer(invocation -> {
            Message<?> message = invocation.getArgument(1);
            CorrelationData correlationData = message.getHeaders().get(
                    AmqpHeaders.PUBLISH_CONFIRM_CORRELATION,
                    CorrelationData.class
            );
            correlationData.getFuture().complete(new CorrelationData.Confirm(true, "ack"));
            return true;
        });
        var publisher = new ResourceEventPublisher(streamBridge, Duration.ofSeconds(1));

        publisher.publish(17);

        verify(streamBridge).send(eq(BINDING_NAME), any(Message.class));
    }

    @Test
    void publish_failsWhenBrokerRejectsMessage() {
        StreamBridge streamBridge = mock(StreamBridge.class);
        when(streamBridge.send(eq(BINDING_NAME), any(Message.class))).thenAnswer(invocation -> {
            Message<?> message = invocation.getArgument(1);
            CorrelationData correlationData = message.getHeaders().get(
                    AmqpHeaders.PUBLISH_CONFIRM_CORRELATION,
                    CorrelationData.class
            );
            correlationData.getFuture().complete(new CorrelationData.Confirm(false, "nack"));
            return true;
        });
        var publisher = new ResourceEventPublisher(streamBridge, Duration.ofSeconds(1));

        assertThatThrownBy(() -> publisher.publish(17))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("rejected");
    }
}
