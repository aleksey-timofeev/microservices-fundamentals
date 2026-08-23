package com.alextim.song.service.mapper;

import com.alextim.song.persistence.entity.Song;
import com.alextim.song.request.SongRequest;
import com.alextim.song.response.SongResponse;
import org.mapstruct.Mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;

@Mapper(componentModel = "spring",
        injectionStrategy = CONSTRUCTOR)
public interface SongMapper {
    Song toDomain(SongRequest request);

    SongResponse toResponse(Song song);
}
