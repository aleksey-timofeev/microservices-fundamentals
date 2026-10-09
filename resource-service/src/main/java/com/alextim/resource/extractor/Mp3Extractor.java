package com.alextim.resource.extractor;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class Mp3Extractor {
    private static final String DATA_ARE_NOT_PARSABLE = "Data aren't parsable";

    private static final String TITLE_ATTRIBUTE = "dc:title";
    private static final String ARTIST_ATTRIBUTE = "xmpDM:artist";
    private static final String ALBUM_ATTRIBUTE = "xmpDM:album";
    private static final String DURATION_ATTRIBUTE = "xmpDM:duration";
    private static final String RELEASE_DATE_ATTRIBUTE = "xmpDM:releaseDate";

    private final Tika tika = new Tika();

    public SongMetadata extract(byte[] data) {
        Metadata metadata = new Metadata();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            tika.parse(inputStream, metadata);
        } catch (IOException e) {
            throw new IllegalArgumentException(DATA_ARE_NOT_PARSABLE, e);
        }

        return new SongMetadata(
                metadata.get(TITLE_ATTRIBUTE),
                metadata.get(ARTIST_ATTRIBUTE),
                metadata.get(ALBUM_ATTRIBUTE),
                metadata.get(DURATION_ATTRIBUTE),
                metadata.get(RELEASE_DATE_ATTRIBUTE)
        );
    }
}
