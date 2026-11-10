package com.alextim.resource.service.messaging;

public interface ResourceEventPublisher {
    void publish(Integer resourceId);
}
