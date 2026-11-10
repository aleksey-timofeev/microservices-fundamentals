package com.alextim.resource.service;

import com.alextim.resource.client.SongClient;
import com.alextim.resource.event.ResourceUploadedEvent;
import com.alextim.resource.exception.NotFoundException;
import com.alextim.resource.persistence.entity.Resource;
import com.alextim.resource.persistence.s3.ResourceStorage;
import com.alextim.resource.repository.ResourceRepository;
import io.awspring.cloud.s3.Location;
import io.awspring.cloud.s3.S3Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {
    private static final String MP3_CONTENT_TYPE = "audio/mpeg";
    private static final String WAV_CONTENT_TYPE = "audio/wav";

    private static final String INVALID_WAV_CONTENT_TYPE_MSG =
            String.format("Invalid file format: %s. Only MP3 files are allowed", WAV_CONTENT_TYPE);
    private static final String CAN_NOT_FIND_RESOURCE_BY_ID_MSG = "Resource with ID=%s not found";

    private static final int MAX_CSV_STRING_LENGTH = 200;
    private static final int CSV_STRING_LENGTH_OVER_LIMIT = MAX_CSV_STRING_LENGTH + 1;
    private static final String NON_INTEGER_ID = "invalid";
    private static final String INVALID_ID_MSG = String.format("Invalid ID format: '%s'. Only positive integers are allowed", NON_INTEGER_ID);
    private static final String CSV_STRING_IS_TOO_LONG = String.format("CSV string is too long: received %d characters, maximum allowed is %d",
                    CSV_STRING_LENGTH_OVER_LIMIT, MAX_CSV_STRING_LENGTH);

    private static final byte[] FILE_CONTENT = "test mp3 content".getBytes(UTF_8);

    private static final Integer FIRST_RESOURCE_ID = 1;
    private static final Integer SECOND_RESOURCE_ID = 2;
    private static final Integer THIRD_RESOURCE_ID = 3;

    @InjectMocks
    private ResourceService testedInstance;

    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private ResourceStorage storage;
    @Mock
    private SongClient songClient;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private S3Resource s3Resource;
    @Mock
    private Location location;
    @Captor
    private ArgumentCaptor<Resource> resourceCaptor;
    @Captor
    private ArgumentCaptor<ResourceUploadedEvent> resourceUploadedEventCaptor;

    @Test
    void uploadResource_shouldRejectUpload_whenContentTypeIsUnsupported() {
        assertThatThrownBy(() -> testedInstance.uploadResource(EMPTY_BYTE_ARRAY, WAV_CONTENT_TYPE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_WAV_CONTENT_TYPE_MSG);

        verifyNoInteractions(storage, resourceRepository, applicationEventPublisher);
    }

    @Test
    void uploadResource_shouldUploadFileAndPublishEvent_whenContentTypeIsAudioMpeg() {
        String uuid = UUID.randomUUID().toString();

        Resource expected = createResource(FIRST_RESOURCE_ID, uuid);
        ResourceUploadedEvent expectedEvent = createResourceUploadedEvent(FIRST_RESOURCE_ID);

        doReturn(s3Resource).when(storage).upload(FILE_CONTENT);
        doReturn(location).when(s3Resource).getLocation();
        doReturn(uuid).when(location).getObject();
        doAnswer(invocation -> {
            Resource resource = invocation.getArgument(0);
            resource.setId(FIRST_RESOURCE_ID);
            return resource;
        }).when(resourceRepository).save(any(Resource.class));

        Integer resourceId = testedInstance.uploadResource(FILE_CONTENT, MP3_CONTENT_TYPE);

        verify(resourceRepository).save(resourceCaptor.capture());
        Resource actual = resourceCaptor.getValue();

        verify(applicationEventPublisher).publishEvent(resourceUploadedEventCaptor.capture());
        ResourceUploadedEvent actualEvent = resourceUploadedEventCaptor.getValue();

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(actualEvent)
                .usingRecursiveComparison()
                .isEqualTo(expectedEvent);

        assertNotNull(resourceId);
        assertThat(resourceId).isEqualTo(FIRST_RESOURCE_ID);
    }

    @Test
    void getResource_shouldThrowException_whenResourceWasNotFound() {
        doReturn(Optional.empty())
                .when(resourceRepository)
                .findById(FIRST_RESOURCE_ID);

        assertThatThrownBy(() -> testedInstance.getResource(FIRST_RESOURCE_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(String.format(CAN_NOT_FIND_RESOURCE_BY_ID_MSG, FIRST_RESOURCE_ID));

        verify(storage, never()).download(any());
    }

    @Test
    void getResource_shouldReturnFileContent_whenResourceWasFound() {
        String locationUuid = UUID.randomUUID().toString();
        Resource resource = createResource(FIRST_RESOURCE_ID, locationUuid);

        doReturn(Optional.of(resource))
                .when(resourceRepository)
                .findById(FIRST_RESOURCE_ID);
        doReturn(FILE_CONTENT)
                .when(storage)
                .download(locationUuid);

        byte[] actualContent = testedInstance.getResource(FIRST_RESOURCE_ID);

        assertThat(actualContent).isEqualTo(FILE_CONTENT);
        verify(resourceRepository).findById(FIRST_RESOURCE_ID);
        verify(storage).download(locationUuid);
    }

    @Test
    void deleteResources_shouldRejectIds_whenCsvStringExceedsMaximumLength() {
        String ids = "1".repeat(CSV_STRING_LENGTH_OVER_LIMIT);

        assertThatThrownBy(() -> testedInstance.deleteResources(ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CSV_STRING_IS_TOO_LONG);

        verifyNoInteractions(storage, resourceRepository, songClient);
    }

    @Test
    void deleteResources_shouldRejectInvalidId_whenIdIsNotNumeric() {
        String ids = String.format("1,%s", NON_INTEGER_ID);

        assertThatThrownBy(() -> testedInstance.deleteResources(ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_ID_MSG);

        verifyNoInteractions(storage, resourceRepository, songClient);
    }

    @Test
    void deleteResources_shouldDeleteOnlyExistingResources_whenSomeIdsDoNotExist() {
        String ids = "1,2,3";

        String firstLocationUuid = UUID.randomUUID().toString();
        String secondLocationUuid = UUID.randomUUID().toString();

        List<Integer> requestedIds = List.of(FIRST_RESOURCE_ID, SECOND_RESOURCE_ID, THIRD_RESOURCE_ID);
        List<Integer> existingIds = List.of(FIRST_RESOURCE_ID, SECOND_RESOURCE_ID);
        List<Resource> resources = List.of(
                createResource(FIRST_RESOURCE_ID, firstLocationUuid),
                createResource(SECOND_RESOURCE_ID, secondLocationUuid));

        doReturn(existingIds).when(resourceRepository).findExistingIds(requestedIds);
        doReturn(resources).when(resourceRepository).findAllById(existingIds);

        List<Integer> actualIds = testedInstance.deleteResources(ids);

        assertThat(actualIds).isEqualTo(existingIds);

        verify(resourceRepository).findExistingIds(requestedIds);
        verify(resourceRepository).findAllById(existingIds);

        verify(storage).delete(firstLocationUuid);
        verify(storage).delete(secondLocationUuid);

        verify(songClient).deleteSongMetadata(existingIds);
        verify(resourceRepository).deleteAllById(existingIds);
    }

    private static Resource createResource(Integer resourceId, String uuid) {
        return Resource.builder()
                .id(resourceId)
                .location(uuid)
                .build();
    }

    private static ResourceUploadedEvent createResourceUploadedEvent(Integer resourceId) {
        return ResourceUploadedEvent.builder()
                .resourceId(resourceId)
                .build();
    }
}
