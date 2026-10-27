package com.alextim.resource.service.messaging;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class ResourceEventPublisher {
    private static final String BINDING_NAME = "resourceUploaded-out-0";

    private final StreamBridge streamBridge;
    private final Duration confirmTimeout;

    public ResourceEventPublisher(
            StreamBridge streamBridge,
            @Value("${resource-service.event-publish-confirm-timeout:5s}") Duration confirmTimeout
    ) {
        this.streamBridge = streamBridge;
        this.confirmTimeout = confirmTimeout;
    }

    public void publish(Integer resourceId) {
        var correlationData = new CorrelationData();
        var message = MessageBuilder.withPayload(resourceId)
                .setHeader(AmqpHeaders.PUBLISH_CONFIRM_CORRELATION, correlationData)
                .build();

        if (!streamBridge.send(BINDING_NAME, message)) {
            throw new IllegalStateException("Resource-uploaded event was not accepted by the message channel");
        }

        try {
            var confirm = correlationData.getFuture().get(confirmTimeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!confirm.ack()) {
                throw new IllegalStateException("Resource-uploaded event was rejected by RabbitMQ: "
                        + confirm.reason());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for RabbitMQ publish confirmation", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Could not confirm resource-uploaded event publication", exception);
        }
    }
}
