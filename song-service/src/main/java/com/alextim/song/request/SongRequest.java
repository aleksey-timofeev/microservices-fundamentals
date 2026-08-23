package com.alextim.song.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Year;

@Data
public class SongRequest {
    @NotNull
    private Integer id;

    @NotNull(message = "Song name is required")
    @Size(min = 1, max = 100, message = "Song name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Artist name is required")
    @Size(min = 1, max = 100, message = "Artist name must be between 1 and 100 characters")
    private String artist;

    @NotNull(message = "Album name is required")
    @Size(min = 1, max = 100, message = "Album name must be between 1 and 100 characters")
    private String album;

    @NotNull(message = "Duration is required")
    @Pattern(
            regexp = "([0-5]\\d):([0-5]\\d)",
            message = "Duration must be in mm:ss format with leading zeros"
    )
    private String duration;

    @NotNull(message = "Year is required")
    @Pattern(
            regexp = "(19\\d{2}|20\\d{2})",
            message = "Year must be between 1900 and 2099"
    )
    private String year;
}
