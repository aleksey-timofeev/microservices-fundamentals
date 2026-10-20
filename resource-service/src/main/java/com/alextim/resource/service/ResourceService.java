package com.alextim.resource.service;

import com.alextim.resource.client.SongClient;
import com.alextim.resource.exception.NotFoundException;
import com.alextim.resource.persistence.entity.Resource;
import com.alextim.resource.persistence.s3.ResourceStorage;
import com.alextim.resource.repository.ResourceRepository;
import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResourceService {
    private static final String INVALID_MEDIA_TYPE_MSG =
            "Invalid file format: %s. Only MP3 files are allowed";

    private static final String CAN_NOT_FIND_RESOURCE_BY_ID_MSG = "Resource with ID=%s not found";
    private static final int MAX_CSV_STRING_LENGTH = 200;
    private static final String CSV_STRING_IS_TOO_LONG =
            "CSV string is too long: received %d characters, maximum allowed is %d";
    private static final String COMMA = ",";

    private static final String INVALID_ID_MSG =
            "Invalid ID format: '%s'. Only positive integers are allowed";

    private final SongClient songClient;
    private final ResourceRepository repository;
    private final ResourceStorage storage;

    @Transactional
    public Integer uploadResource(byte[] file, String contentType) {
        if (!"audio/mpeg".equals(contentType)) {
            throw new IllegalArgumentException(String.format(INVALID_MEDIA_TYPE_MSG, contentType));
        }

        S3Resource s3Resource = storage.upload(file);
        Resource resource = Resource.builder()
                .location(s3Resource.getLocation().getObject())
                .build();
        Resource saved = repository.save(resource);

        return saved.getId();
    }

    public byte[] getResource(Integer id) {
        Resource resource = repository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format(CAN_NOT_FIND_RESOURCE_BY_ID_MSG, id)));
        return storage.download(resource.getLocation());
    }

    @Transactional
    public List<Integer> deleteResources(String ids) {
        int csvLen = StringUtils.length(ids);
        if (csvLen > MAX_CSV_STRING_LENGTH) {
            throw new IllegalArgumentException(String.format(CSV_STRING_IS_TOO_LONG,
                    csvLen,
                    MAX_CSV_STRING_LENGTH));
        }

        List<Integer> validIds = new ArrayList<>();
        for (String id : ids.split(COMMA)) {
            if (!StringUtils.isNumeric(id)) {
                throw new IllegalArgumentException(String.format(INVALID_ID_MSG, id));
            }
            validIds.add(Integer.valueOf(id));
        }
        List<Integer> resourceIds = repository.findExistingIds(validIds);
        if (!resourceIds.isEmpty()) {
            Iterable<Resource> resources = repository.findAllById(resourceIds);
            resources.forEach(resource -> storage.delete(resource.getLocation()));
            songClient.deleteSongMetadata(resourceIds);
            repository.deleteAllById(resourceIds);
        }

        return resourceIds;
    }
}
