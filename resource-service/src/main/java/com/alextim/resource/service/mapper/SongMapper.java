package com.alextim.resource.service.mapper;

import com.alextim.resource.extractor.SongMetadata;
import com.alextim.resource.request.SongRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;

@Mapper(componentModel = "spring",
        injectionStrategy = CONSTRUCTOR)
public interface SongMapper {
    @Mapping(source = "metadata.title", target = "name")
    @Mapping(source = "metadata.duration", target = "duration", qualifiedByName = "formatDuration")
    SongRequest toRequest(Integer id, SongMetadata metadata);

    @Named("formatDuration")
    default String formatDuration(String duration) {
        double seconds = Double.parseDouble(duration);

        long totalSeconds = (long) seconds;
        long minutes = totalSeconds / 60;
        long remainingSeconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}
