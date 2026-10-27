package com.alextim.resourceprocessor.service.mapper;

import com.alextim.resourceprocessor.extractor.SongMetadata;
import com.alextim.resourceprocessor.request.SongRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;

@Mapper(componentModel = "spring",
        injectionStrategy = CONSTRUCTOR)
public interface SongMapper {
    @Mapping(target = "id", source = "resourceId")
    @Mapping(target = "name", source = "metadata.title")
    @Mapping(target = "duration", source = "metadata.duration",
            qualifiedByName = "formatDuration")
    SongRequest toRequest(Integer resourceId, SongMetadata metadata);

    @Named("formatDuration")
    default String formatDuration(String duration) {
        long totalSeconds = Math.round(Double.parseDouble(duration));
        return "%02d:%02d".formatted(
                totalSeconds / 60,
                totalSeconds % 60
        );
    }

}
