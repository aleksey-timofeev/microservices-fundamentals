package com.alextim.song.service;

import com.alextim.song.exception.ConflictException;
import com.alextim.song.persistence.entity.Song;
import com.alextim.song.repository.SongRepository;
import com.alextim.song.request.SongRequest;
import com.alextim.song.service.mapper.SongMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {
    private static final String SONG_TITLE = "We are the champions";
    private static final String ARTIST = "Queen";
    private static final String ALBUM = "News of the world";
    private static final String FORMATTED_DURATION = "02:59";
    private static final String YEAR = "1977";

    private static final Integer FIRST_SONG_ID = 1;
    private static final Integer SECOND_SONG_ID = 2;
    private static final Integer THIRD_SONG_ID = 3;
    private static final String NON_INTEGER_ID = "invalid";
    private static final int MAX_CSV_STRING_LENGTH = 200;
    private static final int CSV_STRING_LENGTH_OVER_LIMIT = MAX_CSV_STRING_LENGTH + 1;

    @Mock
    private SongRepository repository;

    private final SongMapper songMapper = Mappers.getMapper(SongMapper.class);
    private SongService testedInstance;

    @BeforeEach
    void setUp() {
        testedInstance = new SongService(repository, songMapper);
    }

    @Test
    void saveSongMetadata_shouldThrowConflictException_whenResourceIdAlreadyExists() {
        SongRequest request = createSongRequest(FIRST_SONG_ID);
        doReturn(true).when(repository).existsById(FIRST_SONG_ID);

        assertThatThrownBy(() -> testedInstance.saveSongMetadata(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Metadata for resource ID=1 already exists");

        verify(repository, never()).save(any(Song.class));
    }

    @Test
    void deleteSongMetadata_shouldRejectInvalidId_whenIdIsNotNumeric() {
        String ids = String.format("%d,%s", FIRST_SONG_ID, NON_INTEGER_ID);

        assertThatThrownBy(() -> testedInstance.deleteSongMetadata(ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid ID format: 'invalid'. Only positive integers are allowed");

        verifyNoInteractions(repository);
    }

    @Test
    void deleteSongMetadata_shouldRejectIds_whenCsvStringExceedsMaximumLength() {
        String ids = "1".repeat(CSV_STRING_LENGTH_OVER_LIMIT);

        assertThatThrownBy(() -> testedInstance.deleteSongMetadata(ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CSV string is too long: received 201 characters, maximum allowed is 200");

        verifyNoInteractions(repository);
    }

    @Test
    void deleteSongMetadata_shouldDeleteOnlyExistingSongs_whenSomeIdsDoNotExist() {
        String ids = "1,2,3";
        List<Integer> requestedIds = List.of(FIRST_SONG_ID, SECOND_SONG_ID, THIRD_SONG_ID);
        List<Integer> existingIds = List.of(FIRST_SONG_ID, THIRD_SONG_ID);
        doReturn(existingIds).when(repository).findExistingIds(requestedIds);

        List<Integer> actualIds = testedInstance.deleteSongMetadata(ids);

        assertThat(actualIds).isEqualTo(existingIds);
        verify(repository).findExistingIds(requestedIds);
        verify(repository).deleteAllById(existingIds);
    }

    private static SongRequest createSongRequest(Integer id) {
        SongRequest request = new SongRequest();

        request.setId(id);
        request.setName(SONG_TITLE);
        request.setArtist(ARTIST);
        request.setAlbum(ALBUM);
        request.setDuration(FORMATTED_DURATION);
        request.setYear(YEAR);

        return request;
    }
}
