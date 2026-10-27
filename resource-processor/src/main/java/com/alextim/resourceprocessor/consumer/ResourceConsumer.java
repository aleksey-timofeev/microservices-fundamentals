package com.alextim.resourceprocessor.consumer;

import com.alextim.resourceprocessor.service.ResourceProcessingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class ResourceConsumer {
    @Bean
    public Consumer<Integer> resourceUploaded(ResourceProcessingService processingService) {
        return processingService::processEvent;
    }
}
