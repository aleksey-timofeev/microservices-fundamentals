package com.alextim.song.response;

import lombok.Data;

@Data
public class SongResponse {
    private Integer id;
    private String name;
    private String artist;
    private String album;
    private String duration;
    private String year;
}
