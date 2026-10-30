package com.alextim.resource.service.messaging;

import com.alextim.resource.exception.TransientPublishException;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class ResourceEventPublisher {
    private static final String BINDING_NAME = "resourceUploaded-out-0";
    private static final String EVENT_WAS_NOT_ACCEPTED_BY_MESSAGE_CHANNEL_MSG =
            "Resource-uploaded event was not accepted by the message channel";
    private static final String EVENT_WAS_REJECTED_MSG = "Resource-uploaded event was rejected by RabbitMQ: %s";
    private static final String COULD_NOT_CONFIRM_EVENT_PUBLICATION_MSG = "Could not confirm resource-uploaded event publication";
    private static final String INTERRUPTED_WHILE_WAITING_CONFIRMATION_MSG = "Interrupted while waiting for RabbitMQ publish confirmation";
    private static final String RABBIT_MQ_TEMPORARY_UNAVAILABLE_MSG = "RabbitMQ is temporarily unavailable";

    private final StreamBridge streamBridge;
    private final Duration confirmTimeout;
    private final RetryTemplate retryTemplate;

    public ResourceEventPublisher(
            StreamBridge streamBridge,
            @Value("${resource-service.event-publish-confirm-timeout:5s}") Duration confirmTimeout,
            @Qualifier("rabbitMQRetryTemplate") RetryTemplate retryTemplate
    ) {
        this.streamBridge = streamBridge;
        this.confirmTimeout = confirmTimeout;
        this.retryTemplate = retryTemplate;
    }

    public void publish(Integer resourceId) {
        retryTemplate.execute(context -> {
            sendAndAwaitConfirm(resourceId);
            return null;
        });
    }

    private void sendAndAwaitConfirm(Integer resourceId) {
        var correlationData = new CorrelationData();
        var message = MessageBuilder.withPayload(resourceId)
                .setHeader(AmqpHeaders.PUBLISH_CONFIRM_CORRELATION, correlationData)
                .build();

        try {
            if (!streamBridge.send(BINDING_NAME, message)) {
                throw new TransientPublishException(EVENT_WAS_NOT_ACCEPTED_BY_MESSAGE_CHANNEL_MSG);
            }
        } catch (MessageHandlingException exception) {
            if (hasTransientRabbitFailure(exception)) {
                throw new TransientPublishException(RABBIT_MQ_TEMPORARY_UNAVAILABLE_MSG, exception);
            }
            throw exception;
        }
        awaitConfirm(correlationData);
    }

    private boolean hasTransientRabbitFailure(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof AmqpConnectException
                    || cause instanceof AmqpIOException
                    || cause instanceof AmqpTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private void awaitConfirm(CorrelationData correlationData) {
        try {
            var confirm = correlationData.getFuture().get(confirmTimeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!confirm.ack()) {
                throw new TransientPublishException(String.format(EVENT_WAS_REJECTED_MSG, confirm.reason()));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(INTERRUPTED_WHILE_WAITING_CONFIRMATION_MSG, ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new TransientPublishException(COULD_NOT_CONFIRM_EVENT_PUBLICATION_MSG, ex);
        }
    }
}
