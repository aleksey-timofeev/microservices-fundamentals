package com.alextim.resource.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongRequest {
    private Integer id;
    private String name;
    private String artist;
    private String album;
    private String duration;
    private String year;
}
