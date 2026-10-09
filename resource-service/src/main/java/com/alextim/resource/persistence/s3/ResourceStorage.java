package com.alextim.resource.persistence.s3;

import com.alextim.resource.exception.ResourceStorageException;
import io.awspring.cloud.s3.Location;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Component
public class ResourceStorage {
    private static final String CAN_NOT_DOWNLOAD_RESOURCE_MSG =
            "Resource with Location=%s cannot be downloaded";

    private final S3Template s3Template;
    private final String bucket;

    public ResourceStorage(
            S3Template s3Template,
            @Value("${resource-service.storage.bucket}") String bucket
    ) {
        this.s3Template = s3Template;
        this.bucket = bucket;
    }

    public S3Resource upload(byte[] data) {
        return s3Template.upload(bucket,
                UUID.randomUUID().toString(),
                new ByteArrayInputStream(data));
    }

    public byte[] download(String location) {
        try {
            return s3Template.download(bucket, Location.of(bucket, location).getObject())
                    .getContentAsByteArray();
        } catch (IOException e) {
            throw new ResourceStorageException(String.format(CAN_NOT_DOWNLOAD_RESOURCE_MSG, location));
        }
    }

    public void delete(String location) {
        s3Template.deleteObject(bucket, Location.of(bucket, location).getObject());
    }
}
