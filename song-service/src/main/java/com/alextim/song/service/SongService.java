package com.alextim.song.service;

import com.alextim.song.exception.ConflictException;
import com.alextim.song.exception.NotFoundException;
import com.alextim.song.persistence.entity.Song;
import com.alextim.song.repository.SongRepository;
import com.alextim.song.request.SongRequest;
import com.alextim.song.response.SongResponse;
import com.alextim.song.service.mapper.SongMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SongService {
    private static final String CAN_NOT_FIND_SONG_BY_ID_MSG = "Song metadata for ID=%s not found";
    private static final String METADATA_FOR_ID_ALREADY_EXISTS_MSG = "Metadata for resource ID=%s already exists";
    private static final int MAX_CSV_STRING_LENGTH = 200;
    private static final String CSV_STRING_IS_TOO_LONG =
            "CSV string is too long: received %d characters, maximum allowed is %d";

    private static final String INVALID_ID_MSG =
            "Invalid ID format: '%s'. Only positive integers are allowed";
    private static final String COMMA = ",";

    private final SongRepository repository;
    private final SongMapper songMapper;

    @Transactional
    public void saveSongMetadata(SongRequest request) {
        Integer resourceId = request.getId();
        if (repository.existsById(request.getId())) {
            throw new ConflictException(String.format(METADATA_FOR_ID_ALREADY_EXISTS_MSG, resourceId));
        }
        Song song = songMapper.toDomain(request);
        repository.save(song);
    }

    public SongResponse getSongMetadata(Integer id) {
        Song song = repository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format(CAN_NOT_FIND_SONG_BY_ID_MSG, id)));
        return songMapper.toResponse(song);

    }

    @Transactional
    public List<Integer> deleteSongMetadata(String ids) {
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

        repository.deleteAllById(resourceIds);

        return resourceIds;
    }
}
