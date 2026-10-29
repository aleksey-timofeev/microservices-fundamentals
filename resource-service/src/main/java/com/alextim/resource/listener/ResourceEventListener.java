package com.alextim.resource.listener;

import com.alextim.resource.event.ResourceUploadedEvent;
import com.alextim.resource.service.messaging.ResourceEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class ResourceEventListener {
    private final ResourceEventPublisher publisher;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onApplicationEvent(ResourceUploadedEvent event) {
        publisher.publish(event.getResourceId());
    }

}
