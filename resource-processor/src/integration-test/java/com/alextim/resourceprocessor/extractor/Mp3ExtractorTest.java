package com.alextim.resourceprocessor.extractor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Mp3ExtractorIT {
    private static final String EXPECTED_TITLE = "Test Title";
    private static final String EXPECTED_ARTIST = "Test Artist";
    private static final String EXPECTED_ALBUM = "Test Album";
    private static final String EXPECTED_DURATION = "7.810635089874268";
    private static final String EXPECTED_YEAR = "2025";

    private final Mp3Extractor mp3Extractor = new Mp3Extractor();

    @Test
    void extract_shouldExtractMetadata_whenFileIsValid(
        @Value("classpath:mp3/valid-sample-with-required-tags.mp3")
        Resource response
    ) throws IOException {
        byte[] data = response.getInputStream().readAllBytes();
        SongMetadata metadata = mp3Extractor.extract(data);

        assertThat(metadata).isNotNull();

        assertThat(metadata.getTitle()).isEqualTo(EXPECTED_TITLE);
        assertThat(metadata.getArtist()).isEqualTo(EXPECTED_ARTIST);
        assertThat(metadata.getAlbum()).isEqualTo(EXPECTED_ALBUM);
        assertThat(metadata.getDuration()).isEqualTo(EXPECTED_DURATION);
        assertThat(metadata.getYear()).isEqualTo(EXPECTED_YEAR);
    }
}
