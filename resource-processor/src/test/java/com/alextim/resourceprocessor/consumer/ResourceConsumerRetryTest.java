package com.alextim.resourceprocessor.consumer;

import com.alextim.resourceprocessor.ResourceProcessorApplication;
import com.alextim.resourceprocessor.service.ResourceProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = ResourceProcessorApplication.class,
        properties = {
                "spring.cloud.stream.defaultBinder=test",
                "spring.cloud.stream.bindings.resourceUploaded-in-0.consumer.maxAttempts=3",
                "spring.cloud.stream.bindings.resourceUploaded-in-0.consumer.backOffInitialInterval=0",
                "spring.cloud.stream.bindings.resourceUploaded-in-0.consumer.backOffMaxInterval=0"
        }
)
@EnableTestBinder
class ResourceConsumerRetryTest {
    @Autowired
    private InputDestination inputDestination;

    @MockitoBean
    private ResourceProcessingService processingService;

    @Test
    void messageProcessing_retriesTransientFailureAndThenSucceeds() {
        var attempts = new AtomicInteger();
        doAnswer(invocation -> {
            if (attempts.incrementAndGet() == 1) {
                throw new IllegalStateException("temporary failure");
            }
            return null;
        }).when(processingService).processEvent(17);

        inputDestination.send(MessageBuilder.withPayload(17).build(), "resourceUploaded-in-0");

        verify(processingService, times(2)).processEvent(17);
    }

    @Test
    void messageProcessing_propagatesFailureAfterRetriesAreExhausted() {
        doAnswer(invocation -> {
            throw new IllegalStateException("permanent processing failure");
        }).when(processingService).processEvent(18);

        assertThatThrownBy(() ->
                inputDestination.send(MessageBuilder.withPayload(18).build(), "resourceUploaded-in-0")
        ).isInstanceOf(RuntimeException.class);

        verify(processingService, times(3)).processEvent(18);
    }
}
