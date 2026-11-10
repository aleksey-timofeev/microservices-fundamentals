package com.alextim.song.repository;

import com.alextim.configuration.TestcontainersConfiguration;
import com.alextim.song.persistence.entity.Song;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(TestcontainersConfiguration.class)
class SongRepositoryIT {
    private static final Integer FIRST_SONG_ID = 1;
    private static final Integer SECOND_SONG_ID = 2;

    @Autowired
    private SongRepository repository;

    @Test
    void findExistingIds_shouldReturnOnlyExistingIds_whenInputContainsExistingAndMissingIds() {
        Song first = repository.save(createSong(FIRST_SONG_ID));
        Song second = repository.save(createSong(SECOND_SONG_ID));

        List<Integer> actualIds = repository.findExistingIds(
                List.of(first.getId(), second.getId(), Integer.MAX_VALUE));

        assertThat(actualIds).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    private static Song createSong(Integer id) {
        Song song = new Song();

        song.setId(id);
        song.setName("Test song");

        return song;
    }
}
