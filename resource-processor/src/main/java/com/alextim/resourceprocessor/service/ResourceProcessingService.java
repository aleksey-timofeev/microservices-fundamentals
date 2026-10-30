package com.alextim.resourceprocessor.service;

import com.alextim.resourceprocessor.client.ResourceClient;
import com.alextim.resourceprocessor.client.SongClient;
import com.alextim.resourceprocessor.extractor.Mp3Extractor;
import com.alextim.resourceprocessor.extractor.SongMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceProcessingService {
    private static final String PROCESSING_RESOURCE_EVENT_MSG =
            "Processing resource event: resourceId={}";
    private static final String RESOURCE_EVENT_PROCESSED_MSG =
            "Successfully processed resource event: resourceId={}";

    private final ResourceClient resourceClient;
    private final SongClient songClient;
    private final Mp3Extractor mp3Extractor;

    public void processEvent(Integer resourceId) {
        log.info(PROCESSING_RESOURCE_EVENT_MSG, resourceId);

        byte[] mp3File = resourceClient.getResource(resourceId);
        SongMetadata metadata = mp3Extractor.extract(mp3File);
        songClient.createSongMetadata(resourceId, metadata);

        log.info(RESOURCE_EVENT_PROCESSED_MSG, resourceId);
    }
}
