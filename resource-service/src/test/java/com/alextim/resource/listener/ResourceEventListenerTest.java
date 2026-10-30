package com.alextim.resource.listener;

import com.alextim.resource.event.ResourceUploadedEvent;
import com.alextim.resource.service.messaging.ResourceEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResourceEventListenerTest {
    @Mock
    private ResourceEventPublisher resourceEventPublisher;
    @InjectMocks
    private ResourceEventListener resourceEventListener;

    @Captor
    private ArgumentCaptor<Integer> resourceIdCaptor;

    @Test
    void onApplicationEvent_shouldPublishResourceId_whenResourceUploadedEventReceived() {
        ResourceUploadedEvent applicationEvent = ResourceUploadedEvent.builder()
                .resourceId(current().nextInt(1, Integer.MAX_VALUE))
                .build();
        resourceEventListener.onApplicationEvent(applicationEvent);

        verify(resourceEventPublisher).publish(resourceIdCaptor.capture());
        Integer resourceId = resourceIdCaptor.getValue();

        assertEquals(applicationEvent.getResourceId(), resourceId);
    }
}
