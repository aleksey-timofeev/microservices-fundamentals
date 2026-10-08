package com.alextim.resourceprocessor.extractor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SongMetadata {
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String year;
}
