package com.alextim.resourceprocessor.service.mapper;

import com.alextim.resourceprocessor.extractor.SongMetadata;
import com.alextim.resourceprocessor.request.SongRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SongMapperTest {
    private static final Integer RESOURCE_ID = 1;
    private static final String SONG_TITLE = "We are the champions";
    private static final String ARTIST = "Queen";
    private static final String ALBUM = "News of the world";
    private static final String RAW_DURATION = "179";
    private static final String FORMATTED_DURATION = "02:59";
    private static final String YEAR = "1977";

    private final SongMapper testedInstance = Mappers.getMapper(SongMapper.class);

    @Test
    void toRequest_shouldMapSongMetadataToSongRequest() {
        var metadata = createSongMetadata();
        assertThat(testedInstance.toRequest(RESOURCE_ID, metadata))
                .usingRecursiveComparison()
                .isEqualTo(createSongRequest());
    }

    @Test
    void formatDuration_shouldThrowNullPointerException_whenDurationIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> testedInstance.formatDuration(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {SPACE, "abc"})
    void formatDuration_shouldThrowNumberFormatException_whenDurationIsInvalid(String duration) {
        assertThatThrownBy(() -> testedInstance.formatDuration(duration))
                .isInstanceOf(NumberFormatException.class);
    }

    @ParameterizedTest
    @MethodSource("formatDurationScenarios")
    void formatDuration_shouldRoundExtractedSeconds(String rawDuration, String formattedDuration) {
        assertThat(testedInstance.formatDuration(rawDuration))
                .isEqualTo(formattedDuration);
    }

    private static SongMetadata createSongMetadata() {
        return new SongMetadata(
                SONG_TITLE,
                ARTIST,
                ALBUM,
                RAW_DURATION,
                YEAR
        );
    }

    private static SongRequest createSongRequest() {
        return SongRequest.builder()
                .id(RESOURCE_ID)
                .name(SONG_TITLE)
                .artist(ARTIST)
                .album(ALBUM)
                .duration(FORMATTED_DURATION)
                .year(YEAR)
                .build();
    }

    private static Stream<Arguments> formatDurationScenarios() {
        return Stream.of(
                Arguments.of("0", "00:00"),
                Arguments.of("1", "00:01"),
                Arguments.of("59", "00:59"),
                Arguments.of("60", "01:00"),
                Arguments.of("61", "01:01"),
                Arguments.of("178.4", "02:58"),
                Arguments.of("178.5", FORMATTED_DURATION),
                Arguments.of("178.6", FORMATTED_DURATION),
                Arguments.of(RAW_DURATION, FORMATTED_DURATION),
                Arguments.of("3599", "59:59"),
                Arguments.of("3600", "60:00"),
                Arguments.of("3661", "61:01")
        );
    }
}
